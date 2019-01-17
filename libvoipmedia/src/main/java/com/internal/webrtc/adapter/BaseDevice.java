package com.internal.webrtc.adapter;

import com.internal.voipmedia.util.DeviceUtil;

public class BaseDevice {
	/** 手机型号 */
	protected String deviceModel;

	/** 手机制造厂商 */
	protected String deviceManufacturer;

	/** 手机对应的SDK的版本 */
	protected int deviceSdkLevel;

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String model) {
		this.deviceModel = model;
	}


	public String getDeviceManufacturer() {
		return deviceManufacturer;
	}

	public void setDeviceManufacturer(String deviceManufacturer) {
		this.deviceManufacturer = deviceManufacturer;
	}

	public int getDeviceSdkLevel() {
		return deviceSdkLevel;
	}

	public void setDeviceSdkLevel(int sdk_level) {
		this.deviceSdkLevel = sdk_level;
	}

	public String getManufacturerAndModel() {
		StringBuilder builder = new StringBuilder();
		builder.append(deviceManufacturer).append("_").append(deviceModel);
		return builder.toString();
	}

	public String getUniqueIdentity() {
		StringBuilder builder = new StringBuilder();
		builder.append(deviceManufacturer).append("_").append(deviceModel).append("_").append(deviceSdkLevel);
		return builder.toString();
	}

	public static String getLocalUniqueIdentity() {
		StringBuilder builder = new StringBuilder();
		builder.append(DeviceUtil.getManufacturer()).append("_").append(DeviceUtil.getDeviceModel()).append("_")
				.append(DeviceUtil.getDeviceSdkInt());
		return builder.toString();
	}

	
	public static String getLocalManufacturerAndModel() {
		StringBuilder builder = new StringBuilder();
		builder.append(DeviceUtil.getManufacturer()).append("_").append(DeviceUtil.getDeviceModel());
		return builder.toString();
	}
}