package com.app.android.sp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ruturaj on 7/8/17.
 */

public class ActivityTableDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ActivityInfo.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "ActivityTable";
    private static final String driving = "Driving";
    private static final String foot = "Foot";
    private static final String running = "Running";
    private static final String still = "Still";
    private static final String tilting = "LastHour";
    private static final String walking = "Walking";
    private static final String unknown = "Unknown";
    private static final String millis = "Millis";
    private String TAG = "ActivityRecognition";

    public ActivityTableDB(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Drop previously existing table and create a new one
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + millis + " INTEGER PRIMARY KEY, " +
                driving + " REAL , " + foot + " REAL , " +
                running + " REAL, "+ still + " REAL , " +
                tilting + " REAL, "+ walking +" REAL ,  "+ unknown +" REAL)"
        );

    }

    public Cursor getInfo(int number) {
        //Return a particular row
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + TABLE_NAME + " WHERE Rownum = "+Integer.toString(number),null);
        return res;
    }

    public long updateInfo(double drivingc, double footc,double runningc,double stillc,double tiltingc,double walkingc,double unknownc,double millisc) {
        //Update last hour and last min entries
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(driving, drivingc);
        contentValues.put(foot, footc);
        contentValues.put(running, runningc);
        contentValues.put(still,stillc);
        contentValues.put(tilting,tiltingc);
        contentValues.put(walking,walkingc);
        contentValues.put(unknown,unknownc);
        contentValues.put(millis,millisc);
        long rowid = db.insert(TABLE_NAME, null, contentValues );
        return rowid;
    }

    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public void deleteEntries(){
        Log.d(TAG,"act deleting");
        String deleteQuery = "DELETE FROM ActivityTable WHERE Millis NOT IN ( SELECT Millis FROM ActivityTable ORDER BY Millis DESC LIMIT 3)";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(deleteQuery,null);
        Log.d(TAG,"act delete millis "+DatabaseUtils.dumpCursorToString(cursor));
    }

    public double getMin(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MIN(Millis) FROM ActivityTable", null);
        double millis = 0;
        if(cursor!=null) {
            cursor.moveToFirst();
            Log.d(TAG,"act min millis "+cursor.getDouble(cursor.getColumnIndex("MIN(Millis)")));
        }
        return millis;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //updating actually drops the whole table and creates a new one
        //onCreate(db);
    }

}
