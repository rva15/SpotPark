package com.app.android.sp;
//All imports
import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/18/16.
 */
public class CheckInDetails {
    private int dollars=0,cents=0;
    private double longitude=0.0,latitude=0.0;
    private String id = "";
    private int minstoleave = 0;
    private String updatedate,notes;
    private int updatehour,updatemin;



    public CheckInDetails(){}


    public CheckInDetails(double latitude,double longitude, int dollars,int cents,String id,int minstoleave,String updatedate,int updatehour,int updatemin,String notes){

        this.longitude=longitude;
        this.latitude = latitude;
        this.dollars=dollars;
        this.cents=cents;
        this.id=id;
        this.minstoleave=minstoleave;
        this.updatedate = updatedate;
        this.updatehour= updatehour;
        this.updatemin=updatemin;
        this.notes = notes;

    }


    public double   getlongitude(){return this.longitude;}
    public double   getlatitude(){return this.latitude;}
    public int      getdollars(){return this.dollars;}
    public int      getcents(){return this.cents;}
    public String   getid(){return this.id;}
    public int      getminstoleave(){return this.minstoleave;}
    public String   getupdatedate(){return this.updatedate;}
    public int      getupdatehour(){return this.updatehour;}
    public int      getupdatemin(){return this.updatemin;}
    public String   getnotes(){return this.notes;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("longitude",this.longitude);
        result.put("latitude",this.latitude);
        result.put("dollars",this.dollars);
        result.put("cents",this.cents);
        result.put("id",this.id);
        result.put("minstoleave",this.minstoleave);
        result.put("updatedate",this.updatedate);
        result.put("updatehour",this.updatehour);
        result.put("updatemin",this.updatemin);
        result.put("notes",this.notes);

        return result;
    }
}
