package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.ContentValues.TAG;

public class SettingsActivity extends AppCompatActivity {

    ImageButton backButton;
    Button signOut,deleteAccount;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ContactListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
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

        databaseReference= FirebaseDatabase.getInstance().getReference();
        deleteAccount= findViewById(R.id.deleteAccountButton);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser()!=null) {
                    FirebaseUser fUser=mAuth.getCurrentUser();
                    String UidToDelete=fUser.getUid();
                    fUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {    //deletes user from firebase authentication
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: User account deleted");
                                Toast.makeText(SettingsActivity.this, "User account deleted", Toast.LENGTH_SHORT).show();

//                                databaseReference.child("users").child(UidToDelete).removeValue();

                                Intent intent = new Intent(SettingsActivity.this, SignUpActivity.class);
                                startActivity(intent);    //goes back to sign in page
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Error in deleting account. " + e.getMessage());
                            Toast.makeText(SettingsActivity.this, "Error in deleting account. Log in again to retry.  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    //error when placed inside delete account on complete listener
                    databaseReference.child("users").child(UidToDelete).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "onComplete: Deleted from database");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: not deleted from database. "+e.getMessage());
                        }
                    });




                    /*TO-DO: referential integrity - delete all instances of user UID from database*/

                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}