// IPrinter.aidl
package com.icbc.pad.deviceservice.aidl;
//import com.icbc.pad.deviceservice.aidl.DeviceListener;

// Declare any non-default types here with import statements

interface IPrinter {
    int getStatus();
    void setGray(int gray);
    void addText(in Bundle format, String text);
    void addBarCode(in Bundle format, String barcode);
    void addQrCode(in Bundle format, String qrCode);
    void addImage(in Bundle format, in byte[] imageData);
    void feedLine(int lines);
//    void startPrint(PrinterListener listener);
//    void startSaveCachePrint(PrinterListener listener);
    Bundle getPrinterStyle();
}