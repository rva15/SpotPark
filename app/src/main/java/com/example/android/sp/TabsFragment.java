package com.example.android.sp;
// All imports
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruturaj on 9/17/16.
 */
public class TabsFragment extends Fragment {

    // Variable Declarations
    private View view;
    private Adapter adapter;
    private static String TAG = "debugger";
    private static String UID;
    private static boolean isCheckedin;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    //-------------------------Fragment LifeCycle Methods--------------------------//

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();                 //get userid and active CheckIn status
        UID = extras.getString("userid");
        isCheckedin = extras.getBoolean("isCheckedin");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tabslayout, container, false);  //inflate layout
        super.onCreate(savedInstanceState);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        if (!isCheckedin) {
            tabLayout.getTabAt(0).setIcon(R.drawable.checkin);      //set the first tab as per CheckIn status
        }
        if (isCheckedin) {
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

    //-----------------------------View Pager Functions----------------------------//

    public void setupViewPager(ViewPager viewPager) {
        ///Here we have to pass ChildFragmentManager instead of FragmentManager.
        adapter = new Adapter(getChildFragmentManager());
        if (!isCheckedin) {
            adapter.addFragment(new CheckInFragment(), "CHECK-IN");
        }
        if (isCheckedin) {
            adapter.addFragment(new CarlocationFragment(), "LOCATE CAR");
        }
        adapter.addFragment(new SearchFragment(), "SEARCH");
        adapter.addFragment(new ReportFragment(), "REPORT");
        viewPager.setAdapter(adapter);
    }


    //----------------------FragmentStatePagerAdapter Class----------------------//


    static class Adapter extends FragmentStatePagerAdapter {
        public final List<Fragment> mFragments = new ArrayList<>();
        public final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (isCheckedin) {
                    return CarlocationFragment.newInstance(position + 1, UID);
                } else {
                    return CheckInFragment.newInstance(position + 1, UID);
                }
            }
            if (position == 1) {
                return SearchFragment.newInstance(position + 1, UID);
            } else {
                return ReportFragment.newInstance(position + 1, UID);
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
