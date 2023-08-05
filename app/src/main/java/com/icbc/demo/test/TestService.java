package com.icbc.demo.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.icbc.pad.deviceservice.aidl.ITestAidlInterface;

public class TestService extends Service {
    private static final String TAG = TestService.class.getSimpleName();
    private final ITestAidlInterface.Stub mBinder = new TestAidlInterfaceManager();

    public TestService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() [" + intent + "]");
        return super.onStartCommand(intent, flags, startId);
    }
}