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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
//import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class ContactListActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    ListView contactListView;
    ArrayAdapter contactsAdapter;
    ArrayList<String> users = new ArrayList<String>(Arrays.asList("Joe","Ori","Ben","Bob","Ned","Tim","Uma","Mia","Edi","Zak","Ali","Tom","Max","Pip","Dan","Kev","Jil","Ido"));
    ImageButton settingsButton;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    FloatingActionButton floatingActionButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);


//        settingsButton = findViewById(R.id.settingsButton);
        contactListView=findViewById(R.id.contactListView);
        contactsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,users);
        //ArrayAdapter populates a ListView with ArrayList items
        contactListView.setAdapter(contactsAdapter);



        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ContactListActivity.this,ChatActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);
                //chat activity slide in from bottom, contact list activty slide out from top
            }
        });


//        settingsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent2 = new Intent(ContactListActivity.this, SettingsActivity.class);
//                startActivity(intent2);
//                overridePendingTransition(0,0);
//            }
//        });

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