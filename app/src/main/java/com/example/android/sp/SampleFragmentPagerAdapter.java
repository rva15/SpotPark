package com.example.android.sp;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;

/**
 * Created by ruturaj on 9/13/16.
 */
public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    private String tabTitles1[] = new String[] { "CheckIn", "Search", "Report" };
    private String tabTitles2[] = new String[] { "Locate Car", "Search", "Report" };
    private android.content.Context context;
    public static String TAG="debugger";
    public String UID="";
    public boolean isCheckedin;

    public SampleFragmentPagerAdapter(FragmentManager fm, android.content.Context context,String id,boolean status) {
        super(fm);
        this.context = context;
        this.UID = id;
        this.isCheckedin=status;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG,"position is : "+Integer.toString(position));
        if(position==0){
            if(isCheckedin){
                Log.d(TAG,"going to navigation");
                return NavigationFragment.newInstance(position+1,UID);
            }
            else {
                Log.d(TAG,"going to checkin");
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
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        if(isCheckedin) {
            return tabTitles2[position];
        }
        else{
            return tabTitles1[position];
        }
    }

    private int[] imageResId = {
            R.drawable.checkin,
            R.drawable.search,
            R.drawable.report
    };



}
