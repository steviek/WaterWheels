package com.sixbynine.waterwheels.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class Profile implements Parcelable {

  private final String name;
  private final String id;

  public Profile(String name, String id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(id);
  }

  public static final Creator<Profile> CREATOR = new Creator<Profile>() {
    @Override
    public Profile createFromParcel(Parcel source) {
      String name = source.readString();
      String id = source.readString();
      return new Profile(name, id);
    }

    @Override
    public Profile[] newArray(int size) {
      return new Profile[size];
    }
  };

  @Override
  public String toString() {
    return "Profile{" +
        "name='" + name + '\'' +
        ", id='" + id + '\'' +
        '}';
  }
}
