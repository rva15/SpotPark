package com.example.android.sp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ruturaj on 9/2/16.
 */
public class SearchHelperDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "SearchHelp.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "SearchHelper";
    public static final String rownum = "Rownum";
    public static final String key = "key";
    public static final String time1 = "Time";
    public static final String status = "Status";




    public SearchHelperDB(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + rownum + " INTEGER PRIMARY KEY, " +
                key + " TEXT , " + time1 + " INTEGER , " + status + " INTEGER)"

        );
        
    }

    public Cursor getInfo(String keys) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+key+"=?",new String[]{keys});
        return res;
    }

    public void insertEntry(String keys,int times1,int state){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(key,keys);
        contentValues.put(time1,times1);
        contentValues.put(status,state);

        db.insert(TABLE_NAME,null,contentValues);
        close();
    }


    public void updateStatus(String keys) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] filter = new String[]{keys};
        ContentValues cv = new ContentValues();
        cv.put(status,1);
        db.update(TABLE_NAME,cv,"key=?",filter);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
