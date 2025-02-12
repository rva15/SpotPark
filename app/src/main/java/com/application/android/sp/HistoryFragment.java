package com.application.android.sp;
//All imports
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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


public class HistoryFragment extends Fragment  {

    //Variable Declarations
    private static String TAG="debugger";
    private String UID="",curkey,name;
    private DatabaseReference database;
    private ArrayList<String> keys = new ArrayList<String>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private ArrayList<HistoryPlace> historyPlaces = new ArrayList<HistoryPlace>();
    private int i = 0,width,REQ_CODE=2,max,count=0;
    private LinearLayout mv;
    private RecyclerView recList;
    private HistoryAdapter historyAdapter;
    private View view;
    private TextView fetchinghistory;
    private ProgressBar progressBar;

    //----Fragment Lifecycle Functions----------//

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle extras = getArguments();
        UID = extras.getString("userid");   //get the user id

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_history, container, false); //inflate the view
        recList = (RecyclerView) view.findViewById(R.id.hisfraglist);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());     //inflate the recycler view
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        mv = (LinearLayout) view.findViewById(R.id.hisfragmv);
        fetchinghistory = (TextView) view.findViewById(R.id.fetchinghistory);
        progressBar = (ProgressBar) view.findViewById(R.id.his_progressbar);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(),R.color.newuiorange), PorterDuff.Mode.MULTIPLY);
        getHistoryData();                                                         //get user's history


        return view;
    }

    @Override
    public void onStop() {
        i=0;                            //set counter for back to 0
        super.onStop();
    }


    //------------Helper Functions----------------------//

    private void getHistoryData(){

        fetchinghistory.setVisibility(View.VISIBLE);
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        database.child("HistoryKeys").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                max = (int) dataSnapshot.getChildrenCount();
                if(max==0){
                    showdefault();
                }

                if(max>10){                                         //if number of user's checkins >10, fetch only 10
                    max = 10;
                }
                database.child("HistoryKeys").child(UID).orderByKey().limitToLast(10).addChildEventListener(listener1);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //define the ChildEventListener
    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            if(count<(max+1)) {
                width = mv.getWidth();
                final HistoryPlace historyPlace = dataSnapshot.getValue(HistoryPlace.class);
                if(historyPlace!=null && dataSnapshot.getKey()!=null) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
                    StorageReference islandRef = storageRef.child(UID + "/History/" + dataSnapshot.getKey() + ".jpg");

                    if(islandRef!=null) {
                        final long ONE_MEGABYTE = 1024 * 1024;
                        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if(bmp==null){ //if map image unavailable, display default image
                                    bmp = BitmapFactory.decodeResource(getResources(),
                                            R.drawable.mapnotav);
                                }
                                Bitmap cropped = getCroppedMap(bmp);
                                bitmaps.add(cropped);
                                historyPlaces.add(historyPlace);
                                keys.add(dataSnapshot.getKey());
                                i = i + 1;
                                if (i == max) {
                                    progressBar.setVisibility(View.GONE);
                                    fetchinghistory.setVisibility(View.GONE);
                                    historyAdapter = new HistoryAdapter(historyPlaces, keys, bitmaps, getActivity(), HistoryFragment.this, recList, UID, getContext(), max);
                                    recList.setAdapter(historyAdapter);   //set the adapter
                                    database.child("HistoryKeys").child(UID).orderByKey().limitToLast(10).removeEventListener(listener1);
                                }


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                max = max -1;  //dont display this in history
                            }
                        });
                    }
                }
            }
            count=count+1;
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

    private Bitmap getCroppedMap(Bitmap b){

        int p1=1,p2=1,p3=1,p4=1;
        boolean bwidthlarge = true,bheightlarge=true;
        int bwidth = b.getWidth();
        int bheight = b.getHeight();
        if(bwidth>=width){
            bwidthlarge = true;
            p3 = width;
        }
        else{
            bwidthlarge = false;
            p3 = b.getWidth();
        }

        if(bheight>=(width/2)){
            bheightlarge = true;
            p4 = (width)/2;
        }
        else{
            bheightlarge = false;
            p4 = b.getHeight();
        }

        if(bwidthlarge){
            p1 = (bwidth/2) - (width/2);
        }
        else{
            p1 = 0;
        }

        if(bheightlarge){
            p2 = (bheight/2) - (width/4);
        }
        else{
            p2 = 0;
        }

        Bitmap cropped = Bitmap.createBitmap(b, p1, p2, p3, p4);

        return cropped;
    }

    // Names dialog gives its results back to this function
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fetch information from the dialog and call the checkIn function
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showdefault(){
        TextView message = (TextView)view.findViewById(R.id.newuserhistory);
        fetchinghistory.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        message.setVisibility(View.VISIBLE);
    }



}
