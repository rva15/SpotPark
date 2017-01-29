package com.example.android.sp;
//All imports
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.view.ViewPager.LayoutParams;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruturaj on 11/29/16.
 */
public class ContributionsFragment extends Fragment {

    //Variable Declarations
    private static String TAG = "debugger";
    private static String UID;
    private ViewPager contviewPager;
    private TabLayout conttabLayout;
    private View view;
    private Adapter adapter;
    private LinearLayout pointsbar;
    private int pbwidth,points=0,max=0,count=0;
    private DatabaseReference database;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contributions, container, false);
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        UID = extras.getString("userid");
        pbwidth = extras.getInt("width");
        contviewPager = (ViewPager) view.findViewById(R.id.contviewpager);
        setupViewPager(contviewPager);
        conttabLayout = (TabLayout) view.findViewById(R.id.conttabs);
        conttabLayout.setupWithViewPager(contviewPager);
        pointsbar = (LinearLayout)view.findViewById(R.id.pointsbar);
        database = FirebaseDatabase.getInstance().getReference();
        database.child("ReportedTimes").child(UID).addValueEventListener(listener2);
        return view;
    }

    ValueEventListener listener2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            max = (int) dataSnapshot.getChildrenCount();
            if(max==0){
                database.child("UserInformation").child(UID).orderByKey().addListenerForSingleValueEvent(listener3);
            }
            else {
                database.child("ReportedTimes").child(UID).orderByKey().addChildEventListener(listener1);
            }
            database.child("ReportedTimes").child(UID).removeEventListener(listener2);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ChildEventListener listener1 = new ChildEventListener() {
        @Override
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            count = count+1;
            ReportedTimes reportedTimes = dataSnapshot.getValue(ReportedTimes.class);
            if(reportedTimes.getverification()>=2){
                points = points+5;
            }
            else{
                points = points+2;
            }
            if(count==max) {
                database.child("UserInformation").child(UID).orderByKey().addListenerForSingleValueEvent(listener3);
                database.child("ReportedTimes").child(UID).orderByKey().removeEventListener(listener1);
            }
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

    ValueEventListener listener3 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
            points = points + userDetails.getcheckinfeed();
            points = points + 2*userDetails.getreportfeed();
            setupPointsBar();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };



    public void setupPointsBar(){
        int px = dpToPx(16);

        int green = (int)Math.floor(points/10);

        for(int i=1;i<(green+1);i++){
            String name = "pb"+Integer.toString(i);
            int resID = getResources().getIdentifier(name, "id", getActivity().getPackageName());
            TextView tv = (TextView)view.findViewById(resID);
            tv.setBackground(getResources().getDrawable(R.drawable.tvbordergreen,null));
        }

        for(int j=green+3;j<12;j++){
            String name = "pb"+Integer.toString(j);
            int resID = getResources().getIdentifier(name, "id", getActivity().getPackageName());
            TextView tv = (TextView)view.findViewById(resID);
            tv.setBackground(getResources().getDrawable(R.drawable.tvborderwhite,null));
        }

        int remainder = points%10;
        float w1 = (float)remainder/10;
        float w2 = 1-w1;

        String name = "pb"+Integer.toString(green+1);
        int resID = getResources().getIdentifier(name, "id", getActivity().getPackageName());
        TextView tv = (TextView)view.findViewById(resID);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                0,
                px, w1);
        tv.setLayoutParams(param);
        tv.setBackground(getResources().getDrawable(R.drawable.tvbordergreen,null));

        name = "pb"+Integer.toString(green+2);
        resID = getResources().getIdentifier(name, "id", getActivity().getPackageName());
        tv = (TextView)view.findViewById(resID);
        param = new LinearLayout.LayoutParams(
                0,
                px, w2);
        tv.setLayoutParams(param);
        tv.setBackground(getResources().getDrawable(R.drawable.tvborderwhite,null));

        tv = (TextView)view.findViewById(R.id.currentpoints);
        tv.setText(Integer.toString(points));
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        param = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                px);
        int leftmargin = dpToPx(green*16)+dpToPx(Math.round((2+w1)*16));
        param.setMargins(leftmargin,0,0,dpToPx(8));
        tv.setLayoutParams(param);

        tv = (TextView)view.findViewById(R.id.fifty);
        param = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                px);
        param.setMargins(Math.round(pbwidth/2)-dpToPx(40),0,0,0);
        tv.setLayoutParams(param);

        tv = (TextView)view.findViewById(R.id.hundred);
        param = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                px);
        param.setMargins(Math.round(pbwidth/2)-dpToPx(56),0,0,0);
        tv.setLayoutParams(param);

    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setupViewPager(ViewPager viewPager) {
        ///Here we have to pass ChildFragmentManager instead of FragmentManager.
        adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new ContRepFragment(), "Reported Spots");
        adapter.addFragment(new ContRevFragment(), "Parking Feedbacks");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentStatePagerAdapter {
        private FragmentManager mManager;
        public final List<Fragment> mFragments = new ArrayList<>();
        public final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            this.mManager = fm;
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);

        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                    return ContRepFragment.newInstance(position + 1, UID);
                }
            if (position == 1) {
                return ContRevFragment.newInstance(position + 1, UID);
            }
            else{
                return null;
            }
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


    }


}
