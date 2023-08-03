// IDeviceInfo.aidl
package com.icbc.pad.deviceservice.aidl;

// Declare any non-default types here with import statements

interface IDeviceInfo {
    String getSerialNo();
    String getIMSI();
    String getIMEI();
    String getManufacture();
    String getModel();
}