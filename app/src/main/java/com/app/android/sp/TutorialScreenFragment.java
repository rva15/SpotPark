package com.app.android.sp;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by ruturaj on 2/9/17.
 */
public class TutorialScreenFragment extends android.support.v4.app.Fragment {
    private static String IMG_ID = "imgId";
    private static String TIT_ID = "titId";

    /* Each fragment has got an R reference to the image it will display
     * an R reference to the title it will display, and an R reference to the
     * string content.
     */
    private ImageView image;
    private int imageResId;

    private TextView title;
    private int titleResId;

    public static TutorialScreenFragment newInstance(int imageResId, int titleResId) {
        final TutorialScreenFragment f = new TutorialScreenFragment();
        final Bundle args = new Bundle();
        args.putInt(IMG_ID, imageResId);
        args.putInt(TIT_ID, titleResId);
        f.setArguments(args);
        return f;
    }

    // Empty constructor, required as per Fragment docs
    public TutorialScreenFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.imageResId = arguments.getInt(IMG_ID);
            this.titleResId = arguments.getInt(TIT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Identify and set fields!
        View rootView =  inflater.inflate(R.layout.fragment_tutorial_screen, container, false);
        image = (ImageView) rootView.findViewById(R.id.tutorial_screen_image);
        title = (TextView) rootView.findViewById(R.id.tutorial_screen_title);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Get the font
        //Typeface roboto_light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf"); // Remember to add the font to your assets folder!
        //title.setTypeface(roboto_light);
        // Populate fields with info!
        if (TutorialActivity.class.isInstance(getActivity())) {
            title.setText(titleResId);
            // Call Glide to load image
            Glide.with(this)
                    .load(imageResId)
                    .centerCrop()
                    .into(image);
        }
    }
}
