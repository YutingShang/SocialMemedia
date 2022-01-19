package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MemeMenuActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerView;
    private List<String> titles;
    private List<Integer> memeImages ;
    private MenuCardAdapter adapter;
    DatabaseReference databaseReference;
    StorageReference imageReference;
    FirebaseStorage storage;
    Boolean spacingAdded;    //to add spacing between the cards once

    ArrayList<String> memeCategories;
    ArrayList<Bitmap> memeMenuImages;
    //indexes in memeMenuImages will match up with the corresponding category item in memeCategories arraylist

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

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        memeCategories = new ArrayList<>();
        memeMenuImages = new ArrayList<>();
        recyclerView = findViewById(R.id.recylerView);
        spacingAdded=false;         //initially no grid spacing has been added

        /* get categories from database, and a random image from each*/
        databaseReference.child("images").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot categorySnapshot: snapshot.getChildren()){   //for every category
                        int randomChildIndex = getRandInt(1,(int)categorySnapshot.getChildrenCount());  //gets a random child node which is numbered
                        //gets the image url of the random chosen image
                        String imageUrl = categorySnapshot.child(String.valueOf(randomChildIndex)).child("url").getValue().toString();

                        imageReference = storage.getReferenceFromUrl(imageUrl);
                        try{   //create a temporary local file and convert to bitmap
                            File localFile = File.createTempFile("meme", "jpg");
                            imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());    //converts file to bitmap
                                    memeMenuImages.add(bitmap);    //adds to arraylist to populate recycler view
                                    Log.d(TAG, "onSuccess: image lists "+memeMenuImages);

                                    memeCategories.add(categorySnapshot.getKey()); //name of the category
                                    Log.d(TAG, "onSuccess: categories lists "+memeCategories);

                                    setRecyclerView();   //sets adapter and layout manager
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: bitmap not added. "+e.getMessage());
                                }
                            });
                        }catch (IOException e){
                            e.printStackTrace();
                        } } } }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setRecyclerView(){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MemeMenuActivity.this,2,GridLayoutManager.VERTICAL,false);

        recyclerView.setLayoutManager(gridLayoutManager);
        if(!spacingAdded){
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(2,25,true,0));
            //even spacing between cards and edges
            spacingAdded=true;   //so spacing is only added the first time
        }
        recyclerView.setHasFixedSize(true);

        adapter = new MenuCardAdapter(MemeMenuActivity.this,memeMenuImages,memeCategories);
        recyclerView.setAdapter(adapter);
    }


    //random number generator
    private int getRandInt(int lowerBound, int upperBound) {    //both inclusive

        int range= upperBound - lowerBound;
        double doubleRandom = Math.random();    //random decimal [0,1)
        double randomInRange = (doubleRandom * range)+lowerBound;
        int randomInt = (int)Math.round(randomInRange);        //rounding allows both bounds to be included

        return randomInt;
    }

}