package com.application.android.sp;
//All imports
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


public class ContRepFragment extends Fragment {

    private static String UID="";
    private static final String ARG_PAGE = "ARG_PAGE";
    private RecyclerView recList;
    private LinearLayout mv;
    static private ArrayList<Bitmap> crimage;
    static private ArrayList<String> crkey;
    static private ArrayList<String> crdes;
    static private ArrayList<String> crcode;
    static private ArrayList<ReportedTimes> crtimes;
    private DatabaseReference database;
    private static String TAG="debugger";
    private int max,width,i=0;
    private View view;
    private TextView fetchingrep;
    private ProgressBar progressBar;

    //-----Fragment Lifecycle Functions-----------------//

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
        crimage = new ArrayList<>();       //initialize all arrays
        crkey = new ArrayList<>();
        crdes = new ArrayList<>();
        crcode = new ArrayList<>();
        crtimes = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_contrep, container, false); //inflate the view
        recList = (RecyclerView) view.findViewById(R.id.contcardList);             //setup the recycler view
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        fetchingrep = (TextView) view.findViewById(R.id.fetchingrep);
        progressBar = (ProgressBar) view.findViewById(R.id.contrep_progressbar);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(),R.color.newuiorange), PorterDuff.Mode.MULTIPLY);
        getcontrepdata();
        mv = (LinearLayout) view.findViewById(R.id.contmv);
        return view;
    }

    public static ContRepFragment newInstance(int page,String id) {
        UID = id;
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ContRepFragment fragment = new ContRepFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public void getcontrepdata(){
        fetchingrep.setVisibility(View.VISIBLE);
        database = FirebaseDatabase.getInstance().getReference();       //get the Firebase reference
        database.child("ReportedTimes").child(UID).addValueEventListener(listener2);
    }


    ValueEventListener listener2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            max = (int) dataSnapshot.getChildrenCount();
            if(max==0){
                showdefaultmessage();
            }
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
            final ReportedTimes reportedTimes = dataSnapshot.getValue(ReportedTimes.class);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://spotpark-1385.appspot.com");
            StorageReference islandRef = storageRef.child(UID+"/Reported/"+dataSnapshot.getKey()+".jpg");

            final long ONE_MEGABYTE = 1024 * 1024;
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap cropped = getCroppedMap(bmp);
                    crimage.add(cropped);
                    crkey.add(dataSnapshot.getKey());
                    crdes.add(reportedTimes.getdescription());
                    crcode.add(reportedTimes.getlatlngcode());
                    crtimes.add(reportedTimes);
                    i=i+1;
                    if(i==max){
                        fetchingrep.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        CRAdapter ca = new CRAdapter(crimage,crtimes,crkey,getActivity(),recList,UID);
                        recList.setAdapter(ca);
                        database.child("ReportedTimes").child(UID).orderByKey().removeEventListener(listener1);
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
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

    public void showdefaultmessage(){
        TextView message = (TextView) view.findViewById(R.id.newusermessage);
        fetchingrep.setVisibility(View.GONE);
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
