package com.sixbynine.waterwheels.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sixbynine.waterwheels.BaseFragment;
import com.sixbynine.waterwheels.BuildConfig;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.autorefresh.AutoRefreshManager;
import com.sixbynine.waterwheels.util.Keys;
import com.sixbynine.waterwheels.util.Prefs;
import com.sixbynine.waterwheels.view.CheckableTextView;

public final class SettingsFragment extends BaseFragment {

    private CheckableTextView mRefresh;
    private CheckableTextView mWifiOnly;
    private TextView mDebugText;
    private boolean mSyncingViews;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mRefresh = (CheckableTextView) view.findViewById(R.id.refresh_posts);
        mWifiOnly = (CheckableTextView) view.findViewById(R.id.wifi_only);

        mRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mSyncingViews) {
                    if (isChecked) {
                        AutoRefreshManager.scheduleJob(mWifiOnly.isChecked());
                    } else {
                        AutoRefreshManager.cancelJob(mWifiOnly.isChecked());
                    }
                }
            }
        });

        mWifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mSyncingViews) {
                    if (mRefresh.isChecked()) {
                        AutoRefreshManager.scheduleJob(isChecked);
                    } else {
                        AutoRefreshManager.cancelJob(isChecked);
                    }
                }
            }
        });

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

    private void syncViews() {
        mSyncingViews = true;
        switch (AutoRefreshManager.getBackgroundJobStatus()) {
            case WIFI_ONLY:
                mRefresh.setChecked(true);
                mWifiOnly.setChecked(true);
                break;
            case ANY:
                mRefresh.setChecked(true);
                mWifiOnly.setChecked(false);
                break;
            case NONE:
                mRefresh.setChecked(false);
                mWifiOnly.setChecked(false);
                break;
            case NONE_WIFI_ONLY:
                mRefresh.setChecked(false);
                mWifiOnly.setChecked(true);
                break;
        }
        if (BuildConfig.DEBUG) {
            mDebugText.setText(Prefs.getString(Keys.UPDATE_LOG));
        }
        mSyncingViews = false;
    }
}
