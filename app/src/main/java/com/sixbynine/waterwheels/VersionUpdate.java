package com.sixbynine.waterwheels;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;

import com.sixbynine.waterwheels.util.Keys;
import com.sixbynine.waterwheels.util.Prefs;

public enum VersionUpdate {
    THREE(3, R.string.new_features_version_3);

    private final int version;
    private final int text;

    VersionUpdate(int version, int text) {
        this.version = version;
        this.text = text;
    }

    public int getVersion() {
        return version;
    }

    public static void showDialogIfAppropriate(Activity activity) {
        int oldVersion = Prefs.getInt(Keys.APP_VERSION, 2);
        int versionCode;

        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (oldVersion < versionCode) {
            int maxVersion = 0;
            int maxVersionText = 0;
            for (VersionUpdate update : values()) {
                if (update.version > oldVersion && update.version > maxVersion) {
                    maxVersion = update.version;
                    maxVersionText = update.text;
                }
            }

            if (maxVersion > 0) {
                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle(R.string.new_version);
                alertDialog.setMessage(activity.getString(maxVersionText));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }

        Prefs.putInt(Keys.APP_VERSION, versionCode);
    }
}
