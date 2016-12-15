package com.example.android.sp;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v4.app.Fragment;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by ruturaj on 12/9/16.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    static String TAG = "debugger";
    static String UID;
    View view;
    TextView fullname, email;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    int logintype = 0;
    ImageView loginicon,editprofile;
    Button changepswd;
    int REQ_CODE=3;
    String currentemail;
    FirebaseUser user;
    String fn,ln;
    private DatabaseReference database;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        UID = extras.getString("userid");
        Log.d(TAG, "cr uid " + UID);
        database = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();
                Log.d(TAG, "provider id " + providerId);
                if (providerId.equals("password")) {
                    logintype = 1;
                }
                if (providerId.equals("facebook.com")) {
                    logintype = 2;
                }
                if (providerId.equals("google.com")) {
                    logintype = 3;
                }
            }
            ;
        }


        fullname = (TextView) view.findViewById(R.id.fullname);
        email = (TextView) view.findViewById(R.id.email);
        loginicon = (ImageView) view.findViewById(R.id.logintype);
        changepswd = (Button) view.findViewById(R.id.changepass);
        editprofile = (ImageView)view.findViewById(R.id.editprofile);
        changepswd.setOnClickListener(this);
        editprofile.setOnClickListener(this);
        database.child("UserInformation").child(UID).addValueEventListener(listener1);
        return view;
    }

    ValueEventListener listener1 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "children count is " + dataSnapshot.getChildrenCount());
            UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
            fn = userDetails.getfirstname();
            ln = userDetails.getlastname();
            fullname.setText(userDetails.getfirstname() + " " + userDetails.getlastname());
            currentemail = userDetails.getemail();
            email.setText(userDetails.getemail());
            if (logintype == 1) {
                loginicon.setImageResource(R.drawable.email);
                changepswd.setVisibility(View.VISIBLE);
            }
            if (logintype == 2) {
                loginicon.setImageResource(R.drawable.facebook);
                changepswd.setVisibility(View.GONE);
            }
            if (logintype == 3) {
                loginicon.setImageResource(R.drawable.google);
                changepswd.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onClick(View v) {

        Log.d(TAG, "clicked it");
        if (v.getId() == R.id.changepass) {
            showPswdDialog();
        }
        if (v.getId() == R.id.editprofile){
            showProfileDialog();
        }
    }

    public void showProfileDialog(){
        DialogFragment dialogFragment = new EditProfileDialog();
        Bundle args = new Bundle();
        args.putString("fn", fn);
        args.putString("ln", ln);
        args.putString("email", email.getText().toString());
        dialogFragment.setArguments(args);
        dialogFragment.setTargetFragment(SettingsFragment.this, REQ_CODE);       //set target fragment to this fragment
        dialogFragment.show(this.getActivity().getSupportFragmentManager(),"EditProfile fragment");
    }

    public void showPswdDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ChangePswdDialog();
        dialog.setTargetFragment(SettingsFragment.this, REQ_CODE);       //set target fragment to this fragment
        dialog.show(this.getActivity().getSupportFragmentManager(),"ChangePswd fragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fetch information from the dialog and call the checkIn function
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "resultcode " + Integer.toString(resultCode));
        if (resultCode == -1) {
            Bundle bundle = data.getExtras();
            if (bundle.getString("oldpswd") != null) {
                String old = bundle.getString("oldpswd");
                final String new1 = bundle.getString("newpswd1");
                String new2 = bundle.getString("newpswd2");
                if (!new1.equals(new2)) {
                    Log.d(TAG, "dont match");
                    Toast.makeText(getContext(), "Passwords dont match!", Toast.LENGTH_SHORT).show();
                    showPswdDialog();
                } else {
                    Log.d(TAG, "email " + currentemail);
                    Log.d(TAG, "password is " + old);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(currentemail, old);
                    user.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "re-authentication success");
                                    if (!new1.equals("")) {
                                        updatePassword(new1);
                                        Log.d(TAG, "updating");
                                    } else {
                                        Toast.makeText(getContext(), "new password cannot be blank", Toast.LENGTH_SHORT).show();
                                        showPswdDialog();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "re-authentication failed");
                            Toast.makeText(getContext(), "Incorrect Current Password", Toast.LENGTH_SHORT).show();
                            showPswdDialog();
                        }
                    });


                }
            }
            else{
                String fn = bundle.getString("fn");
                Log.d(TAG,"fn is "+fn);
                String ln = bundle.getString("ln");
                String email = bundle.getString("email");
                updateEmail(fn,ln,email);
            }
        }
    }

    public void updatePassword(String newpass){
        Log.d(TAG,"making new password "+newpass);
        user.updatePassword(newpass)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User password updated.");
                        }
                    }
                });
    }

    public void updateEmail(String fname,String lname,String newemail){
        database = FirebaseDatabase.getInstance().getReference();
        email.setText(newemail);
        database.child("UserInformation").child(UID).child("firstname").setValue(fname);
        database.child("UserInformation").child(UID).child("lastname").setValue(lname);
        database.child("UserInformation").child(UID).child("email").setValue(newemail);
    }

}
