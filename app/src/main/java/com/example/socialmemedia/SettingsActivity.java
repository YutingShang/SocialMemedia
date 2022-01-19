package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

public class SettingsActivity extends AppCompatActivity {

    Button signOut,deleteAccount;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    Toolbar toolbar;
    SwitchCompat detoxSwitch;
    Boolean isTouched, detoxMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(SettingsActivity.this, ContactListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                //ContactListActivity slide in from left, AddContactActivity slide out from right
            }
        });

        mAuth = FirebaseAuth.getInstance();
        signOut = findViewById(R.id.signOutButton);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Logging out "+mAuth.getCurrentUser().getUid());
                mAuth.signOut();
                Intent intent = new Intent(SettingsActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });


        deleteAccount= findViewById(R.id.deleteAccountButton);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(SettingsActivity.this,DeleteAccountActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

            }
        });



        databaseReference= FirebaseDatabase.getInstance().getReference();
        detoxSwitch = findViewById(R.id.detoxSwitch);

        isTouched = false;

        detoxSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTouched = true;
                return false;
            }
        });


        //sets initial button to correct state fetched from database
        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("detox").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    detoxMode = Boolean.parseBoolean(snapshot.getValue().toString());
                    detoxSwitch.setChecked(detoxMode);     //sets toggle button to on/off depending if detox is true/false

                }else{     //default setting if none is set = false
                    detoxSwitch.setChecked(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        detoxSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isTouched){
                    isTouched=false;
                    if(isChecked){   //if detox setting is switched on
                        //add "detox" = true under database in users
                        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("detox").setValue(true);
                        Log.d(TAG, "onCheckedChanged: detox on");

                    } else{    //toggle is disabled
                        //set detox to false
                        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("detox").setValue(false);
                        Log.d(TAG, "onCheckedChanged: detox");

                    }

                }

            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            Intent intent= new Intent(SettingsActivity.this,SignUpActivity.class);
            startActivity(intent);
            //prevent glitches on signing up to this page
        }
    }
}