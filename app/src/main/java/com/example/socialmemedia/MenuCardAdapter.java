package com.example.socialmemedia;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;

public class MenuCardAdapter extends RecyclerView.Adapter<MenuCardAdapter.MyViewHolder> {

    private Context context;
    private List<Bitmap> images;   //images on each card
    private List<String> categories;

    public MenuCardAdapter(Context context, List<Bitmap> images, List<String> categories){
        this.context=context;
        this.images=images;
        this.categories = categories;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.imageView.setImageBitmap(images.get(position));

        //when one of the cards is clicked, the correct category and image url is identified and sent to meme feed
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),MemeFeedActivity.class);
                intent.putExtra("category",categories.get(position));  //sends the category name to the meme feed activity

                try{
                    //write file, save bitmap to disk
                    String filename = "meme_bitmap.png";
                    FileOutputStream stream = v.getContext().openFileOutput(filename,Context.MODE_PRIVATE);
                    images.get(position).compress(Bitmap.CompressFormat.PNG,100,stream  );

                    //cleanup - recycle bitmap
                    stream.close();

                    //Add to intent
                    intent.putExtra("bitmap",filename);
                }catch (Exception e){
                    e.printStackTrace();
                }

                //start intent
                v.getContext().startActivity(intent);

            }
        });

    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            cardView = itemView.findViewById(R.id.cardView);

        }
    }
}
