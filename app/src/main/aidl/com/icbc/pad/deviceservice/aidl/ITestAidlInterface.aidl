// ITestAidlInterface.aidl
package com.icbc.pad.deviceservice.aidl;

// Declare any non-default types here with import statements

interface ITestAidlInterface {
    void modifyBuffer(inout byte[] buffer);
    int read(inout byte[] buffer);
}