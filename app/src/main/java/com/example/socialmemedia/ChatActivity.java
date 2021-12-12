package com.example.socialmemedia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {

    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        backButton= findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ContactListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_top,R.anim.slide_out_bottom);
                //ContactList activity will slide in from top, ChatActivity will slide out from bottom

            }
        });

        String otherEmail= getIntent().getStringExtra("name");
        TextView textView = findViewById(R.id.chattext);
        textView.setText("chat with "+otherEmail);


    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_top,R.anim.slide_out_bottom);
    }
}