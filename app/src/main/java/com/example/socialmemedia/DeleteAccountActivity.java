package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Set;

import static android.content.ContentValues.TAG;

public class DeleteAccountActivity extends AppCompatActivity {

    Button deleteButton;
    EditText password;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        password= findViewById(R.id.password);
        deleteButton=findViewById(R.id.deleteAccountButton);
        toolbar=findViewById(R.id.toolbar);
        mAuth= FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(DeleteAccountActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                //smooth page transition animation
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //re-authenticate user
                if(password.getText().toString().trim().isEmpty()){
                    Toast.makeText(DeleteAccountActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                } else if (mAuth.getCurrentUser() != null) {
                    AuthCredential authCredential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), password.getText().toString().trim());

                    mAuth.getCurrentUser().reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //delete firebase account and database details
                                databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "onComplete: Deleted from database");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: not deleted from database. " + e.getMessage());
                                    }
                                });

                                mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {    //deletes user from firebase authentication
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: User account deleted");
                                            Toast.makeText(DeleteAccountActivity.this, "User account deleted", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(DeleteAccountActivity.this, SignUpActivity.class);
                                            startActivity(intent);    //goes back to sign in page
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Error in deleting account. " + e.getMessage());
                                        Toast.makeText(DeleteAccountActivity.this, "Error in deleting account. Log in again to retry.  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull  Exception e) {
                            //ask to retry password
                            Toast.makeText(DeleteAccountActivity.this, "Password incorrect. Please try again. "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onFailure: to re-authenticate. "+e.getMessage());
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            Intent intent= new Intent(DeleteAccountActivity.this,SignUpActivity.class);
            startActivity(intent);
            //prevent glitches on signing up to this page
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}