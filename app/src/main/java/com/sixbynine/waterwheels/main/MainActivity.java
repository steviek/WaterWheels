package com.sixbynine.waterwheels.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.facebook.appevents.AppEventsLogger;
import com.sixbynine.waterwheels.BaseActivity;
import com.sixbynine.waterwheels.LoginFragment;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.VersionUpdate;
import com.sixbynine.waterwheels.manager.FacebookManager;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.offerdisplay.DisplayFragment;
import com.sixbynine.waterwheels.offerdisplay.OnOfferClickListener;
import com.sixbynine.waterwheels.util.Keys;

import java.util.List;

public final class MainActivity extends BaseActivity implements OnOfferClickListener {

    private State state;
    private Offer displayOffer;

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
                    fragment = new ControlFragment();
                    VersionUpdate.showDialogIfAppropriate(this);
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
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new ControlFragment()).commit();
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
            navigateBackFromDisplay();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (state == State.DISPLAY) {
                    navigateBackFromDisplay();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateBackFromDisplay() {
        displayOffer = null;

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new ControlFragment()).commit();
        }

        state = State.LIST;
    }

    @Override
    public void onOfferClick(Offer offer) {
        displayOffer = offer;
        state = State.DISPLAY;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, DisplayFragment.newInstance(displayOffer))
                .addToBackStack("display")
                .commit();
    }
}
