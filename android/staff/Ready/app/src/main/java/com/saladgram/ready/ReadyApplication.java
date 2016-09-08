package com.saladgram.ready;

import android.app.Application;

/**
 * Created by yns on 5/30/16.
 */
public class ReadyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Service.start(getApplicationContext());
    }
}
