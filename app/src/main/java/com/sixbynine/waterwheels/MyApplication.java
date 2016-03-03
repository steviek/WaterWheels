package com.sixbynine.waterwheels;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import android.app.Application;

import com.crittercism.app.Crittercism;
import com.facebook.FacebookSdk;
import com.sixbynine.waterwheels.util.Prefs;
import com.squareup.otto.Bus;

public final class MyApplication extends Application {

    private static MyApplication instance;

    private Bus bus;
    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
        Crittercism.initialize(getApplicationContext(), "5692e3426c33dc0f00f1159f");
        Prefs.initialize(this);
        bus = new Bus();

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        tracker = analytics.newTracker(R.xml.global_tracker);
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public Bus getBus() {
        return bus;
    }

    public Tracker getDefaultTracker() {
        return tracker;
    }
}
