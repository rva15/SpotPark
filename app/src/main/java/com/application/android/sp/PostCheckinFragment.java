package com.application.android.sp;
//All imports
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.vision.text.Line;

import static com.application.android.sp.R.id.earnmore;

/**
 * Created by ruturaj on 1/5/17.
 */
public class PostCheckinFragment extends Fragment {
    //Variable Declarations
    private String UID;
    private View view;
    private byte[] bytearray;
    private Bitmap mapimage;
    private ImageView cinmap;
    private int width,sub;
    private double hours,mins;
    private String time;
    private LinearLayout reminderlayout,editlayout,earnmorelayout;
    private View linetop,linebot;

    //---------------Fragment Lifecycle Methods-------------------//

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();                 //get required data from intent
        UID = extras.getString("userid");
        bytearray = extras.getByteArray("mapimage");
        mapimage = BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length);
        width = extras.getInt("width");
        hours = extras.getDouble("hours");
        mins  = extras.getDouble("mins");
        sub   = extras.getInt("sub");
        time  = gettime((int)hours,(int)mins,sub);          //get the time in suitable format

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

        // Get all the views
        reminderlayout = (LinearLayout) view.findViewById(R.id.reminderlayout);
        editlayout = (LinearLayout) view.findViewById(R.id.editlayout);
        editlayout.setVisibility(View.GONE);
        earnmorelayout = (LinearLayout) view.findViewById(R.id.earnmorelayout);
        earnmorelayout.setVisibility(View.GONE);
        linetop = (View) view.findViewById(R.id.linetop);
        linebot = (View) view.findViewById(R.id.linebot);
        TextView reminder = (TextView) view.findViewById(R.id.remindertv);
        TextView couttime = (TextView) view.findViewById(R.id.expirytime);

        //set the content of the views
        if(time.equals("---")){
            reminder.setText("As per your requirement, there is no reminder set");
        }
        else {
            couttime.setText(time);
        }
        TextView earnmore = (TextView) view.findViewById(R.id.earnmore);
        earnmore.setTextColor(ContextCompat.getColor(getContext(),R.color.black));
        String s = "Earn 2 more keys just by letting us know <b> when you start walking back </b> to get your car out";
        earnmore.setText(fromHtml(s));

        //Animate the views
        Animation anim1 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_right);
        linetop.startAnimation(anim1);
        Animation anim2 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_left);
        linebot.startAnimation(anim2);
        Animation anim3 = AnimationUtils.loadAnimation(getContext(), R.anim.push_up_in);
        anim3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animatemsg2();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        reminderlayout.startAnimation(anim3);


        // Initialize the Ad unit
        NativeExpressAdView adView = (NativeExpressAdView)view.findViewById(R.id.carlocadView);
        AdRequest request = new AdRequest.Builder()
                .addTestDevice(getResources().getString(R.string.test_device_ID))
                .build();
        adView.loadAd(request);
        return view;
    }

    //------------------------Helper Functions----------------------//

    private void animatemsg2(){
        Animation anim4 = AnimationUtils.loadAnimation(getContext(), R.anim.push_up_in);
        anim4.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                editlayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animatemsg3();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        editlayout.setAnimation(anim4);

    }

    private void animatemsg3(){
        Animation anim5 = AnimationUtils.loadAnimation(getContext(), R.anim.push_up_in);
        anim5.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                earnmorelayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        earnmorelayout.setAnimation(anim5);
    }

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
    private String gettime(int hours,int mins,int sub){
        if(hours==123 || mins==123){
            time = "---";
            return time;
        }
        double timeinmins = hours*60 + mins - sub/60000;
        int newhours=0,newmins=0;
        if(timeinmins>0){
            newhours = (int) Math.floor(timeinmins/60);
            newmins  = (int)timeinmins - 60*newhours;
        }
        if(timeinmins<0){
            timeinmins = 24*60 + timeinmins;
            newhours = (int) Math.floor(timeinmins/60);
            newmins  = (int)timeinmins - 60*newhours;
        }
        if(newhours>12){
            if(newmins <10) {
                time = Integer.toString(newhours - 12) + ":0" + Integer.toString(newmins) + " pm";
            }
            else{
                time = Integer.toString(newhours - 12) + ":" + Integer.toString(newmins) + " pm";
            }
        }
        if(newhours<12){
            if(newmins<10) {
                time = Integer.toString(newhours) + ":0" + Integer.toString(newmins) + " am";
            }
            else{
                time = Integer.toString(newhours) + ":" + Integer.toString(newmins) + " am";
            }
        }
        if(newhours==12){
            if(newmins <10) {
                time = Integer.toString(newhours) + ":0" + Integer.toString(newmins) + " pm";
            }
            else{
                time = Integer.toString(newhours) + ":" + Integer.toString(newmins) + " pm";
            }
        }
        return time;
    }

}
