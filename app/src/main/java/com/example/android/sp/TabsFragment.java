package com.example.android.sp;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruturaj on 9/17/16.
 */
public class TabsFragment extends Fragment {

    View view;
    Adapter adapter;
    static String TAG = "debugger";
    static String UID;
    boolean status;
    static boolean isCheckedin;

    private void setupViewPager(ViewPager viewPager) {
        ///Here we have to pass ChildFragmentManager instead of FragmentManager.
        adapter = new Adapter(getChildFragmentManager());
        if(!isCheckedin){
            adapter.addFragment(new CheckInFragment(), "CHECKIN");
        }
        if(isCheckedin){
            adapter.addFragment(new NavigationFragment(), "LOCATE CAR");
        }
        adapter.addFragment(new SearchFragment(), "SEARCH");
        adapter.addFragment(new ReportFragment(), "REPORT");
        viewPager.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_tabslayout, container, false);
        super.onCreate(savedInstanceState);
        HomeScreenActivity activity = (HomeScreenActivity) getActivity();
        UID = activity.getUID();
        isCheckedin = activity.getStatus();
        Log.d(TAG,"user id passed :" +UID);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        if(!isCheckedin) {
            tabLayout.getTabAt(0).setIcon(R.drawable.checkin);
        }
        if(isCheckedin){
            tabLayout.getTabAt(0).setIcon(R.drawable.navigate);
        }
        tabLayout.getTabAt(1).setIcon(R.drawable.search);
        tabLayout.getTabAt(2).setIcon(R.drawable.report);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0){
                if(isCheckedin){
                    Log.d(TAG,"going to navigation");
                    return NavigationFragment.newInstance(position+1,UID);
                }
                else {
                    Log.d(TAG,"user id passed "+UID);
                    return CheckInFragment.newInstance(position + 1, UID);
                }
            }
            if(position==1) {
                return SearchFragment.newInstance(position + 1,UID);
            }
            else{
                return ReportFragment.newInstance(position+1,UID);
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
    }
    }
