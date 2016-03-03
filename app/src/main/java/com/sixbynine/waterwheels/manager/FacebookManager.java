package com.sixbynine.waterwheels.manager;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.sixbynine.waterwheels.BackgroundPollService;
import com.sixbynine.waterwheels.MyApplication;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.data.OfferDbManager;
import com.sixbynine.waterwheels.events.DatabaseUpgradedEvent;
import com.sixbynine.waterwheels.events.FeedRequestFinishedEvent;
import com.sixbynine.waterwheels.events.LoginSuccessEvent;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.model.Post;
import com.sixbynine.waterwheels.util.Keys;
import com.sixbynine.waterwheels.util.Logger;
import com.sixbynine.waterwheels.util.Prefs;
import com.sixbynine.waterwheels.util.Utils;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class FacebookManager implements FacebookCallback<LoginResult> {

    private static final FacebookManager INSTANCE = new FacebookManager();
    private static final String CARPOOL_GROUP_ID = "372772186164295";

    private boolean makingGroupRequest;
    private Status status = Status.INITIALIZED;
    private boolean waitingForDb;

    private FacebookManager() {
        MyApplication.getInstance().getBus().register(this);
        makeGroupPostsRequest(false);
    }

    public static FacebookManager getInstance() {
        return INSTANCE;
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

    public void refreshGroupPostsIfNecessary() {
        makeGroupPostsRequest(false);
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

        if (getBackgroundJobStatus() == BackgroundJobStatus.NONE) {
            scheduleJob(false);
        }

        final long queryTime = System.currentTimeMillis();
        long lastWeek = queryTime - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
        long lastUpdateTime = Math.max(Prefs.getLong(Keys.LAST_UPDATED), lastWeek);

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
                            Logger.d(graphResponse.getJSONObject().toString());
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
                                    List<Offer> offers = FeedManager.extractOffers(feed);
                                    Logger.d("Posts contained %d offers", offers.size());
                                    OfferDbManager.getInstance().storeOffers(offers);
                                    Prefs.putLong(Keys.LAST_UPDATED, queryTime);
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
                parameters.putString("fields", "created_time,from,id,message");
                parameters.putString("limit", "2000");
                parameters.putString("since", String.valueOf(TimeUnit.SECONDS.convert(lastUpdateTime, TimeUnit.MILLISECONDS)));
                request.setParameters(parameters);
                request.executeAsync();
            }
        } else {
            Logger.d("Updated within last half hour");
            status = Status.LOADED;
            MyApplication.getInstance().getBus().post(new FeedRequestFinishedEvent());
        }
    }

    public enum BackgroundJobStatus {
        NONE, WIFI_ONLY, ANY
    }

    public BackgroundJobStatus getBackgroundJobStatus() {
        return BackgroundJobStatus.valueOf(Prefs.getString(Keys.BACKGROUND_JOB, BackgroundJobStatus.NONE.name()));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void scheduleJob(boolean wifiOnly) {
        Context context = MyApplication.getInstance();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();

        ComponentName serviceEndpoint = new ComponentName(context, BackgroundPollService.class);
        JobInfo backgroundPoll = new JobInfo.Builder(1, serviceEndpoint)
                .setRequiredNetworkType(wifiOnly ? JobInfo.NETWORK_TYPE_UNMETERED : JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(TimeUnit.MILLISECONDS.convert(14, TimeUnit.MINUTES))
                .setPersisted(true)
                .build();

        int code = jobScheduler.schedule(backgroundPoll);
        if (code <= 0) {
            Logger.e("Failed to schedule polling job: error %d", code);
        } else {
            Logger.d("Successfully scheduled background job");
            BackgroundJobStatus status = wifiOnly ? BackgroundJobStatus.WIFI_ONLY : BackgroundJobStatus.ANY;
            Prefs.putString(Keys.BACKGROUND_JOB, status.name());
        }
    }

    public Status getStatus() {
        return status;
    }

    public static String getProfilePicUrlFromId(String id) {
        long diameter = MyApplication.getInstance().getResources().getDimensionPixelOffset(R.dimen.photo_diameter_big);
        return "https://graph.facebook.com/" + id + "/picture?type=square&width=" + diameter +"&height=" + diameter;
    }
}
