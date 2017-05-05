package com.app.android.sp;
//All imports
import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/25/16.
 */
public class UserDetails {
    //Variable Declaration
    private String firstname="";
    private String lastname = "";
    private String email = "";
    private int numberofkeys = 0;
    private int reportfeed =0;
    private int checkinfeed=0;
    private int complaints = 0;
    private boolean singletouch = true;

    public UserDetails(){}

    public UserDetails(String firstname,String lastname, String email, int numberofkeys, int reportfeed,int checkinfeed,int complaints,boolean singletouch){
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.numberofkeys = numberofkeys;
        this.reportfeed = reportfeed;
        this.checkinfeed = checkinfeed;
        this.complaints = complaints;
        this.singletouch = singletouch;
    }

    public String getfirstname(){
        return this.firstname;
    }

    public String getlastname(){
        return this.lastname;
    }

    public String getemail(){
        return this.email;
    }

    public int getnumberofkeys(){
        return numberofkeys;
    }

    public int getreportfeed(){
        return this.reportfeed;
    }

    public int getcheckinfeed(){
        return this.checkinfeed;
    }

    public int getcomplaints() {return this.complaints;}

    public boolean getsingletouch() {return this.singletouch;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("firstname", this.firstname);
        result.put("lastname", this.lastname);
        result.put("email", this.email);
        result.put("numberofkeys",this.numberofkeys);
        result.put("reportfeed",this.reportfeed);
        result.put("checkinfeed",this.checkinfeed);
        result.put("complaints",this.complaints);
        result.put("singletouch",this.singletouch);

        return result;
    }
}

