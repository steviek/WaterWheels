package com.sixbynine.waterwheels.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.sixbynine.waterwheels.BaseFragment;
import com.sixbynine.waterwheels.BuildConfig;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.autorefresh.AutoRefreshManager;
import com.sixbynine.waterwheels.autorefresh.AutoRefreshStatus;
import com.sixbynine.waterwheels.events.SettingsChangedEvent;
import com.sixbynine.waterwheels.util.Keys;
import com.sixbynine.waterwheels.util.Prefs;
import com.squareup.otto.Subscribe;

public final class SettingsFragment extends BaseFragment {

    private CompoundButton mRefresh;
    private CompoundButton mWifiOnly;
    private CompoundButton mNotifications;
    private CompoundButton mLight;
    private CompoundButton mSound;
    private CompoundButton mVibrate;
    private TextView mDebugText;
    private boolean mSyncingViews;

    private final OnCheckedChangeListener mRefreshCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!mSyncingViews) {
                if (mRefresh.isChecked()) {
                    AutoRefreshManager.scheduleJob(mWifiOnly.isChecked());
                } else {
                    AutoRefreshManager.cancelJob(mWifiOnly.isChecked());
                }
                syncViews();
            }
        }
    };

    private final OnCheckedChangeListener mNotificationsCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!mSyncingViews) {
                boolean enabled = mNotifications.isChecked();
                boolean light = mLight.isChecked();
                boolean sound = mSound.isChecked();
                boolean vibrate = mVibrate.isChecked();
                NotificationStatus.save(new NotificationStatus(enabled, light, sound, vibrate));
                syncViews();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mRefresh = (CompoundButton) view.findViewById(R.id.refresh_posts);
        mWifiOnly = (CompoundButton) view.findViewById(R.id.wifi_only);
        mNotifications = (CompoundButton) view.findViewById(R.id.notify_me_filter);
        mLight = (CompoundButton) view.findViewById(R.id.light);
        mSound = (CompoundButton) view.findViewById(R.id.sound);
        mVibrate = (CompoundButton) view.findViewById(R.id.vibrate);

        mRefresh.setOnCheckedChangeListener(mRefreshCheckedChangeListener);
        mWifiOnly.setOnCheckedChangeListener(mRefreshCheckedChangeListener);
        mNotifications.setOnCheckedChangeListener(mNotificationsCheckedChangeListener);
        mLight.setOnCheckedChangeListener(mNotificationsCheckedChangeListener);
        mSound.setOnCheckedChangeListener(mNotificationsCheckedChangeListener);
        mVibrate.setOnCheckedChangeListener(mNotificationsCheckedChangeListener);

        mDebugText = (TextView) view.findViewById(R.id.debug_text);
        if (BuildConfig.DEBUG) {
            mDebugText.setVisibility(View.VISIBLE);
        } else {
            mDebugText.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncViews();
    }

    @Subscribe
    public void onSettingsChanged(SettingsChangedEvent event) {
        syncViews();
    }

    private void syncViews() {
        if (getActivity() == null || mRefresh == null) {
            return;
        }

        if (NotificationChecker.checkThatNotificationCanBeSet(getActivity(), new NotificationChecker.OnFinishedListener() {
            @Override
            public void onFinished() {
                syncViews();
            }
        })) {
            return;
        }

        mSyncingViews = true;

        AutoRefreshStatus autoRefreshStatus = AutoRefreshManager.getBackgroundJobStatus();
        mRefresh.setChecked(autoRefreshStatus.isEnabled());
        mWifiOnly.setChecked(autoRefreshStatus.isWifiOnly());
        mWifiOnly.setEnabled(autoRefreshStatus.isEnabled());

        NotificationStatus notificationStatus = NotificationStatus.get();
        mNotifications.setChecked(notificationStatus.isEnabled());
        mLight.setChecked(notificationStatus.shouldLight());
        mSound.setChecked(notificationStatus.shouldSound());
        mVibrate.setChecked(notificationStatus.shouldVibrate());
        mLight.setEnabled(notificationStatus.isEnabled());
        mSound.setEnabled(notificationStatus.isEnabled());
        mVibrate.setEnabled(notificationStatus.isEnabled());

        if (BuildConfig.DEBUG) {
            mDebugText.setText(Prefs.getString(Keys.UPDATE_LOG));
        }
        
        mSyncingViews = false;
    }
}
