package com.internal.webrtc.adapter;

public class AudioEffectOperatDevice extends BaseDevice {

	private boolean isOpenAGC = false;
	private boolean isOpenAEC = false;
	private boolean isOpenNS = false;

	public boolean isOpenAGC() {
		return isOpenAGC;
	}

	public void setOpenAGC(boolean isOpenAGC) {
		this.isOpenAGC = isOpenAGC;
	}

	public boolean isOpenAEC() {
		return isOpenAEC;
	}

	public void setOpenAEC(boolean isOpenAEC) {
		this.isOpenAEC = isOpenAEC;
	}

	public boolean isOpenNS() {
		return isOpenNS;
	}

	public void setOpenNS(boolean isOpenNS) {
		this.isOpenNS = isOpenNS;
	}
}