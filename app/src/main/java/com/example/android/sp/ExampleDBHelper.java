package com.example.android.sp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ruturaj on 8/1/16.
 */
public class ExampleDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "NewCheckInInfo.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "NewCheckIns";
    public static final String rownum = "Rownum";
    public static final String ID = "_id";
    public static final String carlat = "Carlatitude";
    public static final String carlon = "Carlongitude";

    public ExampleDBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + rownum + " INTEGER PRIMARY KEY, " +
                ID + " TEXT , " + carlat + " REAL , " +
                carlon + " REAL)"
        );
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" +rownum+ " , "+ ID + " , " + carlat + "  , " + carlon + ") VALUES (1,null,null,null)");
    }

    public Cursor getInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + TABLE_NAME + " ORDER BY ROWID ASC LIMIT 1",null);
        return res;
    }


    public boolean updateInfo(String key,double carlatitude, double carlongitude) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, key);
        contentValues.put(carlat, carlatitude);
        contentValues.put(carlon, carlongitude);
        db.update(TABLE_NAME, contentValues, rownum + " = ? ", new String[] { Integer.toString(1) } );
        return true;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
