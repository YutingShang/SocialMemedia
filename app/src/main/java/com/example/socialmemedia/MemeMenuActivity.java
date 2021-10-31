package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MemeMenuActivity extends AppCompatActivity {


    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerView;
    private List<String> titles;
    private List<Integer> images;
    private MenuCardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_menu);

        bottomNavigationView= findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.memeMenu);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.contactList:
                        startActivity(new Intent(getApplicationContext(),ContactListActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.memeMenu:
                        return true;
                }
                return false;
            }
        });

        recyclerView = findViewById(R.id.recylerView);
        titles=new ArrayList<>(Arrays.asList("1","2","3","4","5","6","7","8","9","10"));
        images = new ArrayList<>(Arrays.asList(R.drawable.meme1,R.drawable.meme2,R.drawable.meme3,R.drawable.meme4,R.drawable.meme5,R.drawable.meme6, R.drawable.meme7,R.drawable.meme8,R.drawable.meme9,R.drawable.meme10 ));
        adapter = new MenuCardAdapter(this,titles,images);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(adapter);



    }
}