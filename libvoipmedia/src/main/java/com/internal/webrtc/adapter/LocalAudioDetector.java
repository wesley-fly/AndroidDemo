package com.internal.webrtc.adapter;

import android.media.AudioFormat;

import java.util.UUID;

public interface LocalAudioDetector {

	/**
	 * UUID for Automatic Gain Control (AGC) audio pre-processing
	 */
	static final UUID EFFECT_TYPE_AGC = UUID.fromString("0a8abfe0-654c-11e0-ba26-0002a5d5c51b");

	/**
	 * UUID for Acoustic Echo Canceler (AEC) audio pre-processing
	 * 
	 */
	static final UUID EFFECT_TYPE_AEC = UUID.fromString("7b491460-8d4d-11e0-bd61-0002a5d5c51b");

	/**
	 * UUID for Noise Suppressor (NS) audio pre-processing
	 * 
	 */
	static final UUID EFFECT_TYPE_NS = UUID.fromString("58b4b260-8e06-11e0-aa8e-0002a5d5c51b");

	/**
	 * Null effect UUID. Used when the UUID for effect type of
	 * 
	 */
	static final UUID EFFECT_TYPE_NULL = UUID.fromString("ec7178ec-e5e1-4432-a3f4-4657e6795210");

	/**
	 * 初始化AudioRecodre采样率
	 */
	static final int FREQUENCY = 8000;

	/**
	 * 初始化AudioRecoder编码格式
	 */
	static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	/**
	 * 设备是否有AGC硬件
	 * 
	 * @return true有， false其他
	 */
	boolean haveAGC();

	/**
	 * 设备是否有AEC设备
	 * 
	 * @return true有， false其他
	 */
	boolean haveAEC();

	/**
	 * 设备是否有NS设备
	 * 
	 * @return true有， false其他
	 */
	boolean haveNS();
	
	DeviceAdaptation get();
}