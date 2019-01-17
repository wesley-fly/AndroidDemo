package com.internal.webrtc.adapter;

import android.util.Xml;

import com.internal.voipmedia.util.Print;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class SoftVolumeAdapterManager {

	private static final String TAG = SoftVolumeAdapterManager.class.getSimpleName();
	private static final String FILE_NAME = "soft_volume_control.xml";
	private static final SoftVolumeAdapterManager manager = new SoftVolumeAdapterManager();

	private final Map<String, Float> cache = new HashMap<String, Float>();

	private SoftVolumeAdapterManager() {

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(getClass().getResourceAsStream(FILE_NAME), "UTF-8");// 设置需要解析的xml内容
			boolean done = false;
			SoftVolumeDevice device = new SoftVolumeDevice();
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					Print.i(TAG, "name = " + name);
					if ("device".equals(name)) {
						device = new SoftVolumeDevice();
					} else if ("manufacturer".equals(name)) {
						device.setDeviceManufacturer(parser.nextText());
					} else if ("model".equals(name)) {
						device.setDeviceModel(parser.nextText());
					} else if ("sdk_leve".equals(name)) {
						device.setDeviceSdkLevel(Integer.parseInt(parser.nextText()));
					} else if ("volume_value".equals(name)) {
						device.setVolumeValue(Float.parseFloat(parser.nextText()));
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					if ("device".equals(parser.getName())) {
						cache.put(device.getManufacturerAndModel(), device.getVolumeValue());
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
	 * 得到当前设备的软音量配置
	 * 
	 * @return
	 */
	public float getVolumeValue() {
		Float value = cache.get(BaseDevice.getLocalManufacturerAndModel());
		if (null != value) {
			return value.floatValue();
		}
		return 1.0f;
	}

	public static SoftVolumeAdapterManager getInstance() {
		return manager;
	}
}