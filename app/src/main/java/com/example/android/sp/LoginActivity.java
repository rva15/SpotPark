package com.example.android.sp;
import com.example.android.sp.ExampleDBHelper;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    ExampleDBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        dbHelper = new ExampleDBHelper(this);


    }

    public void login(View view) {

        final Cursor cursor = dbHelper.getLoginCreds(0);
        cursor.moveToFirst();
        String usrname = cursor.getString(cursor.getColumnIndex(ExampleDBHelper.COLUMN_UName));
        String pswd = cursor.getString(cursor.getColumnIndex(ExampleDBHelper.COLUMN_Pswd));
        if((username.getText().toString().equals(usrname))&&(password.getText().toString().equals(pswd))){
        //if(true){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else{

            Toast.makeText(LoginActivity.this, username.getText().toString() , Toast.LENGTH_SHORT).show();
        }


    }

    public void signup(View view) {
        dbHelper.addAccount(username.getText().toString(),password.getText().toString());

    }



}


