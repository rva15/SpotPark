package com.example.android.sp;
//All imports
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ruturaj on 8/1/16.
 */
public class CheckInHelperDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CheckInInfo.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "CheckIns";
    private static final String rownum = "Rownum";
    private static final String ID = "_id";
    private static final String carlat = "Carlatitude";
    private static final String carlon = "Carlongitude";
    private static final String cih = "CheckInHour";
    private static final String cim = "CheckInMin";
    private static final String lh = "LastHour";
    private static final String lm = "LastMin";


    public CheckInHelperDB(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Drop previously existing table and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + rownum + " INTEGER PRIMARY KEY, " +
                ID + " TEXT , " + carlat + " REAL , " +
                carlon + " REAL, "+ cih + " REAL , " +
                cim + " REAL, "+ lh+" REAL , "+lm+" REAL)"
        );
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" +rownum+ " , "+ ID + " , " + carlat + "  , " + carlon +" , " + cih + "  , " + cim +" , "+ lh+" , "+lm+ ") VALUES (1,null,null,null,null,null,null,null)");
    }

    public Cursor getInfo() {
        //Return the whole table row
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + TABLE_NAME + " ORDER BY ROWID ASC LIMIT 1",null);
        return res;
    }


    public boolean updateInfo(String key,double carlatitude, double carlongitude,double chkinhour,double chkinmin,double lsthour,double lstmin) {
        //Update last hour and last min entries
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, key);
        contentValues.put(carlat, carlatitude);
        contentValues.put(carlon, carlongitude);
        contentValues.put(cih,chkinhour);
        contentValues.put(cim,chkinmin);
        contentValues.put(lh,lsthour);
        contentValues.put(lm,lstmin);
        db.update(TABLE_NAME, contentValues, rownum + " = ? ", new String[] { Integer.toString(1) } );
        return true;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //updating actually drops the whole table and creates a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
