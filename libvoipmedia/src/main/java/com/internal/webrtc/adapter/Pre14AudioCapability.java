package com.internal.webrtc.adapter;

import com.internal.voipmedia.util.Print;

public class Pre14AudioCapability implements LocalAudioDetector {

	private String TAG = Pre14AudioCapability.class.getSimpleName();

	@Override
	public boolean haveAGC() {
		return false;
	}

	@Override
	public boolean haveAEC() {
		return false;
	}

	@Override
	public boolean haveNS() {
		return false;
	}

	@Override
	public DeviceAdaptation get() {

		DeviceAdaptation adapter = new DeviceAdaptation();
		LocalAudioCapabilityManager capability = LocalAudioCapabilityManager.getInstance();
		int value = 0;
		AudioEffectDevice device = capability.getAudioEffectDevice();
		if (null != device) {
			Print.i(TAG, "Find config and use forceConfig.");

			if (device.isAgc()) {
				adapter.setHaveAGC(true);
			}

			if (device.isAec()) {
				adapter.setHaveAEC(true);
			}

			if (device.isNs()) {
				adapter.setHaveNS(true);
			}
			adapter.setAudioSelect(device.isUseJavaAudio() ? 1 : 0);
			//adapter.setCapability(value);
		}
		return adapter;
	}
}