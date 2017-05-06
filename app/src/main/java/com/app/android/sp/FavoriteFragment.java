package com.app.android.sp;
//All imports
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

/**
 * Created by ruturaj on 10/14/16.
 */
public class FavoriteFragment extends Fragment {

    //Variable Declarations
    private DatabaseReference database;
    private String UID="";
    private static String TAG="debugger";
    private int width,i=0,max;
    private LinearLayout mv;
    private ArrayList<FavoriteInfo> result;
    private RecyclerView recList;
    private View view;
    private TextView fetchingfavorites;
    private ProgressBar progressBar;


    //--------------Fragment Lifecycle Functions----------//
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        UID = extras.getString("userid");

    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorite, container, false); //inflate the view
        recList = (RecyclerView) view.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());      //inflate the recycler view
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        fetchingfavorites = (TextView) view.findViewById(R.id.fetchingfavorites);
        progressBar = (ProgressBar) view.findViewById(R.id.fav_progressbar);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(),R.color.newuiorange), PorterDuff.Mode.MULTIPLY);
        getFavoriteData();
        mv = (LinearLayout) view.findViewById(R.id.favmv);
        return view;
    }

    //------------Helper Functions---------------//

    public void getFavoriteData(){
        fetchingfavorites.setVisibility(View.VISIBLE);
        result = new ArrayList<FavoriteInfo>();
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        database.child("FavoriteKeys").child(UID).addListenerForSingleValueEvent(listener2);

    }


    ValueEventListener listener2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            max = (int) dataSnapshot.getChildrenCount();                   //get the number of favorite spots
            if(max==0){
                showdefault();
            }
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
            final FavoritePlace favoritePlace = dataSnapshot.getValue(FavoritePlace.class);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference islandRef = storageRef.child(UID+"/Favorites/"+dataSnapshot.getKey()+".jpg");

            final long ONE_MEGABYTE = 1024 * 1024;
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap cropped = getCroppedMap(bmp);
                    FavoriteInfo info = new FavoriteInfo();
                    info.name = favoritePlace.getspotname();
                    info.spotimage = cropped;
                    info.latitude = favoritePlace.getflatitude();
                    info.longitude = favoritePlace.getflongitude();
                    info.key = dataSnapshot.getKey();
                    result.add(info);
                    i=i+1;
                    if(i==max){
                        fetchingfavorites.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        FavoritesAdapter ca = new FavoritesAdapter(getresult(),getActivity(),recList,UID); //set the adapter
                        recList.setAdapter(ca);
                        database.child("FavoriteKeys").child(UID).orderByKey().removeEventListener(listener1);
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
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

    private void showdefault(){
        TextView message = (TextView)view.findViewById(R.id.newuserfavorites);
        fetchingfavorites.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        message.setVisibility(View.VISIBLE);
    }

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


}
