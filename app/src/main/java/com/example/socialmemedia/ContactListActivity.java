package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
//import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static android.content.ContentValues.TAG;

public class ContactListActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    ListView contactListView;
    ArrayAdapter contactsAdapter;
//    ArrayList<String> users = new ArrayList<String>(Arrays.asList("Joe","Ori","Ben","Bob","Ned","Tim","Uma","Mia","Edi","Zak","Ali","Tom","Max","Pip","Dan","Kev","Jil","Ido"));

    ArrayList<String> contactNames;
    ArrayList<String> contactEmails;

    ArrayList<ArrayList<String>> chatsDetails;     //[("chatID","timestamp",userUID","userName","userEmail"),...]
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    FloatingActionButton floatingActionButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);


        chatsDetails = new ArrayList<>();
        contactNames=new ArrayList<>();
        contactEmails=new ArrayList<>();

        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        contactListView=findViewById(R.id.contactListView);




        /*populate ArrayList with chat ID from Firebase database*/
        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsDetails.clear();   //clears ArrayList to repopulate it, avoid duplication

                if(snapshot.exists()){
                    for(DataSnapshot contactSnapshot:snapshot.getChildren()){  //for every contact

                        ArrayList<String> thisChat= new ArrayList<String>();  //array to go inside 2d array
                        String chatId=contactSnapshot.getKey();   //name of node is the uid

                        //adds id to ArrayList for this chat
                        thisChat.add(chatId);
                        thisChat.add("timestampTemp");
                        thisChat.add("userIdTemp");
                        thisChat.add("nameTemp");       //temporary names and emails to give 2D arrayList structure
                        thisChat.add("emailTemp");      //allows set() value instead of add() to arrayList - no glitch
                        chatsDetails.add(thisChat);    //[("chatID","timestamp",userUID","userName","userEmail"),...]

                    }

                    Log.d(TAG, "onDataChange: listener 1 "+chatsDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactListActivity.this, "Error in updating chats. "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        /*populate 2d ArrayList with users uid and timestamp from Firebase database*/
        databaseReference.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {  //snapshot of chats

                ArrayList<String> chatsIdArray= new ArrayList<>();
                for (ArrayList<String> eachChat: chatsDetails){    //for every chat in chat Details
                    chatsIdArray.add(eachChat.get(0));            //creates an array of chat ID
                }
                if (snapshot.exists()){
                    for (DataSnapshot eachChatSnapshot: snapshot.getChildren()){   //for every chat id

                        if(chatsIdArray.contains(eachChatSnapshot.getKey())){    //if this this chat id(the key) is a chat containing current user

                            int indexIn2DArray=chatsIdArray.indexOf(eachChatSnapshot.getKey());   //gets index of where chat UID is

                            String timestamp=eachChatSnapshot.child("timestamp").getValue().toString();
                            String contactUid=null; //only stores single user id, doesn't work for group chats

                            for(DataSnapshot eachChatUser: eachChatSnapshot.child("users").getChildren()){    //for each user in chat
                                if(!eachChatUser.getKey().equals(mAuth.getCurrentUser().getUid()) && eachChatUser.getValue().toString().equals("true")){
                                    //if the chat user is not the current user and the user still exists -i.e. "true"
                                    contactUid=eachChatUser.getKey();
                                }
                            }
                            chatsDetails.get(indexIn2DArray).set(1,timestamp);
                            chatsDetails.get(indexIn2DArray).set(2,contactUid);   //set uid under correct chat id in contacts array
                        }
                    }

                    //sorts alphabetically based on time of last message which is at index 1
                    Collections.sort(chatsDetails, new Comparator<ArrayList<String>>() {
                        @Override
                        public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                            return o2.get(1).compareTo(o1.get(1));
                        }
                    });

                    Log.d(TAG, "onDataChange: listener 2 "+chatsDetails);

                    contactNames.clear();
                    contactEmails.clear();

                    for (ArrayList<String> arrayList:chatsDetails){
                        contactNames.add(arrayList.get(3));
                        contactEmails.add(arrayList.get(4));
                    }

                    contactsAdapter = new ArrayAdapter<String>(ContactListActivity.this, android.R.layout.simple_list_item_1,contactEmails);
                    //ArrayAdapter populates a ListView with ArrayList items
                    contactListView.setAdapter(contactsAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactListActivity.this, "Error in updating contacts. "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        /*whenever values under users changes, update user email and name in 2D array*/
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> contactUidArray= new ArrayList<>();
                for (ArrayList<String> eachContact: chatsDetails){    //for every chat contact in chat Details
                    contactUidArray.add(eachContact.get(2));            //creates an array of UID
                }
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){   //for every user uid

                        if(contactUidArray.contains(dataSnapshot.getKey())){    //if this this user's uid(the key) is a contact

                            int indexIn2DArray=contactUidArray.indexOf(dataSnapshot.getKey());   //gets index of where contact UID is

                            String contactName=dataSnapshot.child("name").getValue().toString();
                            String contactEmail=dataSnapshot.child("email").getValue().toString();

                            chatsDetails.get(indexIn2DArray).set(3,contactName);   //set name under correct uid in contacts array
                            chatsDetails.get(indexIn2DArray).set(4,contactEmail);

                        }
                    }
                    Log.d(TAG, "onDataChange: listener 3 "+chatsDetails);

                    contactNames.clear();
                    contactEmails.clear();

                    for (ArrayList<String> arrayList:chatsDetails){
                        contactNames.add(arrayList.get(3));
                        contactEmails.add(arrayList.get(4));
                    }

                    contactsAdapter = new ArrayAdapter<String>(ContactListActivity.this, android.R.layout.simple_list_item_1,contactEmails);
                    //ArrayAdapter populates a ListView with ArrayList items
                    contactListView.setAdapter(contactsAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactListActivity.this, "Error in updating contacts. "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ContactListActivity.this,ChatActivity.class);

                intent.putExtra("name",chatsDetails.get(position).get(3));  //attaches name to intent
                Log.d(TAG, "onItemClick: "+chatsDetails.get(position).get(3));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);
                //chat activity slide in from bottom, contact list activty slide out from top
            }
        });


        bottomNavigationView= findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.contactList);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.memeMenu:
                        startActivity(new Intent(getApplicationContext(),MemeMenuActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.contactList:
                        return true;
                }
                return false;
            }
        });

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        floatingActionButton=findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactListActivity.this, AddContactActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_contact_toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int selectedId=item.getItemId();
        if(selectedId==R.id.settings){
            Intent intent2 = new Intent(ContactListActivity.this, SettingsActivity.class);
            startActivity(intent2);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            //SettingsActivity slide in from right, ContactListActivity slide out from left
        }else if(selectedId==R.id.search){
            Toast.makeText(this, "Searching contact", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Searching contact");
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        this.finishAffinity();    //closes app if back button pressed

    }



}