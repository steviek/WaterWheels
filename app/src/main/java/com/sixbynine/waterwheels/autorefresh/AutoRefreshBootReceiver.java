package com.sixbynine.waterwheels.autorefresh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoRefreshBootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
      switch (AutoRefreshManager.getBackgroundJobStatus()) {
        case ANY:
          AutoRefreshManager.scheduleJob(false);
          break;
        case WIFI_ONLY:
          AutoRefreshManager.scheduleJob(true);
          break;
      }
    }
  }
}
