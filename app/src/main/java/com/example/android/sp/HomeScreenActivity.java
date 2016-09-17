package com.example.android.sp;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

public class HomeScreenActivity extends AppCompatActivity {

    String UID="";
    Boolean isCheckedin;
    public String TAG="debugger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent1 = getIntent();           //Receive intent
        UID     = intent1.getStringExtra(LoginActivity.UID); //Receive logged in user's unique ID
        isCheckedin = intent1.getExtras().getBoolean("sendstatus");
        if(!isCheckedin){
            Log.d(TAG,"not checked in");
        }
        if(isCheckedin){
            Log.d(TAG,"checked in");
        }
        setContentView(R.layout.activity_home_screen);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(),
                HomeScreenActivity.this,UID,isCheckedin));



        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.checkin);
        tabLayout.getTabAt(1).setIcon(R.drawable.search);
        tabLayout.getTabAt(2).setIcon(R.drawable.report);
    }
}
