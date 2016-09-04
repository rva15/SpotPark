package com.example.android.sp;

/**
 * Created by ruturaj on 9/2/16.
 */
public class SPApplication extends android.app.Application {
    private static SPApplication instance;

    public SPApplication() {
        instance = this;
    }

    public static android.content.Context getContext() {
        return instance;
    }

}