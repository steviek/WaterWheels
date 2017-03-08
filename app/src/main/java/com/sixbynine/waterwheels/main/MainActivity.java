package com.sixbynine.waterwheels.main;

import android.app.NotificationManager;
import android.content.Context;
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

  private static boolean inForeground;

  private State state;
  private Offer displayOffer;

  private enum State {
    LOGIN, LIST, DISPLAY
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();

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

    boolean showFilter = getIntent().getBooleanExtra(Keys.SHOW_FILTER, false);

    List<Fragment> fragments = getSupportFragmentManager().getFragments();
    if (fragments == null) {
      Fragment fragment;
      switch (state) {
        case LOGIN:
          fragment = new LoginFragment();
          break;
        case LIST:
          fragment = ControlFragment.newInstance(showFilter);
          VersionUpdate.showDialogIfAppropriate(this);
          break;
        case DISPLAY:
          fragment = DisplayFragment.newInstance(displayOffer);
          break;
        default:
          throw new IllegalStateException("Unexpected state: " + state);
      }

      getSupportFragmentManager().beginTransaction()
          .replace(R.id.content_frame, fragment)
          .commit();

      Offer showOffer = getIntent().getParcelableExtra(Keys.SHOW_OFFER);
      if (showOffer != null) {
        onOfferClick(showOffer);
      }
    }
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

    inForeground = true;
  }

  @Override
  protected void onPause() {
    super.onPause();
    AppEventsLogger.deactivateApp(this);
    inForeground = false;
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

  /**
   * A bit of a hack to determine if we are currently in the foreground.  Should work since this is the only activity,
   * but wouldn't really work otherwise.
   */
  public static boolean isInForeground() {
    return inForeground;
  }
}
