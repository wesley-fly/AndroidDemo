package com.internal.webrtc.adapter;

import android.media.AudioManager;
import android.util.Log;
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
			parser.setInput(getClass().getResourceAsStream(FILE_NAME), "UTF-8");
			boolean done = false;
			AudioModeDevice device = new AudioModeDevice();
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					Log.e(TAG, "name = " + name);
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

	public int getAudioMode()
	{
		Integer mode = cache.get(BaseDevice.getLocalUniqueIdentity());
		if (null != mode)
		{
			return mode.intValue();
		}

		int sdkInt = android.os.Build.VERSION.SDK_INT;
		if (sdkInt > 10)
		{
			return AudioManager.MODE_IN_COMMUNICATION;
		}
		else
		{
			return AudioManager.MODE_IN_CALL;
		}
	}
}