package com.example.socialmemedia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SignUpActivity extends AppCompatActivity {

    Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signUp = findViewById(R.id.signUpbutton);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContactList();
            }
        });
    }

    public void openContactList(){
        Intent intent = new Intent(SignUpActivity.this, ContactListActivity.class);
        startActivity(intent);
    }
}