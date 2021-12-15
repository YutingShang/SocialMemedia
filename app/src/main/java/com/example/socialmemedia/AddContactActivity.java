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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class AddContactActivity extends AppCompatActivity {

    Toolbar toolbar;
    SearchView searchView;
    ListView listView;
    ArrayList<ArrayList<String>> detailsContactsInDatabaseArray;
    ArrayList<String> filteredContactsArray;
    ArrayList<String> emailAddressesInDatabaseArray;
    ArrayList<String> alreadyAddedContactsUidArray;
    ArrayAdapter<String> arrayAdapter;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    MaterialButton addContactButton;
    String userEmailToAdd;


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
        filteredContactsArray = new ArrayList<String>();
        detailsContactsInDatabaseArray= new ArrayList<ArrayList<String>>();
        emailAddressesInDatabaseArray = new ArrayList<String>();
        alreadyAddedContactsUidArray = new ArrayList<String>();


        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){     //"users"

                    detailsContactsInDatabaseArray.clear();      //clears ArrayList in order to update it
                    alreadyAddedContactsUidArray.clear();
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){   //for every child node to users (i.e each individual user's UID)

                        //adds all contacts' uid and email to arrayList that are not the current user
                        /*delete IF if want a chat to self feature available*/
                        if(!dataSnapshot.child("email").getValue().equals(mAuth.getCurrentUser().getEmail())){
                            ArrayList<String> contactDetails = new ArrayList<String>();
                            contactDetails.add(dataSnapshot.getKey());        //gets the name of the snapshot
                            contactDetails.add(dataSnapshot.child("email").getValue().toString());
                            detailsContactsInDatabaseArray.add(contactDetails);        //[("uid1","email1"),("uid2","email2")...]
                        }

                        if(dataSnapshot.child("email").getValue().equals(mAuth.getCurrentUser().getEmail())){
                            for(DataSnapshot contactDataSnapshot:snapshot.child(mAuth.getCurrentUser().getUid()).child("contacts").getChildren()){  //for every contact of user
                                alreadyAddedContactsUidArray.add(contactDataSnapshot.getKey());
                                //adds string of contact uid to an 'existing contacts' array
                            }
                        }

                    }

                    //sorts details alphabetically based on email address which is in index 1
                    Collections.sort(detailsContactsInDatabaseArray, new Comparator<ArrayList<String>>() {
                        @Override
                        public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                            return o1.get(1).compareTo(o2.get(1));
                        }
                    });
                    //creates another arrayList that stores only the email addresses, matches up with detailsContactsInDatabaseArray
                    emailAddressesInDatabaseArray.clear();
                    for (int i=0;i<detailsContactsInDatabaseArray.size();i++){     //iterates over 2d arraylist and appends the email only
                        emailAddressesInDatabaseArray.add(detailsContactsInDatabaseArray.get(i).get(1));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddContactActivity.this, "Failed to load users. "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        arrayAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emailAddressesInDatabaseArray);
        listView.setAdapter(arrayAdapter);    //displays contacts in listView


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){    //when user types into search bar
            @Override
            public boolean onQueryTextSubmit(String query){
                if(emailAddressesInDatabaseArray.contains(query)){    //checks if query entered is an email address contact
                    arrayAdapter.getFilter().filter(query);
                }else{
                    Toast.makeText(AddContactActivity.this, "No user found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText){     //when another query is typed in
                arrayAdapter.getFilter().filter(newText);
                return false;
            }
        });


        /*when a contact is selected in the list*/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filteredContactsArray.clear();                    //removes all items in array to produce a new array
                for(String email: emailAddressesInDatabaseArray){
                    if(email.startsWith(searchView.getQuery().toString().toLowerCase())){        //manually filtering email  contacts array
                        filteredContactsArray.add(email);                         //adding contacts that match search to the list
                    }
                }
//                Toast.makeText(AddContactActivity.this, "User selected"+filteredContactsArray.get(position), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "User selected "+filteredContactsArray.get(position));

                userEmailToAdd= filteredContactsArray.get(position);      //stores a copy of the user selected
            }
        });

        addContactButton=findViewById(R.id.add_contact_button);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: "+userEmailToAdd);
                if(userEmailToAdd!=null){     //if an email has been selected from the ListView

                    //finds email address in emailAddress array, uid is in same index in detailsContacts array, but at the 0 index this 2d array
                    String contactUidToAdd=detailsContactsInDatabaseArray.get(emailAddressesInDatabaseArray.indexOf(userEmailToAdd)).get(0);

                    Log.d(TAG, "onClick: users added "+ alreadyAddedContactsUidArray);
                    if(alreadyAddedContactsUidArray.contains(contactUidToAdd)){
                        Toast.makeText(AddContactActivity.this, userEmailToAdd+" is already in your contacts list", Toast.LENGTH_SHORT).show();
                        //TO-DO: go to chatActivity
                    }else {
                        //adds new contact Uid with value of their email address under "contacts" key
                        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("contacts").child(contactUidToAdd).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
//                                Toast.makeText(AddContactActivity.this, "New contact " + userEmailToAdd + " added", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onComplete: added new contact " + userEmailToAdd);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddContactActivity.this, "Error in adding new contact. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onFailure: error adding new contact. " + e.getMessage());
                            }
                        });

                        //adds a new chat containing messages between this user and contact
                        String chatID= databaseReference.child("chats").push().getKey();
                        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("chats").child(chatID).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "onComplete: new chat created");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failed to create chat");
                            }
                        });

                        //creates a hashmap to add a new "chat" branch in realtime database quickly
                        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                        String welcomeMessage ="Hello this is "+mAuth.getCurrentUser().getEmail();

                        Map<String,Object> chatsUpdate = new HashMap<>();
                        chatsUpdate.put("chats/"+chatID+"/users/"+mAuth.getCurrentUser().getUid(),true);
                        chatsUpdate.put("chats/"+chatID+"/users/"+contactUidToAdd,true);
                        chatsUpdate.put("chats/"+chatID+"/lastMessage",welcomeMessage);
                        chatsUpdate.put("chats/"+chatID+"/timestamp",timestamp);

                        chatsUpdate.put("messages/"+chatID+"/0/sender",mAuth.getCurrentUser().getUid());
                        chatsUpdate.put("messages/"+chatID+"/0/message",welcomeMessage);
                        chatsUpdate.put("messages/"+chatID+"/0/timestamp",timestamp);

                        databaseReference.updateChildren(chatsUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull  Task<Void> task) {
                                Toast.makeText(AddContactActivity.this, "New chat with " + userEmailToAdd + " created", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onComplete: added chat to database");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(AddContactActivity.this, "Failed to create chat. "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onFailure: failed to add chat to database. "+e.getMessage());
                            }
                        });

                    }
                } else{
                    Toast.makeText(AddContactActivity.this, "Please select a contact to add", Toast.LENGTH_SHORT).show();
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