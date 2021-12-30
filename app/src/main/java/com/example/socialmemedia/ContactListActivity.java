package com.example.socialmemedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ContactListActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    ListView contactListView;
//    ArrayList<String> users = new ArrayList<String>(Arrays.asList("Joe","Ori","Ben","Bob","Ned","Tim","Uma","Mia","Edi","Zak","Ali","Tom","Max","Pip","Dan","Kev","Jil","Ido"));

    ArrayList<ArrayList<String>> chatsDetails;     //[("chatID","timestamp",userUID","userName","userEmail"),...]
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    FloatingActionButton floatingActionButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        chatsDetails = new ArrayList<>();
        contactListView=findViewById(R.id.contactListView);
        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();


        /*Listener 1- populate ArrayList with chat ID from Firebase database*/
        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsDetails.clear();   //clears ArrayList to repopulate it, avoid duplication

                if(snapshot.exists()){
                    for(DataSnapshot contactSnapshot:snapshot.getChildren()){  //for every chat
                        if(contactSnapshot.getValue().toString().equals("true")) {        //only display the chat if it has a value "true"
                            ArrayList<String> thisChat = new ArrayList<String>();  //array to go inside 2d array
                            String chatId = contactSnapshot.getKey();   //name of node is the uid

                            //adds id to ArrayList for this chat
                            thisChat.add(chatId);
                            thisChat.add("timestampTemp");
                            thisChat.add("userIdTemp");
                            thisChat.add("nameTemp");       //temporary names and emails to give 2D arrayList structure
                            thisChat.add("emailTemp");      //allows set() value instead of add() to arrayList - no glitch
                            chatsDetails.add(thisChat);    //[("chatID","timestamp",userUID","userName","userEmail"),...]
                        }
                    }

                    Log.d(TAG, "onDataChange: listener 1 "+chatsDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactListActivity.this, "Error in updating chats. "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        /*Listener 2- populate 2d ArrayList with users uid and timestamp from Firebase database*/
        databaseReference.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {  //snapshot of chats

                ArrayList<String> chatsIdArray= new ArrayList<>();
                for (ArrayList<String> eachChat: chatsDetails){    //for every chat in chat Details
                    chatsIdArray.add(eachChat.get(0));            //creates an array of chat ID
                }
                if (snapshot.exists()){
                    for (DataSnapshot eachChatSnapshot: snapshot.getChildren()){   //for every chat id

                        if(chatsIdArray.contains(eachChatSnapshot.getKey())){    //if this this chat id(the key) is a chat of the current user

                            int indexIn2DArray=chatsIdArray.indexOf(eachChatSnapshot.getKey());   //gets index of where chat UID is

                            String timestamp=eachChatSnapshot.child("timestamp").getValue().toString();
                            String contactUid=null; //only stores single user id, doesn't work for group chats

                            for(DataSnapshot eachChatUser: eachChatSnapshot.child("users").getChildren()){    //for each user in chat
                                if(!eachChatUser.getKey().equals(mAuth.getCurrentUser().getUid())){
                                    //if the chat user is not the current user
                                    contactUid=eachChatUser.getKey();         //store the user ID as the contact name to be displayed on the chat
                                }
                            }
                            chatsDetails.get(indexIn2DArray).set(1,timestamp);
                            chatsDetails.get(indexIn2DArray).set(2,contactUid);   //set timestamp and uid under correct chat id in 2D chats array
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


                    //inflates listview with email and name using array adapter
                    setListViewAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactListActivity.this, "Error in updating contacts. "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        /*Listener 3- whenever values under users changes, update user email and name in 2D array*/
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

                            chatsDetails.get(indexIn2DArray).set(3,contactName);   //set name and email under correct uid in chats array
                            chatsDetails.get(indexIn2DArray).set(4,contactEmail);

                        }
                    }
                    Log.d(TAG, "onDataChange: listener 3 "+chatsDetails);

                    if(chatsDetails.size()>0) {
                        if (chatsDetails.get(0).contains("nameTemp")) {  //if contact list not loaded with actual names
                            Log.d(TAG, "onDataChange: glitch nameTemp");
                            ContactListActivity.this.recreate();     //restarts activity
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);
                        }
                    }
                    setListViewAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactListActivity.this, "Error in updating contacts. "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        /*when a chat is selected from the listView*/
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ContactListActivity.this,ChatActivity.class);

                intent.putExtra("chatID",chatsDetails.get(position).get(0));
                intent.putExtra("contactUid",chatsDetails.get(position).get(2));
                intent.putExtra("name",chatsDetails.get(position).get(3));  //attaches name to intent so show on chatActivity
                intent.putExtra("email",chatsDetails.get(position).get(4));

                Log.d(TAG, "onItemClick: "+chatsDetails.get(position).get(3));
                startActivity(intent);          //goes to ChatActivity

                overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);
                //chat activity slide in from bottom, contact list activity slide out from top
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


        /*goes to AddContactActivity to add a new contact*/
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

    /*toolbar buttons menu*/
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
    /**********************************/

    @Override
    public void onBackPressed(){
        this.finishAffinity();    //closes app if back button pressed

    }

    //simple adapter populates a ListView with 2 lines of data
    private void setListViewAdapter(){
        ArrayList<String> contactNames = new ArrayList<>();       //two 1D arraylists to store the contact name and email from 2D array
        ArrayList<String> contactEmails = new ArrayList<>();

        for (ArrayList<String> arrayList:chatsDetails){
            contactNames.add(arrayList.get(3));
            contactEmails.add(arrayList.get(4));
        }

        List<Map<String,String>> data = new ArrayList<Map<String,String>>();
        for (int i=0;i<contactNames.size();i++){
            Map<String,String> dataItem = new HashMap<String,String>(2);
            dataItem.put("Contact Name",contactNames.get(i));                 //add contact name and email to hashmap
            dataItem.put("Contact Email",contactEmails.get(i));
            data.add(dataItem);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(ContactListActivity.this, data, android.R.layout.simple_list_item_2,
                new String[]{"Contact Name","Contact Email"}, new int[] {android.R.id.text1,android.R.id.text2});
                //string titles tell adapter where to put the name and email~ into text1 and text2 positions

        contactListView.setAdapter(simpleAdapter);
    }


}