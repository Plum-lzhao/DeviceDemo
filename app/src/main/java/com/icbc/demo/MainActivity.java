package com.icbc.demo;

import static com.icbc.demo.utils.MyFunc.getSimOperator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.icbc.demo.utils.Logger;
import com.icbc.demo.utils.MyFunc;
import com.icbc.pad.deviceservice.aidl.DeviceListener;
import com.icbc.pad.deviceservice.aidl.IDeviceService;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ICBCDemo";
    private static final String DEVICE_SERVICE_ACTION = "com.icbc.pad.device_service";
    private static final String DEVICE_SERVICE_PACKAGE_NAME = "com.icbc.pad.deviceservice";
    private IDeviceService mDeviceService;     // 从 service 进程获取的实例
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0;
    private static final String[] REQUIRED_STORAGE_PERMISSIONS = new String[] {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private final String portKey = "devname";
    private final String portValue = "/dev/ttyS7";
    private Bundle mBundle = new Bundle();
    private static final int BPS_9600    = 0x04;
    private static final int BPS_115200  = 0x09;
    private static final int PAR_NOPAR   = 'N';
    private static final int PAR_EVEN    = 'E';
    private static final int PAR_ODD     = 'O';

    private static final int DBS_7       = 0x07;
    private static final int DBS_8       = 0x08;

    private static final String systemTime = "1995-09-05 10:07:01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService();

        initView();

        requestPermission();
    }

    private void initView() {
        TextView cashBoxStatus = (TextView) findViewById(R.id.cashBoxStatus);

        Button read = (Button) findViewById(R.id.read);
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeviceService != null) {
                    try {
                        byte[] buffer = new byte[512];
                        int count = readSerial(buffer, 500);
//                        Logger.address(buffer);
                        if (count > 0) {
                            Logger.d(TAG, Logger.D_FLAG_SERIAL, "buffer:[" + MyFunc.ByteArrToHex(buffer, count) + "], count:[" + count + "]");
                        } else {
                            Logger.d(TAG, Logger.D_FLAG_SERIAL, "readCount:[" + count + "]");
                        }
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d(TAG, "onClick() mDeviceService is null");
                }
            }
        });

        Button write = (Button) findViewById(R.id.write);
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeviceService != null) {
                    try {
                        byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
                        int count = writeSerial(data, 100);
//                        Logger.address(data);
                        if (count > 0) {
                            Logger.d(TAG, Logger.D_FLAG_SERIAL, "data:[" + MyFunc.ByteArrToHex(data, count) + "], count:[" + count + "]");
                        } else {
                            Logger.d(TAG, Logger.D_FLAG_SERIAL, "writeCount:[" + count + "]");
                        }
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d(TAG, "onClick() mDeviceService is null");
                }
            }
        });

        Button clean = (Button) findViewById(R.id.clean);
        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeviceService != null) {
                    try {
                        boolean result = clearInputBuffer();
                        Logger.d(TAG, Logger.D_FLAG_SERIAL, "clearInputBuffer() result:[" + result + "]");
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d(TAG, "onClick() mDeviceService is null");
                }
            }
        });

        Button input = (Button) findViewById(R.id.input);
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeviceService != null) {
                    try {
                        boolean result = isBufferEmpty(true);
                        Logger.d(TAG, Logger.D_FLAG_SERIAL, "isBufferEmpty(input) result:[" + result + "]");
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d(TAG, "onClick() mDeviceService is null");
                }
            }
        });

        Button output = (Button) findViewById(R.id.output);
        output.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeviceService != null) {
                    try {
                        boolean result = isBufferEmpty(false);
                        Logger.d(TAG, Logger.D_FLAG_SERIAL, "isBufferEmpty(output) result:[" + result + "]");
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d(TAG, "onClick() mDeviceService is null");
                }
            }
        });

        Button openCashBox = (Button) findViewById(R.id.openCashBox);
        openCashBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeviceService != null) {
                    try {
                        Log.d(TAG, "onServiceConnected: openCashBox:[start]");
                        openCashBox(new DeviceListener.Stub() {
                            @Override
                            public void callBack(String type, Bundle data) throws RemoteException {
                                Log.d(TAG, "DeviceListener: callBack() type:[" + type + "]");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        cashBoxStatus.setText(type);
                                        cashBoxStatus.setBackgroundColor(Color.GREEN);
                                        if (data != null) {
                                            Log.d(TAG, "DeviceListener: callBack() data errCode:[" + data.getString("errCode") + "]");
                                            Log.d(TAG, "DeviceListener: callBack() data errMsg:[" + data.getString("errMsg") + "]");
                                            cashBoxStatus.setBackgroundColor(Color.RED);
                                        }
                                    }
                                });
                            }
                        });
                        Log.d(TAG, "onServiceConnected: openCashBox:[end]");
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d(TAG, "onClick() mDeviceService is null");
                }
            }
        });
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
//        ComponentName comp = new ComponentName("com.icbc.pad", "com.icbc.pad.deviceservices");
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

    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDeviceService = IDeviceService.Stub.asInterface(iBinder);

            try {
                Log.d(TAG, "onServiceConnected() ************* DeviceInfo **************");
                Log.d(TAG, "onServiceConnected() SN:[" + getSerialNo() + "]");
                Log.d(TAG, "onServiceConnected() IMSI:[" + getIMSI() + "]");
                Log.d(TAG, "onServiceConnected() IMEI:[" + getIMEI() + "]");
                Log.d(TAG, "onServiceConnected() Manufacture:[" + getManufacture() + "]");
                Log.d(TAG, "onServiceConnected() Model:[" + getModel() + "]");
                Log.d(TAG, "onServiceConnected() ************* DeviceInfo **************");


//                Log.d(TAG, "onServiceConnected() ************* DemoDeviceInfo **************");
//                Log.d(TAG, "onServiceConnected() IMSI(0):[" + MyFunc.getSubscriberId(0) + "]");
//                Log.d(TAG, "onServiceConnected() IMSI(1):[" + MyFunc.getSubscriberId(1) + "]");
//                Log.d(TAG, "onServiceConnected() IMSI:[" + MyFunc.getSimOperator() + "]");
//                Log.d(TAG, "onServiceConnected() IMSI(0):[" + MyFunc.getSimOperator(0) + "]");
//                Log.d(TAG, "onServiceConnected() IMSI(1):[" + MyFunc.getSimOperator(1) + "]");
//                Log.d(TAG, "onServiceConnected() IMEI(0):[" + MyFunc.getImei(0) + "]");
//                Log.d(TAG, "onServiceConnected() IMEI(1):[" + MyFunc.getImei(1) + "]");
//                Log.d(TAG, "onServiceConnected() ************* DemoDeviceInfo **************");


                Log.d(TAG, "onServiceConnected() ************* Serial **************");
                Log.d(TAG, "onServiceConnected() init Bundle:[" + portKey + ", " + portValue + "]");
                mBundle.putString(portKey, portValue);
                Log.d(TAG, "onServiceConnected() init Serial:[" + initSerial() + "]");
                Log.d(TAG, "onServiceConnected() open Serial:[" + openSerial() + "]");
//                Log.d(TAG, "onServiceConnected() closeSerial:[" + closeSerial() + "]");
                Log.d(TAG, "onServiceConnected() ************* Serial **************");


                Log.d(TAG, "onServiceConnected() ************* Command **************");
                mBundle.clear();
                Log.d(TAG, "onServiceConnected: getCmds:" + Arrays.toString(getCmds()));
                Log.d(TAG, "onServiceConnected: setSystemTime:[start (" + systemTime + ")]");
                setSystemTime(systemTime);
                Log.d(TAG, "onServiceConnected: setSystemTime:[end]");
//                Log.d(TAG, "onServiceConnected: openCashBox:[start]");
//                openCashBox(new DeviceListener.Stub() {
//                    @Override
//                    public void callBack(String type, Bundle data) throws RemoteException {
//                        Log.d(TAG, "DeviceListener: callBack() type:[" + type + "]");
//                        if (data != null) {
//                            Log.d(TAG, "DeviceListener: callBack() data errCode:[" + data.getString("errCode") + "]");
//                            Log.d(TAG, "DeviceListener: callBack() data errMsg:[" + data.getString("errMsg") + "]");
//                        }
//                    }
//                });
//                Log.d(TAG, "onServiceConnected: openCashBox:[end]");
                Log.d(TAG, "onServiceConnected() ************* Command **************");
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // 异常的时候才会回调
            Log.d(TAG, "onServiceDisconnected()");
            mDeviceService = null;
        }
    };

    public String getSerialNo() throws RemoteException {
        return mDeviceService.getDeviceInfo().getSerialNo();
    }

    public String getIMSI() throws RemoteException {
        return mDeviceService.getDeviceInfo().getIMSI();
    }

    public String getIMEI() throws RemoteException {
        return mDeviceService.getDeviceInfo().getIMEI();
    }

    public String getManufacture() throws RemoteException {
        return mDeviceService.getDeviceInfo().getManufacture();
    }

    public String getModel() throws RemoteException {
        return mDeviceService.getDeviceInfo().getModel();
    }

    public boolean initSerial() throws RemoteException {
        return mDeviceService.getSerialPortEx(mBundle).init(BPS_9600, PAR_NOPAR, DBS_8);
    }

    public boolean openSerial() throws RemoteException {
        return mDeviceService.getSerialPortEx(mBundle).open();
    }

    public boolean closeSerial() throws RemoteException {
        return mDeviceService.getSerialPortEx(mBundle).close();
    }

    public int readSerial(byte[] buffer, int timeout) throws RemoteException {
        return mDeviceService.getSerialPortEx(mBundle).read(buffer, timeout);
    }

    public int writeSerial(byte[] data, int timeout) throws RemoteException {
        return mDeviceService.getSerialPortEx(mBundle).write(data, timeout);
    }

    public boolean clearInputBuffer() throws RemoteException {
        return mDeviceService.getSerialPortEx(mBundle).clearInputBuffer();
    }

    public boolean isBufferEmpty(boolean input) throws RemoteException {
        return mDeviceService.getSerialPortEx(mBundle).isBufferEmpty(input);
    }

    public String[] getCmds() throws RemoteException {
        return mDeviceService.getExCmd().getCmds();
    }

    public void setSystemTime(String systemTime) throws RemoteException {
        if (TextUtils.isEmpty(systemTime)) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("currentDateTime", systemTime);
        Bundle commandBlock = mDeviceService.getExCmd().commandBlock("System.UpdateTime", bundle);
        Log.d(TAG, "setSystemTime() result:[" + commandBlock.getInt("result", 0) + "], desc:[" + commandBlock.getString("desc") + "]");
    }

    public void openCashBox(DeviceListener deviceListener) throws RemoteException {
        mDeviceService.getExCmd().commandNonBlock("CashBox.Open", null, deviceListener);
    }


    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUIRED_STORAGE_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_PHONE_STATE }, STORAGE_PERMISSIONS_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SET_TIME) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.SET_TIME }, STORAGE_PERMISSIONS_REQUEST_CODE);
        }
    }
}