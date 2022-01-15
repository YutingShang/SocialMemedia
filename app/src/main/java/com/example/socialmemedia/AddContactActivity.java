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
    ArrayList<ArrayList<String>> detailsContactsInDatabaseArray;   //stores uid, email & public key & modulus of all contacts [("uid1","email1","publicKey","publicModulus"),...]
    ArrayList<String> filteredContactsArray;             //queried contacts
    ArrayList<String> emailAddressesInDatabaseArray;     //stores email addresses of all contacts
    ArrayList<String> alreadyAddedContactsUidArray;      //stores uid of current contacts
    ArrayAdapter<String> arrayAdapter;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    MaterialButton addContactButton;
    String userEmailToAdd, messageNumber;
    int publicKeySelf, publicModulusSelf,privateKeySelf;

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


        /*retrieves all user emails and uid from databse*/
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
                            contactDetails.add(dataSnapshot.getKey());        //gets the name of the snapshot (uid)
                            contactDetails.add(dataSnapshot.child("email").getValue().toString());
                            contactDetails.add(dataSnapshot.child("publicKey").getValue().toString());
                            contactDetails.add(dataSnapshot.child("publicModulus").getValue().toString());
                            detailsContactsInDatabaseArray.add(contactDetails);        //[("uid1","email1"),("uid2","email2")...]
                        }

                        if(dataSnapshot.child("email").getValue().equals(mAuth.getCurrentUser().getEmail())){     //for current user
                            for(DataSnapshot contactDataSnapshot:snapshot.child(mAuth.getCurrentUser().getUid()).child("contacts").getChildren()){  //iterate through contacts
                                alreadyAddedContactsUidArray.add(contactDataSnapshot.getKey());
                                //adds string of contact uid to an 'existing contacts' array
                                //Either is an existing contact or previously was a contact so a chat still exists
                            }
                            publicKeySelf = Integer.parseInt(dataSnapshot.child("publicKey").getValue().toString());
                            publicModulusSelf = Integer.parseInt(dataSnapshot.child("publicModulus").getValue().toString());
                            privateKeySelf = Integer.parseInt(dataSnapshot.child("privateKey").getValue().toString());
                            //retrieves the public key for self
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


        /*when user types into search bar*/
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
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


        /*when button is pressed after a contact is selected*/
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

                        //checks the state of the contact, whether there is an existing chat or old chat that was deleted
                        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("contacts").child(contactUidToAdd).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {    //gets the contact uid

                                String contactState= snapshot.getValue().toString();
                                if(contactState.equals("true")){
                                    Toast.makeText(AddContactActivity.this, userEmailToAdd+" is already in your contacts list", Toast.LENGTH_SHORT).show();
                                }else if(contactState.equals("false")){
                                    Toast.makeText(AddContactActivity.this, "A chat with "+userEmailToAdd+" already exists. You can select add contact in the chat", Toast.LENGTH_SHORT).show();
                                }else{      //contact UID has a value of the corresponding chatID~ means chat was previously deleted

                                    //sets chatID to true so that it displays again
                                    String chatID=contactState;
                                    databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("chats").child(chatID).setValue(true);
                                    //sets contact to true to add contact
                                    databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("contacts").child(contactUidToAdd).setValue(true);

                                    //sends a new welcome back message, first counts number of messages to get the right index for the new message
                                    databaseReference.child("messages").child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {    //all messages in chat

                                            if (snapshot.exists()) {
                                                messageNumber = String.valueOf((int)snapshot.getChildrenCount());  //this is index of new message
                                                Log.d(TAG, "onDataChange: message No"+messageNumber);
                                            } else {
                                                messageNumber = "0";
                                            }

                                            //then retrieves encrypted symmetric key for self to send a welcome back message
                                            databaseReference.child("chats").child(chatID).child("users").child(mAuth.getCurrentUser().getUid()).child("chatSymmetricKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                                    String encryptedSymmetricKeySelf = snapshot.getValue().toString();
                                                    EncryptionManager encryptionManager = new EncryptionManager();
                                                    int symmetricKey = Integer.parseInt(encryptionManager.RSAdecrypt(encryptedSymmetricKeySelf,privateKeySelf,publicModulusSelf));


                                                    //creates a hashmap to add a new "chat" and "message" branch in realtime database quickly
                                                    String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                                                    String reWelcomeMessage ="Hello im back! ("+mAuth.getCurrentUser().getEmail()+")";
                                                    String encryptedReWelcomeMessage = encryptionManager.symmetricEncrypt(reWelcomeMessage,symmetricKey);

                                                    Map<String,Object> chatsUpdate = new HashMap<>();
                                                    chatsUpdate.put("chats/"+chatID+"/lastMessage",reWelcomeMessage);   //keep this as un-encrypted, for ease to contact list activity
                                                    chatsUpdate.put("chats/"+chatID+"/timestamp",timestamp);

                                                    chatsUpdate.put("messages/"+chatID+"/"+messageNumber+"/sender",mAuth.getCurrentUser().getUid());
                                                    chatsUpdate.put("messages/"+chatID+"/"+messageNumber+"/message",encryptedReWelcomeMessage);   //long term stored encrypted
                                                    chatsUpdate.put("messages/"+chatID+"/"+messageNumber+"/timestamp",timestamp);

                                                    //updates messages and last message in chats
                                                    databaseReference.updateChildren(chatsUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull  Task<Void> task) {
                                                            Toast.makeText(AddContactActivity.this, "Chat with "+userEmailToAdd+" re-added", Toast.LENGTH_SHORT).show();
                                                            Log.d(TAG, "onComplete: added chat to database");
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "onFailure: failed to add chat to database. "+e.getMessage());
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }else {   //completely new contact

                        //adds a new chat for this user and contact in the database and retrieves the unique key produced
                        String chatID= databaseReference.child("chats").push().getKey();

                        //add this chatID to the current user ~ triggers listener 1 (also 3)
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

                        //also add this chatID to the other user
                        databaseReference.child("users").child(contactUidToAdd).child("chats").child(chatID).setValue(true);

                        //creates a hashmap to add a new "chat" and "message" branch in realtime database quickly
                        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

                        EncryptionManager encryptionManager = new EncryptionManager();
                        int symmetricKeyChat = encryptionManager.getRandInt(1,9);  //generates random symmetric key num between 1-9
                        String welcomeMessage ="Hello this is "+mAuth.getCurrentUser().getEmail();
                        String encryptedWelcomeMessage = encryptionManager.symmetricEncrypt(welcomeMessage,symmetricKeyChat);


                        System.out.println("sym key"+symmetricKeyChat);
                        int publicKeyOther = Integer.parseInt(detailsContactsInDatabaseArray.get(emailAddressesInDatabaseArray.indexOf(userEmailToAdd)).get(2));
                        int publicModulusOther = Integer.parseInt(detailsContactsInDatabaseArray.get(emailAddressesInDatabaseArray.indexOf(userEmailToAdd)).get(3));
                        //gets public key & modulus of other user from the 2D array, which was retrieved from the database
                        String encryptedSymKeySelf = encryptionManager.RSAencrypt(String.valueOf(symmetricKeyChat),publicKeySelf,publicModulusSelf);
                        String encryptedSymKeyOther = encryptionManager.RSAencrypt(String.valueOf(symmetricKeyChat),publicKeyOther,publicModulusOther);
                        //makes two copies of the symmetricKeyChat, one encrypted for each user


                        Map<String,Object> chatsUpdate = new HashMap<>();
                        chatsUpdate.put("chats/"+chatID+"/users/"+mAuth.getCurrentUser().getUid()+"/displayMessageNo",-1);   //index of the message to display from
                        chatsUpdate.put("chats/"+chatID+"/users/"+contactUidToAdd+"/displayMessageNo",-1);   //-1 means show all messages
                        chatsUpdate.put("chats/"+chatID+"/users/"+mAuth.getCurrentUser().getUid()+"/chatSymmetricKey",encryptedSymKeySelf);
                        chatsUpdate.put("chats/"+chatID+"/users/"+contactUidToAdd+"/chatSymmetricKey",encryptedSymKeyOther);

                        chatsUpdate.put("chats/"+chatID+"/lastMessage",welcomeMessage);
                        chatsUpdate.put("chats/"+chatID+"/timestamp",timestamp);

                        chatsUpdate.put("messages/"+chatID+"/0/sender",mAuth.getCurrentUser().getUid());
                        chatsUpdate.put("messages/"+chatID+"/0/message",encryptedWelcomeMessage);
                        chatsUpdate.put("messages/"+chatID+"/0/timestamp",timestamp);

                        //adding all atomic updates to chats and messages ~ triggers listener 2 for "chats"
                        databaseReference.updateChildren(chatsUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull  Task<Void> task) {
                                Toast.makeText(AddContactActivity.this, "Chat with new contact " + userEmailToAdd + " created", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onComplete: added chat to database");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddContactActivity.this, "Failed to create chat. "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onFailure: failed to add chat to database. "+e.getMessage());
                            }
                        });

                        //adds new contact Uid with value of their email address under "contacts" key
                        //add contacts child to users node last to trigger listener 3 again to update contact list with correct names
                        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("contacts").child(contactUidToAdd).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "onComplete: added new contact " + userEmailToAdd);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: error adding new contact. " + e.getMessage());
                            }
                        });

                        //also add current user as contact under the recipient to avoid two chats with same people
                        //set contact as "false" to show they haven't actually added them as a contact but a chat exists
                        databaseReference.child("users").child(contactUidToAdd).child("contacts").child(mAuth.getCurrentUser().getUid()).setValue(false);

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