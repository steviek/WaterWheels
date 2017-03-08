package com.sixbynine.waterwheels.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.sixbynine.waterwheels.util.Prefs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PlaceChoice implements Parcelable {
  private final String name;
  private final List<Place> places;

  public PlaceChoice(String name, List<Place> places) {
    this.name = name;
    this.places = places;
  }

  public String getName() {
    return name;
  }

  public List<Place> getPlaces() {
    return places;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof PlaceChoice) {
      PlaceChoice other = (PlaceChoice) o;
      if (!name.equals(other.name)) {
        return false;
      }

      List<Place> placesDifference = new ArrayList<>(this.places);
      placesDifference.removeAll(other.places);
      return places.size() == other.places.size() && placesDifference.isEmpty();
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (places != null ? places.hashCode() : 0);
    return result;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeInt(places.size());
    for (Place place : places) {
      dest.writeString(place.name());
    }
  }

  public static final Creator<PlaceChoice> CREATOR = new Creator<PlaceChoice>() {
    @Override
    public PlaceChoice createFromParcel(Parcel source) {
      String name = source.readString();
      int n = source.readInt();
      List<Place> places = new ArrayList<>(n);
      for (int i = 0; i < n; i++) {
        places.add(Place.valueOf(source.readString()));
      }
      return new PlaceChoice(name, places);
    }

    @Override
    public PlaceChoice[] newArray(int size) {
      return new PlaceChoice[size];
    }
  };

  public static void saveToPrefs(PlaceChoice placeChoice, String prefix) {
    if (placeChoice == null) {
      Prefs.putString(prefix + ":name", null);
      Prefs.putStringSet(prefix + ":places", null);
    } else {
      Prefs.putString(prefix + ":name", placeChoice.name);
      Set<String> places = new HashSet<>();
      for (Place place : placeChoice.getPlaces()) {
        places.add(place.name());
      }
      Prefs.putStringSet(prefix + ":places", places);
    }
  }

  public static PlaceChoice fromPrefs(String prefix) {
    String name = Prefs.getString(prefix + ":name", null);
    if (name == null) {
      return null;
    }
    Set<String> places = Prefs.getStringSet(prefix + ":places");
    List<Place> placesList = new ArrayList<>();
    for (String place : places) {
      placesList.add(Place.valueOf(place));
    }
    return new PlaceChoice(name, placesList);
  }
}
