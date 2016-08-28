package com.example.android.sp;

//Necessary imports
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.util.Log;

public class OptionActivity2 extends AppCompatActivity {
    //Variable declaration
    String UID="";
    public final static String ID="";

    //onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option2);
        Intent intentoption2 = getIntent();           //Receive intent from Login Activity
        UID     = intentoption2.getStringExtra(LoginActivity.UID);    //get user's unique ID
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void search(View view){
        //Go to search Activity
        Intent intent = new Intent(this, SearchActivity.class); //send Intent
        intent.putExtra(ID,UID);
        startActivity(intent);
        this.finish();
    }

    public void navigate(View view){
        //Go to Main Activity
        Intent intent = new Intent(this, NavigationActivity.class); //send Intent
        intent.putExtra("user_id",UID);
        startActivity(intent);
        this.finish();
    }

    public void report(View view){
        //Go to report Activity
    }
}