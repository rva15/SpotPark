package com.example.android.sp;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 9/8/16.
 */
public class ReportedTimes {
    boolean fullday,fullweek,mon,tue,wed,thu,fri,sat,sun;
    int starthours,startmins,endhours,endmins;

    public ReportedTimes(){}

    public ReportedTimes(boolean fullday,int starthours,int startmins,int endhours,int endmins,boolean fullweek,boolean mon,boolean tue, boolean wed,boolean thu,boolean fri,boolean sat, boolean sun){
        this.fullday = fullday;
        this.starthours=starthours;
        this.startmins=startmins;
        this.endhours=endhours;
        this.endmins=endmins;
        this.fullweek=fullweek;
        this.mon=mon;
        this.tue=tue;
        this.wed=wed;
        this.thu=thu;
        this.fri=fri;
        this.sat=sat;
        this.sun=sun;
    }

    public boolean getfullday(){return this.fullday;}
    public int getstarthours(){return this.starthours;}
    public int getstartmins(){return this.startmins;}
    public int getendhours(){return this.endhours;}
    public int getendmins(){return this.endmins;}
    public boolean getfullweek(){return this.fullweek;}
    public boolean getmon(){return this.mon;}
    public boolean gettue(){return this.tue;}
    public boolean getwed(){return this.wed;}
    public boolean getthu(){return this.thu;}
    public boolean getfri(){return this.fri;}
    public boolean getsat(){return this.sat;}
    public boolean getsun(){return this.sun;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fullday",this.fullday);
        result.put("starthours",this.starthours);
        result.put("startmins",this.startmins);
        result.put("endhours",this.endhours);
        result.put("endmins",this.endmins);
        result.put("fullweek",this.fullweek);
        result.put("mon",this.mon);
        result.put("tue",this.tue);
        result.put("wed",this.wed);
        result.put("thu",this.thu);
        result.put("fri",this.fri);
        result.put("sat",this.sat);
        result.put("sun",this.sun);

        return result;
    }


}
