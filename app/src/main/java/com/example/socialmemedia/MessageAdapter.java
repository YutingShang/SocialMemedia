package com.example.socialmemedia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    private Context context;
    private List<Chat> mChat;
    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> mChat){
        this.context=context;
        this.mChat = mChat;


    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }


    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat=mChat.get(position);
        holder.show_message.setText(chat.getMessage());


    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView show_message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message =itemView.findViewById(R.id.show_message);
            
            
        }

    }
    @Override
    public int getItemViewType(int position){
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }
}

