package com.app.android.sp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

/**
 * Created by ruturaj on 2/9/17.
 */
public class TutorialActivity extends AppCompatActivity {
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private String UID="";
    private boolean isCheckedin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        Intent intent = getIntent();
        UID     = intent.getStringExtra("userid"); //Receive logged in user's unique ID
        isCheckedin = intent.getExtras().getBoolean("sendstatus"); //true if user has active CheckIn

        // Assign buttons behavior
        TextView exit_btn = (TextView) findViewById(R.id.exit_btn);
        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TutorialActivity.this, HomeScreenActivity.class); //send Intent
                intent.putExtra("userid", UID);
                intent.putExtra("sendstatus",isCheckedin);
                intent.putExtra("startedfrom","login");
                startActivity(intent);
            }
        });


        //Set the pager with an adapter
        mPager = (ViewPager) findViewById(R.id.tutorial_pager);
        mPager.setOffscreenPageLimit(3); // Helps to keep fragment alive, otherwise I will have to load again images
        mPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        //Bind the title indicator to the adapter
        CirclePageIndicator titleIndicator = (CirclePageIndicator) findViewById(R.id.circle_indicator);
        titleIndicator.setViewPager(mPager);
    }
}
