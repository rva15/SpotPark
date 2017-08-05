package com.application.android.sp;
//All imports
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ruturaj on 12/9/16.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    //Variable declaration
    private static String TAG = "debugger";
    private static String UID;
    private View view;
    private TextView fullname, email;
    private ImageView dp,contactemail,icons8,contactfb;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private int logintype = 0;
    private ImageView loginicon,editprofile;
    private Button changepswd;
    private int REQ_CODE=3;
    private String currentemail;
    private FirebaseUser user;
    private String fn,ln;
    private DatabaseReference database;
    private Bitmap bmp;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private LinearLayout profileinfo;
    private Switch stswitch;
    private boolean ststatus;
    private RelativeLayout gotostsettings;
    public  HomeScreenActivity homeScreenActivity;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        UID = extras.getString("userid");
        database = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //get the user's login type
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();
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

        //get his other information
        fullname = (TextView) view.findViewById(R.id.fullname);
        email = (TextView) view.findViewById(R.id.email);
        loginicon = (ImageView) view.findViewById(R.id.logintype);
        changepswd = (Button) view.findViewById(R.id.changepass);
        dp = (ImageView) view.findViewById(R.id.dp);
        dp.setOnClickListener(this);
        changepswd.setOnClickListener(this);
        profileinfo = (LinearLayout)view.findViewById(R.id.profileinfo);
        profileinfo.setOnClickListener(this);
        contactemail = (ImageView)view.findViewById(R.id.contactemail);
        contactemail.setOnClickListener(this);
        contactfb = (ImageView)view.findViewById(R.id.contactfb);
        contactfb.setOnClickListener(this);
        icons8 = (ImageView) view.findViewById(R.id.icons8);
        icons8.setOnClickListener(this);
        gotostsettings = (RelativeLayout) view.findViewById(R.id.gotostsettings);
        gotostsettings.setOnClickListener(this);
        stswitch = (Switch) view.findViewById(R.id.stswitch);
        stswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    if(!TextUtils.isEmpty(UID)) {
                        database.child("UserInformation").child(UID).child("singletouch").setValue(true);
                    }
                } else {
                    // The toggle is disabled
                    if(!TextUtils.isEmpty(UID)) {
                        database.child("UserInformation").child(UID).child("singletouch").setValue(false);
                    }
                }
            }
        });
        database.child("UserInformation").child(UID).addListenerForSingleValueEvent(listener1);
        setdp();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity){
            a=(Activity) context;
            homeScreenActivity = (HomeScreenActivity) a;
        }
    }

    private void setdp(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
        StorageReference islandRef = storageRef.child(UID+"/Profile/dp.jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Display pic downloaded
                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                dp.setImageBitmap(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

    }

    ValueEventListener listener1 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
            fn = userDetails.getfirstname();
            ln = userDetails.getlastname();
            fullname.setText(userDetails.getfirstname() + " " + userDetails.getlastname());
            currentemail = userDetails.getemail();
            ststatus = userDetails.getsingletouch();
            stswitch.setChecked(ststatus);
            email.setText(userDetails.getemail());
            //set the change password button visibility
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
        if (v.getId() == R.id.changepass) {
            showPswdDialog();
        }
        if(v.getId() == R.id.dp){
            if(homeScreenActivity!=null) {
                selectImage();
            }
            else{
                Toast.makeText(getContext(),"An error occurred. Please try again.",Toast.LENGTH_SHORT).show();
            }
        }
        if(v.getId() == R.id.profileinfo){
            if(homeScreenActivity!=null) {
                showProfileDialog();
            }
            else{
                Toast.makeText(getContext(),"An error occurred. Please try again.",Toast.LENGTH_SHORT).show();
            }
        }
        if(v.getId() == R.id.contactemail){
            sendEmail();
        }
        if(v.getId() == R.id.contactfb){
            sendFbMsg();
        }
        if(v.getId() == R.id.icons8){
            icons8();
        }
        if(v.getId() == R.id.gotostsettings){
            if(homeScreenActivity!=null) {
                homeScreenActivity.getSTSettings();
            }
            else{
                Toast.makeText(getContext(),"An error occurred. Please try again.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void icons8(){
        String url = "https://icons8.com";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void sendEmail(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"ruturaj.iitb@gmail.com"});
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendFbMsg(){
        String url = "https://www.facebook.com/thespotparkapp/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void showProfileDialog(){
        DialogFragment dialogFragment = new EditProfileDialog();
        Bundle args = new Bundle();
        args.putString("fn", fn);
        args.putString("ln", ln);
        args.putString("email", email.getText().toString());
        args.putParcelable("dp",bmp);
        dialogFragment.setArguments(args);
        dialogFragment.setTargetFragment(SettingsFragment.this, REQ_CODE);       //set target fragment to this fragment
        dialogFragment.show(this.getActivity().getSupportFragmentManager(),"EditProfile fragment");
    }

    private void showPswdDialog() {
        //Create an instance of the dialog fragment and show it
        //DialogFragment dialog = new ChangePswdDialog();
        //dialog.setTargetFragment(SettingsFragment.this, REQ_CODE);       //set target fragment to this fragment
        //dialog.show(this.getActivity().getSupportFragmentManager(),"ChangePswd fragment");
    }

    //function that allows setting of display picture
    private void selectImage() {

        final CharSequence[] items = { "Take Photo", "Choose from Library"
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add a New Display Picture!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                String userChoosenTask="";
                boolean result= checkPermission(getContext());
                if (items[item].equals("Take Photo")) {
                    userChoosenTask="Take Photo";
                    if(result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask="Choose from Library";
                    if(result)
                        galleryIntent();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),2);
    }


    //check permissions from the user and take appropriate actions
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
        StorageReference dpRef = storageRef.child(UID + "/Profile/dp.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap cropped = cropimage(bm);
        cropped.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] bytes = baos.toByteArray();
        UploadTask uploadTask = dpRef.putBytes(bytes);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                homeScreenActivity.getSettings();
                homeScreenActivity.refreshMainAdapter();
            }
        });
    }

    private Bitmap cropimage(Bitmap srcBmp){
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
        StorageReference dpRef = storageRef.child(UID + "/Profile/dp.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap cropped = cropimage(thumbnail);
        cropped.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] bytes2 = baos.toByteArray();
        UploadTask uploadTask = dpRef.putBytes(bytes2);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                homeScreenActivity.getSettings();
                homeScreenActivity.refreshMainAdapter();
            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3) {
            Bundle bundle = data.getExtras();
            if (bundle.getString("oldpswd") != null) {
                String old = bundle.getString("oldpswd");
                final String new1 = bundle.getString("newpswd1");
                String new2 = bundle.getString("newpswd2");
                if (!new1.equals(new2)) {
                    Toast.makeText(getContext(), "Passwords dont match!", Toast.LENGTH_SHORT).show();
                    showPswdDialog();
                } else {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(currentemail, old);
                    user.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (!new1.equals("")) {
                                        updatePassword(new1);
                                    } else {
                                        Toast.makeText(getContext(), "new password cannot be blank", Toast.LENGTH_SHORT).show();
                                        showPswdDialog();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Incorrect Current Password", Toast.LENGTH_SHORT).show();
                            showPswdDialog();
                        }
                    });


                }
            } else {
                String fn = bundle.getString("fn");
                String ln = bundle.getString("ln");
                String newemail = bundle.getString("email");
                fullname.setText(fn+" "+ln);
                email.setText(newemail);
                updateEmail(fn, ln, newemail);
                if(homeScreenActivity!=null) {
                    homeScreenActivity.refreshMainAdapter();
                }
            }

        }
        if(requestCode==1){
            onCaptureImageResult(data);
        }
        if(requestCode==2){
            onSelectFromGalleryResult(data);
        }


    }

    private void updatePassword(String newpass){
        user.updatePassword(newpass)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                });
    }

    private void updateEmail(String fname,String lname,String newemail){
        database = FirebaseDatabase.getInstance().getReference();
        email.setText(newemail);
        database.child("UserInformation").child(UID).child("firstname").setValue(fname);
        database.child("UserInformation").child(UID).child("lastname").setValue(lname);
        database.child("UserInformation").child(UID).child("email").setValue(newemail);
    }

}
