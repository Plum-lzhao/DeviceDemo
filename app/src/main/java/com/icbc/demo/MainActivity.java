package com.icbc.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.icbc.pad.deviceservice.aidl.IDeviceInfo;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ICBCDemo";
    private static final String DEVICE_SERVICE_ACTION = "com.icbc.pad.device_service";
    private static final String DEVICE_SERVICE_PACKAGE_NAME = "com.icbc.pad";
    private IDeviceInfo mDeviceInfoService;     // 从 service 进程获取的实例
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0;
    private static final String[] REQUIRED_STORAGE_PERMISSIONS = new String[] {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        startService();

    }

    /**
     * 根据 action 绑定 service
     */
    private void startService() {
        Intent intent = new Intent();
        intent.setAction(DEVICE_SERVICE_ACTION);
        intent.setPackage(DEVICE_SERVICE_PACKAGE_NAME);    // Android 5.0 开始不再支持隐式启动 service 需要写成包名
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 根据包名启动 service
     */
//    private void startService() {
//        Intent intent = new Intent();
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        ComponentName comp = new ComponentName("com.icbc.pad","com.icbc.pad.deviceservices");
//        intent.setComponent(comp);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        } else {
//            startService(intent);
//        }
//    }

    /**
     * 停止/解绑 service
     */
    private void stopService() {
        unbindService(serviceConnection);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDeviceInfoService = IDeviceInfo.Stub.asInterface(iBinder);

            try {
                Log.d(TAG, "onServiceConnected() SN:[" + getSerialNo() + "]");
//                Log.d(TAG, "onServiceConnected() IMSI:[" + getIMSI() + "]");
//                Log.d(TAG, "onServiceConnected() IMEI:[" + getIMEI() + "]");
                Log.d(TAG, "onServiceConnected() Manufacture:[" + getManufacture() + "]");
                Log.d(TAG, "onServiceConnected() Model:[" + getModel() + "]");
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // 异常的时候才会回调
            Log.d(TAG, "onServiceDisconnected()");
            mDeviceInfoService = null;
        }
    };

    public String getSerialNo() throws RemoteException {
        return mDeviceInfoService.getSerialNo();
    }

    public String getIMSI() throws RemoteException {
        return mDeviceInfoService.getIMSI();
    }

    public String getIMEI() throws RemoteException {
        return mDeviceInfoService.getIMEI();
    }

    public String getManufacture() throws RemoteException {
        return mDeviceInfoService.getManufacture();
    }

    public String getModel() throws RemoteException {
        return mDeviceInfoService.getModel();
    }

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUIRED_STORAGE_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_PHONE_STATE }, STORAGE_PERMISSIONS_REQUEST_CODE);
        }
    }
}