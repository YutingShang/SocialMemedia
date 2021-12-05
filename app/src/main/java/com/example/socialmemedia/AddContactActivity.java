package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.ContentValues.TAG;

public class AddContactActivity extends AppCompatActivity {

    Toolbar toolbar;
    SearchView searchView;
    ListView listView;
    ArrayList<String> contactsInDatabaseArray;
    ArrayList<String> filteredContactsArray;
    ArrayAdapter<String> arrayAdapter;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    MaterialButton addContactButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(AddContactActivity.this, ContactListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                //ContactListActivity slide in from left, AddContactActivity slide out from right
            }
        });

        searchView=findViewById(R.id.search_view);
        listView= findViewById(R.id.add_contact_listview);
        mAuth= FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        contactsInDatabaseArray = new ArrayList<String>();
        filteredContactsArray = new ArrayList<String>();

        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){   //for every child node to users (i.e each individual user's UID)

                        if(!dataSnapshot.child("email").getValue().toString().equals(mAuth.getCurrentUser().getEmail())) {
                            contactsInDatabaseArray.add(dataSnapshot.child("email").getValue().toString());
                            //adds all the available users to an array to make searching for new contact easier
                        }
                    }
                    Collections.sort(contactsInDatabaseArray);  //sorts emails alphabetically ascending order
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddContactActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });


        arrayAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,contactsInDatabaseArray);
        listView.setAdapter(arrayAdapter);    //displays contacts in listView


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){    //when user types into search bar
            @Override
            public boolean onQueryTextSubmit(String query){
                if(contactsInDatabaseArray.contains(query)){
                    arrayAdapter.getFilter().filter(query);
                }else{
                    Toast.makeText(AddContactActivity.this, "No user found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText){
                arrayAdapter.getFilter().filter(newText);
                return false;
            }
        });

        /*when a contact is selected in the list*/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filteredContactsArray.removeAll(filteredContactsArray);
                for(String email: contactsInDatabaseArray){
                    if(email.startsWith(searchView.getQuery().toString())){
                        filteredContactsArray.add(email);
                    }
                }
                Log.d(TAG, "Add user "+filteredContactsArray);
                Toast.makeText(AddContactActivity.this, "Add user "+filteredContactsArray.get(position), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Add user "+filteredContactsArray.get(position));
            }
        });

        addContactButton=findViewById(R.id.add_contact_button);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }



}