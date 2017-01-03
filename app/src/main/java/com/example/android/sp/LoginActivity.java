// This is the main Login screen for the app
//package name
package com.example.android.sp;
//all imports
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener,SignupDialog.SignupDialogListener
{
    //Initializing all objects and variables

    // -- General Utilities --
    private EditText username,user,pass,fn,ln,password;
    private static final String TAG = "debugger";
    private final static String UID="";
    private String userid="";
    private String logoutFlag = "0";
    private String firstname="",email="",lastname="";
    private String logoutFlagString = "logoutflag";
    private int count=0;
    private boolean isCheckedIn=false;
    private Bitmap profilepic;

    // -- Firebase variables --
    private FirebaseAuth mAuthstart,mAuthfb,mAuthlogin,mAuthsignup, mAuthgoogle;
    private DatabaseReference mDatabase;
    private DatabaseReference database;
    private FirebaseAuth.AuthStateListener mAuthListener,newAccountListener;

    // -- Fb login variables --
    private CallbackManager callbackManager;
    private LoginButton fblogin_button;

    // -- Google login variables --
    private GoogleApiClient mGoogleApiClient;
    private Uri googlepic;



    // -------------  Activity LifeCycle Functions -------------------------------//


    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);                 //call onCreate method of super class
        FacebookSdk.sdkInitialize(getApplicationContext()); //this line has to come before setting the view
        setContentView(R.layout.activity_login);            //setup the content view for the loginActivity
        Firebase.setAndroidContext(this);                   //set Firebase context

        //create an auth state listener allows already logged in users to proceed
        mAuthstart = FirebaseAuth.getInstance();            //get current state of login
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //if someone is already signed in, move on to main activity
                    Log.d(TAG, "UID of signed in user " + user.getUid());
                    checkStatus(user.getUid());   //See if user has active CheckIn and then proceed
                } else {
                    // there is no one signed in
                    Log.d(TAG, "no one signed in");
                }

            }
        };
        mAuthstart.addAuthStateListener(mAuthListener); //add the above listener to the firebaseAuth object

        // Configure facebook login material
        callbackManager = CallbackManager.Factory.create();                //callbackManager for facebook login
        fblogin_button = (LoginButton) findViewById(R.id.fblogin_button);  //find facebook's login button
        ImageView fblogin = (ImageView) findViewById(R.id.fblogin);        //load the fblogin button image
        fblogin.setOnClickListener(this);                                  //set onClick listener on it
        fblogin_button.setReadPermissions(Arrays.asList("public_profile","email")); //setup facebook permissions
        //Add the callback manager to facebook's login button
        fblogin_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
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
                Log.d(TAG, "user cancelled fb login");
            }

            @Override
            public void onError(FacebookException exception)
            {
                Log.d(TAG, "Unable to login");
            }
        });



        // Configure google sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getApplicationContext().getString(R.string.web_client_id)) //web client id to connect to firebase
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* LoginActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton signInButton = (SignInButton)findViewById(R.id.google_login);
        setGooglePlusButtonText(signInButton);                                 //modify the text on google sign in button
        signInButton.setOnClickListener(this);


        // Setup email login
        username = (EditText) findViewById(R.id.username);                    //find username textbox
        username.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);       //turn off its auto correct
        password = (EditText) findViewById(R.id.password);                    //find password textbox


        //Create another listener for signups
        newAccountListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //account created, add user to firebase
                    Log.d(TAG, "New account created for id " + user.getUid());
                    addNewUser(user.getUid());    // add New User details to database
                    checkStatus(user.getUid());


                } else {
                    // account creation unsuccessful
                    Log.d(TAG, "account not created");
                }
            }
        };

        //logout current user on receiving a signal from CheckInActivity
        Intent intent = getIntent();
        if(intent.getExtras() != null && intent.getStringExtra(logoutFlagString) != null){
            logoutFlag = intent.getStringExtra(logoutFlagString);
            if(logoutFlag.equals("1")) {
                LoginManager.getInstance().logOut();  //logout Facebook
                FirebaseAuth.getInstance().signOut(); //logout Firebase
            }
            logoutFlag="0";
        }

        //Setup initial state of progress bar
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
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



    //------------------- Utility Functions ----------------------------------//

    //-----------This function checks if the user has an active CheckIn ---//
    private void checkStatus(String UID){
        showLoading();            //display the progress bar
        userid = UID;
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference

        ValueEventListener valuelistener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(count==0) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "CheckIn exists");
                        isCheckedIn=true;

                    }
                    else if(!dataSnapshot.exists()){
                        Log.d(TAG, "CheckIn does not exist");
                        isCheckedIn=false;

                    }
                    goAhead(userid);     //Proceed to HomeScreenActivity
                }
                count=count+1;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                //Do nothing

            }
        };
        database.child("CheckInUsers").child(userid).addListenerForSingleValueEvent(valuelistener); //attach listener

    }

    //go Ahead to HomeScreenActivity
    private void goAhead(String ID){
        checkStatus(ID);
        Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class); //send Intent
        intent.putExtra("userid", ID);
        intent.putExtra("sendstatus",isCheckedIn);
        intent.putExtra("startedfrom","login");
        startActivity(intent);
    }

    //Display progress bar
    private void showLoading(){
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        findViewById(R.id.mainlayout).setVisibility(View.GONE);
        fblogin_button.setVisibility(View.GONE);
    }

    //add a new user to database
    private void addNewUser(String userID){
        Log.d(TAG, "Adding new user");
        mDatabase = FirebaseDatabase.getInstance().getReference();          //get a firebase key for the update
        String key = mDatabase.child("UserInformation").push().getKey();
        UserDetails user = new UserDetails(firstname,lastname,email,0,0,0); //make a new user object
        Map<String, Object> newUser = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/UserInformation/"+userID, newUser);
        mDatabase.updateChildren(childUpdates);

        //upload user's profile picture to firebase
        if(profilepic!=null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference dpRef = storageRef.child(userid + "/Profile/dp.jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            profilepic.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = dpRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "image upload failed");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "image upload success");
                }
            });
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_login:
                googlelogin();
                break;
            case R.id.fblogin:
                fblogin_button.performClick();
                // ...
        }
    }

    //Utility to convert dp to pixels
    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }



    //------------------------ Email Signup Functions -----------------------------------------------//



    private void showSignupDialog(View v) {
        // Create an instance of the Signup Dialog fragment and show it
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
    private void signup() {
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
                            fblogin_button.setVisibility(View.VISIBLE);
                            findViewById(R.id.mainlayout).setVisibility(View.VISIBLE);
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Unable to create the account!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //----------------------- Email Login -----------------------------------------------//

    //This function is triggered when you press the login button
    private void login(View view) {

        if(!username.getText().toString().contains(".com") || !username.getText().toString().contains("@"))
        {
            Toast.makeText(this,"Invalid email !",Toast.LENGTH_SHORT).show();     //check for validity of email id
            return;
        }
        showLoading();                                 //show loading
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
                            fblogin_button.setVisibility(View.VISIBLE);
                            findViewById(R.id.mainlayout).setVisibility(View.VISIBLE);
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
                //get user's information from facebook
                try
                {
                    email       =   object.getString("email");
                    firstname  =   object.optString("first_name");
                    lastname   =   object.optString("last_name");
                }
                catch (JSONException e)
                {
                    Log.d(TAG,"could not fetch user details");
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
        Bundle params = new Bundle();
        params.putString("fields", "id,email,gender,cover,picture.type(large)");
        //Make another graphRequest to get the facebook profilepic
        GraphRequest graphRequest2 = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
            /* handle the result */
                        if (response != null) {
                            try {
                                JSONObject data = response.getJSONObject();
                                String profilePicUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                Log.d(TAG,"dp url "+profilePicUrl);
                                Picasso.with(getApplicationContext()).load(profilePicUrl).into(new com.squareup.picasso.Target() {
                                    @Override
                                    public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                                        profilepic = bitmap;
                                        Log.d(TAG,"profile pic loaded");
                                    }
                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    }
                                });

                            } catch (Exception e) {

                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        graphRequest2.executeAsync();
        Log.d(TAG, "onAuthStateChanged:token"+token.getCurrentAccessToken());
        mAuthfb = FirebaseAuth.getInstance();            //get Firebase Instance
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
                            fblogin_button.setVisibility(View.VISIBLE);
                            findViewById(R.id.mainlayout).setVisibility(View.VISIBLE);
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


    protected void setGooglePlusButtonText(SignInButton signInButton) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText("Google Login");
                tv.setHeight(dpToPx(16));
                return;
            }
        }
    }



    private void googlelogin(){
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
            googlepic = account.getPhotoUrl();

            Log.d(TAG,"googlepic "+googlepic.toString());
            Picasso.with(getApplicationContext()).load(googlepic).into(new com.squareup.picasso.Target() {
                @Override
                public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                    profilepic = bitmap;
                    Log.d(TAG,"profile pic loaded");
                }
                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });

            firebaseAuthWithGoogle(account);
            Log.d(TAG,"google sign in success");

        } else {
            Log.d(TAG,"google sign in not successful");

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
                            fblogin_button.setVisibility(View.VISIBLE);
                            findViewById(R.id.mainlayout).setVisibility(View.VISIBLE);
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }



}