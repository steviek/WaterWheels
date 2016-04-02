package com.sixbynine.waterwheels.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Returns a minus the largest substring in b
     */
    public static String removeLargestPrefix(String a, CharSequence b) {
        for (int i = 0; i < a.length() && i < b.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return a.substring(i);
            }
        }
        return a;
    }

    /**
     * Returns the logical OR of the boolean clauses.  This method can be used with statements are parameters to ensure
     * that all statements are executed, which is not guaranteed inline due to Java short-circuit logic.
     */
    public static boolean or(boolean b1) {
        return or(b1, false);
    }

    /**
     * Returns the logical OR of the boolean clauses.  This method can be used with statements are parameters to ensure
     * that all statements are executed, which is not guaranteed inline due to Java short-circuit logic.
     */
    public static boolean or(boolean b1, boolean b2) {
        return or(b1, b2, false);
    }

    /**
     * Returns the logical OR of the boolean clauses.  This method can be used with statements are parameters to ensure
     * that all statements are executed, which is not guaranteed inline due to Java short-circuit logic.
     */
    public static boolean or(boolean b1, boolean b2, boolean b3) {
        return or(b1, b2, b3, false);
    }

    /**
     * Returns the logical OR of the boolean clauses.  This method can be used with statements are parameters to ensure
     * that all statements are executed, which is not guaranteed inline due to Java short-circuit logic.
     */
    public static boolean or(boolean b1, boolean b2, boolean b3, boolean b4) {
        return b1 || b2 || b3 || b4;
    }
}
