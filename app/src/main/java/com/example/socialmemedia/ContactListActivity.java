package com.example.socialmemedia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class ContactListActivity extends AppCompatActivity {

    ListView contactListView;
    ArrayAdapter contactsAdapter;
    ArrayList<String> users = new ArrayList<String>(Arrays.asList("Ori","Joe","Ben","Bob","Ned","Tim","Uma","Mia","Edi","Zak","Ali","Tom","Max","Pip","Dan","Kev","Jil","Ido"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

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
    }
}