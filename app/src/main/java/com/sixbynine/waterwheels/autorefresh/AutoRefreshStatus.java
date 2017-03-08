package com.sixbynine.waterwheels.autorefresh;

public enum AutoRefreshStatus {
  NONE, NONE_WIFI_ONLY, WIFI_ONLY, ANY;

  public boolean isEnabled() {
    return this == WIFI_ONLY || this == ANY;
  }

  public boolean isWifiOnly() {
    return this == NONE_WIFI_ONLY || this == WIFI_ONLY;
  }
}
