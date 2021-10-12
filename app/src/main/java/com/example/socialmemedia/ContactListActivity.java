package com.example.socialmemedia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;

public class ContactListActivity extends AppCompatActivity {

    ListView contactListView;
    ArrayAdapter contactsAdapter;
    ArrayList<String> users = new ArrayList<String>(Arrays.asList("Ori","Joe","Ben","Bob","Ned","Tim","Uma","Mia","Edi","Zak","Ali","Tom","Max","Pip","Dan","Kev","Jil","Ido"));
    ImageButton settingsButton;
    BottomNavigationView navView;
    AppBarConfiguration appBarConfiguration;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);


        settingsButton = findViewById(R.id.settingsButton);
        contactListView=findViewById(R.id.contactListView);
        contactsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,users);
        //ArrayAdapter populates a ListView with ArrayList items
        contactListView.setAdapter(contactsAdapter);



        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ContactListActivity.this,ChatActivity.class);
                startActivity(intent);
            }
        });


        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(ContactListActivity.this, SettingsActivity.class);
                startActivity(intent2);
            }
        });

        navView = findViewById(R.id.bottomNav_view);
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_contactList,R.id.navigation_memeMenu).build();
        navController = Navigation.findNavController(this,R.id.navHostFragment);
        NavigationUI.setupActionBarWithNavController(this,navController,appBarConfiguration);
        NavigationUI.setupWithNavController(navView,navController);

    }
}