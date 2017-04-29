package com.sixbynine.waterwheels.model;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public enum Place {
  ANCASTER("Ancaster", "Ancaster", 43.217779, -79.987283),
  BARRIE("Barrie", "Barrie", 44.389356, -79.690332),
  BLUE_MOUNTAIN("Blue Mountain", "Blue\\s?Mountain", 44.676161, -77.946865),
  BRACEBRIDGE("Bracebridge", "Bracebridge", 45.038956, -79.307879),
  BRAMPTON("Brampton", "Brampton", 43.702150, -79.689512),
  CAMBRIDGE("Cambridge", "Cambridge", 43.361621, -80.314428),
  CHICOPEE("Chicopee", "Chicopee", 43.434141, -80.424551),
  DAVIS_CENTRE("Davis Centre", "Davis\\sCent(er|re)|DC|Campus", 43.472940, -80.541812, PlaceGroup.WATERLOO),
  DOWNTOWN_TORONTO("Toronto (Downtown)", "Downtown|DT", 43.655987, -79.379443, PlaceGroup.TORONTO),
  DUNDAS_SQUARE("Dundas Square", "Dundas Square", 43.655987, -79.379443, PlaceGroup.TORONTO),
  ETOBICOKE("Etobicoke", "Etobicoke", 43.659388, -79.5453947, PlaceGroup.TORONTO),
  FAIRVIEW("Fairview", "Fairview|fvmall", 43.779628, -79.345762, PlaceGroup.TORONTO),
  FINCH("Finch Station", "Finch", 43.780467, -79.414636, PlaceGroup.TORONTO),
  FIRST_MARKHAM_PLACE("First Markham Place", "First Markham Place|FMP", 43.849985, -79.349020, PlaceGroup.MARKHAM_RICHMOND_HILL),
  GTA("GTA", "GTA", 43.779628, -79.345762),
  GUELPH("Guelph", "Guelph", 43.544805, -80.248167),
  HAMILTON("Hamilton", "Hamilton", 43.250021, -79.866091),
  HWY_7_LESLIE("Highway 7 & Leslie", "HWY 7(\\s*(and|/|&)\\s*)Leslie", 43.844296, -79.382370, PlaceGroup.MARKHAM_RICHMOND_HILL),
  KINGSTON("Kingston", "Kingston", 44.231172, -76.485954),
  KIPLING("Kipling", "Kipling", 43.637752, -79.535050, PlaceGroup.TORONTO),
  KITCHENER("Kitchener", "Kitchener", 43.449385, -80.493128),
  LINDSAY("Lindsay", "Lindsay", 44.358291, 78.735464),
  LONDON("London", "London", 43.001030, -81.276942, PlaceGroup.LONDON),
  MARKHAM("Markham", "Markham", 43.849985, -79.349020, PlaceGroup.MARKHAM_RICHMOND_HILL),
  MARKVILLE("Markville", "Markville", 43.865923, -79.291865, PlaceGroup.MARKHAM_RICHMOND_HILL),
  MILTON("Milton", "Milton", 43.518299, -79.877404),
  MISSISSAUGA("Mississauga", "[a-z]*sau?gu?a", 43.589045, -79.644120, PlaceGroup.MISSISSAUGA),
  MONTREAL("Montreal", "Montreal", 45.501689, -73.567256),
  NEWMARKET("Newmarket", "Newmarket", 44.059187, -79.461256),
  NIAGARA_FALLS("Niagara Falls", "Niagara", 43.089558, -79.084944),
  NORTH_YORK("North York", "North\\s?York", 43.780467, -79.414636, PlaceGroup.TORONTO),
  OAKVILLE("Oakville", "Oakville", 43.467517, -79.687666),
  OTTAWA("Ottawa", "Ottawa", 45.421530, -75.697193),
  PACIFIC_MALL("Pacific Mall", "Pacific\\s?Mall|P\\s?Mall", 43.824276, -79.306591, PlaceGroup.MARKHAM_RICHMOND_HILL, PlaceGroup.TORONTO, PlaceGroup.SCARBOROUGH),
  PEARSON("Pearson Airport", "Pearson|Airport", 43.677718, -79.624820),
  PREMIUM_OUTLETS("Premium Outlets", "Premium outlet", 43.575347, -79.829407),
  RICHMOND_HILL("Richmond Hill", "Richmond\\s?Hill|RHill", 43.840186, -79.425296, PlaceGroup.MARKHAM_RICHMOND_HILL),
  SCARBOROUGH("Scarborough", "Scarborough", 43.775914, -79.257767, PlaceGroup.SCARBOROUGH),
  SCHOMBERG("Schomberg", "Schomberg", 44.000873, -79.683147),
  SHELBURNE("Shelburne", "Shelburne", 44.079119, -80.201173),
  SHEPPARD("Sheppard", "Sheppard", 43.7620348, -79.4118972, PlaceGroup.TORONTO),
  SQUARE_ONE("Square One", "Square?\\s?One|sq\\s?1|sq one", 43.589045, -79.644120, PlaceGroup.MISSISSAUGA),
  STC("STC", "STC", 43.775914, -79.257767, PlaceGroup.SCARBOROUGH),
  ST_CATHERINES("St. Catherine's", "St. Catherine's|Brock", 43.1175731, -79.2498812),
  TORONTO("Toronto", "Toronto|TRT", 43.779628, -79.345762, PlaceGroup.TORONTO),
  WATERLOO("Waterloo", "Waterloo|Loo|UW|KW|BK|Burger\\s?King", 43.472615, -80.535306, PlaceGroup.WATERLOO),
  WESTERN("Western University", "Western", 43.001030, -81.276942, PlaceGroup.LONDON),
  WILSON("Wilson", "Wilson", 43.734650, -79.450939, PlaceGroup.TORONTO),
  WINDSOR("Windsor", "Windsor", 42.314937, -83.036363),
  WINNIPEG("Winnipeg", "Winnipeg", 49.899754, -97.137494),
  UNION("Union Station", "Union", 43.645223, -79.380828, PlaceGroup.TORONTO),
  VAUGHAN("Vaughan", "Vaughan", 43.825463, -79.537989),
  WARDEN_STEELES("Warden & Steeles", "Warden\\s*[\"and\"|/|&]\\s*Steeles", 43.819563, -79.324523, PlaceGroup.TORONTO, PlaceGroup.SCARBOROUGH, PlaceGroup.MARKHAM_RICHMOND_HILL),
  YONGE_SHEPPARD("Yonge & Sheppard", "Yo?u?nge?(\\s*(and|/|&)\\s*)Sheppard", 43.761548, -79.411624, PlaceGroup.TORONTO),
  YONGE_STEELES("Yonge & Steeles", "Yo?u?nge?(\\s*(and|/|&)\\s*)Steeles", 43.798004, -79.419570, PlaceGroup.TORONTO),
  YORK_MILLS("York Mills Station", "York\\s?Mills", 43.744842, -79.405686, PlaceGroup.TORONTO),
  YORKDALE("Yorkdale", "Yorkdale", 43.725140, -79.451827, PlaceGroup.TORONTO);

  public static final String REGEX;

  private static final Map<Place, Pattern> PATTERNS = new HashMap<>();

  static {
    List<String> regexParts = new ArrayList<>(values().length);
    for (Place place : values()) {
      PATTERNS.put(place, Pattern.compile(place.regex, Pattern.CASE_INSENSITIVE));
      regexParts.add(place.getRegex());
    }
    REGEX = Joiner.on("|").join(regexParts);
  }

  private final String name;
  private final String regex;
  private final double latitude;
  private final double longitude;
  private final PlaceGroup[] parents;

  Place(String name, String regex, double latitude, double longitude, PlaceGroup... parents) {
    this.name = name;
    this.regex = regex;
    this.latitude = latitude;
    this.longitude = longitude;
    this.parents = parents;
  }

  public String getRegex() {
    return regex;
  }

  public String getName() {
    return name;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public PlaceGroup[] getParents() {
    return parents;
  }

  public static Place getPlace(String s) {
    for (Place place : values()) {
      if (PATTERNS.get(place).matcher(s).matches()) {
        return place;
      }
    }
    throw new IllegalArgumentException(s + " does not match any place's regex");
  }

  public static List<PlaceChoice> getAllPlaceChoices() {
    List<PlaceChoice> placeChoices = new ArrayList<>();
    Multimap<PlaceGroup, Place> groups = HashMultimap.create();
    for (Place place : values()) {
      if (place.getParents().length > 0) {
        for (PlaceGroup parent : place.getParents()) {
          groups.put(parent, place);
        }
      } else {
        placeChoices.add(new PlaceChoice(place.name, ImmutableList.of(place)));
      }
    }
    for (PlaceGroup key : groups.keySet()) {
      placeChoices.add(0, new PlaceChoice(key.getName(), ImmutableList.copyOf(groups.get(key))));
    }
    return placeChoices;
  }
}
