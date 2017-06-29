package com.app.android.sp;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by ruturaj on 5/19/17.
 */

public class ARService extends IntentService {
    private String TAG = "debugger";

    public ARService() {
        super("ActivityRecognizedService");
    }

    public ARService(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        Log.d(TAG,"activityrecognition handling");
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    if(activity.getConfidence()>50) {
                        Log.d("ActivityRecogition", "In Vehicle: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    if(activity.getConfidence()>50) {
                        Log.d("ActivityRecogition", "On Bicycle: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    if(activity.getConfidence()>50) {
                        Log.d("ActivityRecogition", "On Foot: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.RUNNING: {
                    if(activity.getConfidence()>50) {
                        Log.d("ActivityRecogition", "Running: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.STILL: {
                    if(activity.getConfidence()>50) {
                        Log.d("ActivityRecogition", "Still: " + activity.getConfidence()+" "+readLatitude());
                    }

                    break;
                }
                case DetectedActivity.TILTING: {
                    if(activity.getConfidence()>50) {
                        Log.d("ActivityRecogition", "Tilting: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.WALKING: {
                    if(activity.getConfidence()>50) {
                        Log.d("ActivityRecogition", "Walking: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    if(activity.getConfidence()>50) {
                        Log.d("ActivityRecogition", "Unknown: " + activity.getConfidence());
                    }
                    break;
                }
            }
        }
    }

    private String readLatitude(){
        String line="";
        StringBuffer buffer= new StringBuffer();
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getCacheDir(), "curLat");
            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"An error occured. Please complete this action manually from the SpotPark app",Toast.LENGTH_LONG).show();
            stopSelf();
            e.printStackTrace();
        }
        return buffer.toString();
    }

}
