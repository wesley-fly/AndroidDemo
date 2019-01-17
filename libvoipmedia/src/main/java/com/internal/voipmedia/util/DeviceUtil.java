package com.internal.voipmedia.util;

import android.media.AudioManager;
import android.os.Build;

import com.internal.webrtc.adapter.AudioModeAdapterManager;

import java.util.Locale;

/**
 * 获取手机设备的信息及设备兼容设备
 */
public class DeviceUtil {

	public static final String TAG = DeviceUtil.class.getSimpleName();

	public static boolean isSupportStreamVoiceCall() {
		String mode = Build.MODEL.replaceAll(" +", "");
		Print.i(TAG, "Build.MODEL = " + mode);
		if (mode.equalsIgnoreCase("GM800") || mode.equalsIgnoreCase("ZTE-CN760") || mode.equalsIgnoreCase("ZTE-UV880")
				|| "5860A".equalsIgnoreCase(mode) || "5680A".equalsIgnoreCase(mode)
				|| "LenovoA765e".equalsIgnoreCase(mode) || "huaweig520-0000".equalsIgnoreCase(mode)) {
			return false;
		}
		return true;
	}

	public static boolean isMultiSimCardDevice() {
		return Build.MODEL.contains("XT800");
	}

	public static boolean isVoiceSpeakerOutDevice() {
		String device = SystemInfo.getDeviceModel();
		return (device.equalsIgnoreCase("SCH-i909") || device.equalsIgnoreCase("ZTE-U V880") || device
				.equalsIgnoreCase("GT-I5700"));
	}

	public static boolean isRingbackSpeakerOutDevice() {
		String device = SystemInfo.getDeviceModel();
		return device.equalsIgnoreCase("ZTE-U V880");
	}

	/**
	 * 判断设备是否是小米手机
	 * **/
	public static boolean isMI() {
		String device = SystemInfo.getDeviceModel();
		if (null != device) {
			return device.startsWith("MI");
		} else {
			return false;
		}
	}

	public static boolean isNotSupportLongPress() {
		String device = SystemInfo.getDeviceModel();
		return (device.equalsIgnoreCase("MB525") || device.equalsIgnoreCase("ME860")
				|| device.equalsIgnoreCase("MotoA953") || device.equalsIgnoreCase("U8800")
				|| device.equalsIgnoreCase("U8500") || device.equalsIgnoreCase("ME525")
				|| device.equalsIgnoreCase("ME525+") || device.equalsIgnoreCase("XT800+")
				|| device.equalsIgnoreCase("XT800") || device.equalsIgnoreCase("MB526"));
	}

	public static boolean isNotSupportMaxAmplitude() {
		String device = SystemInfo.getDeviceModel();
		return (device.equalsIgnoreCase("HTC Incredible S"));

	}

	public static int getDeviceSdkInt() {
		return android.os.Build.VERSION.SDK_INT;
	}

	/**
	 * 获取手机的制造厂商（没有空格，全部小写）
	 *
	 * @return
	 */
	public static String getManufacturer() {
		String brand = android.os.Build.MANUFACTURER.replaceAll(" +", "");
		if (brand != null)
			brand = brand.toLowerCase(Locale.getDefault());
		else
			brand = "unknown";
		return brand;
	}

	/**
	 * 获取手机型号（没有空格，全部小写）
	 * 
	 * @return
	 */
	public static String getDeviceModel() {
		String model = Build.MODEL.replaceAll(" +", "");
		if (model != null)
			model = model.toLowerCase(Locale.getDefault());
		else
			model = "unknown";
		return model;
	}

	public static int getDeviceAudioMode() {
		return AudioModeAdapterManager.getInstance().getAudioMode();
	}

//	/**
//	 * 是否Debug，如果是Debug模式返回true，其他返回false
//	 * 
//	 * @return
//	 */
//	public static boolean isDebug() {
//		return ApplicationInfo.FLAG_DEBUGGABLE == (Freepp.context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
//	}
//
//	/**
//	 * 判断摄像头 是否存在
//	 * 
//	 * @return 存在 返回true，其他返回false
//	 */
//	public static boolean checkCameraHardware() {
//		if (Freepp.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//			// this device has a camera
//			return true;
//		} else {
//			// no camera on this device
//			return false;
//		}
//	}
//
//	/**
//	 * 隐藏软键盘
//	 * 
//	 * @param windowToken
//	 */
//	public static void hideSoftInput(IBinder windowToken) {
//		InputMethodManager imm = (InputMethodManager) Freepp.context.getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
//	}
}
