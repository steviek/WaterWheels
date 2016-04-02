package com.sixbynine.waterwheels.filter;

import android.os.Parcel;
import android.os.Parcelable;

import com.sixbynine.waterwheels.model.PlaceChoice;
import com.sixbynine.waterwheels.util.Logger;
import com.sixbynine.waterwheels.util.Prefs;

import java.util.concurrent.TimeUnit;

public final class FilterFragmentState implements Parcelable {

    private PlaceChoice origin;
    private PlaceChoice destination;
    private long timeStart;
    private long timeEnd;

    public PlaceChoice getOrigin() {
        return origin;
    }

    /**
     * Sets the specified PlaceChoice as the origin.
     * @return true if this changed the state, false otherwise
     */
    public boolean setOrigin(PlaceChoice origin) {
        if (origin.equals(this.origin)) {
            return false;
        }
        this.origin = origin;
        return true;
    }

    public PlaceChoice getDestination() {
        return destination;
    }

    /**
     * Sets the specified PlaceChoice as the destination.
     * @return true if this changed the state, false otherwise
     */
    public boolean setDestination(PlaceChoice destination) {
        if (destination.equals(this.destination)) {
            return false;
        }
        this.destination = destination;
        return true;
    }

    public long getTimeStart() {
        return timeStart;
    }

    /**
     * Sets the given long as the start time
     * @return true if this changed the state, false otherwise
     */
    public boolean setTimeStart(long timeStart) {
        if (timeStart == this.timeStart) {
            return false;
        }
        this.timeStart = timeStart;
        return true;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    /**
     * Sets the given long as the end time
     * @return true if this changed the state, false otherwise
     */
    public boolean setTimeEnd(long timeEnd) {
        if (timeEnd == this.timeEnd) {
            return false;
        }
        this.timeEnd = timeEnd;
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(origin != null ? 1 : 0);
        if (origin != null) {
            dest.writeParcelable(origin, flags);
        }
        dest.writeInt(destination != null ? 1 : 0);
        if (destination != null) {
            dest.writeParcelable(destination, flags);
        }
        dest.writeLong(timeStart);
        dest.writeLong(timeEnd);
    }

    public static final Creator<FilterFragmentState> CREATOR = new Creator<FilterFragmentState>() {
        @Override
        public FilterFragmentState createFromParcel(Parcel source) {
            FilterFragmentState state = new FilterFragmentState();
            if (source.readInt() == 1) {
                state.setOrigin((PlaceChoice) source.readParcelable(PlaceChoice.class.getClassLoader()));
            }
            if (source.readInt() == 1) {
                state.setDestination((PlaceChoice) source.readParcelable(PlaceChoice.class.getClassLoader()));
            }
            state.setTimeStart(source.readLong());
            state.setTimeEnd(source.readLong());
            return state;
        }

        @Override
        public FilterFragmentState[] newArray(int size) {
            return new FilterFragmentState[size];
        }
    };

    static FilterFragmentState getState() {
        FilterFragmentState state = new FilterFragmentState();
        long savedTime = Prefs.getLong("state:saved");
        long tenMinutesAgo = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
        // save the last filter for 10 minutes
        if (savedTime > tenMinutesAgo) {
            state.origin = PlaceChoice.fromPrefs("state:origin");
            state.destination = PlaceChoice.fromPrefs("state:destination");
            state.timeStart = Prefs.getLong("state:start");
            state.timeEnd = Prefs.getLong("state:end");
        }
        Logger.d("GetState: %s", state.toString());
        return state;
    }

    static void saveState(FilterFragmentState state) {
        Logger.d("SaveState: %s", state.toString());
        Prefs.putLong("state:saved", System.currentTimeMillis());
        PlaceChoice.saveToPrefs(state.origin, "state:origin");
        PlaceChoice.saveToPrefs(state.destination, "state:destination");
        Prefs.putLong("state:start", state.timeStart);
        Prefs.putLong("state:end", state.timeEnd);
    }

    @Override
    public String toString() {
        return "FilterFragmentState{" +
                "origin=" + origin +
                ", destination=" + destination +
                ", timeStart=" + timeStart +
                ", timeEnd=" + timeEnd +
                '}';
    }
}
