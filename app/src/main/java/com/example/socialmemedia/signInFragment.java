package com.example.socialmemedia;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.ContentValues.TAG;

public class signInFragment extends Fragment {

    FirebaseAuth mAuth;
    Button signInButton,resetPasswordButton;
    DatabaseReference databaseReference;
    EditText email,password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        signInButton = getView().findViewById(R.id.signInButton);
        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();    //connects to online Firebase database //get reference of top level of database
        email = getView().findViewById(R.id.email_address);
        password = getView().findViewById(R.id.password);
        resetPasswordButton = getView().findViewById(R.id.resetPasswordButton);



        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getString(email).isEmpty()) {
                    Log.d(TAG, "onClick: error reset email empty");
                    Toast.makeText(getContext(), "Error in resetting email. Please enter an email address", Toast.LENGTH_SHORT).show();
                }else {
                    sendEmailPasswordResetWithContinueUrl();
                }
            }
        });

    }

    /***************other methods**************************************************/
    private void signIn(){
        if(getString(email).isEmpty()){
            Toast.makeText(getContext(),"Please enter your email",Toast.LENGTH_SHORT).show();
        } else if(!isEmailValid(getString(email))){
            Toast.makeText(getContext(),"Please enter a valid email address",Toast.LENGTH_SHORT).show();
        } else if(getString(password).isEmpty()){
            Toast.makeText(getContext(),"Please enter your password",Toast.LENGTH_SHORT).show();
        } else{
            mAuth.signInWithEmailAndPassword(getString(email),getString(password)).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        if(mAuth.getCurrentUser().isEmailVerified()){    //just before switching to the ContactListActivity, check user if verified
                            Log.d(TAG, "onComplete: logged on"+mAuth.getUid());
                            Toast.makeText(getContext(),"Log in successful",Toast.LENGTH_SHORT).show();
                            sendUser();
                        }else{
                            Toast.makeText(getContext(), "Please verify your email before logging on", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();            //if not verified log them out again and ask for their email to be verified
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Error in signing in. "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    
    private void sendEmailPasswordResetWithContinueUrl(){     //dynamic link that should redirect on any device
        String url = "https://www.example.com";       //web link
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(url)
                .setIOSBundleId("com.example.ios")      //ios link
                .setAndroidPackageName("com.example.socialmemedia",false,null)     //android link if app detected as installed
                .build();

        mAuth.sendPasswordResetEmail(getString(email),actionCodeSettings).addOnCompleteListener(new OnCompleteListener<Void>() {
            /**sending the password reset with the actionCodeSettings means the continue url will be triggered after the link is clicked
            so user should be redirected back to the app if they are on android**/
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: resetting email sent");
                    Toast.makeText(getContext(), "Password reset email sent to "+getString(email), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: resetting email"+e.getMessage());
                Toast.makeText(getContext(), "Error in sending password reset email. " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendUser(){
        Intent intent = new Intent(getContext(),ContactListActivity.class);
        startActivity(intent);
    }

    private boolean isEmailValid(CharSequence email){
        if (email== null){
            return false;
        }
        else{
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();  //returns true or false if email matches or not
        }
    }

    private String getString(EditText editText){
        return editText.getText().toString().trim();
    }
}
