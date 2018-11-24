package com.steve.runintest;

import android.app.Application;

import com.steve.runintest.utils.AppCrashHandler;
import com.steve.runintest.utils.BitGL;

/**
 * Created by steve on 2/4/18.
 */

public class RunInApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BitGL.init(this.getResources());
//        AppCrashHandler.getInstance().init(this);
    }
}
