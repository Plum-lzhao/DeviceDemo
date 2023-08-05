package com.icbc.demo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.icbc.demo.utils.Logger;
import com.icbc.demo.utils.MyFunc;
import com.icbc.pad.deviceservice.aidl.ITestAidlInterface;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = TestActivity.class.getSimpleName();

    private static final String TEST_SERVICE_ACTION = "com.icbc.pad.test_service";
    private static final String DEVICE_SERVICE_PACKAGE_NAME = "com.icbc.pad.deviceservice";

    private ITestAidlInterface mTestService;     // 从 service 进程获取的实例

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bingService();

        initView();
    }

    private void initView() {
        Button read = (Button) findViewById(R.id.read);
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTestService != null) {
                    try {
                        byte[] data = new byte[3]; // {0, 0, 0}
                        mTestService.modifyBuffer(data);
                        Logger.address(data);
                        Logger.d(TAG, Logger.D_FLAG_DEBUG, "data:[" + MyFunc.ByteArrToHex(data, data.length) + "]");
                        byte[] buffer = new byte[512];
                        int size = mTestService.read(buffer);
                        Logger.address(buffer);
                        Logger.d(TAG, Logger.D_FLAG_DEBUG, "buffer:[" + MyFunc.ByteArrToHex(buffer, size) + "]");
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

   /**
    * 根据包名绑定 service
    */
//    private void bingService() {
//        Intent intent = new Intent();
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        ComponentName comp = new ComponentName("com.icbc.demo","com.icbc.demo.test.TestService");
//        intent.setComponent(comp);
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//    }

    /**
     * 根据 action 绑定 service
     */
    private void bingService() {
        Intent intent = new Intent();
        intent.setAction(TEST_SERVICE_ACTION);
        intent.setPackage(DEVICE_SERVICE_PACKAGE_NAME);    // Android 5.0 开始不再支持隐式启动 service 需要写成包名
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 停止/解绑 service
     */
    private void stopService() {
        unbindService(serviceConnection);
    }

    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mTestService = ITestAidlInterface.Stub.asInterface(iBinder);
            Log.d(TAG, "onServiceConnected()");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // 异常的时候才会回调
            Log.d(TAG, "onServiceDisconnected()");
            mTestService = null;
        }
    };
}
