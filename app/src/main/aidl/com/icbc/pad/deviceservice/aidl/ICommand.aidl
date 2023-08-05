// ICommand.aidl
package com.icbc.pad.deviceservice.aidl;

import com.icbc.pad.deviceservice.aidl.DeviceListener;

// Declare any non-default types here with import statements

interface ICommand {
    String[] getCmds();
    Bundle commandBlock(String cmd, in Bundle params);
    void commandNonBlock(String cmd, in Bundle params, DeviceListener listener);
}