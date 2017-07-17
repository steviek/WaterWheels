package com.sixbynine.waterwheels.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.sixbynine.waterwheels.MyApplication;
import com.sixbynine.waterwheels.events.DatabaseUpgradedEvent;
import com.sixbynine.waterwheels.manager.FeedManager;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.model.Place;
import com.sixbynine.waterwheels.model.Post;
import com.sixbynine.waterwheels.model.Profile;
import com.sixbynine.waterwheels.util.Keys;
import com.sixbynine.waterwheels.util.Logger;
import com.sixbynine.waterwheels.util.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class OfferDbManager {

  private static final int ALGORITHM_VERSION = 3;

  private static OfferDbManager instance;

  private final OfferDbHelper dbHelper = new OfferDbHelper(MyApplication.getInstance());

  private State state;

  private OfferDbManager() {
    if (Prefs.getInt(Keys.ALGORITHM_VERSION, ALGORITHM_VERSION) != ALGORITHM_VERSION) {
      state = State.UPDGRADING;
      reprocessOffers();
    } else {
      state = State.READY;
    }
    Prefs.putInt(Keys.ALGORITHM_VERSION, ALGORITHM_VERSION);
  }

  public enum State {
    UPDGRADING, READY
  }

  public static OfferDbManager getInstance() {
    if (instance == null) {
      synchronized (OfferDbManager.class) {
        if (instance == null) {
          instance = new OfferDbManager();
        }
      }
    }
    return instance;
  }

  public void clearStaleOffers() {
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(OfferContract.Offer.TABLE_NAME, OfferContract.Offer.COLUMN_NAME_TIME + " < ?",
            new String[]{String.valueOf(System.currentTimeMillis())});
        Logger.d("Deleted %d stale rows", rowsDeleted);
        return null;
      }
    }.execute();
  }

  public StoreResult storeOffers(List<Offer> offers) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    boolean allSucceeded = true;
    ImmutableList.Builder<Offer> newOffers = ImmutableList.builder();

    for (Offer offer : offers) {
      long oneHour = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
      String query = "SELECT " + OfferContract.Offer._ID +
          " FROM " + OfferContract.Offer.TABLE_NAME +
          " WHERE (" + OfferContract.Offer.COLUMN_NAME_POST_ID + " = ?) OR ((" +
          OfferContract.Offer.COLUMN_NAME_DESTINATION + " = ? OR " +
          OfferContract.Offer.COLUMN_NAME_ORIGIN + " = ?) AND " +
          OfferContract.Offer.COLUMN_NAME_POST_FROM_ID + " = ? AND " +
          OfferContract.Offer.COLUMN_NAME_TIME + " >= ? AND " +
          OfferContract.Offer.COLUMN_NAME_TIME + " <= ?)";
      Cursor c = db.rawQuery(query,
          new String[]{
              offer.getPost().getId(),
              offer.getDestination().name(),
              offer.getOrigin().name(),
              offer.getPost().getFrom().getId(),
              String.valueOf(offer.getTime() - oneHour),
              String.valueOf(offer.getTime() + oneHour)});

      // delete older posts that match, in case they've posted a new one with updated info
      c.moveToFirst();
      while (!c.isAfterLast()) {
        String[] whereArgs = new String[]{c.getString(c.getColumnIndexOrThrow(OfferContract.Offer._ID))};
        db.delete(OfferContract.Offer.TABLE_NAME, OfferContract.Offer._ID + " = ?", whereArgs);
        c.moveToNext();
      }

      if (c.getCount() == 0) {
        newOffers.add(offer);
      } else {
        Logger.d("Deleted %d older post(s) for the same ride", c.getCount());
      }
      c.close();

      ContentValues cv = new ContentValues();
      cv.put(OfferContract.Offer.COLUMN_NAME_DESTINATION, offer.getDestination().name());
      cv.put(OfferContract.Offer.COLUMN_NAME_ORIGIN, offer.getOrigin().name());
      cv.put(OfferContract.Offer.COLUMN_NAME_PHONE, offer.getPhoneNumber().orNull());
      cv.put(OfferContract.Offer.COLUMN_NAME_PRICE, offer.getPrice().or(-1));
      cv.put(OfferContract.Offer.COLUMN_NAME_TIME, offer.getTime());
      cv.put(OfferContract.Offer.COLUMN_NAME_POST_CREATED_TIME, offer.getPost().getCreatedTime());
      cv.put(OfferContract.Offer.COLUMN_NAME_POST_UPDATED_TIME, offer.getPost().getUpdatedTime());
      cv.put(OfferContract.Offer.COLUMN_NAME_POST_ID, offer.getPost().getId());
      cv.put(OfferContract.Offer.COLUMN_NAME_POST_MESSAGE, offer.getPost().getMessage());
      cv.put(OfferContract.Offer.COLUMN_NAME_POST_FROM_ID, offer.getPost().getFrom().getId());
      cv.put(OfferContract.Offer.COLUMN_NAME_POST_FROM_NAME, offer.getPost().getFrom().getName());
      long rowId = db.insert(OfferContract.Offer.TABLE_NAME, null, cv);
      if (rowId == -1) {
        allSucceeded = false;
        Logger.e("Error inserting for " + offer);
      }
    }
    return new StoreResult(allSucceeded, offers, newOffers.build());
  }

  public List<Offer> getOffers() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor c = db.rawQuery("SELECT * FROM " + OfferContract.Offer.TABLE_NAME +
        " WHERE " + OfferContract.Offer.COLUMN_NAME_TIME + " > " + System.currentTimeMillis() +
        " ORDER BY " + OfferContract.Offer.COLUMN_NAME_TIME + " ASC", null);

    ImmutableList.Builder<Offer> list = ImmutableList.builder();

    int indexDestination = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_DESTINATION);
    int indexOrigin = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_ORIGIN);
    int indexPhone = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_PHONE);
    int indexPrice = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_PRICE);
    int indexTime = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_TIME);
    int indexPostCreatedTime = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_POST_CREATED_TIME);
    int indexPostUpdatedTime = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_POST_UPDATED_TIME);
    int indexPostId = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_POST_ID);
    int indexPostMessage = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_POST_MESSAGE);
    int indexPostFromId = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_POST_FROM_ID);
    int indexPostFromName = c.getColumnIndexOrThrow(OfferContract.Offer.COLUMN_NAME_POST_FROM_NAME);

    c.moveToFirst();
    while (!c.isAfterLast()) {
      Place destination = Place.valueOf(c.getString(indexDestination));
      Place origin = Place.valueOf(c.getString(indexOrigin));
      Optional<String> phone = Optional.fromNullable(c.getString(indexPhone));
      int rawPrice = c.getInt(indexPrice);
      Optional<Integer> price = rawPrice == -1 ? Optional.<Integer>absent() : Optional.of(rawPrice);
      long time = c.getLong(indexTime);
      long postCreatedTime = c.getLong(indexPostCreatedTime);
      long postUpdatedTime = c.getLong(indexPostUpdatedTime);
      String postId = c.getString(indexPostId);
      String postMessage = c.getString(indexPostMessage);
      String postFromId = c.getString(indexPostFromId);
      String postFromName = c.getString(indexPostFromName);

      Profile from = new Profile(postFromName, postFromId);
      Post post = new Post(postId, postMessage, postCreatedTime, postUpdatedTime, from);
      Offer offer = new Offer(post, price, origin, destination, phone, time);
      list.add(offer);
      c.moveToNext();
    }
    c.close();
    return list.build();
  }

  private void reprocessOffers() {
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        List<Offer> offers = getOffers();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from " + OfferContract.Offer.TABLE_NAME);
        List<Post> posts = new ArrayList<>();
        for (Offer offer : offers) {
          posts.add(offer.getPost());
        }
        List<Offer> realOffers = FeedManager.extractOffers(posts);
        storeOffers(realOffers);
        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        state = State.READY;
        MyApplication.getInstance().getBus().post(new DatabaseUpgradedEvent());
      }
    }.execute();
  }

  public State getState() {
    return state;
  }

  public static final class StoreResult {
    private final boolean success;
    private final List<Offer> offersSaved;
    private final List<Offer> newOffers;

    public StoreResult(boolean success, List<Offer> offersSaved, List<Offer> newOffers) {
      this.success = success;
      this.offersSaved = offersSaved;
      this.newOffers = newOffers;
    }

    public boolean isSuccess() {
      return success;
    }

    public List<Offer> getOffersSaved() {
      return offersSaved;
    }

    public List<Offer> getNewOffers() {
      return newOffers;
    }
  }
}
