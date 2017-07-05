package com.app.android.sp;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
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
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    if(activity.getConfidence()>50) {
                        Intent servIntent = new Intent(getApplicationContext(), ARLocService.class);
                        getApplicationContext().startService(servIntent);
                        Log.d("ActivityRecogition", "In Vehicle: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    if(activity.getConfidence()>50) {
                        Intent servIntent = new Intent(getApplicationContext(), ARLocService.class);
                        getApplicationContext().startService(servIntent);
                        Log.d("ActivityRecogition", "On Bicycle: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    if(activity.getConfidence()>50) {
                        Intent servIntent = new Intent(getApplicationContext(), ARLocService.class);
                        getApplicationContext().startService(servIntent);
                        Log.d("ActivityRecogition", "On Foot: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.RUNNING: {
                    if(activity.getConfidence()>50) {
                        Intent servIntent = new Intent(getApplicationContext(), ARLocService.class);
                        getApplicationContext().startService(servIntent);
                        Log.d("ActivityRecogition", "Running: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.STILL: {
                    if(activity.getConfidence()>50) {
                        Intent servIntent = new Intent(getApplicationContext(), ARLocService.class);
                        getApplicationContext().startService(servIntent);
                        Log.d("ActivityRecogition", "Still: " + activity.getConfidence());
                    }

                    break;
                }
                case DetectedActivity.TILTING: {
                    if(activity.getConfidence()>50) {
                        Intent servIntent = new Intent(getApplicationContext(), ARLocService.class);
                        getApplicationContext().startService(servIntent);
                        Log.d("ActivityRecogition", "Tilting: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.WALKING: {
                    if(activity.getConfidence()>50) {
                        Intent servIntent = new Intent(getApplicationContext(), ARLocService.class);
                        getApplicationContext().startService(servIntent);
                        Log.d("ActivityRecogition", "Walking: " + activity.getConfidence());
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    if(activity.getConfidence()>50) {
                        Intent servIntent = new Intent(getApplicationContext(), ARLocService.class);
                        getApplicationContext().startService(servIntent);
                        Log.d("ActivityRecogition", "Unknown: " + activity.getConfidence());
                    }
                    break;
                }
            }
        }
    }

}
