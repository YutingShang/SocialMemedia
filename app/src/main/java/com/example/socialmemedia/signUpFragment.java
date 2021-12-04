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


public class signUpFragment extends Fragment {

    FirebaseAuth mAuth;
    Button signUpButton,resendVerificationButton;
    DatabaseReference databaseReference;
    EditText name,email,password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();
        name = getView().findViewById(R.id.name_signUp);
        email = (EditText)getView().findViewById(R.id.email_address_signUp);
        password = getView().findViewById(R.id.password_signUp);
        signUpButton = getView().findViewById(R.id.signUpButton);
        resendVerificationButton= getView().findViewById(R.id.resendVerificationButton);


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        resendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getString(email).isEmpty()) {
                    Toast.makeText(getContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
                } else if (getString(password).isEmpty()) {
                    Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(getString(email), getString(password)).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            sendEmailVerificationWithContinueUrl();
                            //sending a verification email requires the mAuth.getCurrentUser() so user needs to be logged in
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "error in resending email verification. " + e.getMessage());
                            Toast.makeText(getContext(), "Error in resending email verification. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    /***************other methods**************************************************/
    private void signUp() {
        //first validates the user input
        if (getString(name).isEmpty()) {
            Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
        } else if(!isAlpha(getString(name))){
            Toast.makeText(getContext(),  "Please enter a valid name", Toast.LENGTH_SHORT).show();
        } else if(getString(email).isEmpty()){
            Toast.makeText(getContext(),"Please enter your email",Toast.LENGTH_SHORT).show();
        } else if(!isEmailValid(getString(email))){
            Toast.makeText(getContext(),"Please enter a valid email address",Toast.LENGTH_SHORT).show();
        } else if(getString(password).isEmpty()) {
            Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
        }else if(isStrongPassword(getString(password))!=1){
            switch (isStrongPassword(getString(password))){
                case -1:
                    Toast.makeText(getContext(), "Please enter a secure password. Refrain from trivial passwords", Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    Toast.makeText(getContext(), "Please enter a secure password. You must include at least one letter and one number", Toast.LENGTH_SHORT).show();
                    break;
                case -3:
                    Toast.makeText(getContext(), "Please enter a secure passord. It must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else{
            mAuth.createUserWithEmailAndPassword(getString(email),getString(password)).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        //add user data to a new branch on the database
                        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("name").setValue(getString(name));
                        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("email").setValue(getString(email)).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getContext(),"Sign up successful",Toast.LENGTH_SHORT).show();
                                    sendEmailVerificationWithContinueUrl();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {            //failed to add data to database
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: saving data"+e.getMessage());
                                Toast.makeText(getContext(), "Failure in saving data: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {    //failed to create new user
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Error in signing up "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    public void onStart() {   //when application opens, e.g between switching applications
        super.onStart();
        FirebaseUser user=mAuth.getCurrentUser();
        if (user!=null) {    //if user is signed in
            FirebaseAuth.getInstance().getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                //refreshes data of current user so isEmailVerified will be updated
                @Override
                public void onSuccess(Void unused) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user.isEmailVerified()) {          //if user has verified their email
                        Log.d(TAG, "onSuccess: user is verified "+user.getUid());
                        sendUser();     //start intent to ContactListActivity
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Reload user failed. Please manually log in.  "+e.getMessage());
                    Toast.makeText(getContext(), "Reload user failed. Please manually log in.  "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void sendUser(){
        Intent intent = new Intent(getContext(),ContactListActivity.class);
        startActivity(intent);

    }

    //method to get string from text box, prevent lots of code repetition
    private String getString(EditText editText){
        return editText.getText().toString().trim();
    }


    /******Input validation methods**************************/
    private boolean isEmailValid(CharSequence email){
        if (email== null){
            return false;
        }
        else{
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();  //returns true or false if email matches or not
        }
    }

    private boolean isAlpha(String string) {
        for (int i = 0; i < string.length(); i++) {
            if ((string.charAt(i)<'A' || string.charAt(i)>'Z') && (string.charAt(i)<'a' || string.charAt(i)>'z') && (string.charAt(i)!=' ')){
                //if a single character in the word is outside the alphabet and is not a space, then it contains unaccepted symbols
                return false;
            }
        }
        return true;
    }

    private boolean containsNumbers(String string){
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i)>='0' && string.charAt(i)<='9'){
                return true;
            }
        }
        return false;
    }

    private boolean containsLetters(String string) {
        for (int i = 0; i < string.length(); i++) {
            if ((string.charAt(i)>='A' && string.charAt(i)<='Z') || (string.charAt(i)>='a' || string.charAt(i)<='z')){
                return true;
            }
        }
        return false;
    }

    private int isStrongPassword(String password){
        if(password.toLowerCase().contains("password") || password.contains("123") || password.toLowerCase().contains("qwerty")){
            return -1;
        } else if (!containsNumbers(password) || !containsLetters(password) ){
            return -2;
        } else if (password.length()<6){
            return -3;
        }
        return 1;
    }

    /**********************dynamic link after email***************************/
    private void sendEmailVerificationWithContinueUrl(){       //dynamic link that should redirect on any device
        String url = "https://www.example.com/verify?uid="+mAuth.getCurrentUser().getUid();    //web link
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(url)
                .setIOSBundleId("com.example.ios")      //ios link
                .setAndroidPackageName("com.example.socialmemedia",false,null)
                //android link if app detected as installed
                .build();

        mAuth.getCurrentUser().sendEmailVerification(actionCodeSettings).addOnCompleteListener(new OnCompleteListener<Void>() {
            /**actionCodeSettings means the continue url will be triggered after the link is clicked
             so user should be redirected back to the app if they are on android**/
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: email verification continue url sent");
                    Toast.makeText(getContext(), "Verification email sent to "+mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    //no need to call sendUser() because email link will redirect to the app, and the OnStart() the user would be logged in
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: verification url"+e.getMessage());
                Toast.makeText(getContext(), "Email verification link not sent. "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
