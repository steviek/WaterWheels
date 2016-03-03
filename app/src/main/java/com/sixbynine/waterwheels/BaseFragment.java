package com.sixbynine.waterwheels;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseFragment extends Fragment {

    private boolean registered;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getInstance().getBus().register(this);
        registered = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!registered) {
            MyApplication.getInstance().getBus().register(this);
            registered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MyApplication.getInstance().getBus().unregister(this);
        registered = false;
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }
}
