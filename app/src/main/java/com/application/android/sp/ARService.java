package com.application.android.sp;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import java.util.ArrayList;

/**
 * Created by ruturaj on 5/19/17.
 */

public class ARService extends IntentService {
    private String TAG = "ActivityRecognition";

    public ARService() {
        super("ActivityRecognizedService");
    }

    public ARService(String name) {

        super(name);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            Log.d(TAG,"sending broadcast");
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            Intent localIntent = new Intent("com.app.android.sp.BROADCAST_ACTION");
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
            localIntent.putExtra("com.app.android.sp.ACTIVITY_EXTRA", detectedActivities);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }


}
