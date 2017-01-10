package com.example.android.sp;
//All imports
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ruturaj on 1/5/17.
 */
public class PostCheckinFragment extends Fragment {
    //Variable Declarations
    private String UID;
    private View view;
    private Bitmap mapimage;
    private ImageView cinmap;
    private int width;
    private double hours,mins;
    private String time;

    //---------------Fragment Lifecycle Methods-------------------//

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();                 //get required data from intent
        UID = extras.getString("userid");
        mapimage = extras.getParcelable("mapimage");
        width = extras.getInt("width");
        hours = extras.getDouble("hours");
        mins  = extras.getDouble("mins");
        time  = gettime((int)hours,(int)mins);          //get the time in suitable format

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_postcheckin, container, false);  //inflate layout
        super.onCreate(savedInstanceState);
        cinmap = (ImageView) view.findViewById(R.id.mapimage);

        // Crop the google map screenshot if it is vertical, if horizontal then leave as is
        Bitmap cropped = mapimage;
        if(2*mapimage.getHeight()>width) {
            cropped = Bitmap.createBitmap(mapimage, (int) (mapimage.getWidth() / 2 - width / 2), (int) (mapimage.getHeight() / 2 - width / 4), width, (int) width / 2);
        }
        cinmap.setImageBitmap(cropped);

        // Set the text for the two text views
        TextView reminder = (TextView) view.findViewById(R.id.remindertv);
        TextView couttime = (TextView) view.findViewById(R.id.expirytime);
        if(time.equals("---")){
            reminder.setText("As per your requirement, there is no reminder set");
        }
        else {
            couttime.setText(time);
        }
        TextView earnmore = (TextView) view.findViewById(R.id.earnmore);
        String s = "You just earned two keys by checking in to this place. Earn <b> two more keys by letting us know </b> when you leave!";
        earnmore.setText(fromHtml(s));
        return view;
    }

    //------------------------Helper Functions----------------------//

    // This method required due to 'from html' syntax deprecation
    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    //Utility to get time in suitable format
    private String gettime(int hours,int mins){
        if(hours==123 || mins==123){
            time = "---";
            return time;
        }
        if(hours>12){
            if(mins <10) {
                time = Integer.toString(hours - 12) + ":0" + Integer.toString(mins) + " pm";
            }
            else{
                time = Integer.toString(hours - 12) + ":" + Integer.toString(mins) + " pm";
            }
        }
        if(hours<12){
            if(mins<10) {
                time = Integer.toString(hours) + ":0" + Integer.toString(mins) + " am";
            }
            else{
                time = Integer.toString(hours) + ":" + Integer.toString(mins) + " am";
            }
        }
        if(hours==12){
            if(mins <10) {
                time = Integer.toString(hours) + ":0" + Integer.toString(mins) + " pm";
            }
            else{
                time = Integer.toString(hours) + ":" + Integer.toString(mins) + " pm";
            }
        }
        return time;
    }

}
