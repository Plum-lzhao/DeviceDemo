package com.icbc.global;

import android.app.Application;
import android.content.Context;

public class GRes extends Application {

    private static final String TAG = GRes.class.getSimpleName();
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
