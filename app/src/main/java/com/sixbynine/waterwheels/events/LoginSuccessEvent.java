package com.sixbynine.waterwheels.events;

import com.facebook.login.LoginResult;

public final class LoginSuccessEvent {

    private final LoginResult loginResult;

    public LoginSuccessEvent(LoginResult loginResult) {
        this.loginResult = loginResult;
    }

    public LoginResult getLoginResult() {
        return loginResult;
    }
}
