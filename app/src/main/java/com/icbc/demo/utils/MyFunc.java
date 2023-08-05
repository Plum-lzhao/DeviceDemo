package com.icbc.demo.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.icbc.global.GRes;

import java.lang.reflect.Method;

/**
 * @author
 *数据转换工具
 */
public class MyFunc {
	//-------------------------------------------------------
	// 判断奇数或偶数，位运算，最后一位是 1 则为奇数，为 0 是偶数
	public static int isOdd(int num) {
		return num & 0x1;
	}
	//-------------------------------------------------------
	public static int HexToInt(String inHex) {    // Hex 字符串转 int
		return Integer.parseInt(inHex, 16);
	}
	//-------------------------------------------------------
	public static byte HexToByte(String inHex) {    // Hex 字符串转 byte
		return (byte)Integer.parseInt(inHex,16);
	}
	//-------------------------------------------------------
	public static String Byte2Hex(Byte inByte) {    // 1 字节转 2 个 Hex 字符
		return String.format("%02x", inByte).toUpperCase();
	}
	//-------------------------------------------------------
	public static String ByteArrToHex(byte[] inBytArr) {    // 字节数组转 hex 字符串
		StringBuilder strBuilder = new StringBuilder();
		int j = inBytArr.length;
		for (int i=0; i<j; i++) {
			strBuilder.append(Byte2Hex(inBytArr[i]));
			strBuilder.append(" ");
		}
		return strBuilder.toString();
	}
	//-------------------------------------------------------
	public static String ByteArrToHex(byte[] inBytArr, int size) {    // 字节数组转 hex 字符串
		StringBuilder strBuilder = new StringBuilder();
		int j = size;
		for (int i = 0; i < j; i++) {
			strBuilder.append(Byte2Hex(inBytArr[i]));
			strBuilder.append(" ");
		}
		if (strBuilder.length() > 0) {
			strBuilder.deleteCharAt(strBuilder.length() - 1);
		}
		return strBuilder.toString();
	}
	//-------------------------------------------------------
	public static String ByteArrToHex(byte[] inBytArr, int offset, int byteCount) {    // 字节数组转 hex 字符串，可选长度
		StringBuilder strBuilder = new StringBuilder();
		int j = byteCount;
		for (int i=offset; i<j; i++) {
			strBuilder.append(Byte2Hex(inBytArr[i]));
		}
		return strBuilder.toString();
	}
	//-------------------------------------------------------
	//转hex字符串转字节数组
	public static byte[] HexToByteArr(String inHex) {    // hex 字符串转字节数组
		int hexlen = inHex.length();
		byte[] result;
		if (isOdd(hexlen) == 1) {    // 奇数
			hexlen++;
			result = new byte[(hexlen/2)];
			inHex="0"+inHex;
		} else {//偶数
			result = new byte[(hexlen/2)];
		}
		int j = 0;
		for (int i=0; i<hexlen; i+=2) {
			result[j] = HexToByte(inHex.substring(i, i+2));
			j++;
		}
		return result;
	}
	//-------------------------------------------------------
	public static boolean isNullOrEmpty(byte[] array) {
		if (array == null || array.length == 0) {
			return true;
		}
		for (byte b : array) {
			if (b != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 反射获取 getSubscriberId ，既imsi
	 *
	 * @param subId
	 * @return
	 */
	public static String getSubscriberId(int subId) {
		TelephonyManager telephonyManager = (TelephonyManager) GRes.getContext().getSystemService(Context.TELEPHONY_SERVICE);// 取得相关系统服务
		Class<?> telephonyManagerClass = null;
		String imsi = null;
		try {
			telephonyManagerClass = Class.forName("android.telephony.TelephonyManager");

			if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
				Method method = telephonyManagerClass.getMethod("getSubscriberId", int.class);
				imsi = (String) method.invoke(telephonyManager, subId);
			} else if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.LOLLIPOP) {
				Method method = telephonyManagerClass.getMethod("getSubscriberId", long.class);
				imsi = (String) method.invoke(telephonyManager, (long) subId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.i("getSubscriberId", "IMSI==" + imsi);
		return imsi;
	}

	/**
	 * 反射获取 getSubscriptionId ，既 subid
	 *
	 * @param slotId 卡槽位置（0，1）
	 * @return
	 */
	public static int getSubscriptionId(int slotId) {
		try {
			Method datamethod;
			int setsubid = -1;//定义要设置为默认数据网络的subid
			//获取默认数据网络subid   getDefaultDataSubId
			Class<?> SubscriptionManager = Class.forName("android.telephony.SubscriptionManager");
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) { // >= 24  7.0
				datamethod = SubscriptionManager.getDeclaredMethod("getDefaultDataSubscriptionId");
			} else {
				datamethod = SubscriptionManager.getDeclaredMethod("getDefaultDataSubId");
			}
			datamethod.setAccessible(true);
			int SubId = (int) datamethod.invoke(SubscriptionManager);


			Method subManagermethod = SubscriptionManager.getDeclaredMethod("from", Context.class);
			subManagermethod.setAccessible(true);
			Object subManager = subManagermethod.invoke(SubscriptionManager, GRes.getContext());

			//getActiveSubscriptionInfoForSimSlotIndex  //获取卡槽0或者卡槽1  可用的subid
			Method getActivemethod = SubscriptionManager.getDeclaredMethod("getActiveSubscriptionInfoForSimSlotIndex", int.class);
			getActivemethod.setAccessible(true);
			Object msubInfo = getActivemethod.invoke(subManager, slotId);  //getSubscriptionId

			Class<?> SubInfo = Class.forName("android.telephony.SubscriptionInfo");

			//slot0   获取卡槽0的subid
			int subid = -1;
			if (msubInfo != null) {
				Method getSubId0 = SubInfo.getMethod("getSubscriptionId");
				getSubId0.setAccessible(true);
				subid = (int) getSubId0.invoke(msubInfo);
			}
			Log.i("getSubscriptionId", "slotId=" + slotId + ", subid=" + subid);
			return subid;
		} catch (Exception e) {
			Log.e("getSubscriptionId", e.getLocalizedMessage());
		}
		return -1;
	}

	/**
	 * 获取运营商 IMSI
	 * 默认为 IMEI1对应的 IMSI
	 *
	 * @return
	 */
	public static String getSimOperator() {
		TelephonyManager telephonyManager = (TelephonyManager) GRes.getContext().getSystemService(Context.TELEPHONY_SERVICE);// 取得相关系统服务
		return telephonyManager.getSimOperator();
	}

	/**
	 * 根据卡槽位置 获取运营商 IMSI
	 *
	 * @param slotId 卡槽位置（0，1）
	 * @return
	 */
	public static String getSimOperator(int slotId) {
		int subid = getSubscriptionId(slotId);
		if (subid == -1) {
			return null;
		}

		String imsi = getSubscriberId(subid);
		if (!TextUtils.isEmpty(imsi)) {
			return imsi;
		}

		return null;
	}

	/**
	 * 通过卡槽位置拿 IMEI
	 *
	 * @param slotId (0, 1卡槽位置）
	 * @return
	 */
	public static String getImei(int slotId) {
		if (slotId != 0 && slotId != 1) {
			return null;
		}

		TelephonyManager tm = (TelephonyManager) GRes.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			return tm.getDeviceId(slotId);

		} else if (slotId == 0){
			return tm.getDeviceId();

		} else {
			TelephonyManager telephonyManager = (TelephonyManager) GRes.getContext().getSystemService(Context.TELEPHONY_SERVICE);// 取得相关系统服务
			Class<?> telephonyManagerClass = null;
			String imei = null;

			try {
				telephonyManagerClass = Class.forName("android.telephony.TelephonyManager");
				Method method = telephonyManagerClass.getMethod("getImei", int.class);
				imei = (String) method.invoke(telephonyManager, slotId);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Log.i("getImei", "imei==" + imei);

			return imei;
		}
	}
}