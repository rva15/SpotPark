// This is the main Login screen for the app
//package name
package com.example.android.sp;

//all imports
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.android.sp.SignupDialog;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener,SignupDialog.SignupDialogListener
{
    //Initializing all objects and variables
    private CallbackManager callbackManager;
    LoginButton login_button;
    private FirebaseAuth mAuthstart,mAuthfb,mAuthlogin,mAuthsignup, mAuthgoogle;
    private DatabaseReference mDatabase;
    public EditText username,user,pass,fn,ln;
    public EditText password;
    private FirebaseAuth.AuthStateListener mAuthListener,newAccountListener;
    private static final String TAG = "Sign in debug ";
    public final static String UID="";
    String userid="";
    String logoutFlag = "0";
    GoogleApiClient mGoogleApiClient;
    public String firstname="",email="",lastname="",platenumber="";
    int numberOfKeys=0,count=0;
    DatabaseReference database;
    boolean isCheckedIn=false;

    // -------------  Activity LifeCycle Functions -------------------------------//


    //onCreate method
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);                 //call onCreate method of super class
        Firebase.setAndroidContext(this);                   //set Firebase context

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("283432722166-icn0f1dke2845so2ag841mpvdklssum7.apps.googleusercontent.com") //web client id to connect to firebase
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* LoginActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //this is to receive the intent from CheckInActivity
        Intent intent = getIntent();
        //logout current user on receiving a signal from CheckInActivity
        if(intent.getExtras() != null && intent.getStringExtra(CheckInActivity.fbl) != null){
            logoutFlag = intent.getStringExtra(CheckInActivity.fbl);
            if(logoutFlag.equals("1")) {
                LoginManager.getInstance().logOut();//logout Facebook
                FirebaseAuth.getInstance().signOut(); //logout Firebase
            }
            logoutFlag="0";
        }

        FacebookSdk.sdkInitialize(getApplicationContext()); //set Facebook context
        callbackManager = CallbackManager.Factory.create(); //callbackManager for facebook login
        mAuthstart = FirebaseAuth.getInstance();            //get current state of login
        //create an auth state listener allows already logged in users to proceed
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //if someone is already signed in, move on to main activity
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    checkStatus(user.getUid());
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
                    //account created, add user to firebase
                    Log.d(TAG, "onAuthStateChanged:acc created" + user.getUid());
                    addNewUser(user.getUid());
                    checkStatus(user.getUid());


                } else {
                    // account creation unsuccessful
                    Log.d(TAG, "onAuthStateChanged:account not created");

                }

            }
        };
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login1);  //setup the content view for the loginActivity
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        SignInButton signInButton = (SignInButton)findViewById(R.id.google_login);
        setGooglePlusButtonText(signInButton);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        login_button        = (LoginButton) findViewById(R.id.login_button);  //find facebook's login button
        username = (EditText) findViewById(R.id.username);                    //find username textbox
        username.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);       //turn off its auto correct
        password = (EditText) findViewById(R.id.password);                    //find password textbox
        findViewById(R.id.google_login).setOnClickListener(this);
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
                Log.d(TAG, "Unable to login");
            }
        });

        //End of onCreate method
    }


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


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Toast.makeText(this,"Connectivity problems!",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }



    //------------------- Functions belonging to Activity ----------------------------------//

    public void checkStatus(String UID){
        showLoading();
        userid = UID;
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        //com.google.firebase.database.Query getcheckin = database.child("CheckInUsers").orderByKey().equalTo(UID);
        //getcheckin.addChildEventListener(listener1);

        ValueEventListener valuelistener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(count==0) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "exists");
                        isCheckedIn=true;

                    }
                    else if(!dataSnapshot.exists()){
                        Log.d(TAG, "not exists");
                        isCheckedIn=false;

                    }
                    goAhead(userid);
                }
                count=count+1;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

            }
        };
        database.child("CheckInUsers").child(userid).addListenerForSingleValueEvent(valuelistener);

    }

    //go Ahead to relevant Options Activity
    public void goAhead(String ID){
        checkStatus(ID);
        if(isCheckedIn){
            Intent intent = new Intent(LoginActivity.this, OptionActivity2.class); //send Intent
            intent.putExtra(UID,ID);
            startActivity(intent);
            Log.d(TAG, "onAuthStateChanged:going ahead");
            this.finish();                                                      //destroy login activity
        }
        else{
            Intent intent = new Intent(LoginActivity.this, OptionsActivity.class); //send Intent
            intent.putExtra(UID,ID);
            startActivity(intent);
            Log.d(TAG, "onAuthStateChanged:going ahead");
            this.finish();                                                      //destroy login activity
        }

    }

    public void showLoading(){
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        login_button.setVisibility(View.GONE);

        //Toast.makeText(this,"Authenticating credentials",Toast.LENGTH_LONG).show();
    }


    //------------------------ Email Signup Functions -----------------------------------------------//

    //add a new user to database
    public void addNewUser(String userID){
        Log.d(TAG, "onAuthStateChanged:addNewUser");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("UserInformation").push().getKey();
        UserDetails user = new UserDetails(firstname,lastname,email,numberOfKeys,platenumber);
        Map<String, Object> newUser = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/UserInformation/"+userID, newUser);
        mDatabase.updateChildren(childUpdates);

    }

    public void showNoticeDialog(View v) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new SignupDialog();
        dialog.show(getSupportFragmentManager(),"Signup fragment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Dialog dialogView = dialog.getDialog();
        fn = (EditText) dialogView.findViewById(R.id.firstname);
        ln = (EditText) dialogView.findViewById(R.id.lastname);
        user = (EditText) dialogView.findViewById(R.id.newusername);
        pass = (EditText) dialogView.findViewById(R.id.newuserpassword);
        signup();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        //Do nothing
        Log.d(TAG,"negative click");
    }

    // This function is triggered when user presses signup button
    public void signup() {
        showLoading();//show loading

        firstname = fn.getText().toString();
        lastname = ln.getText().toString();
        email = user.getText().toString();
        mAuthsignup = FirebaseAuth.getInstance();               //get Firebase instance
        //create a listener for signup process
        mAuthsignup.addAuthStateListener(newAccountListener);        //add listener
        mAuthsignup.createUserWithEmailAndPassword(user.getText().toString(), pass.getText().toString()) //create the user
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            login_button.setVisibility(View.VISIBLE);
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Unable to create the account!",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    //----------------------- Email Login -----------------------------------------------//

    //This function is triggered when you press the login button
    public void login(View view) {

        if(!username.getText().toString().contains(".com") || !username.getText().toString().contains("@"))
        {
            Toast.makeText(this,"Invalid email !",Toast.LENGTH_SHORT).show();     //check for validity of email id
            return;
        }
        showLoading();                                 //show loading toast
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
                            login_button.setVisibility(View.VISIBLE);
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });

    }

    //------------------------------------Facebook Login ------------------------------------//

    //this method is called on fb's successful login
    private void handleFacebookAccessToken(AccessToken token) {

        showLoading();
        GraphRequest graphRequest   =   GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback()
        {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response)
            {
                Log.d("JSON", ""+response.getJSONObject().toString());

                try
                {
                    email       =   object.getString("email");
                    firstname  =   object.optString("first_name");
                    lastname   =   object.optString("last_name");
                }
                catch (JSONException e)
                {
                    Log.d(TAG,"could not fetch details");
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
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
                            login_button.setVisibility(View.VISIBLE);
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    //on activity result function
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 45) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    //----------------------------------- Google Login ----------------------------------------//


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_login:
                googlelogin();
                break;
            // ...
        }
    }

    protected void setGooglePlusButtonText(SignInButton signInButton) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText("Login with Google");
                return;
            }
        }
    }


    public void googlelogin(){
        Log.d(TAG,"googlelogin called");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 45);
    }


    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            firstname = account.getGivenName();
            lastname = account.getFamilyName();
            email = account.getEmail();

            firebaseAuthWithGoogle(account);
            Log.d(TAG,"google sign in success");

        } else {
            Log.d(TAG,"no success");

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showLoading();
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        mAuthgoogle = FirebaseAuth.getInstance();
        mAuthgoogle.addAuthStateListener(newAccountListener);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuthgoogle.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            login_button.setVisibility(View.VISIBLE);
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }



}