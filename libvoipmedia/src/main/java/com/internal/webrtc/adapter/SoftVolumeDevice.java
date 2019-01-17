package com.internal.webrtc.adapter;

public class SoftVolumeDevice extends BaseDevice {

	private float mVolumeValue;

	public float getVolumeValue() {
		return mVolumeValue;
	}

	public void setVolumeValue(float volumeValue) {
		mVolumeValue = volumeValue;
	}
}