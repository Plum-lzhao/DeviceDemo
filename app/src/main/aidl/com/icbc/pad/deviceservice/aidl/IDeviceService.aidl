// IDeviceService.aidl
package com.icbc.pad.deviceservice.aidl;

import com.icbc.pad.deviceservice.aidl.ISerialPort;
import com.icbc.pad.deviceservice.aidl.IDeviceInfo;
import com.icbc.pad.deviceservice.aidl.IPrinter;
import com.icbc.pad.deviceservice.aidl.ICommand;

// Declare any non-default types here with import statements

interface IDeviceService {
    ISerialPort getSerialPortEx(in Bundle params);
    IDeviceInfo getDeviceInfo();
    IPrinter getPrinter();
    ICommand getExCmd();
}