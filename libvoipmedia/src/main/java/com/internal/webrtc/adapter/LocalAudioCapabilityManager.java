package com.internal.webrtc.adapter;

import android.util.Xml;

import com.internal.voipmedia.util.Print;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class LocalAudioCapabilityManager {

	private static final String FILE_NAME = "local_audio_capability.xml";
	private final Map<String, AudioEffectDevice> devices = new HashMap<String, AudioEffectDevice>();
	private String TAG = getClass().getSimpleName();

	private static final LocalAudioCapabilityManager instence = new LocalAudioCapabilityManager();

	public static LocalAudioCapabilityManager getInstance() {
		return instence;
	}

	private LocalAudioCapabilityManager() {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(getClass().getResourceAsStream(FILE_NAME), "UTF-8");// 设置需要解析的xml内容
			boolean done = false;
			AudioEffectDevice device = new AudioEffectDevice();
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
//					Print.i(TAG, "name = " + name);
					if ("device".equals(name)) {
						device = new AudioEffectDevice();
					} else if ("manufacturer".equals(name)) {
						device.setDeviceManufacturer(parser.nextText());
					} else if ("model".equals(name)) {
						device.setDeviceModel(parser.nextText());
					} else if ("agc".equals(name)) {
						device.setAgc(Boolean.parseBoolean(parser.nextText()));
					} else if ("aec".equals(name)) {
						device.setAec(Boolean.parseBoolean(parser.nextText()));
					} else if ("ns".equals(name)) {
						device.setNs(Boolean.parseBoolean(parser.nextText()));
					} else if ("force_java_audio".equals(name)) {
						device.setUseJavaAudio(Boolean.parseBoolean(parser.nextText()));
					} else if ("sdk_leve".equals(name)) {
						device.setDeviceSdkLevel(Integer.parseInt(parser.nextText()));
					}
				} else if (eventType == XmlPullParser.END_TAG) {

					if ("device".equals(parser.getName())) {
						devices.put(device.getManufacturerAndModel(), device);
					}

					if ("devices".equals(parser.getName())) {
						done = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		Print.i(TAG, "deviceSize = " + devices.size());
		Print.i(TAG, devices);
	}

	public AudioEffectDevice getAudioEffectDevice() {
		return devices.get(BaseDevice.getLocalManufacturerAndModel());
	}

}