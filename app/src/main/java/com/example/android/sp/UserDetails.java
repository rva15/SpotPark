package com.example.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 8/25/16.
 */
public class UserDetails {
    public String firstname="";
    public String lastname = "";
    public String email = "";
    public int numberofkeys = 0;
    public String platenumber = "";

    public UserDetails(){}

    public UserDetails(String firstname,String lastname, String email, int numberofkeys, String platenumber){
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.numberofkeys = numberofkeys;
        this.platenumber = platenumber;
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

    public String getplatenumber(){
        return this.platenumber;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("firstname", this.firstname);
        result.put("lastname", this.lastname);
        result.put("email", this.email);
        result.put("numberofkeys",this.numberofkeys);
        result.put("platenumber",this.platenumber);

        return result;
    }
}

