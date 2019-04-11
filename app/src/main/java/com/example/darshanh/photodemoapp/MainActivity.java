package com.example.darshanh.photodemoapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<ImageUri> imageList;
    ArrayList<String> imagePaths;
    Uri selectedImage;
    boolean permission;
    SingletonImageUri imageInstance;
    ArrayList<String> compressedImagesPath = new ArrayList<String>();// list of file paths
    File[] listFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission=isStoragePermissionGranted();
        mRecyclerView = (RecyclerView) findViewById(R.id.imageList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
      //  imageList=new ArrayList<>();
        compressedImagesPath=getFromSdcard();

        imageInstance=SingletonImageUri.getUniqueInstance();
        imageList=new ArrayList<ImageUri>();
        for(int i=0;i<compressedImagesPath.size();i++){
            Uri compressedImageUri=Uri.fromFile(new File(compressedImagesPath.get(i)));
            ImageUri imageUri=new ImageUri(compressedImageUri);
            imageList.add(imageUri);
        }

//        Log.d("imageList size",imageList.size()+"");
        mAdapter= new ImageAdapter(imageList,getApplicationContext(),this);
        mRecyclerView.setAdapter(mAdapter);
        Button selectImage=(Button)findViewById(R.id.galleryButton);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),CustomImageSelector.class);
                startActivityForResult(i,1);

            }
        });
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }
    public ArrayList<String> getFromSdcard()
    {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "PhotoDemo/Images");
        ArrayList<String> tempImagesPath = new ArrayList<String>();
        if (file.isDirectory())
        {
            listFile = file.listFiles();


            for (int i = 0; i < listFile.length; i++)
            {

                tempImagesPath.add(listFile[i].getAbsolutePath());

            }
        }
        return tempImagesPath;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK ) {

            imagePaths=data.getStringArrayListExtra("selectedImagePath");
            for(int i=0;i<imagePaths.size();i++){
                selectedImage=Uri.parse(imagePaths.get(i));
                compressImage();
            }
           // Toast.makeText(getApplicationContext(),"Selected Image Count: "+imagePaths.size(),Toast.LENGTH_SHORT).show();
          //  compressImage();
           /* Cursor returnCursor = getContentResolver().query(selectedImage, null, null, null, null);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();*/

        }
        else{
            Toast.makeText(getApplicationContext(),"You've not selected any file!!",Toast.LENGTH_LONG).show();
        }
    }

    private void compressImage() {
      //  long before=getImageSize(selectedImage);
        CompressImage compressImage=new CompressImage(selectedImage,getApplicationContext());
        String compressedImagePath=compressImage.compressImage(selectedImage.toString());
        selectedImage=Uri.fromFile(new File(compressedImagePath));
        //  long after=getImageSize(selectedImage);

        ImageUri uri=new ImageUri(selectedImage);
        imageList.add(uri);
        SingletonImageUri.getUniqueInstance().setUri(imageList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view, int position) {
        ImageUri img=imageList.get(position);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        try {
            imageView.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(img.getImgUri())));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveArrayList(imageList,"Image Uri ArrayList");
    }

    public void saveArrayList(ArrayList<ImageUri> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public ArrayList<ImageUri> getArrayList(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
