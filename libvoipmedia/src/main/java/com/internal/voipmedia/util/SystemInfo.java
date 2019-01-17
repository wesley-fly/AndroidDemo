package com.internal.voipmedia.util;

import android.os.Build;

/**
 * 获得软件的一些基本信息如版本等信息和手机当前的语言等信息
 */
public class SystemInfo {

	public static String getVersionType() {
		return "0"; // 公网版本
		// return "1"; //正文订制版
	}

	/**
	 * 获取当前的手机的操作系统
	 * 
	 * @return
	 */
	public static String getOsName() {
		return "android";
	}

	/**
	 * 获得当前手机的系统版本
	 * 
	 * @return
	 */
	public static String getOsVersion() {
		return Build.VERSION.RELEASE;
	}

	/**
	 * 获取手机的制造商
	 * 
	 * @return
	 */
	public static String getManufacturer() {
		return Build.MANUFACTURER;
	}

	/** The end-user-visible name for the end product. */
	public static String getDeviceModel() {
		return Build.MODEL;
	}

	public static String getCpuArch() {
		return "arm";
	}

	public static String getClientName() {
		return "mobile";
	}

	/**
	 * 是否支持调用startForeground方法
	 * <p>
	 * 由于android系统升级导致调用该方法系统会提示程序为恶意应用
	 * 
	 * @return 如果支持返回true，其他返回false
	 */
	public static boolean isSupportStartForeground() {
		return !(Build.VERSION.SDK_INT > 17);
	}

}
