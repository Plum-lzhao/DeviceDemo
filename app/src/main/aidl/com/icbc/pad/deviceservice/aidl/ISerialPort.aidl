// ISerialPort.aidl
package com.icbc.pad.deviceservice.aidl;

// Declare any non-default types here with import statements

interface ISerialPort {
    boolean open();
    boolean close();
    boolean init(int bps, int par, int dbs);
    int read(out byte[] buffer, int timeout);
    int write(in byte[] data, int timeout);
    boolean clearInputBuffer();
    boolean isBufferEmpty(boolean input);
}