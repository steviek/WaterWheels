package com.sixbynine.waterwheels.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Optional;
import com.sixbynine.waterwheels.MyApplication;
import com.sixbynine.waterwheels.data.OfferDbManager;
import com.sixbynine.waterwheels.events.OfferPinnedChangedEvent;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Offer implements Parcelable {
    private final Optional<Integer> price;
    private final Place origin;
    private final Place destination;
    private final Optional<String> phoneNumber;
    private final long time;
    private final Post post;
    private final boolean pinned;

    public Offer(
            Post post,
            Optional<Integer> price,
            Place origin,
            Place destination,
            Optional<String> phoneNumber,
            long time,
            boolean pinned) {

        this.post = post;
        this.price = checkNotNull(price);
        this.origin = checkNotNull(origin);
        this.destination = checkNotNull(destination);
        this.phoneNumber = checkNotNull(phoneNumber);
        this.time = time;
        this.pinned = pinned;
    }

    public Post getPost() {
        return post;
    }

    public Optional<Integer> getPrice() {
        return price;
    }

    public Place getOrigin() {
        return origin;
    }

    public Place getDestination() {
        return destination;
    }

    public Optional<String> getPhoneNumber() {
        return phoneNumber;
    }

    public long getTime() {
        return time;
    }

    public boolean isPinned() {
        return pinned;
    }

    /**
     * Pins the post so that it does not get deleted, or unpins it.
     * @return a copy of the offer, now with pinned toggled
     */
    public Offer togglePinned() {
        Offer copy = new Offer(post, price, origin, destination, phoneNumber, time, !pinned);

        OfferDbManager.getInstance().updatePinned(copy);
        MyApplication.getInstance().getBus().post(new OfferPinnedChangedEvent(copy));

        return copy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(price.or(-1));
        dest.writeString(origin.name());
        dest.writeString(destination.name());
        dest.writeString(phoneNumber.or("null"));
        dest.writeLong(time);
        dest.writeParcelable(post, flags);
        dest.writeInt(pinned ? 1 : 0);
    }

    public static final Creator<Offer> CREATOR = new Creator<Offer>() {
        @Override
        public Offer createFromParcel(Parcel source) {
            int rawPrice = source.readInt();
            Optional<Integer> price = rawPrice == -1 ? Optional.<Integer>absent() : Optional.of(rawPrice);
            Place origin = Place.valueOf(source.readString());
            Place destination = Place.valueOf(source.readString());
            String phoneNumber = source.readString();
            Optional<String> phone = phoneNumber.equals("null") ? Optional.<String>absent() : Optional.of(phoneNumber);
            long time = source.readLong();
            Post post = source.readParcelable(Post.class.getClassLoader());
            boolean pinned = source.readInt() == 1;
            return new Offer(post, price, origin, destination, phone, time, pinned);
        }

        @Override
        public Offer[] newArray(int size) {
            return new Offer[size];
        }
    };

    @Override
    public String toString() {
        return "Offer{" +
                "price=" + price +
                ", origin=" + origin +
                ", destination=" + destination +
                ", phoneNumber=" + phoneNumber +
                ", time=" + time +
                ", post=" + post +
                ", pinned=" + pinned +
                '}';
    }
}
