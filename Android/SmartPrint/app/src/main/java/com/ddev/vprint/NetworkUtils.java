package com.ddev.vprint;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Danish Shah on 14/08/2016.
 */

public class NetworkUtils {

    public static boolean checkNetwork(Context cxt) {

        ConnectivityManager cm = (ConnectivityManager) cxt.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info != null && info.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }

}
