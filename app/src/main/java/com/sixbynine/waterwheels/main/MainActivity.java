package com.sixbynine.waterwheels.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.facebook.appevents.AppEventsLogger;
import com.sixbynine.waterwheels.BaseActivity;
import com.sixbynine.waterwheels.LoginFragment;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.manager.FacebookManager;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.util.Keys;

import java.util.List;

public final class MainActivity extends BaseActivity implements MainFragment.Callback {

    private State state;
    private Offer displayOffer;

    private Fragment mainFragment;

    private enum State {
        LOGIN, LIST, DISPLAY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FacebookManager.getInstance().isLoggedIn()) {
            if (savedInstanceState == null) {
                state = State.LIST;
            } else {
                state = State.valueOf(savedInstanceState.getString(Keys.STATUS));
                displayOffer = savedInstanceState.getParcelable(Keys.DISPLAY_OFFER);
            }
        } else {
            state = State.LOGIN;
        }

        if (displayOffer == null && state == State.DISPLAY) {
            state = State.LIST;
        }

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Fragment fragment;
        if (fragments != null) {
            fragment = fragments.get(0);
        } else {
            switch (state) {
                case LOGIN:
                    fragment = new LoginFragment();
                    break;
                case LIST:
                    mainFragment = new MainFragment();
                    fragment = mainFragment;
                    break;
                case DISPLAY:
                    fragment = DisplayFragment.newInstance(displayOffer);
                    break;
                default:
                    throw new IllegalStateException("Unexpected state: " + state);
            }
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Keys.STATUS, state.name());
        outState.putParcelable(Keys.DISPLAY_OFFER, displayOffer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);

        if (state == State.LOGIN && FacebookManager.getInstance().isLoggedIn()) {
            state = State.DISPLAY;
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new MainFragment()).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onBackPressed() {
        if (state == State.DISPLAY) {
            displayOffer = null;
            if (mainFragment == null) {
                mainFragment = new MainFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mainFragment).commit();
            state = State.LIST;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (state == State.DISPLAY) {
                    displayOffer = null;
                    if (mainFragment == null) {
                        mainFragment = new MainFragment();
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mainFragment).commit();
                    state = State.LIST;
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOfferClicked(Offer offer) {
        displayOffer = offer;
        state = State.DISPLAY;
        getSupportFragmentManager().beginTransaction().replace(
                R.id.content_frame,
                DisplayFragment.newInstance(displayOffer)).commit();
    }
}
