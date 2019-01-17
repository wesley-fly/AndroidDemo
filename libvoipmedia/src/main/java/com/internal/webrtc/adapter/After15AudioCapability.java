package com.internal.webrtc.adapter;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;

import com.internal.voipmedia.util.FileLog;
import com.internal.voipmedia.util.Print;

import java.util.UUID;

@SuppressLint("NewApi")
public class After15AudioCapability implements LocalAudioDetector {

	private String TAG = After15AudioCapability.class.getSimpleName();

	private boolean isHaveAGC = false;
	private boolean isHaveAEC = false;
	private boolean isHaveNS = false;
	private boolean initialize = false;

	private boolean isHaveCapability(UUID type, int sessionID) {
		AudioEffect efect = null;
		if (EFFECT_TYPE_AGC.equals(type)) {
			efect = AutomaticGainControl.create(sessionID);
		} else if (EFFECT_TYPE_AEC.equals(type)) {
			efect = AcousticEchoCanceler.create(sessionID);
		} else if (EFFECT_TYPE_NS.equals(type)) {
			efect = NoiseSuppressor.create(sessionID);
		}
		if (null != efect) {
			return efect.getEnabled();
		}
		return false;
	}

	/**
	 * 初始化判断设备是否有相应的声音处理设备
	 */
	private void init() {
		AudioEffect.Descriptor[] desc = AudioEffect.queryEffects();
		if (null != desc && desc.length > 0) {
			for (AudioEffect.Descriptor d : desc) {
				UUID type = d.type;
				if (EFFECT_TYPE_AGC.equals(type)) {
					isHaveAGC = true;
				} else if (EFFECT_TYPE_AEC.equals(type)) {
					isHaveAEC = true;
				} else if (EFFECT_TYPE_NS.equals(type)) {
					isHaveNS = true;
				}
			}
		}
		initialize = true;
	}

	@Override
	public boolean haveAGC() {
		if (!initialize) {
			init();
		}
		return isHaveAGC;
	}

	@Override
	public boolean haveAEC() {
		if (!initialize) {
			init();
		}
		return isHaveAEC;
	}

	@Override
	public boolean haveNS() {
		if (!initialize) {
			init();
		}
		return isHaveNS;
	}

	@Override
	public DeviceAdaptation get() {
		DeviceAdaptation adapter = new DeviceAdaptation();

		AudioRecord audioRecord = null;
		try {
			int bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AUDIO_ENCODING);
			int truncated = bufferSize % 480;
			if (truncated != 0) {
				bufferSize += 480 - truncated;
			}
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, AUDIO_ENCODING, bufferSize);

			Print.i(TAG, "SDK_INIT = " + Build.VERSION.SDK_INT);

			int sessionID = audioRecord.getAudioSessionId();
			Print.i(TAG, "audioSessionID = " + sessionID);
			if (-1 == sessionID) {
				Print.w(TAG, "audio session id was " + sessionID);
				return adapter;
			}

			AudioEffect.Descriptor[] desc = AudioEffect.queryEffects();
			if (null != desc && desc.length > 0) {
				for (AudioEffect.Descriptor d : desc) {
					Print.i(TAG, "name = " + d.name);
					UUID type = d.type;
					if (EFFECT_TYPE_AGC.equals(type)) {
						FileLog.log(TAG, "this device have AGC.");
						adapter.setHaveAGC(true);
						if (isHaveCapability(EFFECT_TYPE_AGC, sessionID)) {
							FileLog.log(TAG, "AGC enable was true.");
							adapter.setDefaultAGCEnable(true);
						}
					} else if (EFFECT_TYPE_AEC.equals(type)) {
						FileLog.log(TAG, "this device have AEC.");
						adapter.setHaveAEC(true);
						if (isHaveCapability(EFFECT_TYPE_AEC, sessionID)) {
							FileLog.log(TAG, "AEC enable was true.");
							adapter.setDefaultAECEnable(true);
						}
					} else if (EFFECT_TYPE_NS.equals(type)) {
						FileLog.log(TAG, "this device have NS.");
						adapter.setHaveNS(true);
						if (isHaveCapability(EFFECT_TYPE_NS, sessionID)) {
							FileLog.log(TAG, "NS enable was true.");
							adapter.setDefaultNSEnable(true);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != audioRecord) {
				audioRecord.release();
			}
		}

		LocalAudioCapabilityManager capability = LocalAudioCapabilityManager.getInstance();
		AudioEffectDevice device = capability.getAudioEffectDevice();
		if (null != device) {
			Print.i(TAG, "Find config and use config parameter.");
			int capatility = 0;
			int audioSelect = 0;
			Print.i(TAG, "force use config.");
			if (device.isAgc()) {
				adapter.setHaveAGC(true);
			}

			if (device.isAec()) {
				adapter.setHaveAEC(true);
			}

			if (device.isNs()) {
				adapter.setHaveNS(true);
			}
			audioSelect = device.isUseJavaAudio() ? 1 : 0;
			adapter.setAudioSelect(audioSelect);
			//adapter.setCapability(capatility);
		} else {
			Print.i(TAG, "Default algorithm");
			int capatility = 0;
			if (adapter.isDefaultAGCEnable()) {
				adapter.setHaveAGC(false);
			}

			if (adapter.isDefaultAECEnable()) {
				adapter.setHaveAEC(false);
			}

			if (adapter.isDefaultNSEnable()) {
				adapter.setHaveNS(false);
			}

			if (adapter.isDefaultNSEnable() || adapter.isDefaultAGCEnable() || adapter.isDefaultAECEnable()) {
				adapter.setAudioSelect(1);
			}
		}
		return adapter;
	}

}