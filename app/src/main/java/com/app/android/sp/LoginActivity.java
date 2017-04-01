// This is the main Login screen for the app
//package name
package com.app.android.sp;
//all imports
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
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


public class LoginActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {
    //Initializing all objects and variables

    // -- General Utilities --
    private EditText username, pass, fn, ln, password;
    private static final String TAG = "debugger";
    private final static String UID = "";
    private String userid = "";
    private String logoutFlag = "0";
    private String firstname = "", email = "", lastname = "";
    private String logoutFlagString = "logoutflag";
    private int count = 0;
    private boolean isCheckedIn = false;
    private Bitmap profilepic;

    // -- Firebase variables --
    private FirebaseAuth mAuthstart, mAuthfb, mAuthlogin, mAuthsignup, mAuthgoogle;
    private DatabaseReference database;
    private FirebaseAuth.AuthStateListener mAuthListener, newAccountListener;
    private FirebaseUser user;

    // -- Fb login variables --
    private CallbackManager callbackManager;
    private LoginButton fblogin_button;

    // -- Google login variables --
    private GoogleApiClient mGoogleApiClient;
    private Uri googlepic;


    // -------------  Activity LifeCycle Functions -------------------------------//


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);                 //call onCreate method of super class
        //FacebookSdk.sdkInitialize(getApplicationContext()); //this line has to come before setting the view
        setContentView(R.layout.activity_login);            //setup the content view for the loginActivity
        Firebase.setAndroidContext(this);                   //set Firebase context


        // Configure facebook login material
        callbackManager = CallbackManager.Factory.create();                //callbackManager for facebook login
        fblogin_button = (LoginButton) findViewById(R.id.fblogin_button);  //find facebook's login button
        fblogin_button.setVisibility(View.GONE);
        ImageView fblogin = (ImageView) findViewById(R.id.fblogin);        //load the fblogin button image
        fblogin.setOnClickListener(this);                                  //set onClick listener on it
        fblogin_button.setReadPermissions(Arrays.asList("public_profile", "email")); //setup facebook permissions
        //Add the callback manager to facebook's login button
        fblogin_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //on successfull login we pass fb access token to this function
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException exception) {

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
        SignInButton signInButton = (SignInButton) findViewById(R.id.google_login);
        setGooglePlusButtonText(signInButton);                                 //modify the text on google sign in button
        signInButton.setVisibility(View.GONE);
        ImageView googlelogin = (ImageView) findViewById(R.id.googlelogin);
        googlelogin.setOnClickListener(this);


        // Setup email login
        //username = (EditText) findViewById(R.id.username);                    //find username textbox
        //username.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);       //turn off its auto correct
        //password = (EditText) findViewById(R.id.password);                    //find password textbox






        //Setup initial state of progress bar
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        //ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        //progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        //Async some initialization tasks
        new ActivityStartBackground().execute();

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
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Toast.makeText(this, "Connectivity problems!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            // do something on back.
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    //------------------- Utility Functions ----------------------------------//


    private class ActivityStartBackground extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... UID) {
            mAuthstart = FirebaseAuth.getInstance();            //get current state of login
            mAuthfb = FirebaseAuth.getInstance();            //get Firebase Instances
            mAuthgoogle = FirebaseAuth.getInstance();
            mAuthsignup = FirebaseAuth.getInstance();
            mAuthlogin = FirebaseAuth.getInstance();



            //Create another listener for signups
            newAccountListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        //account created, add user to firebase
                        new CheckExistanceBackground().execute(user.getUid());    //Check if this account already exists
                    } else {
                        // account creation unsuccessful
                    }
                }
            };

            //logout current user on receiving a signal from CheckInActivity
            Intent intent = getIntent();
            if (intent.getExtras() != null && intent.getStringExtra(logoutFlagString) != null) {
                logoutFlag = intent.getStringExtra(logoutFlagString);
                if (logoutFlag.equals("1")) {
                    LoginManager.getInstance().logOut();  //logout Facebook
                    FirebaseAuth.getInstance().signOut(); //logout Firebase
                }
                logoutFlag = "0";
            }

            return "";
        }

        @Override
        protected void onPreExecute(){
            showLoading();             //show the progress circle
        }

        @Override
        protected void onPostExecute(String result) {
            new FirstAuthBackground().execute();      //check if user is logged in
        }
    }

    private class CheckExistanceBackground extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... UID) {
            userid = UID[0];
            database = FirebaseDatabase.getInstance().getReference();  //check User Information database to see if the user exists
            database.child("UserInformation").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        addNewUser(userid);       //user is new, add him to database
                    }
                    else{
                        new CheckStatusBackground().execute(userid); //if not,see if there is active checkIn
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected void onPostExecute(String result) {

        }
    }



    private class CheckStatusBackground extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... UID) {
            userid = UID[0];
            database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference

            final ValueEventListener valuelistener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if(count==0) {
                        if (dataSnapshot.exists()) {
                            isCheckedIn=true;
                            mAuthstart.removeAuthStateListener(mAuthListener);
                            mAuthfb.removeAuthStateListener(newAccountListener);
                            mAuthlogin.removeAuthStateListener(mAuthListener);
                            mAuthsignup.removeAuthStateListener(newAccountListener);
                            mAuthgoogle.removeAuthStateListener(newAccountListener);
                            Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class); //send Intent to home
                            intent.putExtra("userid", userid);
                            intent.putExtra("sendstatus",isCheckedIn);
                            intent.putExtra("startedfrom","login");
                            startActivity(intent);

                        }
                        else if(!dataSnapshot.exists()){
                            isCheckedIn=false;
                            mAuthfb.removeAuthStateListener(newAccountListener);
                            mAuthlogin.removeAuthStateListener(mAuthListener);
                            mAuthsignup.removeAuthStateListener(newAccountListener);
                            mAuthgoogle.removeAuthStateListener(newAccountListener);
                            Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class); //send Intent to home
                            intent.putExtra("userid", userid);
                            intent.putExtra("sendstatus",isCheckedIn);
                            intent.putExtra("startedfrom","login");
                            startActivity(intent);

                        }

                    }
                    count=count+1;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    //Do nothing

                }
            };
            database.child("CheckInUsers").child(userid).addListenerForSingleValueEvent(valuelistener); //attach listener

            return "";
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    private class FirstAuthBackground extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... UID) {
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        //if someone is already signed in, proceed to check his status
                        new CheckStatusBackground().execute(user.getUid());
                        mAuthstart.removeAuthStateListener(mAuthListener);
                    } else {
                        // there is no one signed in
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        findViewById(R.id.mainlayout).setVisibility(View.VISIBLE);
                        mAuthstart.removeAuthStateListener(mAuthListener);
                    }
                }
            };
            mAuthstart.addAuthStateListener(mAuthListener); //add the above listener to the firebaseAuth object

            return null;
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }

    //-----------This function checks if the user has an active CheckIn ---//

    //Display progress bar
    private void showLoading(){
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        findViewById(R.id.mainlayout).setVisibility(View.GONE);
        fblogin_button.setVisibility(View.GONE);
    }

    //add a new user to database
    private void addNewUser(String userID){
        database = FirebaseDatabase.getInstance().getReference();          //get a firebase key for the update
        String key = database.child("UserInformation").push().getKey();
        UserDetails user = new UserDetails(firstname,lastname,email,10,0,0,0); //make a new user object
        Map<String, Object> newUser = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/UserInformation/"+userID, newUser);
        database.updateChildren(childUpdates);

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
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        }

        Intent intent = new Intent(LoginActivity.this, TutorialActivity.class); //send Intent to Tutorial Activity
        intent.putExtra("userid", userid);
        intent.putExtra("sendstatus",isCheckedIn);
        intent.putExtra("startedfrom","login");
        startActivity(intent);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.googlelogin:
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



    /*public void showSignupDialog(View v) {
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
        //create a listener for signup process
        profilepic = BitmapFactory.decodeResource(getResources(),R.drawable.user); //default profile pic
        mAuthsignup.addAuthStateListener(newAccountListener);        //add listener
        Log.d(TAG,"email is "+user.getText().toString());
        Log.d(TAG,"password is "+pass.getText().toString());
        mAuthsignup.createUserWithEmailAndPassword(user.getText().toString(), pass.getText().toString()) //create the user
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            findViewById(R.id.mainlayout).setVisibility(View.VISIBLE);
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Unable to create the account!",
                                    Toast.LENGTH_LONG).show();
                            Log.d(TAG,"unable to create "+task.getException());
                        }
                    }
                });


    }

    //----------------------- Email Login -----------------------------------------------//

    //This function is triggered when you press the login button
    public void login(View view) {

        if(!username.getText().toString().contains("@"))
        {
            Toast.makeText(this,"Invalid email !",Toast.LENGTH_SHORT).show();     //check for validity of email id
            return;
        }
        showLoading();                                 //show loading

        mAuthlogin.addAuthStateListener(mAuthListener);//add listener to it
        mAuthlogin.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString()) //sign in with email
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            findViewById(R.id.mainlayout).setVisibility(View.VISIBLE);
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });

    }*/

    //------------------------------------Facebook Login ------------------------------------//

    //this method is called on fb's successful login
    private void handleFacebookAccessToken(AccessToken token) {

        showLoading();
        GraphRequest graphRequest   =   GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback()
        {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response)
            {
                //get user's information from facebook
                try
                {
                    email       =   object.getString("email");
                    firstname  =   object.optString("first_name");
                    lastname   =   object.optString("last_name");
                }
                catch (JSONException e)
                {
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
                                Picasso.with(getApplicationContext()).load(profilePicUrl).into(new com.squareup.picasso.Target() {
                                    @Override
                                    public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                                        profilepic = bitmap;
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
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 45);
    }


    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            firstname = account.getGivenName();
            lastname = account.getFamilyName();
            email = account.getEmail();
            googlepic = account.getPhotoUrl();


            Picasso.with(getApplicationContext()).load(googlepic).into(new com.squareup.picasso.Target() {
                @Override
                public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                    profilepic = bitmap;
                }
                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });

            firebaseAuthWithGoogle(account);

        } else {
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showLoading();
        mAuthgoogle.addAuthStateListener(newAccountListener);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuthgoogle.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            findViewById(R.id.mainlayout).setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }



}