package com.example.android.sp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HistoryFragment extends Fragment implements View.OnClickListener {

    static String TAG="debugger",name;
    String UID="",curkey;
    ImageView image1,image2,image3,image4,image5;
    DatabaseReference database;
    ArrayList<ImageView> images = new ArrayList<ImageView>();
    ArrayList<TextView> dates = new ArrayList<TextView>();
    ArrayList<TextView> times = new ArrayList<TextView>();
    ArrayList<Button> buttons = new ArrayList<Button>();
    ArrayList<Double> lats = new ArrayList<Double>();
    ArrayList<Double> lons = new ArrayList<Double>();
    ArrayList<String> keys = new ArrayList<String>();
    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    ArrayList<android.support.v7.widget.CardView> cards = new ArrayList<android.support.v7.widget.CardView>();
    int i = 0,width,REQ_CODE=2;
    LinearLayout mv;
    CheckBox checkBox1,checkBox2,checkBox3,checkBox4,checkBox5;
    double curlatitude,curlongitude;
    ImageView curimage;
    Bitmap curbitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        UID = extras.getString("userid");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false); //inflate the view
        images.add((ImageView) view.findViewById(R.id.historyimage1));
        images.add((ImageView) view.findViewById(R.id.historyimage2));
        images.add((ImageView) view.findViewById(R.id.historyimage3));
        images.add((ImageView) view.findViewById(R.id.historyimage4));
        images.add((ImageView) view.findViewById(R.id.historyimage5));
        dates.add((TextView) view.findViewById(R.id.date1));
        dates.add((TextView) view.findViewById(R.id.date2));
        dates.add((TextView) view.findViewById(R.id.date3));
        dates.add((TextView) view.findViewById(R.id.date4));
        dates.add((TextView) view.findViewById(R.id.date5));
        times.add((TextView) view.findViewById(R.id.time1));
        times.add((TextView) view.findViewById(R.id.time2));
        times.add((TextView) view.findViewById(R.id.time3));
        times.add((TextView) view.findViewById(R.id.time4));
        times.add((TextView) view.findViewById(R.id.time5));
        buttons.add((Button) view.findViewById(R.id.hisbutton1));
        buttons.add((Button) view.findViewById(R.id.hisbutton2));
        buttons.add((Button) view.findViewById(R.id.hisbutton3));
        buttons.add((Button) view.findViewById(R.id.hisbutton4));
        buttons.add((Button) view.findViewById(R.id.hisbutton5));
        view.findViewById(R.id.hisbutton1).setOnClickListener(this);
        view.findViewById(R.id.hisbutton2).setOnClickListener(this);
        view.findViewById(R.id.hisbutton3).setOnClickListener(this);
        view.findViewById(R.id.hisbutton4).setOnClickListener(this);
        view.findViewById(R.id.hisbutton5).setOnClickListener(this);
        checkBox1 = (CheckBox)view.findViewById(R.id.hisbox1);
        checkBox2 = (CheckBox)view.findViewById(R.id.hisbox2);
        checkBox3 = (CheckBox)view.findViewById(R.id.hisbox3);
        checkBox4 = (CheckBox)view.findViewById(R.id.hisbox4);
        checkBox5 = (CheckBox)view.findViewById(R.id.hisbox5);
        view.findViewById(R.id.hisbox1).setOnClickListener(this);
        view.findViewById(R.id.hisbox2).setOnClickListener(this);
        view.findViewById(R.id.hisbox3).setOnClickListener(this);
        view.findViewById(R.id.hisbox4).setOnClickListener(this);
        view.findViewById(R.id.hisbox5).setOnClickListener(this);
        cards.add((android.support.v7.widget.CardView) view.findViewById(R.id.hiscard1));
        cards.add((android.support.v7.widget.CardView) view.findViewById(R.id.hiscard2));
        cards.add((android.support.v7.widget.CardView) view.findViewById(R.id.hiscard3));
        cards.add((android.support.v7.widget.CardView) view.findViewById(R.id.hiscard4));
        cards.add((android.support.v7.widget.CardView) view.findViewById(R.id.hiscard5));
        cards.get(0).setVisibility(View.GONE);
        cards.get(1).setVisibility(View.GONE);
        cards.get(2).setVisibility(View.GONE);
        cards.get(3).setVisibility(View.GONE);
        cards.get(4).setVisibility(View.GONE);
        mv = (LinearLayout) view.findViewById(R.id.mainview);

        getHistoryData();

        return view;
    }

    public void getHistoryData(){
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        database.child("HistoryKeys").child(UID).orderByKey().limitToLast(5).addChildEventListener(listener1);

    }

    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            width = mv.getWidth();
            Log.d(TAG,"main view width "+Integer.toString(width));
            Log.d(TAG,"download key "+dataSnapshot.getKey());
            final HistoryPlace historyPlace = dataSnapshot.getValue(HistoryPlace.class);
            final String date = historyPlace.getdate();
            final String time = historyPlace.gettime();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference islandRef = storageRef.child(UID+"/History/"+dataSnapshot.getKey()+".jpg");

            final long ONE_MEGABYTE = 1024 * 1024;
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Log.d(TAG, "download success");
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap cropped = Bitmap.createBitmap(bmp, (int)(bmp.getWidth()/2 - width/2),(int)(bmp.getHeight()/2 - width/4),width,(int)width/2);
                    if(i<5) {
                        images.get(4-i).setImageBitmap(cropped);
                        dates.get(4-i).setText(date);
                        times.get(4-i).setText(time);
                        lats.add(historyPlace.getplatitude());
                        lons.add(historyPlace.getplongitude());
                        keys.add(dataSnapshot.getKey());
                        bitmaps.add(bmp);
                        cards.get(4-i).setVisibility(View.VISIBLE);
                    }
                    i=i+1;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.d(TAG, "download failed");
                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {                 //currently all these functions have been left empty

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    public void onClick(View v) {

        Log.d(TAG,"clicked it");
        if(v.getId()==R.id.hisbutton1) {
            Bundle data = new Bundle();
            data.putDouble("latitude", lats.get(4));
            data.putDouble("longitude", lons.get(4));
            NavutilityFragment navutilityFragment = new NavutilityFragment();
            navutilityFragment.setArguments(data);
            android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, navutilityFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            return;
        }
        if(v.getId()==R.id.hisbutton2) {
            Bundle data = new Bundle();
            data.putDouble("latitude", lats.get(3));
            data.putDouble("longitude", lons.get(3));
            NavutilityFragment navutilityFragment = new NavutilityFragment();
            navutilityFragment.setArguments(data);
            android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, navutilityFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            return;
        }
        if(v.getId()==R.id.hisbutton3) {
            Bundle data = new Bundle();
            data.putDouble("latitude", lats.get(2));
            data.putDouble("longitude", lons.get(2));
            NavutilityFragment navutilityFragment = new NavutilityFragment();
            navutilityFragment.setArguments(data);
            android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, navutilityFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            return;
        }
        if(v.getId()==R.id.hisbutton4) {
            Bundle data = new Bundle();
            data.putDouble("latitude", lats.get(1));
            data.putDouble("longitude", lons.get(1));
            NavutilityFragment navutilityFragment = new NavutilityFragment();
            navutilityFragment.setArguments(data);
            android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, navutilityFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            return;
        }
        if(v.getId()==R.id.hisbutton5) {
            Bundle data = new Bundle();
            data.putDouble("latitude", lats.get(0));
            data.putDouble("longitude", lons.get(0));
            NavutilityFragment navutilityFragment = new NavutilityFragment();
            navutilityFragment.setArguments(data);
            android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, navutilityFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            return;
        }
        if(v.getId()==R.id.hisbox1){
            Log.d(TAG,"hisbox was checked");
            if(checkBox1.isChecked()) {
                curlatitude = lats.get(4);
                curlongitude = lons.get(4);
                curbitmap = bitmaps.get(4);
                curkey = keys.get(4);
                showNamesDialog();
            }
        }
        if(v.getId()==R.id.hisbox2){
            Log.d(TAG,"hisbox was checked");
            if(checkBox2.isChecked()) {
                curlatitude = lats.get(3);
                curlongitude = lons.get(3);
                curbitmap = bitmaps.get(3);
                curkey = keys.get(3);
                showNamesDialog();
            }
        }
        if(v.getId()==R.id.hisbox3){
            Log.d(TAG,"hisbox was checked");
            if(checkBox3.isChecked()) {
                curlatitude = lats.get(2);
                curlongitude = lons.get(2);
                curbitmap = bitmaps.get(2);
                curkey = keys.get(2);
                showNamesDialog();
            }
        }
        if(v.getId()==R.id.hisbox4){
            Log.d(TAG,"hisbox was checked");
            if(checkBox4.isChecked()) {
                curlatitude = lats.get(1);
                curlongitude = lons.get(1);
                curbitmap = bitmaps.get(1);
                curkey = keys.get(1);
                showNamesDialog();
            }
        }
        if(v.getId()==R.id.hisbox5){
            Log.d(TAG,"hisbox was checked");
            if(checkBox5.isChecked()) {
                curlatitude = lats.get(0);
                curlongitude = lons.get(0);
                curbitmap = bitmaps.get(0);
                curkey = keys.get(0);
                showNamesDialog();
            }
        }


    }

    public void showNamesDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new NamesDialog();
        dialog.setTargetFragment(HistoryFragment.this, REQ_CODE);       //set target fragment to this fragment
        dialog.show(this.getActivity().getSupportFragmentManager(),"Names fragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fetch information from the dialog and call the checkIn function
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"resultcode "+Integer.toString(resultCode));
        if(resultCode==-1){
            Bundle bundle = data.getExtras();
            String spotname = bundle.getString("spotname", name);

            FavoritePlace favoritePlace = new FavoritePlace(curlatitude,curlongitude,spotname);
            Map<String, Object> favoriteMap = favoritePlace.toMap();
            Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
            childUpdates.put("/FavoriteKeys/"+UID+"/"+curkey, favoriteMap);
            database.updateChildren(childUpdates);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            curbitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] datum = baos.toByteArray();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference favoriteRef = storageRef.child(UID+"/Favorites/"+curkey+".jpg");

            UploadTask uploadTask = favoriteRef.putBytes(datum);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG,"image upload failed");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d(TAG,"image upload success");
                }
            });

            Toast.makeText(this.getContext(),"Added this spot to Favorites",Toast.LENGTH_SHORT).show();

        }


    }




}
