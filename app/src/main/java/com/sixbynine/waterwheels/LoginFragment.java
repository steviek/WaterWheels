package com.sixbynine.waterwheels;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.sixbynine.waterwheels.manager.FacebookManager;

public final class LoginFragment extends BaseFragment {

  private CallbackManager mCallbackManager;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_login, container, false);

    mCallbackManager = CallbackManager.Factory.create();

    final LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
    loginButton.setReadPermissions("public_profile", "email", "user_friends");
    loginButton.setFragment(this);
    loginButton.registerCallback(mCallbackManager, FacebookManager.getInstance());

    return view;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mCallbackManager.onActivityResult(requestCode, resultCode, data);
  }
}
