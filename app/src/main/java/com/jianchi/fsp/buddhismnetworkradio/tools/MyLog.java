package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.util.Log;

/**
 * Created by fsp on 16-7-9.
 * Log.v() Log.d() Log.i() Log.w() 以及 Log.e() 。
 * 根据首字母对应VERBOSE，DEBUG,INFO, WARN，ERROR
 *
 public static final int VERBOSE = 2;
 public static final int DEBUG = 3;
 public static final int INFO = 4;
 public static final int WARN = 5;
 public static final int ERROR = 6;
 public static final int ASSERT = 7;
 */
public class MyLog {
    public static int logLevel = Log.ERROR;

    public static void i(String tag, String msg) {
        if (logLevel <= Log.INFO) {
            msg=msg==null?"":msg;
            android.util.Log.i(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (logLevel <= Log.ERROR) {
            msg=msg==null?"":msg;
            android.util.Log.e(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (logLevel <= Log.DEBUG) {
            msg=msg==null?"":msg;
            android.util.Log.d(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (logLevel <= Log.VERBOSE) {
            msg=msg==null?"":msg;
            android.util.Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (logLevel <= Log.WARN) {
            msg=msg==null?"":msg;
            android.util.Log.w(tag, msg);
        }
    }
}

