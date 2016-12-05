package com.example.android.sp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContRepFragment extends Fragment {

    static String UID="";
    public static final String ARG_PAGE = "ARG_PAGE";
    RecyclerView recList;
    LinearLayout mv;
    static private ArrayList<Bitmap> crimage;
    static private ArrayList<String> crkey;
    static private ArrayList<String> crdes;
    static private ArrayList<String> crcode;
    static private ArrayList<ReportedTimes> crtimes;
    DatabaseReference database;
    static String TAG="debugger";
    int max,width,i=0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

       // Bundle extras = getArguments();
       // UID = extras.getString("userid");
       // Log.d(TAG,"cr uid "+UID);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        crimage = new ArrayList<>();
        crkey = new ArrayList<>();
        crdes = new ArrayList<>();
        crcode = new ArrayList<>();
        crtimes = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_contrep, container, false); //inflate the view
        recList = (RecyclerView) view.findViewById(R.id.contcardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        getcontrepdata();
        mv = (LinearLayout) view.findViewById(R.id.contmv);
        return view;
    }

    public static ContRepFragment newInstance(int page,String id) {
        UID = id;
        Log.d(TAG," id passed :"+UID);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ContRepFragment fragment = new ContRepFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public void getcontrepdata(){
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        database.child("ReportedTimes").child(UID).addValueEventListener(listener2);
        Log.d(TAG,"get contrep");
    }


    ValueEventListener listener2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG,"children count is "+dataSnapshot.getChildrenCount());
            max = (int) dataSnapshot.getChildrenCount();
            database.child("ReportedTimes").child(UID).orderByKey().addChildEventListener(listener1);
            database.child("ReportedTimes").child(UID).removeEventListener(listener2);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };



    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            width = mv.getWidth();
            Log.d(TAG,"main view width "+Integer.toString(width));
            Log.d(TAG,"download key "+dataSnapshot.getKey());
            final ReportedTimes reportedTimes = dataSnapshot.getValue(ReportedTimes.class);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference islandRef = storageRef.child(UID+"/Reported/"+dataSnapshot.getKey()+".jpg");

            final long ONE_MEGABYTE = 1024 * 1024;
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Log.d(TAG, "download success "+Integer.toString(i));
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap cropped = Bitmap.createBitmap(bmp, (int)(bmp.getWidth()/2 - width/2),(int)(bmp.getHeight()/2 - width/4),width,(int)width/2);
                    crimage.add(cropped);
                    crkey.add(dataSnapshot.getKey());
                    crdes.add(reportedTimes.getdescription());
                    crcode.add(reportedTimes.getlatlngcode());
                    crtimes.add(reportedTimes);
                    i=i+1;
                    if(i==max){
                        CRAdapter ca = new CRAdapter(crimage,crtimes,crkey,getActivity(),recList,UID);
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
