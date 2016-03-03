package com.sixbynine.waterwheels.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class Post implements Parcelable {

    private final String id;
    private final String message;
    private final long createdTime;
    private final Profile from;

    public Post(String id, String message, long createdTime, Profile from) {
        this.id = id;
        this.message = message;
        this.createdTime = createdTime;
        this.from = from;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public Profile getFrom() {
        return from;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(message);
        dest.writeLong(createdTime);
        dest.writeParcelable(from, flags);
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            String id = source.readString();
            String message = source.readString();
            long createdTime = source.readLong();
            Profile from = source.readParcelable(Profile.class.getClassLoader());
            return new Post(id, message, createdTime, from);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", createdTime=" + createdTime +
                ", from=" + from +
                '}';
    }
}
