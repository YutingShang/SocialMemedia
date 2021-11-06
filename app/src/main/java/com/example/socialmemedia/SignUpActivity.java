package com.example.socialmemedia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

public class SignUpActivity extends AppCompatActivity {

    Button signUp;
    private TabLayout tabLayout;
    private ViewPager viewPager;


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
}