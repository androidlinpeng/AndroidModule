package com.okhttp.utils;

import android.util.Log;


public class LogUtils {
    private static boolean debug = true;

    public static void i(String tag,String msg) {
        if (debug) {
            Log.i(tag, msg);
        }
    }

    public static void e(String tag,String msg) {
        if (debug) {
            Log.e(tag, msg);
        }
    }

}

