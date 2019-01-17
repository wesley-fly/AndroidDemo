package com.internal.webrtc.adapter;

public class AudioEffectDevice extends BaseDevice {



	/** 是否强制关闭AGC */
	private boolean isHaveAgc = true;

	/** 是否强制关闭AEC */
	private boolean isHaveAec = true;

	/** 是否强制关闭NS */
	private boolean isHaveNs = true;

	/** 是否强制使用java Audio */
	private boolean isUseJavaAudio = false;

	@Override
	public String toString() {
		return deviceManufacturer + ":" + deviceModel + ":" + isHaveAgc + ":" + isHaveAec + ":" + isHaveNs + ":" + isUseJavaAudio;
	}



	public boolean isAgc() {
		return isHaveAgc;
	}

	public void setAgc(boolean isHaveAgc) {
		this.isHaveAgc = isHaveAgc;
	}

	public boolean isAec() {
		return isHaveAec;
	}

	public void setAec(boolean isHaveAec) {
		this.isHaveAec = isHaveAec;
	}

	public boolean isNs() {
		return isHaveNs;
	}

	public void setNs(boolean isHaveNs) {
		this.isHaveNs = isHaveNs;
	}

	public boolean isUseJavaAudio() {
		return isUseJavaAudio;
	}

	public void setUseJavaAudio(boolean isUseJavaAudio) {
		this.isUseJavaAudio = isUseJavaAudio;
	}
}