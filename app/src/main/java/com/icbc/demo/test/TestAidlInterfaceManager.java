package com.icbc.demo.test;

import android.os.RemoteException;

import com.icbc.demo.utils.Logger;
import com.icbc.demo.utils.MyFunc;
import com.icbc.pad.deviceservice.aidl.ITestAidlInterface;

public class TestAidlInterfaceManager extends ITestAidlInterface.Stub {
    private static final String TAG = TestAidlInterfaceManager.class.getSimpleName();
    @Override
    public void modifyBuffer(byte[] buffer) throws RemoteException {
        Logger.address(buffer);
        // 在这里修改缓冲区
        for (int i=0; i<buffer.length; i++) {
            buffer[i] = (byte) (buffer[i] + 1);
        }
        Logger.d(TAG, "buffer:[" + MyFunc.ByteArrToHex(buffer, buffer.length) + "]");
    }

    @Override
    public int read(byte[] buffer) throws RemoteException {
        Logger.address(buffer);
        // 在这里修改缓冲区
        for (int i=0; i<buffer.length; i++) {
            buffer[i] = (byte) (buffer[i] + 1);
        }
        Logger.d(TAG, "buffer:[" + MyFunc.ByteArrToHex(buffer, buffer.length) + "]");
        return buffer.length;
    }
}
