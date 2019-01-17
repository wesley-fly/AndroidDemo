package com.internal.webrtc.adapter;

import android.util.Xml;

import com.internal.voipmedia.util.Print;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LocalDisableVideoManager {
	private static final LocalDisableVideoManager manager = new LocalDisableVideoManager();
	private static final String FILE_NAME = "video_control.xml";
	private String TAG = getClass().getSimpleName();
	private Set<String> cache = new HashSet<String>();

	private LocalDisableVideoManager() {

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(getClass().getResourceAsStream(FILE_NAME), "UTF-8");// 设置需要解析的xml内容
			boolean done = false;
			BaseDevice device = new BaseDevice();
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					Print.i(TAG, "name = " + name);
					if ("device".equals(name)) {
						device = new BaseDevice();
					} else if ("manufacturer".equals(name)) {
						device.setDeviceManufacturer(parser.nextText());
					} else if ("model".equals(name)) {
						device.setDeviceModel(parser.nextText());
					} else if ("sdk_leve".equals(name)) {
						device.setDeviceSdkLevel(Integer.parseInt(parser.nextText()));
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					if ("device".equals(parser.getName())) {
						cache.add(device.getManufacturerAndModel());
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

	/**
	 * 本地视频是否禁用
	 * 
	 * @return true禁用本地视频，false其他
	 */
	public boolean localVideoDisable() {
		return cache.contains(BaseDevice.getLocalManufacturerAndModel());
	}

	public static LocalDisableVideoManager getInstance() {
		return manager;
	}

}