package com.sixbynine.waterwheels.util;

import android.util.Log;

import com.sixbynine.waterwheels.BuildConfig;

public final class Logger {

  private static final String TAG = "waterwheels";

  private Logger() {
  }

  public static void d(String message, Object... args) {
    if (BuildConfig.DEBUG) {
      Log.d(TAG, String.format(message, args));
    }
  }

  public static void w(String message, Object... args) {
    if (BuildConfig.DEBUG) {
      Log.w(TAG, String.format(message, args));
    }
  }

  public static void e(Throwable e) {
    if (BuildConfig.DEBUG) {
      Log.e(TAG, e.getMessage(), e);
    }
  }

  public static void e(String message, Object... args) {
    if (BuildConfig.DEBUG) {
      Log.e(TAG, String.format(message, args));
    }
  }
}
