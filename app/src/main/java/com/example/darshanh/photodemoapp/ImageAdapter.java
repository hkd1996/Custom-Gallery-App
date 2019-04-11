package com.example.darshanh.photodemoapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>{
    ArrayList<ImageUri> imageList;
    Context context;
    private static OnItemClickListener clickListener;

    public ImageAdapter(ArrayList<ImageUri> imageList, Context ctx,OnItemClickListener clickListener) {
        this.imageList = imageList;
        this.context=ctx;
        this.clickListener=clickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_layout,viewGroup,false);
        ImageViewHolder imageViewHolder=new ImageViewHolder(view);
        return imageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, final int i) {
        ImageUri dispImageUri=imageList.get(i);

        Uri uri=dispImageUri.getImgUri();
        try {
            imageViewHolder.Image.setImageBitmap(BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView Image;

        public ImageViewHolder(View itemView) {
            super(itemView);
            Image=itemView.findViewById(R.id.listImage);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view,this.getAdapterPosition());
        }
    }
}
