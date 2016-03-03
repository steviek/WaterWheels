package com.sixbynine.waterwheels;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private boolean registered;
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getInstance().getBus().register(this);
        registered = true;
        tracker = MyApplication.getInstance().getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!registered) {
            MyApplication.getInstance().getBus().register(this);
            registered = true;
        }
        tracker.setScreenName(this.getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.getInstance().getBus().unregister(this);
        registered = false;
    }
}
