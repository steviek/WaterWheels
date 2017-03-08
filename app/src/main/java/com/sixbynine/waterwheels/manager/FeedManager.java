package com.sixbynine.waterwheels.manager;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.model.Place;
import com.sixbynine.waterwheels.model.Post;
import com.sixbynine.waterwheels.model.TravelPoints;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FeedManager {

  private static final String DRIVING_REGEX = "^Driv(.|\\s)*$|" +
      "^Offer(.|\\s)*$|" +
      "^Ride(.|\\s)*$|" +
      "^Going(.|\\s)*$|" +
      "^Carpool(.|\\s)*$|" +
      "^.*$1[05].*inbox.*$|" +
      "^.*(spot|seat).*(left|available|remain).*$";
  private static final Pattern DRIVING_PATTERN = Pattern.compile(DRIVING_REGEX, Pattern.CASE_INSENSITIVE);

  private static final Pattern PICKUP_PATTERN = Pattern.compile("pick\\s?up|drop\\s?off", Pattern.CASE_INSENSITIVE);
  private static final Pattern REQUEST_PATTERN = Pattern.compile("(.|\n)*Looking(.|\n)*|(.|\n)*anyone driv(.|\n)*|Request(.|\n)*|(.|\n)*need a ride(.|\n)*", Pattern.CASE_INSENSITIVE);

  private static final String DIRECTION_AT_START = "^(" + Place.REGEX + ")\\s*(to|-+>|>+)\\s*(" + Place.REGEX + ")(.|\\s)*$";
  private static final Pattern DIRECTION_AT_START_PATTERN = Pattern.compile(DIRECTION_AT_START, Pattern.CASE_INSENSITIVE);

  private FeedManager() {
  }

  public static List<Offer> extractOffers(List<Post> feed) {
    return extractOffers(feed, 0);
  }

  static List<Offer> extractOffers(List<Post> feed, long since) {
    List<Offer> list = new ArrayList<>();
    for (Post post : feed) {
      String message = post.getMessage();

      // Ignore old posts or ones with no message.
      if (message == null || post.getCreatedTime() <= since) {
        continue;
      }

      // We only want offers, so check if the user is driving and not picking up.
      boolean driving = (DRIVING_PATTERN.matcher(message).matches()
          || PICKUP_PATTERN.matcher(message).find()
          || DIRECTION_AT_START_PATTERN.matcher(message).matches())
          && !REQUEST_PATTERN.matcher(message).matches();
      if (!driving) {
        continue;
      }

      // Only deal with posts we can extract travel points from.
      TravelPoints travelPoints = TravelPoints.tryParse(post.getMessage());
      if (travelPoints == null) {
        continue;
      }

      // Only deal with posts for which we have a valid future time.
      long time = getTime(post);
      if (time < System.currentTimeMillis()) {
        continue;
      }

      list.add(new Offer(
          post,
          getPrice(post),
          travelPoints.getOrigin(),
          travelPoints.getDestination(),
          getPhoneNumber(message),
          time));
    }
    return list;
  }

  private static final Pattern PRICE_PATTERN = Pattern.compile("\\$ *(\\d+)|(\\d+) *\\$|(\\d+)/seat");

  private static Optional<Integer> getPrice(Post feedPost) {
    Matcher m = PRICE_PATTERN.matcher(feedPost.getMessage());
    if (m.find()) {
      for (int i = 1; i <= 3; i++) {
        if (m.group(i) != null) {
          return Optional.of(Integer.parseInt(m.group(i)));
        }
      }
    }
    return Optional.absent();
  }

  private static final Pattern PHONE_PATTERN = Pattern.compile("\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})");

  private static Optional<String> getPhoneNumber(String message) {
    Matcher m = PHONE_PATTERN.matcher(message);
    if (m.find()) {
      return Optional.of(m.group(1) + m.group(2) + m.group(3));
    }
    return Optional.absent();
  }

  private enum Time {
    MORNING(10, "Morning"),
    NOON(12, "Noon"),
    AFTERNOON(15, "Afternoon"),
    EVENING(18, "Evening"),
    NIGHT(21, "Night"),
    ANY_TIME(12, "Any\\s?time"),
    AFTER_WORK(18, "After work");

    static final Pattern PATTERN;
    static final Map<Time, Pattern> PATTERNS;

    static {
      PATTERNS = new HashMap<>();
      List<String> parts = new ArrayList<>();
      for (Time time : values()) {
        parts.add(time.regex);
        PATTERNS.put(time, Pattern.compile(time.regex, Pattern.CASE_INSENSITIVE));
      }
      PATTERN = Pattern.compile("(" + Joiner.on("|").join(parts) + ")", Pattern.CASE_INSENSITIVE);
    }

    final int hour;
    final String regex;

    Time(int hour, String regex) {
      this.hour = hour;
      this.regex = regex;
    }

    static Time getTime(String s) {
      for (Time time : values()) {
        if (PATTERNS.get(time).matcher(s).matches()) {
          return time;
        }
      }
      throw new IllegalArgumentException("Invalid time: " + s);
    }
  }

  private enum Day {
    SUNDAY("Sund?a?y?", Calendar.SUNDAY),
    MONDAY("Mond?a?y?", Calendar.MONDAY),
    TUESDAY("Tues?d?a?y?", Calendar.TUESDAY),
    WEDNESDAY("Wedn?e?s?d?a?y?", Calendar.WEDNESDAY),
    THURSDAY("Thur?s?d?a?y?", Calendar.THURSDAY),
    FRIDAY("Frid?a?y?", Calendar.FRIDAY),
    SATURDAY("Satu?r?d?a?y?", Calendar.SATURDAY);

    static final Map<Day, Pattern> PATTERNS;

    static {
      PATTERNS = new HashMap<>();
      for (Day day : values()) {
        PATTERNS.put(day, Pattern.compile("\\W" + day.regex + "\\W", Pattern.CASE_INSENSITIVE));
      }
    }

    final String regex;
    final int calendarDay;

    Day(String regex, int calendarDay) {
      this.regex = regex;
      this.calendarDay = calendarDay;
    }

    static String regex() {
      String regex = "(";
      for (int i = 0; i < values().length - 1; i++) {
        regex += "\\W" + values()[i].regex + "\\W|";
      }
      return regex + "\\W" + values()[values().length - 1].regex + "\\W)";
    }

    static Day getDay(String dayString) {
      for (Day day : values()) {
        if (PATTERNS.get(day).matcher(dayString).matches()) {
          return day;
        }
      }
      throw new IllegalArgumentException(dayString + " is not a recognized day");
    }
  }

  private static final String MONTH_REGEX = "(Janu?a?r?y?|Febr?u?a?r?y?|Marc?h?|Apri?l?|May|June?|July?|Augu?s?t?|Sept?e?m?b?e?r?|Octo?b?e?r?|Nove?m?b?e?r?|Dece?m?b?e?r?)";
  private static final Pattern DAY_THE_DATE_PATTERN = Pattern.compile(Day.regex() + " the " + "(\\d\\d?)(st|nd|rd|th)", Pattern.CASE_INSENSITIVE);
  private static final Pattern MONTH_DAY_PATTERN = Pattern.compile(MONTH_REGEX + "\\s?(\\d\\d?)\\D", Pattern.CASE_INSENSITIVE);
  private static final Pattern DAY_MONTH_PATTERN = Pattern.compile("\\D(\\d\\d?)(st|nd|rd|th)?\\s?" + MONTH_REGEX, Pattern.CASE_INSENSITIVE);
  private static final Pattern X_PM_PATTERN = Pattern.compile("(\\d\\d?)([:.]?(\\d\\d))?\\s?((a.?m.?)|(p.?m.?))", Pattern.CASE_INSENSITIVE);
  private static final Pattern AT_X = Pattern.compile("((at|leave?i?n?g?)(\\saround)?|around)\\s(\\d\\d?)[:.]?(\\d\\d)?\\D", Pattern.CASE_INSENSITIVE);
  private static final Pattern IN_X_MINS = Pattern.compile("in (\\d\\d)\\s?mins", Pattern.CASE_INSENSITIVE);
  private static final Pattern COLON_TIME_PATTERN = Pattern.compile("(\\d\\d?)[:.]\\s?(\\d\\d)");
  private static final Pattern TODAY_PATTERN = Pattern.compile("Today", Pattern.CASE_INSENSITIVE);
  private static final Pattern TOMORROW_PATTERN = Pattern.compile("To?mo?rr?o?w?s?", Pattern.CASE_INSENSITIVE);
  private static final Pattern DAY_PATTERN = Pattern.compile(Day.regex(), Pattern.CASE_INSENSITIVE);
  private static final Pattern RIGHT_NOW_PATTERN = Pattern.compile("now|asap|within the hour", Pattern.CASE_INSENSITIVE);

  private static long getTime(Post post) {
    Calendar postDate = Calendar.getInstance();
    postDate.setTimeInMillis(TimeUnit.MILLISECONDS.convert(post.getCreatedTime(), TimeUnit.SECONDS));
    postDate.set(
        postDate.get(Calendar.YEAR),
        postDate.get(Calendar.MONTH),
        postDate.get(Calendar.DAY_OF_MONTH),
        0,
        0,
        0);

    String message = post.getMessage();

    long rideDate = 0;

    Matcher todayMatcher = TODAY_PATTERN.matcher(message);
    if (todayMatcher.find()) {
      rideDate = postDate.getTimeInMillis();
    }

    if (rideDate == 0) {
      Matcher dayTheDateMatcher = DAY_THE_DATE_PATTERN.matcher(message);
      if (dayTheDateMatcher.find()) {
        int day = Integer.parseInt(dayTheDateMatcher.group(2));
        if (day < postDate.get(Calendar.DAY_OF_MONTH)) {
          postDate.add(Calendar.MONTH, 1);
        }
        postDate.set(Calendar.DAY_OF_MONTH, day);
        rideDate = postDate.getTimeInMillis();
      }
    }

    if (rideDate == 0) {
      Matcher monthDayMatcher = MONTH_DAY_PATTERN.matcher(message);
      if (monthDayMatcher.find()) {
        int day = Integer.parseInt(monthDayMatcher.group(2));
        if (day < postDate.get(Calendar.DAY_OF_MONTH)) {
          postDate.add(Calendar.MONTH, 1);
        }
        postDate.set(Calendar.DAY_OF_MONTH, day);
        rideDate = postDate.getTimeInMillis();
      }
    }

    if (rideDate == 0) {
      Matcher dayMonthMatcher = DAY_MONTH_PATTERN.matcher(message);
      if (dayMonthMatcher.find()) {
        int day = Integer.parseInt(dayMonthMatcher.group(1));
        if (day < postDate.get(Calendar.DAY_OF_MONTH)) {
          postDate.add(Calendar.MONTH, 1);
        }
        postDate.set(Calendar.DAY_OF_MONTH, day);
        rideDate = postDate.getTimeInMillis();
      }
    }

    if (rideDate == 0) {
      Matcher dayMatcher = DAY_PATTERN.matcher(message);
      if (dayMatcher.find()) {
        Day day = Day.getDay(dayMatcher.group(0));
        postDate.add(Calendar.DAY_OF_MONTH, (day.calendarDay - postDate.get(Calendar.DAY_OF_WEEK) + 7) % 7);
        rideDate = postDate.getTimeInMillis();
      }
    }

    if (rideDate == 0) {
      Matcher tmrwMatcher = TOMORROW_PATTERN.matcher(message);
      if (tmrwMatcher.find()) {
        postDate.add(Calendar.DAY_OF_MONTH, 1);
        rideDate = postDate.getTimeInMillis();
      }
    }

    if (rideDate == 0) {
      rideDate = postDate.getTimeInMillis();
    }


    long dayTime = 0;

    Matcher xPmMatcher = X_PM_PATTERN.matcher(message);
    if (xPmMatcher.find()) {
      int hour = Integer.parseInt(xPmMatcher.group(1));
      if (hour < 12 && xPmMatcher.group(6) != null) {
        hour += 12;
      }
      dayTime = TimeUnit.MILLISECONDS.convert(hour, TimeUnit.HOURS);
      if (xPmMatcher.group(3) != null) {
        int minutes = Integer.parseInt(xPmMatcher.group(3));
        dayTime += TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
      }
    }

    if (dayTime == 0) {
      Matcher colonTimeMatcher = COLON_TIME_PATTERN.matcher(message);
      if (colonTimeMatcher.find()) {
        int hour = Integer.parseInt(colonTimeMatcher.group(1));
        if (hour < 12) {
          hour += 12;
        }
        int minutes = Integer.parseInt(colonTimeMatcher.group(2));
        dayTime = TimeUnit.MILLISECONDS.convert(hour, TimeUnit.HOURS)
            + TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
      }
    }

    if (dayTime == 0) {
      Matcher timeOfDayMatcher = Time.PATTERN.matcher(message);
      if (timeOfDayMatcher.find()) {
        Time time = Time.getTime(timeOfDayMatcher.group(0));
        dayTime = TimeUnit.MILLISECONDS.convert(time.hour, TimeUnit.HOURS);
      }
    }

    if (dayTime == 0) {
      Matcher rightNowMatcher = RIGHT_NOW_PATTERN.matcher(message);
      if (rightNowMatcher.find()) {
        // add an hour to the right now post
        return TimeUnit.MILLISECONDS.convert(post.getCreatedTime() + 60 * 60, TimeUnit.SECONDS);
      }
    }

    if (dayTime == 0) {
      Matcher atXMatcher = AT_X.matcher(message);
      if (atXMatcher.find()) {
        int hour = Integer.parseInt(atXMatcher.group(4));
        if (hour < 12) {
          hour += 12;
        }
        dayTime = TimeUnit.MILLISECONDS.convert(hour, TimeUnit.HOURS);
        if (atXMatcher.group(5) != null) {
          int minutes = Integer.parseInt(atXMatcher.group(5));
          dayTime += TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
        }
      }
    }

    if (dayTime == 0) {
      Matcher inXMinsMatcher = IN_X_MINS.matcher(message);
      if (inXMinsMatcher.find()) {
        int mins = Integer.parseInt(inXMinsMatcher.group(1));
        return TimeUnit.MILLISECONDS.convert(post.getCreatedTime() + mins * 60, TimeUnit.SECONDS);
      }
    }

    if (dayTime > 0) {
      return rideDate + dayTime;
    }

    return -1;
  }

  private static long printMatcherGroups(Matcher fullDateMatcher) {
    for (int i = 0; i < fullDateMatcher.groupCount(); i++) {
      System.out.println(i + ": " + fullDateMatcher.group(i));
    }
    throw new IllegalStateException();
  }
}
