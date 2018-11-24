package com.steve.runintest.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by steve on 2/4/18.
 */

public class AppCrashHandler extends AppCrashLog{
    private static AppCrashHandler mCrashHandler = null;

    private AppCrashHandler(){}

    public static AppCrashHandler getInstance() {
        if(mCrashHandler == null) {mCrashHandler = new AppCrashHandler();}
        return mCrashHandler;
    }
    @Override
    public void initParams() {
        Log.e("************", "initParams");
        AppCrashLog.CACHE_LOG = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"log";
    }
}
