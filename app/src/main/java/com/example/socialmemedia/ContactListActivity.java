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
    ArrayList<ArrayList<String>> contactsDetails;
    ArrayList<String> tempContactEmails;
    ArrayList<String> contactNames;
    ArrayList<String> contactEmails;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    FloatingActionButton floatingActionButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        contactsDetails= new ArrayList<ArrayList<String>>();
        tempContactEmails= new ArrayList<>();
        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

//        contacts= new ArrayList<String>();
        contactListView=findViewById(R.id.contactListView);

        /*populate users ArrauList with name and email of contacts from Firebase database*/
        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("contacts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactsDetails.clear();   //clears ArrayList to repopulate it, avoid duplication

                if(snapshot.exists()){
                    for(DataSnapshot contactSnapshot:snapshot.getChildren()){  //for every contact

                        ArrayList<String> thisContact= new ArrayList<String>();  //array to go inside 2d array
                        String contactUid=contactSnapshot.getKey();   //name of node is the uid

                        //adds uid to ArrayList for this contact
                        thisContact.add(contactUid);
                        thisContact.add("nameTemp");       //temporary names and emails to give 2D arrayList structure
                        thisContact.add("emailTemp");      //allows set() value instead of add() to arrayList - no glitch
                        contactsDetails.add(thisContact);    //[("id1","name1","email1"),("id2","name2","email2")...]

                    }

                    Log.d(TAG, "onDataChange: "+contactsDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*whenever values under users changes*/
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> contactUidArray= new ArrayList<>();
                for (ArrayList<String> eachContact: contactsDetails){    //for every contact in contact Details
                    contactUidArray.add(eachContact.get(0));            //creates an array of UID
                    Log.d(TAG, "onDataChange: :)) "+contactsDetails);

                }
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){   //for every user uid

                        if(contactUidArray.contains(dataSnapshot.getKey())){    //if this this user's uid(the key) is a contact

                            int indexIn2DArray=contactUidArray.indexOf(dataSnapshot.getKey());   //gets index of where contact UID is

                            String contactName=dataSnapshot.child("name").getValue().toString();
                            String contactEmail=dataSnapshot.child("email").getValue().toString();

                            contactsDetails.get(indexIn2DArray).set(1,contactName);   //set name under correct uid in contacts array
                            contactsDetails.get(indexIn2DArray).set(2,contactEmail);

                        }
                    }



                    //sorts alphabetically based on email which is at index 2
                    Collections.sort(contactsDetails, new Comparator<ArrayList<String>>() {
                        @Override
                        public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                            return o1.get(1).compareTo(o2.get(1));
                        }
                    });

                    Log.d(TAG, "onDataChange: "+contactsDetails);


                    contactNames=new ArrayList<>();
                    contactEmails=new ArrayList<>();

                    for (ArrayList<String> arrayList:contactsDetails){
                        contactNames.add(arrayList.get(1));
                        contactEmails.add(arrayList.get(2));
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

                intent.putExtra("name",contactsDetails.get(position).get(1));  //attaches name to intent
                Log.d(TAG, "onItemClick: "+contactsDetails.get(position).get(1));
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