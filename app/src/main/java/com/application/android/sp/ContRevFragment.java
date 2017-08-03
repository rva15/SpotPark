package com.application.android.sp;
//All imports
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by ruturaj on 11/28/16.
 */
public class ContRevFragment extends Fragment {

    //Variable Declarations
    private static String UID="";
    private static final String ARG_PAGE = "ARG_PAGE";
    private static String TAG="debugger";
    private TextView numcins,numreps,cinpoints,reppoints;
    private DatabaseReference database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ContRevFragment fragment = new ContRevFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void getUserData(){
        database = FirebaseDatabase.getInstance().getReference();
        database.child("UserInformation").child(UID).addListenerForSingleValueEvent(valueEventListener);
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


        }
    };

    public void setTableEntries(int c,int r){
        numcins.setText(Integer.toString(c));
        numreps.setText(Integer.toString(r));
        cinpoints.setText(Integer.toString(c));
        reppoints.setText(Integer.toString(r*2));
    }



}
