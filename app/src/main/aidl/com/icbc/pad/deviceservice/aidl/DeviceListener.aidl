// DeviceListener.aidl
package com.icbc.pad.deviceservice.aidl;

// Declare any non-default types here with import statements

interface DeviceListener {
 /**
  * 回调方法
  * @param type - 回调类型， 用于标识本次回来的类型
  * @param data - 回调输出数据， 根据具体命令及回调类型会有不同的定义
  */
 void callBack(String type, inout Bundle data);
}