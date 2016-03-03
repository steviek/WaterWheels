package com.sixbynine.waterwheels.main;

import android.os.Parcel;
import android.os.Parcelable;

import com.sixbynine.waterwheels.model.PlaceChoice;
import com.sixbynine.waterwheels.util.Logger;
import com.sixbynine.waterwheels.util.Prefs;

import java.util.concurrent.TimeUnit;

public final class MainFragmentState implements Parcelable {

    private PlaceChoice origin;
    private PlaceChoice destination;
    private long timeStart;
    private long timeEnd;
    private boolean filterOpen;

    public PlaceChoice getOrigin() {
        return origin;
    }

    public void setOrigin(PlaceChoice origin) {
        this.origin = origin;
    }

    public PlaceChoice getDestination() {
        return destination;
    }

    public void setDestination(PlaceChoice destination) {
        this.destination = destination;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public boolean isFilterOpen() {
        return filterOpen;
    }

    public void setFilterOpen(boolean filterOpen) {
        this.filterOpen = filterOpen;
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
        dest.writeInt(filterOpen ? 1 : 0);
    }

    public static final Creator<MainFragmentState> CREATOR = new Creator<MainFragmentState>() {
        @Override
        public MainFragmentState createFromParcel(Parcel source) {
            MainFragmentState state = new MainFragmentState();
            if (source.readInt() == 1) {
                state.setOrigin((PlaceChoice) source.readParcelable(PlaceChoice.class.getClassLoader()));
            }
            if (source.readInt() == 1) {
                state.setDestination((PlaceChoice) source.readParcelable(PlaceChoice.class.getClassLoader()));
            }
            state.setTimeStart(source.readLong());
            state.setTimeEnd(source.readLong());
            state.setFilterOpen(source.readInt() == 1);
            return state;
        }

        @Override
        public MainFragmentState[] newArray(int size) {
            return new MainFragmentState[size];
        }
    };

    static MainFragmentState getState() {
        MainFragmentState state = new MainFragmentState();
        long savedTime = Prefs.getLong("state:saved");
        long tenMinutesAgo = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
        // save the last filter for 10 minutes
        if (savedTime > tenMinutesAgo) {
            state.origin = PlaceChoice.fromPrefs("state:origin");
            state.destination = PlaceChoice.fromPrefs("state:destination");
            state.timeStart = Prefs.getLong("state:start");
            state.timeEnd = Prefs.getLong("state:end");
            state.filterOpen = Prefs.getBoolean("state:open");
        }
        Logger.d("GetState: %s", state.toString());
        return state;
    }

    static void saveState(MainFragmentState state) {
        Logger.d("SaveState: %s", state.toString());
        Prefs.putLong("state:saved", System.currentTimeMillis());
        PlaceChoice.saveToPrefs(state.origin, "state:origin");
        PlaceChoice.saveToPrefs(state.destination, "state:destination");
        Prefs.putLong("state:start", state.timeStart);
        Prefs.putLong("state:end", state.timeEnd);
        Prefs.putBoolean("state:open", state.filterOpen);
    }

    @Override
    public String toString() {
        return "MainFragmentState{" +
                "origin=" + origin +
                ", destination=" + destination +
                ", timeStart=" + timeStart +
                ", timeEnd=" + timeEnd +
                ", filterOpen=" + filterOpen +
                '}';
    }
}
