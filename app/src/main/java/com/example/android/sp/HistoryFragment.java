package com.example.android.sp;
//All imports
import android.content.Intent;
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
import android.widget.Toast;
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
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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
        View view = inflater.inflate(R.layout.fragment_history, container, false); //inflate the view
        recList = (RecyclerView) view.findViewById(R.id.hisfraglist);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());     //inflate the recycler view
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        mv = (LinearLayout) view.findViewById(R.id.hisfragmv);
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
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        database.child("HistoryKeys").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                max = (int) dataSnapshot.getChildrenCount();
                Log.d(TAG,"max is "+Integer.toString(max));
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
                Log.d(TAG, "main view width " + Integer.toString(width));
                Log.d(TAG, "download key " + dataSnapshot.getKey());
                final HistoryPlace historyPlace = dataSnapshot.getValue(HistoryPlace.class);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
                StorageReference islandRef = storageRef.child(UID + "/History/" + dataSnapshot.getKey() + ".jpg");

                final long ONE_MEGABYTE = 1024 * 1024;
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "download success");
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Bitmap cropped = bmp;
                        if (bmp.getHeight() * 2 > width) {   //check this to avoid crash
                            cropped = Bitmap.createBitmap(bmp, (int) (bmp.getWidth() / 2 - width / 2), (int) (bmp.getHeight() / 2 - width / 4), width, (int) width / 2);
                        }
                        bitmaps.add(cropped);
                        historyPlaces.add(historyPlace);
                        keys.add(dataSnapshot.getKey());
                        Log.d(TAG, "max i is " + Integer.toString(i));
                        i = i + 1;
                        if (i == max) {
                            Log.d(TAG, "setting adapter");
                            historyAdapter = new HistoryAdapter(historyPlaces, keys, bitmaps, getActivity(), HistoryFragment.this,recList, UID);
                            recList.setAdapter(historyAdapter);   //set the adapter
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

    // Names dialog gives its results back to this function
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fetch information from the dialog and call the checkIn function
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_CODE){
            Bundle bundle = data.getExtras();
            String spotname = bundle.getString("spotname");
            Double latitude = bundle.getDouble("latitude");
            Double longitude = bundle.getDouble("longitude");
            Bitmap bitmap   = bundle.getParcelable("bitmap");
            String key      = bundle.getString("key");

            FavoritePlace favoritePlace = new FavoritePlace(latitude,longitude,spotname);
            Map<String, Object> favoriteMap = favoritePlace.toMap();
            Map<String, Object> childUpdates = new HashMap<>();            //put the database entries into a map
            childUpdates.put("/FavoriteKeys/"+UID+"/"+key, favoriteMap);
            childUpdates.put("/HistoryKeys/"+UID+"/"+key+"/isfavorite",1);
            database.updateChildren(childUpdates);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] datum = baos.toByteArray();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference favoriteRef = storageRef.child(UID+"/Favorites/"+key+".jpg");

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
