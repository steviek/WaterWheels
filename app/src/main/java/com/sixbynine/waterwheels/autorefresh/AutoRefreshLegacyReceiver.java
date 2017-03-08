package com.sixbynine.waterwheels.autorefresh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;

import com.sixbynine.waterwheels.MyApplication;
import com.sixbynine.waterwheels.events.FeedRequestFinishedEvent;
import com.sixbynine.waterwheels.manager.FacebookManager;
import com.sixbynine.waterwheels.util.Logger;
import com.squareup.otto.Subscribe;

public final class AutoRefreshLegacyReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Logger.d("Service wakeup");
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WaterWheels");

    wl.acquire();
    Logger.d("Lock acquired");


    if (AutoRefreshManager.getBackgroundJobStatus() == AutoRefreshStatus.ANY || isUsingWifi(context)) {
      Logger.d("onStartJob background poll");
      MyApplication.getInstance().getBus().register(new Object() {
        @Subscribe
        public void onFeedRequestFinished(FeedRequestFinishedEvent event) {
          Logger.d("onJobFinished");
          MyApplication.getInstance().getBus().unregister(this);
          wl.release();
        }
      });
      FacebookManager.getInstance().refreshGroupPosts();
    } else {
      wl.release(); //don't make network request if not on wifi and wifi-only
    }
  }

  private static boolean isUsingWifi(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
  }
}
