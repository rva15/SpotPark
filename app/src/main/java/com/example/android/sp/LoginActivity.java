// This is the main Login screen for the app
//package name
package com.example.android.sp;

//all imports
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class LoginActivity extends AppCompatActivity
{
    //Initializing all objects and variables
    private CallbackManager callbackManager;
    LoginButton login_button;
    private FirebaseAuth mAuthstart,mAuthfb,mAuthlogin,mAuthsignup;
    private DatabaseReference mDatabase;
    public EditText username;
    public EditText password;
    private FirebaseAuth.AuthStateListener mAuthListener,newAccountListener;
    private static final String TAG = "Sign in debug ";
    public final static String UID="";
    String logoutFlag = "0", time= "";
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    //onCreate gets called at whenever the screen opens
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        //this is to receive the intent from MainActivity
        Intent intent = getIntent();
        //logout current user on receiving a signal from MainActivity
        if(intent.getExtras() != null && intent.getStringExtra(CheckInActivity.fbl) != null){
            logoutFlag = intent.getStringExtra(CheckInActivity.fbl);
            if(logoutFlag.equals("1")){
                LoginManager.getInstance().logOut();  //logout Facebook
                FirebaseAuth.getInstance().signOut(); //logout Firebase
                logoutFlag="0";}
        }
        super.onCreate(savedInstanceState);                 //call onCreate method of super class
        Firebase.setAndroidContext(this);                   //set Firebase context
        FacebookSdk.sdkInitialize(getApplicationContext()); //set Facebook context
        callbackManager = CallbackManager.Factory.create(); //callbackManager for facebook login
        mAuthstart = FirebaseAuth.getInstance();            //get current state of login
        //create an auth state listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //if someone is already signed in, move on to main activity
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    goAhead(user.getUid());


                } else {
                    // there is no one signed in
                    Log.d(TAG, "onAuthStateChanged:no user");

                }

            }
        };
        mAuthstart.addAuthStateListener(mAuthListener);//add the above listener to the firebaseAuth object
        //Create another listener for signups
        newAccountListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //if someone is already signed in, move on to main activity
                    Log.d(TAG, "onAuthStateChanged:acc created" + user.getUid());
                    addNewUser(user.getUid(),null,0,null);
                    goAhead(user.getUid());


                } else {
                    // there is no one signed in
                    Log.d(TAG, "onAuthStateChanged:no user");

                }

            }
        };
        setContentView(R.layout.activity_login);  //setup the content view for the loginActivity
        login_button        = (LoginButton) findViewById(R.id.login_button);  //find facebook's login button
        username = (EditText) findViewById(R.id.username);                    //find username textbox
        password = (EditText) findViewById(R.id.password);                    //find password textbox
        login_button.setReadPermissions(Arrays.asList("public_profile","email")); //setup facebook permissions
        //Add the callback manager to facebook's login button
        login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                //on successfull login we pass fb access token to this function
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel()
            {

            }

            @Override
            public void onError(FacebookException exception)
            {

            }
        });

    //End of onCreate method
    }

    //go Ahead to Main Activity
    public void goAhead(String ID){
        Intent intent = new Intent(LoginActivity.this, OptionsActivity.class); //send Intent
        intent.putExtra(UID,ID);
        startActivity(intent);
        Log.d(TAG, "onAuthStateChanged:going ahead");
        this.finish();                                                      //destroy login activity

    }

    //add a new user to database
    public void addNewUser(String userID,String userName,int numberOfKeys, String plateNumber){
        Log.d(TAG, "onAuthStateChanged:addNewUser");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("UserInformation").push().getKey();
        User user = new User(userName,numberOfKeys,plateNumber);
        Map<String, Object> newUser = user.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/UserInformation/"+userID, newUser);
        mDatabase.updateChildren(childUpdates);

    }

    //this method is called on fb's successful login
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "onAuthStateChanged:token"+token.getCurrentAccessToken());
        mAuthfb = FirebaseAuth.getInstance();       //get Firebase Instance
        mAuthfb.addAuthStateListener(newAccountListener);//add previously defined listener
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken()); //get the access token from fb
        mAuthfb.signInWithCredential(credential)    //sign in to firebase using that credential
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    //This function is triggered when you press the login button
    public void login(View view) {

        mAuthlogin = FirebaseAuth.getInstance();       //get Firebase instance

        mAuthlogin.addAuthStateListener(mAuthListener);//add listener to it
        mAuthlogin.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString()) //sign in with email
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });

    }

    // This function is triggered when user presses signup button
    public void signup(View view) {
        mAuthsignup = FirebaseAuth.getInstance();               //get Firebase instance
        //create a listener for signup process
        mAuthsignup.addAuthStateListener(newAccountListener);        //add listener
        mAuthsignup.createUserWithEmailAndPassword(username.getText().toString(), password.getText().toString()) //create the user
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "could not create",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    //Activity lifecycle functions. Currently just set to default
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //something to do with fb login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}