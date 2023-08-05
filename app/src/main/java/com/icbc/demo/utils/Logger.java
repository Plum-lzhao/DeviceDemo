package com.icbc.demo.utils;

import android.os.SystemProperties;
import android.util.Log;

public class Logger {
    private static final String TAG = "DeviceService";
    public static final boolean DEBUG = true;
    public final static int D_FLAG_DEVICE_INFO    = 0x01;
    public final static int D_FLAG_COMMAND        = 0x02;
    public final static int D_FLAG_SERIAL         = 0X04;
    public final static int D_FLAG_DEBUG          = 0x08;
    private final static int mEnableLogFlags = SystemProperties.getInt("persist.deviceservice.debug_level",
//            D_FLAG_DEVICE_INFO |
//            D_FLAG_COMMAND |
            D_FLAG_SERIAL |
            D_FLAG_DEBUG);

    public static void d(String msg) {
        Log.d(TAG, msg);
        LogWriteUtil.getInstance().logNormal(msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
        LogWriteUtil.getInstance().logNormal(msg);
    }

    public static void d(String tag, int flag, String msg) {
        if ((mEnableLogFlags & flag) != 0) {
            Log.d(tag, msg);
            LogWriteUtil.getInstance().logNormal(msg);
        }
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
        LogWriteUtil.getInstance().logNormal(msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
        LogWriteUtil.getInstance().logNormal(msg);
    }

    public static void address(Object obj) {
        int address = System.identityHashCode(obj);
        d("Object Address:[" + Integer.toHexString(address) + "]");
    }
}
