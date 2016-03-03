package com.sixbynine.waterwheels;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import com.sixbynine.waterwheels.events.FeedRequestFinishedEvent;
import com.sixbynine.waterwheels.manager.FacebookManager;
import com.sixbynine.waterwheels.util.Logger;
import com.squareup.otto.Subscribe;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class BackgroundPollService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        Logger.d("onStartJob background poll");
        MyApplication.getInstance().getBus().register(new Object() {
            @Subscribe
            public void onFeedRequestFinished(FeedRequestFinishedEvent event) {
                Logger.d("onJobFinished");
                MyApplication.getInstance().getBus().unregister(this);
                jobFinished(params, false);
            }
        });
        FacebookManager.getInstance().refreshGroupPostsIfNecessary();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }


}
