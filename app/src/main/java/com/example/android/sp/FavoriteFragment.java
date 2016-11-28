package com.example.android.sp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruturaj on 10/14/16.
 */
public class FavoriteFragment extends Fragment {

    DatabaseReference database;
    String UID="";
    static String TAG="debugger";
    int width,i=0,max;
    LinearLayout mv;
    ArrayList<FavoriteInfo> result;
    RecyclerView recList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        UID = extras.getString("userid");
        List<FavoriteInfo> result = new ArrayList<FavoriteInfo>();


    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false); //inflate the view
        recList = (RecyclerView) view.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        getFavoriteData();
        mv = (LinearLayout) view.findViewById(R.id.favmv);
        return view;
    }

    public void getFavoriteData(){
        result = new ArrayList<FavoriteInfo>();
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        database.child("FavoriteKeys").child(UID).addValueEventListener(listener2);


    }



    public void getnumber(String number){
        Log.d(TAG,"number is "+number);
    }

    ValueEventListener listener2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG,"children count is "+dataSnapshot.getChildrenCount());
            max = (int) dataSnapshot.getChildrenCount();
            database.child("FavoriteKeys").child(UID).orderByKey().addChildEventListener(listener1);
            database.child("FavoriteKeys").child(UID).removeEventListener(listener2);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            width = mv.getWidth();
            Log.d(TAG,"main view width "+Integer.toString(width));
            Log.d(TAG,"download key "+dataSnapshot.getKey());
            final FavoritePlace favoritePlace = dataSnapshot.getValue(FavoritePlace.class);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference islandRef = storageRef.child(UID+"/Favorites/"+dataSnapshot.getKey()+".jpg");

            final long ONE_MEGABYTE = 1024 * 1024;
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Log.d(TAG, "download success "+Integer.toString(i));
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap cropped = Bitmap.createBitmap(bmp, (int)(bmp.getWidth()/2 - width/2),(int)(bmp.getHeight()/2 - width/4),width,(int)width/2);
                    FavoriteInfo info = new FavoriteInfo();
                    info.name = favoritePlace.spotname;
                    info.spotimage = cropped;
                    info.latitude = favoritePlace.getflatitude();
                    info.longitude = favoritePlace.getflongitude();
                    info.key = dataSnapshot.getKey();
                    result.add(info);
                    i=i+1;
                    if(i==max){
                        ContactAdapter ca = new ContactAdapter(getresult(),getActivity(),recList,UID);
                        recList.setAdapter(ca);
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.d(TAG, "download failed");
                }
            });
        }

        public ArrayList<FavoriteInfo> getresult(){
            return result;
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


}
