package com.sixbynine.waterwheels.settings;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.autorefresh.AutoRefreshManager;
import com.sixbynine.waterwheels.autorefresh.AutoRefreshStatus;

public final class NotificationChecker {

  public interface OnFinishedListener {
    void onFinished();
  }

  /**
   * Checks whether the notification status is in an invalid state, i.e. if notifications are enabled, but
   * auto-refresh is not.  Shows a dialog prompting the user to enable auto-refresh if it is disabled.
   *
   * @param activity the activity that the dialog will be displayed in if this is invalid
   * @param listener a callback for when the dialog is dismissed
   * @return true if the dialog was shown, false otherwise
   */
  public static boolean checkThatNotificationCanBeSet(Activity activity, final OnFinishedListener listener) {
    AutoRefreshStatus autoRefreshStatus = AutoRefreshManager.getBackgroundJobStatus();
    NotificationStatus notificationStatus = NotificationStatus.get();

    if (!autoRefreshStatus.isEnabled() && notificationStatus.isEnabled()) {
      AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
      alertDialog.setTitle(R.string.must_enable_refresh_title);
      alertDialog.setMessage(activity.getString(R.string.must_enable_refresh));
      alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, activity.getString(R.string.not_now),
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              NotificationStatus.disable();
              dialog.dismiss();
              listener.onFinished();
            }
          });
      alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, activity.getString(R.string.wifi_only),
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              AutoRefreshManager.scheduleJob(true);
              dialog.dismiss();
              listener.onFinished();
            }
          });
      alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.any_network),
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              AutoRefreshManager.scheduleJob(false);
              dialog.dismiss();
              listener.onFinished();
            }
          });
      alertDialog.show();
      return true;
    }
    return false;
  }

}
