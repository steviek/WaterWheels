package com.sixbynine.waterwheels.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TravelPoints {

  private static final String TO_REGEX = "\\s*(\\stoo?\\s|>+|-+\\s?>|-|→)\\s*";
  private static final String A_B_SIMPLE_REGEX = "(" + Place.REGEX + ")" + TO_REGEX + "(" + Place.REGEX + ")";
  private static final String A_B_FULL_REGEX = "(" + Place.REGEX + ").*" + TO_REGEX + ".*(" + Place.REGEX + ")";
  private static final String B_A_REGEX = "\\sto\\s+(" + Place.REGEX + ").*from.*(" + Place.REGEX + ")";
  private static final String DESTINATION_REGEX = "\\s(too?|leaving for)\\s+(" + Place.REGEX + ")";
  private static final String ORIGIN_REGEX = "\\sfrom\\s+(" + Place.REGEX + ")";
  private static final Pattern A_B_SIMPLE_PATTERN = Pattern.compile(A_B_SIMPLE_REGEX, Pattern.CASE_INSENSITIVE);
  private static final Pattern A_B_FULL_PATTERN = Pattern.compile(A_B_FULL_REGEX, Pattern.CASE_INSENSITIVE);
  private static final Pattern REVERSE_PATTERN = Pattern.compile(B_A_REGEX, Pattern.CASE_INSENSITIVE);
  private static final Pattern DESTINATION_PATTERN = Pattern.compile(DESTINATION_REGEX, Pattern.CASE_INSENSITIVE);
  private static final Pattern ORIGIN_PATTERN = Pattern.compile(ORIGIN_REGEX, Pattern.CASE_INSENSITIVE);

  private final Place origin;
  private final Place destination;

  public TravelPoints(Place origin, Place destination) {
    this.origin = origin;
    this.destination = destination;
  }

  public Place getOrigin() {
    return origin;
  }

  public Place getDestination() {
    return destination;
  }

  public static TravelPoints tryParse(String message) {
    Matcher mForward = A_B_SIMPLE_PATTERN.matcher(message);
    if (mForward.find()) {
      return new TravelPoints(Place.getPlace(mForward.group(1)), Place.getPlace(mForward.group(10)));
    }

    Matcher mForwardFull = A_B_FULL_PATTERN.matcher(message);
    if (mForwardFull.find()) {
      return new TravelPoints(Place.getPlace(mForwardFull.group(1)), Place.getPlace(mForwardFull.group(10)));
    }

    Matcher mReverse = REVERSE_PATTERN.matcher(message);
    if (mReverse.find()) {
      return new TravelPoints(Place.getPlace(mReverse.group(9)), Place.getPlace(mReverse.group(1)));
    }

    Matcher mDestination = DESTINATION_PATTERN.matcher(message);
    if (mDestination.find()) {
      String destination = mDestination.group(2);
      if (!destination.toLowerCase().endsWith("loo")) {
        return new TravelPoints(Place.WATERLOO, Place.getPlace(destination));
      }
    }

    Matcher mOrigin = ORIGIN_PATTERN.matcher(message);
    if (mOrigin.find()) {
      String origin = mOrigin.group(1);
      if (!origin.toLowerCase().endsWith("loo")) {
        return new TravelPoints(Place.getPlace(origin), Place.WATERLOO);
      }
    }

    return null;
  }
}
