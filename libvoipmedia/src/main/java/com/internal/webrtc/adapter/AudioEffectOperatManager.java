package com.internal.webrtc.adapter;

import android.util.Xml;

import com.internal.voipmedia.util.Print;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioEffectOperatManager {

	private static final String TAG = AudioEffectOperatManager.class.getSimpleName();
	private static final String FILE_NAME = "local_audio_capability_operat.xml";

	private static final AudioEffectOperatManager manager = new AudioEffectOperatManager();

	private final Map<String, AudioEffectOperatDevice> cache = new HashMap<String, AudioEffectOperatDevice>();

	private AudioEffectOperatManager() {

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(getClass().getResourceAsStream(FILE_NAME), "UTF-8");// 设置需要解析的xml内容
			boolean done = false;
			AudioEffectOperatDevice device = new AudioEffectOperatDevice();
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					Print.i(TAG, "name = " + name);
					if ("device".equals(name)) {
						device = new AudioEffectOperatDevice();
					} else if ("manufacturer".equals(name)) {
						device.setDeviceManufacturer(parser.nextText());
					} else if ("model".equals(name)) {
						device.setDeviceModel(parser.nextText());
					} else if ("sdk_leve".equals(name)) {
						device.setDeviceSdkLevel(Integer.parseInt(parser.nextText()));
					} else if ("open_agc".equals(name)) {
						device.setOpenAGC(Boolean.parseBoolean(parser.nextText()));
					} else if ("open_aec".equals(name)) {
						device.setOpenAEC(Boolean.parseBoolean(parser.nextText()));
					} else if ("open_ns".equals(name)) {
						device.setOpenNS(Boolean.parseBoolean(parser.nextText()));
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					if ("device".equals(parser.getName())) {
						cache.put(device.getManufacturerAndModel(), device);
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
	}

	public static AudioEffectOperatManager getInstance() {
		return manager;
	}

	public AudioEffectOperatDevice getDevice() {

		return cache.get(BaseDevice.getLocalManufacturerAndModel());
	}
}