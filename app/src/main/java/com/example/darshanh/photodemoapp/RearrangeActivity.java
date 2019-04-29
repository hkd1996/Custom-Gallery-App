package com.example.darshanh.photodemoapp;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class RearrangeActivity extends AppCompatActivity {

    private static final int NBR_ITEMS = 60;
    private android.support.v7.widget.GridLayout mGrid;
    private ScrollView mScrollView;
    private ValueAnimator mAnimator;
    private AtomicBoolean mIsScrolling = new AtomicBoolean(false);
    ArrayList<String> compressedImagesPath = new ArrayList<String>();
    int indexOfMyView=0,oldIndex;
    float oldX=0;
    float oldY=0;
    File[] listFile;
    boolean permission;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rearrange_layout);
   //     permission=isStoragePermissionGranted();
        compressedImagesPath=getIntent().getStringArrayListExtra("images");
        mScrollView = (ScrollView)findViewById(R.id.scroll_view);
        mScrollView.setSmoothScrollingEnabled(true);

        mGrid = (android.support.v7.widget.GridLayout) findViewById(R.id.grid_layout);
        mGrid.setOnDragListener(new DragListener());

        final LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < compressedImagesPath.size(); i++) {
            final View itemView = inflater.inflate(R.layout.grid_item, mGrid, false);
            final ImageView imageView=(ImageView)itemView.findViewById(R.id.image);
            Uri compressedImageUri=Uri.fromFile(new File(compressedImagesPath.get(i)));
            imageView.setImageURI(compressedImageUri);

            itemView.setOnLongClickListener(new LongPressListener());
            itemView.setId(i);
            mGrid.addView(itemView);
        }
        Button done=(Button)findViewById(R.id.doneBtn);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                i.putStringArrayListExtra("rearranged images",compressedImagesPath);
                startActivity(i);
            }
        });

    }
   /* public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
    }*/
   /* public ArrayList<String> getFromSdcard()
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
*/
    static class LongPressListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            final ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, view, 0);
            view.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    class DragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final View view = (View) event.getLocalState();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_LOCATION:
                    // do nothing if hovering above own position
                    if (view == v) return true;
                    // get the new list index
                    oldX=event.getX();
                    oldY=event.getY();
                    final int index = calculateNewIndex(oldX,oldY);
                  // Toast.makeText(getApplicationContext(),"Old index "+index,Toast.LENGTH_SHORT).show();
                    final Rect rect = new Rect();
                    mScrollView.getHitRect(rect);
                    final int scrollY = mScrollView.getScrollY();

                    if (event.getY() -  scrollY > mScrollView.getBottom() - 250) {
                        startScrolling(scrollY, mGrid.getHeight());
                    } else if (event.getY() - scrollY < mScrollView.getTop() + 250) {
                        startScrolling(scrollY, 0);
                    } else {
                        stopScrolling();
                    }


                    // remove the view from the old position
                    oldIndex=view.getId();
                   // Toast.makeText(getApplicationContext()," Old Index "+view.getId(),Toast.LENGTH_SHORT).show();
                    mGrid.removeView(view);
                    // and push to the new

                    mGrid.addView(view, index);
                   // indexOfMyView = ((android.support.v7.widget.GridLayout) view.getParent()).indexOfChild(view);
                  /*  Collections.swap(compressedImagesPath,index,indexOfMyView);
                    for(int i=0;i<compressedImagesPath.size();i++){
                        Log.d("elements",compressedImagesPath.get(i));
                    }*/

                    break;
                case DragEvent.ACTION_DROP:
                    view.setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {

                        view.setVisibility(View.VISIBLE);
                    }
                  //  oldIndex=calculateNewIndex(oldX,oldY);
                    indexOfMyView = ((android.support.v7.widget.GridLayout) view.getParent()).indexOfChild(view);
                    view.setId(indexOfMyView);
                    getItemPostions();
                    view.setOnDragListener(null);
                    break;
            }
           // Toast.makeText(getApplicationContext()," Old Index "+oldIndex,Toast.LENGTH_SHORT).show();
            return true;
        }

        private void getItemPostions() {
            //indexOfMyView = ((android.support.v7.widget.GridLayout) view.getParent()).indexOfChild(view);


               // Toast.makeText(getApplicationContext(),"Old Index "+oldIndex+" New Index "+indexOfMyView,Toast.LENGTH_SHORT).show();
                Collections.swap(compressedImagesPath,oldIndex,indexOfMyView);



           // View item=mGrid.getChildAt(0);

        }
    }

    private void startScrolling(int from, int to) {
        if (from != to && mAnimator == null) {
            mIsScrolling.set(true);
            mAnimator = new ValueAnimator();
            mAnimator.setInterpolator(new OvershootInterpolator());
            mAnimator.setDuration(Math.abs(to - from));
            mAnimator.setIntValues(from, to);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mScrollView.smoothScrollTo(0, (int) valueAnimator.getAnimatedValue());
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsScrolling.set(false);
                    mAnimator = null;
                }
            });
            mAnimator.start();
        }
    }

    private void stopScrolling() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    private int calculateNewIndex(float x, float y) {
        // calculate which column to move to
        final float cellWidth = mGrid.getWidth() / mGrid.getColumnCount();
        final int column = (int)(x / cellWidth);

        // calculate which row to move to
        final float cellHeight = mGrid.getHeight() / mGrid.getRowCount();
        final int row = (int)Math.floor(y / cellHeight);

        // the items in the GridLayout is organized as a wrapping list
        // and not as an actual grid, so this is how to get the new index
        int index = row * mGrid.getColumnCount() + column;
        if (index >= mGrid.getChildCount()) {
            index = mGrid.getChildCount() - 1;
        }

        return index;
    }
}
