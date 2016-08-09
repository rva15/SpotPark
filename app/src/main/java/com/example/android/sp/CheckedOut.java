package com.example.android.sp;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class CheckedOut extends AppCompatActivity {

    ExampleDBHelper dbHelper1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checked_out);

        dbHelper1 = new ExampleDBHelper(this);
        final Cursor cursor = dbHelper1.getNumKeys(0);
        cursor.moveToFirst();
        int number = cursor.getInt(cursor.getColumnIndex(ExampleDBHelper.COLUMN_NumKeys));
        String num = String.valueOf(number);
        Intent intent = getIntent();
        String text = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        TextView textView = new TextView(this);
        textView.setText(text);


        TextView textView1 = new TextView(this);
        textView1.setText("Number of keys : " + num);


        ViewGroup layout = (ViewGroup) findViewById(R.id.thegroup);
        layout.addView(textView);
        layout.addView(textView1);
    }
}
