package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.ContentValues.TAG;

public class SignUpActivity extends AppCompatActivity {

    Button signUp;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        tabLayout=findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewPager);

        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerAdapter.addFragment(new signInFragment(),"Sign In");
        viewPagerAdapter.addFragment(new signUpFragment(),"Sign Up");

        viewPager.setAdapter(viewPagerAdapter);


//        signUp = findViewById(R.id.signUpButton);
//
//        signUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openContactList();
//            }
//        });
    }

//    public void openContactList(){
//        Intent intent = new Intent(SignUpActivity.this, ContactListActivity.class);
//        startActivity(intent);
//    }

//    @Override
//    public void onStart() {   //when application opens, e.g between switching applications
//        super.onStart();
//        FirebaseUser user=mAuth.getCurrentUser();
//        if (user!=null) {    //if user is signed in
//            FirebaseAuth.getInstance().getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
//                //refreshes data of current user so isEmailVerified will be updated
//                @Override
//                public void onSuccess(Void unused) {
//                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                    if (user.isEmailVerified()) {          //if user has verified their email
//                        Log.d(TAG, "onSuccess: user is verified "+user.getUid());
//                        sendUser();     //start intent to ContactListActivity
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(SignUpActivity.this, "Reload user failed. Please manually log in.  "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

    public void sendUser(){
        Intent intent = new Intent(SignUpActivity.this,ContactListActivity.class);
        startActivity(intent);
    }
}