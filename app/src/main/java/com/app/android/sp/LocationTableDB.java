package com.app.android.sp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.app.android.sp.SearchHelperDB.rownum;

/**
 * Created by ruturaj on 7/9/17.
 */

public class LocationTableDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LocationInfo.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "LocationTable";
    private static final String latitude = "Latitude";
    private static final String longitude = "Longitude";
    private static final String millis = "Millis";
    private String TAG = "ActivityRecognition";


    public LocationTableDB(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //Drop previously existing table and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + millis + " INTEGER PRIMARY KEY, " +
                latitude + " REAL , " +
                longitude + " REAL)"
        );

    }

    public Cursor getInfo(int number) {
        //Return a particular row
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + TABLE_NAME + " WHERE Rownum = "+Integer.toString(number),null);
        return res;
    }

    public boolean updateInfo(double lat, double lon,double millisc) {
        //Update last hour and last min entries
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(latitude, lat);
        contentValues.put(longitude, lon);
        contentValues.put(millis,millisc);
        db.insert(TABLE_NAME, null, contentValues );
        return true;
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
        Log.d(TAG,"loc deleting");
        String deleteQuery = "DELETE FROM LocationTable WHERE Millis NOT IN ( SELECT Millis FROM LocationTable ORDER BY Millis DESC LIMIT 3)";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(deleteQuery,null);
        Log.d(TAG,"loc delete millis "+ DatabaseUtils.dumpCursorToString(cursor));
    }

    public double getMin(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MIN(Millis) FROM LocationTable", null);
        double millis = 0;
        if(cursor!=null) {
            cursor.moveToFirst();
            Log.d(TAG,"loc min millis "+cursor.getDouble(cursor.getColumnIndex("MIN(Millis)")));
        }
        return millis;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //updating actually drops the whole table and creates a new one
        onCreate(db);
    }


}
