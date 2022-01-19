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


        storage = FirebaseStorage.getInstance();
        recyclerView = findViewById(R.id.recylerView);
        images = new ArrayList<>();

        memeCategory = getIntent().getStringExtra("category");   //gets the category for this feed
        Toast.makeText(this, memeCategory, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate: meme category "+memeCategory);


        Uri uri = Uri.parse(getIntent().getStringExtra("uri"));  //gets uri of image clicked on
        try {
            if(  uri!=null   ){
                firstBitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , uri);   //convert uri to bitmap
                images.add(firstBitmap);         //first bitmap to display at top of recycler view
            }
        }
        catch (Exception e) {
            Log.d(TAG, "onCreate: error in converting uri to bitmap "+e.getMessage());
        }


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

                                    Uri uri = getImageUri(MemeFeedActivity.this,thisBitmap);   //convert bitmap to uri
                                    try {
                                        if(uri!=null){
                                            Bitmap newBitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , uri);   //convert uri to bitmap
                                            if (newBitmap.sameAs(firstBitmap)){
                                                Log.d(TAG, "onSuccess: same");
                                            }else{
                                                Log.d(TAG, "onSuccess: same NOT");
                                                images.add(thisBitmap);    //adds to arraylist if image is different to the first
                                            }
                                        }
                                    }
                                    catch (Exception e) {
                                        Log.d(TAG, "onCreate: error in converting uri to bitmap "+e.getMessage());
                                    }

                                    imageFeedAdapter = new ImageFeedAdapter(MemeFeedActivity.this,images);

                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MemeFeedActivity.this);
                                    recyclerView.setLayoutManager(linearLayoutManager);
                                    recyclerView.setAdapter(imageFeedAdapter);


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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}