package com.example.android.sp;
//All imports
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

/**
 * Created by ruturaj on 1/13/17.
 */
public class PostReportFragment extends Fragment {
    //Variable Declarations
    private String UID;
    private View view;
    private byte[] repmapimage;
    private ImageView repspotimage;
    private int width;

    //---------------Fragment Lifecycle Methods-------------------//

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();                 //get required data from intent
        UID = extras.getString("userid");
        repmapimage = extras.getByteArray("repmapimage");
        width = extras.getInt("width");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_postreport, container, false);  //inflate layout
        super.onCreate(savedInstanceState);
        repspotimage = (ImageView) view.findViewById(R.id.repspotimage);

        // Crop the google map screenshot if it is vertical, if horizontal then leave as is
        Bitmap mapimage = BitmapFactory.decodeByteArray(repmapimage, 0, repmapimage.length);
        Bitmap cropped = mapimage;
        if(2*mapimage.getHeight()>width) {
            cropped = Bitmap.createBitmap(mapimage, (int) (mapimage.getWidth() / 2 - width / 2), (int) (mapimage.getHeight() / 2 - width / 4), width, (int) width / 2);
        }
        repspotimage.setImageBitmap(cropped);


        // Initialize the Ad unit
        NativeExpressAdView adView = (NativeExpressAdView)view.findViewById(R.id.postrepadView);
        AdRequest request = new AdRequest.Builder()
                .addTestDevice(getResources().getString(R.string.test_device_ID))
                .build();
        adView.loadAd(request);
        return view;
    }


}
