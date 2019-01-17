package com.internal.webrtc.adapter;

public class DeviceAdaptation {
	private boolean isHaveAGC = false;
	private boolean defaultAGCEnable = false;
	// //
	private boolean isHaveAEC = false;
	private boolean defaultAECEnable = false;
	// ///
	private boolean isHaveNS = false;
	private boolean defaultNSEnable = false;
	// /
	private int AUD_USE_AGC=(1<<0);
	private int AUD_USE_RX_AGC=(1<<1);
	private int AUD_USE_AECM=(1<<2);
	private int AUD_USE_NS=(1<<3);
	private int AUD_CODEC_ADAPTIVE=(1<<4);
	private int  AUD_CODEC_ISAC_MIDRATE= (1<<6);
	private int audioProcessCap=AUD_USE_AGC|AUD_USE_AECM|AUD_USE_NS	;
	private int audioSelect = 0;
	//该函数的意义在于，是否开启引擎的aecm，agc 和ns。如果有硬件aecm,agc,ns的话，则不用开启。
	private int capability = audioProcessCap;

	public boolean isHaveAGC() {
		return isHaveAGC;
	}

	public void setHaveAGC(boolean isHaveAGC) {
		if(isHaveAGC)
			capability = audioProcessCap&(~AUD_USE_AGC);
		this.isHaveAGC = isHaveAGC;
	}

	public boolean isDefaultAGCEnable() {
		return defaultAGCEnable;
	}

	public void setDefaultAGCEnable(boolean defaultAGCEnable) {
		if(defaultAGCEnable)
			capability = audioProcessCap&(~AUD_USE_AGC);
		this.defaultAGCEnable = defaultAGCEnable;
	}

	public boolean isHaveAEC() {
		return isHaveAEC;
	}

	public void setHaveAEC(boolean isHaveAEC) {
		if(isHaveAEC)
			capability = audioProcessCap&(~AUD_USE_AECM);
		this.isHaveAEC = isHaveAEC;
	}

	public boolean isDefaultAECEnable() {
		return defaultAECEnable;
	}

	public void setDefaultAECEnable(boolean defaultAECEnable) {
		if(defaultAECEnable)
			capability = audioProcessCap&(~AUD_USE_AECM);
		this.defaultAECEnable = defaultAECEnable;
	}

	public boolean isHaveNS() {
		return isHaveNS;
	}

	public void setHaveNS(boolean isHaveNS) {
		if(isHaveNS)
			capability = audioProcessCap&(~AUD_USE_NS);
		this.isHaveNS = isHaveNS;
	}

	public boolean isDefaultNSEnable() {
		return defaultNSEnable;
	}

	public void setDefaultNSEnable(boolean defaultNSEnable) {
		if(defaultNSEnable)
			capability = audioProcessCap&(~AUD_USE_NS);
		this.defaultNSEnable = defaultNSEnable;
	}

	public int getAudioSelect() {
		return audioSelect;
	}

	public void setAudioSelect(int audioSelect) {
		this.audioSelect = audioSelect;
	}

	public int getCapability() {
		return capability;
	}

	public void setCapability(int capability) {
		this.capability = capability;
	}
}