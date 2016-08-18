package com.example.android.sp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

public class OptionsActivity extends AppCompatActivity {

    String UID="";
    public final static String ID="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Intent intent1 = getIntent();           //Receive intent from Login Activity
        UID     = intent1.getStringExtra(FacebookLogin.UID);


    }

    public void search(View view){
        //Go to search Activity
        Intent intent = new Intent(this, SearchActivity.class); //send Intent
        intent.putExtra(ID,UID);
        startActivity(intent);
        this.finish();
    }

    public void checkin(View view){
        //Go to Main Activity
        Intent intent = new Intent(this, MainActivity.class); //send Intent
        intent.putExtra(ID,UID);
        startActivity(intent);
        this.finish();
    }

    public void report(View view){
        //Go to report Activity
    }
}
