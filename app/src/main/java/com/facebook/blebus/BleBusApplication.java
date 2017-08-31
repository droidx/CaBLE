package com.facebook.blebus;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by priteshsankhe on 05/12/16.
 */

public class BleBusApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PollReceiver.scheduleAlarms(this);

    }
}
