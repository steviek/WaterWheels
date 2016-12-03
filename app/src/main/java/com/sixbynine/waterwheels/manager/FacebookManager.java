package com.sixbynine.waterwheels.manager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sixbynine.waterwheels.BuildConfig;
import com.sixbynine.waterwheels.MyApplication;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.data.OfferDbManager;
import com.sixbynine.waterwheels.data.OfferDbManager.StoreResult;
import com.sixbynine.waterwheels.events.DatabaseUpgradedEvent;
import com.sixbynine.waterwheels.events.FeedRequestFinishedEvent;
import com.sixbynine.waterwheels.events.LoginSuccessEvent;
import com.sixbynine.waterwheels.filter.FilterFragmentState;
import com.sixbynine.waterwheels.main.MainActivity;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.model.Post;
import com.sixbynine.waterwheels.settings.NotificationStatus;
import com.sixbynine.waterwheels.util.Keys;
import com.sixbynine.waterwheels.util.Logger;
import com.sixbynine.waterwheels.util.Prefs;
import com.sixbynine.waterwheels.util.Utils;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class FacebookManager implements FacebookCallback<LoginResult> {

    private static FacebookManager instance;
    private static final String CARPOOL_GROUP_ID = "372772186164295";
    private static final boolean SINCE_BROKEN = true;

    private boolean makingGroupRequest;
    private Status status = Status.INITIALIZED;

    private FacebookManager() {
        MyApplication.getInstance().getBus().register(this);
        makeGroupPostsRequest(false);
    }

    public static FacebookManager getInstance() {
        if (instance == null) {
            synchronized (FacebookManager.class) {
                if (instance == null) {
                    instance = new FacebookManager();
                }
            }
        }
        return instance;
    }

    public enum Status {
        INITIALIZED,
        LOADING,
        LOADED,
        NOT_WATERLOO
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        MyApplication.getInstance().getBus().post(new LoginSuccessEvent(loginResult));
        makeGroupPostsRequest(false);
    }

    @Override
    public void onCancel() {
        Logger.d("Login canceled");
    }

    @Override
    public void onError(FacebookException e) {
        Logger.e(e);
    }

    public boolean isLoggedIn() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    public void refreshGroupPosts() {
        makeGroupPostsRequest(true);
    }

    @Subscribe
    public void onDatabaseUpgradedEvent(DatabaseUpgradedEvent event) {
        makeGroupPostsRequest(false);
    }

    private void makeGroupPostsRequest(boolean force) {
        Logger.d("makeGroupPosts, force = " + force);
        if (!Utils.isNetworkAvailable(MyApplication.getInstance())) {
            Logger.d("No internet");
            status = Status.LOADED;
            MyApplication.getInstance().getBus().post(new FeedRequestFinishedEvent());
            return;
        }

        status = Status.LOADING;

        if (OfferDbManager.getInstance().getState() == OfferDbManager.State.UPDGRADING) {
            return;
        }

        final long queryTime = System.currentTimeMillis();
        long lastWeek = queryTime - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
        final long lastUpdateTime = Math.max(Prefs.getLong(Keys.LAST_UPDATED), lastWeek);

        Logger.d("Last update time: " + lastUpdateTime);

        if (force || System.currentTimeMillis() - lastUpdateTime > TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES)) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            status = Status.LOADING;
            if (accessToken != null && !makingGroupRequest) {
                Logger.d("Making group request");
                makingGroupRequest = true;
                GraphRequest request = new GraphRequest(accessToken, "/" + CARPOOL_GROUP_ID + "/feed", null, HttpMethod.GET, new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        if (graphResponse.getError() == null) {
                            new AsyncTask<JSONObject, Void, Void>() {
                                @Override
                                protected Void doInBackground(JSONObject... params) {
                                    OfferDbManager.getInstance().clearStaleOffers();
                                    List<Post> feed = FacebookFunctions.GRAPH_RESPONSE_POST_LIST.apply(params[0]);
                                    Logger.d("Got back %d posts", feed.size());
                                    if (feed.isEmpty() && !Prefs.getBoolean(Keys.HAS_SUCCESSFUL_REQUESTS)) {
                                        status = FacebookManager.Status.NOT_WATERLOO;
                                        return null;
                                    } else {
                                        Prefs.putBoolean(Keys.HAS_SUCCESSFUL_REQUESTS, true);
                                    }
                                    List<Offer> offers = FeedManager.extractOffers(feed, TimeUnit.SECONDS.convert(lastUpdateTime, TimeUnit.MILLISECONDS));

                                    Logger.d("Posts contained %d offers", offers.size());
                                    StoreResult result = OfferDbManager.getInstance().storeOffers(offers);
                                    Prefs.putLong(Keys.LAST_UPDATED, queryTime);

                                    if (!MainActivity.isInForeground()) {
                                        Predicate<Offer> filter = FilterFragmentState.getState().getUngenerousPredicate();
                                        buildNotification(Iterables.filter(result.getNewOffers(), filter));
                                    }

                                    if (BuildConfig.DEBUG) {
                                        String updateLog = Prefs.getString(Keys.UPDATE_LOG, "");
                                        Prefs.putString(Keys.UPDATE_LOG, updateLog
                                                + new SimpleDateFormat("H:mm").format(queryTime) + "\n");
                                    }

                                    status = FacebookManager.Status.LOADED;
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void result) {
                                    super.onPostExecute(result);
                                    makingGroupRequest = false;
                                    MyApplication.getInstance().getBus().post(new FeedRequestFinishedEvent());
                                }
                            }.execute(graphResponse.getJSONObject());
                        } else {
                            Logger.e(graphResponse.getError().getException());
                            MyApplication.getInstance().getBus().post(new FeedRequestFinishedEvent());
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("date_format", "U");
                parameters.putString("fields", "created_time,from,id,message,updated_time");
                parameters.putString("limit", "2000");

                if (!SINCE_BROKEN) {
                    parameters.putString("since", String.valueOf(TimeUnit.SECONDS.convert(lastUpdateTime, TimeUnit.MILLISECONDS)));
                }

                request.setParameters(parameters);
                request.executeAsync();
            }
        } else {
            Logger.d("Updated within last half hour");
            status = Status.LOADED;
            MyApplication.getInstance().getBus().post(new FeedRequestFinishedEvent());
        }
    }

    public Status getStatus() {
        return status;
    }

    public static String getProfilePicUrlFromId(String id) {
        long diameter = MyApplication.getInstance().getResources().getDimensionPixelOffset(R.dimen.photo_diameter_big);
        return "https://graph.facebook.com/" + id + "/picture?type=square&width=" + diameter + "&height=" + diameter;
    }

    private static final SimpleDateFormat sdf24hrShort = new SimpleDateFormat("H:mm");
    private static final SimpleDateFormat sdf12hrShort = new SimpleDateFormat("h:mm a");

    private void buildNotification(Iterable<Offer> offers) {
        NotificationStatus notificationStatus = NotificationStatus.get();

        int size = Iterables.size(offers);

        if (size > 0 && notificationStatus.isEnabled()) {
            Context context = MyApplication.getInstance();
            boolean is24hr = DateFormat.is24HourFormat(context);

            Offer offer = Iterables.getFirst(offers, null);
            Date date = new Date(offer.getTime());
            String formattedTime = is24hr ? sdf24hrShort.format(date) : sdf12hrShort.format(date);

            String title = size == 1
                    ? context.getString(R.string.new_ride)
                    : context.getString(R.string.x_new_rides, size);

            String text = context.getString(
                    size == 1 ? R.string.offer_format : R.string.offer_format_multiple,
                    offer.getOrigin().getName(),
                    offer.getDestination().getName(),
                    formattedTime);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.getInstance())
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text);

            if (notificationStatus.shouldLight()) {
                builder.setLights(Color.YELLOW, 3000, 3000);
            }

            if (notificationStatus.shouldSound()) {
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
            }

            if (notificationStatus.shouldVibrate()) {
                builder.setVibrate(new long[] { 500, 1000 });
            }

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(Keys.SHOW_FILTER, true);

            if (size == 1) {
                intent.putExtra(Keys.SHOW_OFFER, offer);
            }

            PendingIntent pIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pIntent);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(1, builder.build());
        }
    }

    public void buildDebugNotification(Offer offer) {
        buildNotification(ImmutableList.of(offer));
    }
}
