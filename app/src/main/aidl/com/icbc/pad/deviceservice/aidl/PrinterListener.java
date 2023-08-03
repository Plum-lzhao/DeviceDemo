package com.icbc.pad.deviceservice.aidl;

interface PrinterListener {
 /**
  * 打印成功回调
  */
 void onFinish();
 /**
  * 打印失败回调
  * @param error - 错误码
  * <ul>
  * <li>ERROR_PAPERENDED(0xF0) - 缺纸， 不能打印</li>
  * <li>ERROR_HARDERR(0xF2) - 硬件错误</li>
  * <li>ERROR_OVERHEAT(0xF3) - 打印头过热</li>
  * <li>ERROR_BUFOVERFLOW(0xF5) - 缓冲模式下所操作的位置超出范围</li>
  * <li>ERROR_LOWVOL(0xE1) - 低压保护</li>
  * <li>ERROR_PAPERENDING(0xF4) - 纸张将要用尽， 还允许打印(单步进针打特有返回值)</li>
  * <li>ERROR_MOTORERR(0xFB) - 打印机芯故障(过快或者过慢)</li>
  * <li>ERROR_PENOFOUND(0xFC) - 自动定位没有找到对齐位置,纸张回到原来位置</li>
  * <li>ERROR_PAPERJAM(0xEE) - 卡纸</li>
  * <li>ERROR_NOBM(0xF6) - 没有找到黑标</li>
  * <li>ERROR_BUSY(0xF7) - 打印机处于忙状态</li>
  * <li>ERROR_BMBLACK(0xF8) - 黑标探测器检测到黑色信号</li>
  * <li>ERROR_WORKON(0xE6) - 打印机电源处于打开状态</li>
  * <li>ERROR_LIFTHEAD(0xE0) - 打印头抬起(自助热敏打印机特有返回值)</li>
  * <li>ERROR_CUTPOSITIONERR(0xE2) - 切纸刀不在原位(自助热敏打印机特有返回值)</li>
  * <li>ERROR_LOWTEMP(0xE3) - 低温保护或 AD 出错(自助热敏打印机特有返回值)</li>
  * </ul>
  */
 void onError(int error);
}
