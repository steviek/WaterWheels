package com.sixbynine.waterwheels;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public abstract class BaseFragment extends Fragment {

  private boolean registered;

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
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
