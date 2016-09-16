package com.example.android.sp;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by ruturaj on 9/13/16.
 */
public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "CheckIn", "Search", "Report" };
    private android.content.Context context;
    public static String TAG="debugger";

    public SampleFragmentPagerAdapter(FragmentManager fm, android.content.Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG,"position is : "+Integer.toString(position));
        if(position==0){
            return CheckInFragment.newInstance(position+1);
        }
        if(position==1) {
            return SearchFragment.newInstance(position + 1);
        }
        else{
            return CheckInFragment.newInstance(position+1);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

}
