package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MemeFeedActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    List<Bitmap> images;
    ImageFeedAdapter imageFeedAdapter;
    StorageReference imageReference;
    DatabaseReference databaseReference;
    FirebaseStorage storage;
    String memeCategory;
    Bitmap firstBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_feed);

        toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MemeFeedActivity.this,MemeMenuActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                //ContactListActivity slide in from left, AddContactActivity slide out from right
            }
        });


        memeCategory = getIntent().getStringExtra("category");   //gets the category for this feed
        Toast.makeText(this, memeCategory, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate: meme category "+memeCategory);

        images = new ArrayList<>();
        firstBitmap = null;
        String filename = getIntent().getStringExtra("bitmap");
        try{
            FileInputStream stream = this.openFileInput(filename);       //retrieves stored bitmap file
            firstBitmap = BitmapFactory.decodeStream(stream);           //decodes to bitmap
            images.add(firstBitmap);
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        //initialisations
        storage = FirebaseStorage.getInstance();
        recyclerView = findViewById(R.id.recylerView);
        imageFeedAdapter = new ImageFeedAdapter(MemeFeedActivity.this,images);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MemeFeedActivity.this);

        //set recyclerView adapters and layout
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(imageFeedAdapter);


        databaseReference = FirebaseDatabase.getInstance().getReference();
        //get the image file paths for this category from the database
        databaseReference.child("images").child(memeCategory).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot memeSnapshot: snapshot.getChildren()){
                        String imageUrl = memeSnapshot.child("url").getValue().toString();    //gets the url from database
                        imageReference = storage.getReferenceFromUrl(imageUrl);          //the specific reference to the image in firebase storage

                        try {         //retrieves image to a temporary file
                            File localFile = File.createTempFile("meme", "jpg");
                            imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Bitmap thisBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());        //convert filepath to bitmap

                                    //check bitmap not the same as first one
                                    if(!thisBitmap.sameAs(firstBitmap)){
                                        images.add(thisBitmap);

                                        //update recyclerView with newly loaded meme
                                        imageFeedAdapter.notifyItemInserted(images.size()-1);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: bitmap not added");
                                }
                            });
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}