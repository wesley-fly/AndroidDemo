package com.internal.webrtc.adapter;

import android.util.Xml;

import com.internal.voipmedia.util.Print;
import com.internal.voipmedia.util.RuntimeExecu;
import com.internal.voipmedia.util.RuntimeExecu.CpuInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AecAdapterManager {
	private String TAG = getClass().getSimpleName();
	private final String FILE_NAME = "cpu_info_black.xml";
	private static final AecAdapterManager manager = new AecAdapterManager();
	private Map<String, Boolean> devices = new HashMap<String, Boolean>();

	private AecAdapterManager() {
		try {
			XmlPullParser parser = Xml.newPullParser();
			
			parser.setInput(getClass().getResourceAsStream(FILE_NAME), "UTF-8");// 设置需要解析的xml内容
			boolean done = false;
			AecDevices device = new AecDevices();
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					Print.i(TAG, "name = " + name);
					if ("device".equals(name)) {
						device = new AecDevices();
					} else if ("manufacturer".equals(name)) {
						device.setDeviceManufacturer(parser.nextText());
					} else if ("model".equals(name)) {
						device.setDeviceModel(parser.nextText());
					} else if ("sdk_leve".equals(name)) {
						device.setDeviceSdkLevel(Integer.parseInt(parser.nextText()));
					} else if ("use_aec".equals(name)) {
						device.setUseAec(Boolean.parseBoolean(parser.nextText()));
					}
				} else if (eventType == XmlPullParser.END_TAG) {

					if ("device".equals(parser.getName())) {
						devices.put(device.getManufacturerAndModel(), device.isUseAec());
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

	public boolean isUerAec() {
		String identity = BaseDevice.getLocalManufacturerAndModel();
		Boolean aec = devices.get(identity);
		if (null != aec) {
			return aec.booleanValue();
		}
		CpuInfo cpuInfo = RuntimeExecu.getCpuInfo();
		Print.d(TAG, "cpuInfo = " + cpuInfo.toString());
		if (cpuInfo.isArmV7 && cpuInfo.haveVfP) {
			return true;
		}
		return false;
	}

	public static AecAdapterManager getInstance() {
		return manager;
	}

}