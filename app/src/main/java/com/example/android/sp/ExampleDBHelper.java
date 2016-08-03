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

    public static final String DATABASE_NAME = "UserInfo.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "keys";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_UName = "uname";
    public static final String COLUMN_Pswd = "pswd";
    public static final String COLUMN_NumKeys = "numkeys";
    public static final String usrinit = "some";
    public static final String pswdinit = "some";

    public ExampleDBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER , " + COLUMN_UName + " TEXT , " + COLUMN_Pswd + " TEXT ," +
                COLUMN_NumKeys + " INTEGER)"
        );
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + " , " + COLUMN_UName + " , " + COLUMN_Pswd + " , " + COLUMN_NumKeys + ") VALUES (0,null,null,0)");
    }

    public Cursor getNumKeys(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + TABLE_NAME + " WHERE " +
                COLUMN_ID + "=?", new String[] { Integer.toString(id) } );
        return res;
    }

    public Cursor getLoginCreds(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + TABLE_NAME + " WHERE " +
                COLUMN_ID + "=?", new String[] { Integer.toString(id) } );
        return res;
    }

    public boolean updateKey(int id,int number) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID,id );
        contentValues.put(COLUMN_NumKeys, number);
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_NumKeys + " = " + COLUMN_NumKeys + " + 1" );
        return true;
    }

    public boolean addAccount(String usrname, String pswd) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_UName,usrname );
        contentValues.put(COLUMN_Pswd, pswd);
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_UName + " = \"" + usrname + "\"" );
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_Pswd + " = \"" + pswd + "\"" );
        return true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
