package com.application.android.sp;

import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by ruturaj on 9/2/16.
 */
public class SPApplication extends android.app.Application {
    private static SPApplication instance;

    public SPApplication() {
        instance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static android.content.Context getContext() {
        return instance;
    }

}