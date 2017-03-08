package com.sixbynine.waterwheels.settings;

import com.sixbynine.waterwheels.util.Keys;
import com.sixbynine.waterwheels.util.Prefs;

public class NotificationStatus {
  private final boolean enabled;
  private final boolean light;
  private final boolean sound;
  private final boolean vibrate;

  public NotificationStatus(boolean enabled, boolean light, boolean sound, boolean vibrate) {
    this.enabled = enabled;
    this.light = light;
    this.sound = sound;
    this.vibrate = vibrate;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean shouldLight() {
    return light;
  }

  public boolean shouldSound() {
    return sound;
  }

  public boolean shouldVibrate() {
    return vibrate;
  }

  public static NotificationStatus get() {
    int status = Prefs.getInt(Keys.NOTIFICATION_STATUS, -1);
    if (status == -1) {
      return new NotificationStatus(false, true, true, true);
    } else {
      boolean enabled = (status & 1) > 0;
      boolean light = (status & 2) > 0;
      boolean sound = (status & 4) > 0;
      boolean vibrate = (status & 8) > 0;
      return new NotificationStatus(enabled, light, sound, vibrate);
    }
  }

  public static void save(NotificationStatus status) {
    int val = 0;

    val += status.enabled ? 1 : 0;
    val += status.light ? 2 : 0;
    val += status.sound ? 4 : 0;
    val += status.vibrate ? 8 : 0;

    Prefs.putInt(Keys.NOTIFICATION_STATUS, val);
  }

  public static void disable() {
    NotificationStatus status = get();
    save(new NotificationStatus(false, status.light, status.sound, status.vibrate));
  }

  public static void enable() {
    NotificationStatus status = get();
    save(new NotificationStatus(true, status.light, status.sound, status.vibrate));
  }
}
