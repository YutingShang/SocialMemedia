package com.example.socialmemedia;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MenuCardAdapter extends RecyclerView.Adapter<MenuCardAdapter.MyViewHolder> {

    private Context context;
    private List<String> titles;
    private List<Integer> images;

    public MenuCardAdapter(Context context, List<String> titles, List<Integer> images){
        this.context=context;
        this.titles=titles;
        this.images=images;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.textView.setText(titles.get(position));
        holder.imageView.setImageResource(images.get(position));

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            textView =itemView.findViewById(R.id.textView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(v.getContext(),"Clicked on Meme ",Toast.LENGTH_SHORT).show();   //doesn't work?
                    Intent intent = new Intent(v.getContext(),MemeFeedActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                    v.getContext().startActivity(intent);

                }
            });
        }
    }
}
