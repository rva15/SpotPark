package com.example.android.sp;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ruturaj on 11/28/16.
 */
public class ContRevFragment extends Fragment {
    static String UID="";
    public static final String ARG_PAGE = "ARG_PAGE";
    static String TAG="debugger";
    TextView numcins,numreps,cinpoints,reppoints;
    DatabaseReference database;

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

        View view = inflater.inflate(R.layout.fragment_contver, container, false); //inflate the view
        numcins = (TextView) view.findViewById(R.id.numcins);
        numreps = (TextView) view.findViewById(R.id.numreps);
        cinpoints = (TextView) view.findViewById(R.id.cinpoints);
        reppoints = (TextView) view.findViewById(R.id.reppoints);
        getUserData();
        return view;
    }

    public static ContRevFragment newInstance(int page,String id) {
        UID = id;
        Log.d(TAG," id passed :"+UID);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ContRevFragment fragment = new ContRevFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void getUserData(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("UserInformation").child(UID).addValueEventListener(valueEventListener);
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
            int c = userDetails.getcheckinfeed();
            int r = userDetails.getreportfeed();
            setTableEntries(c,r);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

        }
    };

    public void setTableEntries(int c,int r){
        numcins.setText(Integer.toString(c));
        numreps.setText(Integer.toString(r));
        cinpoints.setText(Integer.toString(c));
        reppoints.setText(Integer.toString(r*2));
    }



}
