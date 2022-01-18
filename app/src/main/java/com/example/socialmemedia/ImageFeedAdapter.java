package com.example.socialmemedia;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageFeedAdapter extends RecyclerView.Adapter<ImageFeedAdapter.ViewHolder> {
    private Context context;
    private List<Bitmap> images;   //drawable reference is Integer?

    public ImageFeedAdapter(Context context, List<Bitmap> images){
        this.context=context;
        this.images=images;

    }

    @NonNull
    @Override
    public ImageFeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.meme_feed_item,parent,false);
        return new ImageFeedAdapter.ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ImageFeedAdapter.ViewHolder holder, int position) {

        holder.imageView.setImageBitmap(images.get(position));

    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView =itemView.findViewById(R.id.imageView);
            //describes the location of components of itemView
        }
    }
}
