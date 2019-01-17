package com.internal.webrtc.adapter;

import android.media.AudioManager;
import android.util.Xml;

import com.internal.voipmedia.util.Print;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioModeAdapterManager {
	private String TAG = getClass().getSimpleName();
	private static final String FILE_NAME = "audio_mode.xml";
	private static final AudioModeAdapterManager manager = new AudioModeAdapterManager();

	private Map<String, Integer> cache = new HashMap<String, Integer>();

	public static AudioModeAdapterManager getInstance() {
		return manager;
	}

	private AudioModeAdapterManager() {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(getClass().getResourceAsStream(FILE_NAME), "UTF-8");// 设置需要解析的xml内容
			boolean done = false;
			AudioModeDevice device = new AudioModeDevice();
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					Print.i(TAG, "name = " + name);
					if ("device".equals(name)) {
						device = new AudioModeDevice();
					} else if ("manufacturer".equals(name)) {
						device.setDeviceManufacturer(parser.nextText());
					} else if ("model".equals(name)) {
						device.setDeviceModel(parser.nextText());
					} else if ("sdk_leve".equals(name)) {
						device.setDeviceSdkLevel(Integer.parseInt(parser.nextText()));
					} else if ("audio_mode".equals(name)) {
						device.setAudioMode(Integer.parseInt(parser.nextText()));
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					if ("device".equals(parser.getName())) {
						cache.put(device.getUniqueIdentity(), device.getAudioMode());
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


	/* modes for setPhoneState, must match AudioSystem.h audio_mode */
	// public static final int MODE_INVALID = -2;
	// public static final int MODE_CURRENT = -1;
	// public static final int MODE_NORMAL = 0;
	// public static final int MODE_RINGTONE = 1;
	// public static final int MODE_IN_CALL = 2;
	// public static final int MODE_IN_COMMUNICATION = 3;
	// public static final int NUM_MODES = 4;
	public int getAudioMode()
	{
		Integer mode = cache.get(BaseDevice.getLocalUniqueIdentity());
		if (null != mode)
		{
			// switch (mode)
			// {
			// case 2:
			// return AudioManager.MODE_IN_CALL;
			// case 3:
			// return AudioManager.MODE_IN_COMMUNICATION;
			// default:
			// return mode;
			// }
			return mode.intValue();
		}
		//
		int sdkInt = android.os.Build.VERSION.SDK_INT;
		if (sdkInt > 10)
		{
			// AudioManager.MODE_IN_COMMUNICATION
			return AudioManager.MODE_IN_COMMUNICATION;
		} else
		{
			// AudioManager.MODE_IN_CALL
			return AudioManager.MODE_IN_CALL;
		}
	}
}