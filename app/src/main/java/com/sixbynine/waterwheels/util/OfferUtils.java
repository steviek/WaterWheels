package com.sixbynine.waterwheels.util;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;

import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.model.Offer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class OfferUtils {

  private OfferUtils() {
  }

  private static final SimpleDateFormat sdf24hrShort = new SimpleDateFormat("H:mm");
  private static final SimpleDateFormat sdf12hrShort = new SimpleDateFormat("h:mm a");
  private static final SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE");
  private static final SimpleDateFormat sdfMedium = new SimpleDateFormat("MMM d");
  private static final SimpleDateFormat sdfLong = new SimpleDateFormat("EEE MMM d");

  public static String makePrettyTimestamp(Context context, long time) {
    // get a calendar and set it to the start of the next day
    Calendar cal = Calendar.getInstance();
    cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
    cal.add(Calendar.DATE, 1);

    String shortTime = getShortTime(context, time);

    if (time < cal.getTimeInMillis()) {
      return context.getString(R.string.today_at_x, shortTime);
    }

    // the day after tomorrow now
    cal.add(Calendar.DATE, 1);

    if (time < cal.getTimeInMillis()) {
      return context.getString(R.string.tomorrow_at_x, shortTime);
    }

    // to start of next week
    cal.add(Calendar.DATE, 5);

    if (time < cal.getTimeInMillis()) {
      return context.getString(R.string.x_at_y, sdfDay.format(new Date(time)), shortTime);
    }

    //otherwise use full date
    return context.getString(R.string.x_at_y, sdfLong.format(new Date(time)), shortTime);
  }

  private static String getShortTime(Context context, long time) {
    boolean is24hr = DateFormat.is24HourFormat(context);
    return is24hr ? sdf24hrShort.format(new Date(time)) : sdf12hrShort.format(new Date(time));
  }

  public static String makePrettyPostTime(Context context, long time) {
    Resources res = context.getResources();

    long timeAgo = System.currentTimeMillis() - time;

    if (timeAgo < TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES)) {
      return res.getString(R.string.few_seconds_ago);
    }

    if (timeAgo < TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)) {
      int minutes = (int) TimeUnit.MINUTES.convert(timeAgo, TimeUnit.MILLISECONDS);
      return res.getQuantityString(R.plurals.x_mins, minutes, minutes);
    }

    if (timeAgo < TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)) {
      int hours = (int) TimeUnit.HOURS.convert(timeAgo, TimeUnit.MILLISECONDS);
      return res.getQuantityString(R.plurals.x_hrs, hours, hours);
    }

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -1);
    int yesterday = cal.get(Calendar.DAY_OF_WEEK);

    cal.setTimeInMillis(time);
    if (yesterday == cal.get(Calendar.DAY_OF_WEEK)) {
      return res.getString(R.string.yesterday_at_x, getShortTime(context, time));
    }

    return context.getString(R.string.x_at_y, sdfMedium.format(new Date(time)), getShortTime(context, time));
  }

  public static String getStaticMapUrl(Offer offer, int width, int height) {
    return String.format("https://maps.googleapis.com/maps/api/staticmap" +
            "?markers=color:red%%7C%f,%f" +
            "&markers=color:green%%7C%f,%f" +
            "&key=%s" +
            "&size=%dx%d",
        offer.getOrigin().getLatitude(),
        offer.getOrigin().getLongitude(),
        offer.getDestination().getLatitude(),
        offer.getDestination().getLongitude(),
        SecretKeys.GOOGLE_API_KEY,
        width,
        height);
  }
}
