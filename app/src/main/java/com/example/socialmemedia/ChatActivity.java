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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    String chatName, chatID, contactUid, contactEmail;
    EditText textBox;
    ImageButton sendButton;
    ListView chatListView;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> messagesArray;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    int messageNumber,displayFromMessageNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatName= getIntent().getStringExtra("name");
        chatID = getIntent().getStringExtra("chatID");
        contactUid= getIntent().getStringExtra("contactUid");
        contactEmail= getIntent().getStringExtra("email");

        toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(chatName);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {    //back button
            @Override
            public void onClick(View v) {
                sendToContactListActivity();
            }
        });

        /*messaging*/
        mAuth= FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();
        textBox= findViewById(R.id.text_box);
        sendButton = findViewById(R.id.send_button);
        chatListView = findViewById(R.id.listView);
        messagesArray= new ArrayList<>();




        /*sending a message*/
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!textBox.getText().toString().isEmpty()){    //checks if text box is not empty

                    String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                    Map<String,Object> messageData= new HashMap<>();
                    messageData.put("sender",mAuth.getCurrentUser().getUid());
                    messageData.put("message",textBox.getText().toString());
                    messageData.put("timestamp",timestamp);

                    databaseReference.child("messages").child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {    //all messages in chat
                            if(snapshot.exists()){
                                messageNumber=(int)snapshot.getChildrenCount();  //this is index of new message

                            }else{
                                messageNumber=0;
                            }

                            //adds new message under "messages" for this chatID
                            databaseReference.child("messages").child(chatID).child(String.valueOf(messageNumber)).setValue(messageData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    textBox.setText("");   //clears input box once message is added to database
                                    Log.d(TAG, "onComplete: message sent");

                                   // databaseReference.child("users").child(contactUid).child("chats").child(chatID).setValue(true);
                                    //sets chatID to true for other user to ensure the chat is not hidden for them

                                    //checks if the other user has the chat deleted
                                    databaseReference.child("users").child(contactUid).child("chats").child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                if(snapshot.getValue().toString().equals("false")){  //i.e chatsID="false" ,contacts value is a chatID
                                                    //contact doesn't want to add this user as a contact but has a new chat message
                                                    databaseReference.child("users").child(contactUid).child("chats").child(chatID).setValue(true);
                                                    databaseReference.child("users").child(contactUid).child("contacts").child(mAuth.getCurrentUser().getUid()).setValue(false);

                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Error in sending message. "+e.getMessage());
                                    Toast.makeText(ChatActivity.this, "Error in sending message. "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            //updates the last message for this chatID under "chats"
                            databaseReference.child("chats").child(chatID).child("lastMessage").setValue(textBox.getText().toString());
                            databaseReference.child("chats").child(chatID).child("timestamp").setValue(timestamp).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull  Task<Void> task) {
                                    Log.d(TAG, "onComplete: last message updated");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull  Exception e) {
                                    Log.d(TAG, "onFailure: last message not upated. "+e.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });

        /*getting the displayMessageNo for this chat and user*/
        databaseReference.child("chats").child(chatID).child("users").child(mAuth.getCurrentUser().getUid()).child("displayMessageNo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    displayFromMessageNumber= Integer.parseInt(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*displaying the messages*/
        databaseReference.child("messages").child(chatID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    messagesArray.clear();   //clears array to repopulate it

                    for(DataSnapshot dataSnapshot: snapshot.getChildren()) {  //for each message
                        Log.d(TAG, "onDataChange: message "+dataSnapshot.getKey()+" vs "+displayFromMessageNumber);
                        if(Integer.parseInt(dataSnapshot.getKey())>=displayFromMessageNumber) {    //only show messages after or = this number
                            String newMessage;
                            if (dataSnapshot.child("sender").getValue().toString().equals(mAuth.getCurrentUser().getUid())) {   //sent by current user
                                newMessage = dataSnapshot.child("message").getValue().toString();       //gets message from database
                            } else {       //sent by other person
                                newMessage = ">" + dataSnapshot.child("message").getValue().toString();
                            }
                            messagesArray.add(newMessage);
                        }
                    }

                    arrayAdapter = new ArrayAdapter<String>(ChatActivity.this, android.R.layout.simple_list_item_1, messagesArray);
                    chatListView.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_top,R.anim.slide_out_bottom);
    }

    /*toolbar buttons menu*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_chat_toolbar,menu);

        Log.d(TAG, "onCreateOptionsMenu: helloo");
        //if contact is "false" then show "Add Contact" else hide it, converse for "Remove Contact"
        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("contacts").child(contactUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {   //contactUid
                if (snapshot.exists()){
                    String contactState = snapshot.getValue().toString();   //"true" or "false"
                    Log.d(TAG, "onDataChange: contact state "+contactState);

                    if(contactState.equals("true")){                  //already a contact
                        menu.findItem(R.id.add_contact).setVisible(false);
                        menu.findItem(R.id.remove_contact_chat).setTitle("Remove Contact and Chat");

                    }else{  //either "false" or a "chatID"      //not a contact or was removed
                        menu.findItem(R.id.add_contact).setVisible(true);
                        menu.findItem(R.id.remove_contact_chat).setTitle("Remove Chat");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int selectedId=item.getItemId();
        if(selectedId==R.id.view_contact){
            Toast.makeText(this, "Email: "+contactEmail, Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Displaying contact details");
        }else if(selectedId==R.id.add_contact){
            databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("contacts").child(contactUid).setValue(true);
            Toast.makeText(this, "Contact "+contactEmail+" has been added", Toast.LENGTH_SHORT).show();
        }
        else if(selectedId==R.id.remove_contact_chat){
            databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("chats").child(chatID).setValue(false);
            //"false" to not display chat in contact list activity
            databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("contacts").child(contactUid).setValue(chatID);
            //"chatID" so that if you want to re-add user, you use the same chatId again

            clearChat();  //next time show all messages after and including this one
            Toast.makeText(this, "Chat and contact "+contactEmail+" has been deleted", Toast.LENGTH_SHORT).show();
            sendToContactListActivity();
        }else if(selectedId==R.id.clear_chat){
            clearChat();
            Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
        }

        return true;
    }
    /**********************************/

    private void clearChat(){
        messagesArray.clear();   //clears array to repopulate it
        arrayAdapter = new ArrayAdapter<String>(ChatActivity.this, android.R.layout.simple_list_item_1, messagesArray);
        chatListView.setAdapter(arrayAdapter);

        //set message index to view from next time, first find how many messages there are so far
        databaseReference.child("messages").child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {    //all messages in chat
                if (snapshot.exists()) {
                    messageNumber = (int) snapshot.getChildrenCount();  //this is index of new message
                } else {
                    messageNumber = 0;
                }
                databaseReference.child("chats").child(chatID).child("users").child(mAuth.getCurrentUser().getUid()).child("displayMessageNo").setValue(messageNumber);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendToContactListActivity(){
        Intent intent = new Intent(ChatActivity.this, ContactListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_top,R.anim.slide_out_bottom);
        //ContactList activity will slide in from top, ChatActivity will slide out from bottom
    }
}