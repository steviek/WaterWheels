package com.sixbynine.waterwheels.autorefresh;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.sixbynine.waterwheels.MyApplication;
import com.sixbynine.waterwheels.util.Keys;
import com.sixbynine.waterwheels.util.Logger;
import com.sixbynine.waterwheels.util.Prefs;

import java.util.concurrent.TimeUnit;

public class AutoRefreshManager {

    public static AutoRefreshStatus getBackgroundJobStatus() {
        return AutoRefreshStatus.valueOf(Prefs.getString(Keys.BACKGROUND_JOB, AutoRefreshStatus.NONE.name()));
    }

    public static void scheduleJob(boolean wifiOnly) {
        Context context = MyApplication.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancelAll();

            ComponentName serviceEndpoint = new ComponentName(context, AutoRefreshJobService.class);
            JobInfo backgroundPoll = new JobInfo.Builder(1, serviceEndpoint)
                    .setRequiredNetworkType(wifiOnly ? JobInfo.NETWORK_TYPE_UNMETERED : JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES))
                    .setPersisted(true)
                    .build();

            int code = jobScheduler.schedule(backgroundPoll);
            if (code <= 0) {
                Logger.e("Failed to schedule polling job: error %d", code);
            }
        } else {
            ComponentName receiver = new ComponentName(context, AutoRefreshBootReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            Intent intent = new Intent(context, AutoRefreshLegacyReceiver.class);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_HALF_HOUR, pIntent);
        }
        Logger.d("Successfully scheduled background job");
        AutoRefreshStatus status = wifiOnly ? AutoRefreshStatus.WIFI_ONLY : AutoRefreshStatus.ANY;
        Prefs.putString(Keys.BACKGROUND_JOB, status.name());
    }

    public static void cancelJob(boolean wifiOnly) {
        Context context = MyApplication.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancelAll();
        } else {
            Intent intent = new Intent(context, AutoRefreshLegacyReceiver.class);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pIntent);

            ComponentName receiver = new ComponentName(context, AutoRefreshBootReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
        AutoRefreshStatus status = wifiOnly ? AutoRefreshStatus.NONE_WIFI_ONLY : AutoRefreshStatus.NONE;
        Prefs.putString(Keys.BACKGROUND_JOB, status.name());
        Logger.d("Canceled the job");
    }
}
