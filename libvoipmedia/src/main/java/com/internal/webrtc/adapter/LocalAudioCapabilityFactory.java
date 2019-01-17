package com.internal.webrtc.adapter;

import android.os.Build;

public class LocalAudioCapabilityFactory {
	public static LocalAudioDetector create() {
		if (Build.VERSION.SDK_INT < 14) {
			return new Pre14AudioCapability();
		} else if (Build.VERSION.SDK_INT > 15) {
			return new After15AudioCapability();
		} else {
			return new Post1415AudioCapability();
		}
	}
}