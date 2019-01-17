package com.internal.webrtc;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

import com.internal.voipmedia.util.Print;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dalvik.system.DexClassLoader;

public class VideoCaptureDeviceInfoAndroid extends VideoCaptureDeviceInfo {

	// Context
	public static Context mActivity;

	// Set this to 2 for VERBOSE logging. 1 for DEBUG
	private static int LOGLEVEL = 0;
	private static boolean VERBOSE = LOGLEVEL >= 2;
	private static boolean DEBUG = true;//LOGLEVEL >= 1;
	private static boolean HWCODEC_H264 = true;
	private final int VWIDTH = 352;
	private final int VHEIGH = 288;
	private final int FRAMERATE = 15;
	private static final String TAG = "*WEBRTCJNI*";
	private VideoCaptureAndroid vca = null;

	private static int m_frameRate;
	private static int m_height;
	private static int m_width;

	//String currentDeviceUniqueId;
	int id;
	private List<AndroidVideoCaptureDevice> deviceList;

	public static VideoCaptureDeviceInfoAndroid CreateVideoCaptureDeviceInfoAndroid(int in_id, Context in_context) {
		if (DEBUG) {
			Print.e(TAG, String.format(Locale.US, "VideoCaptureDeviceInfoAndroid" + "id:" + in_id));
		}

		VideoCaptureDeviceInfoAndroid self = new VideoCaptureDeviceInfoAndroid(in_id, in_context);
		if (self != null && self.Init() == 0) {
			return self;
		} else {
			if (DEBUG) {
				Print.e(TAG, "Failed to create VideoCaptureDeviceInfoAndroid.");
			}
		}
		return null;
	}

	private VideoCaptureDeviceInfoAndroid(int in_id, Context in_context) {
		id = in_id;
		mActivity = in_context;
		deviceList = new ArrayList<AndroidVideoCaptureDevice>();
	}


	private static boolean isScreenOriatationPortrait(Context context)
	{
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	private int Init() {
		// Populate the deviceList with available cameras and their
		// capabilities.
		Print.e(TAG,"++++++++++++++++init+++++++++++++++++++++");
		Camera camera = null;
		try {

			if (android.os.Build.VERSION.SDK_INT > 8) {
				// From Android 2.3 and onwards
				Print.e(TAG, "CAMERA NUMBER="+Camera.getNumberOfCameras());
				for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
					AndroidVideoCaptureDevice newDevice = new AndroidVideoCaptureDevice();

					Camera.CameraInfo info = new Camera.CameraInfo();
					Camera.getCameraInfo(i, info);
					newDevice.index = i;
					//if(isScreenOriatationPortrait(mActivity))
						//newDevice.orientation = info.orientation;
					//else
						//newDevice.orientation = 180;
					newDevice.orientation = info.orientation;
					if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
						newDevice.deviceUniqueName = "Camera " + i + ", Facing back, Orientation " + info.orientation;
					} else {
						newDevice.deviceUniqueName = "Camera " + i + ", Facing front, Orientation " + info.orientation;
						newDevice.frontCameraType = FrontFacingCameraType.Android23;
					}
					Print.e(TAG, newDevice.deviceUniqueName);
					camera = Camera.open(i);
					Camera.Parameters parameters = camera.getParameters();
					AddDeviceInfo(newDevice, parameters);
					camera.release();
					camera = null;
					deviceList.add(newDevice);
				}
			} else {
				// Prior to Android 2.3
				AndroidVideoCaptureDevice newDevice;
				Camera.Parameters parameters;

				newDevice = new AndroidVideoCaptureDevice();
				camera = Camera.open();
				parameters = camera.getParameters();
				newDevice.deviceUniqueName = "Camera 1, Facing back";
				newDevice.orientation = 90;
				AddDeviceInfo(newDevice, parameters);
				deviceList.add(newDevice);
				camera.release();
				camera = null;

				newDevice = new AndroidVideoCaptureDevice();
				newDevice.deviceUniqueName = "Camera 2, Facing front";
				parameters = SearchOldFrontFacingCameras(newDevice);
				if (parameters != null) {
					newDevice.frontCameraType = FrontFacingCameraType.Android22;
					AddDeviceInfo(newDevice, parameters);
					deviceList.add(newDevice);
				}
				Print.e(TAG, newDevice.deviceUniqueName);
			}
		} catch (Exception ex) {
			Print.w("*WEBRTC*", "Failed to init VideoCaptureDeviceInfo ex:" + ex.getLocalizedMessage());
			return -1;
		}
		VerifyCapabilities();
		Print.e(TAG, "INIT END");
		return 0;
	}

	// Adds the capture capabilities of the currently opened device
	private void AddDeviceInfo(AndroidVideoCaptureDevice newDevice, Camera.Parameters parameters) {

		List<Size> sizes = parameters.getSupportedPreviewSizes();

		if (sizes == null) {
			newDevice.captureCapabilies = new CaptureCapabilityAndroid[1];
			newDevice.captureCapabilies[0] = new CaptureCapabilityAndroid();
			newDevice.captureCapabilies[0].width = VWIDTH;
			newDevice.captureCapabilies[0].height = VHEIGH;
			newDevice.captureCapabilies[0].maxFPS = FRAMERATE;

			return;
		}

		List<Integer> frameRates = parameters.getSupportedPreviewFrameRates();
		int maxFPS = 15;
		int baseFps = 15;
		int fpsdifference = Integer.MAX_VALUE;
		for (Integer frameRate : frameRates) {
			Print.v("*WEBRTC*", "VideoCaptureDeviceInfoAndroid:frameRate " + frameRate);
			if (Math.abs(frameRate - baseFps) < fpsdifference) {
				maxFPS = frameRate;
				fpsdifference = Math.abs(frameRate - baseFps);
			} else if (Math.abs(frameRate - baseFps) == fpsdifference) {
				if (frameRate < baseFps) {
					maxFPS = frameRate;
				}
			}

		}

		if (maxFPS > 15) {
			maxFPS = 15;
		}

		int base_resolution = VWIDTH * VHEIGH; // 基准分辨率
		int difference = Integer.MAX_VALUE;// 差值
		newDevice.captureCapabilies = new CaptureCapabilityAndroid[1];
		newDevice.captureCapabilies[0] = new CaptureCapabilityAndroid();
		// int initArea = 0;
		for (Size size : sizes) {
			Print.v(TAG, "width = " + size.width + ", height = " + size.height);
			int area = size.width * size.height;
			if (Math.abs(area - base_resolution) < difference) {
				newDevice.captureCapabilies[0].height = size.height;
				newDevice.captureCapabilies[0].width = size.width;
				newDevice.captureCapabilies[0].maxFPS = maxFPS;
				difference = Math.abs(area - base_resolution);
			} else if (Math.abs(area - base_resolution) == difference) {
				// 上下限的问题
				if (area < base_resolution) {
					newDevice.captureCapabilies[0].height = size.height;
					newDevice.captureCapabilies[0].width = size.width;
					newDevice.captureCapabilies[0].maxFPS = maxFPS;
				}
			}

		}
		Print.i(TAG, "width = " + newDevice.captureCapabilies[0].width + ", height = "
				+ newDevice.captureCapabilies[0].height + ",Fps = " + newDevice.captureCapabilies[0].maxFPS);
	}

	// Function that make sure device specific capabilities are
	// in the capability list.
	// Ie Galaxy S supports CIF but does not list CIF as a supported capability.
	// Motorola Droid Camera does not work with frame rate above 15fps.
	// http://code.google.com/p/android/issues/detail?id=5514#c0
	private void VerifyCapabilities() {
		// Nexus S or Galaxy S
		if (android.os.Build.DEVICE.equalsIgnoreCase("GT-I9000") || android.os.Build.DEVICE.equalsIgnoreCase("crespo")) {
			CaptureCapabilityAndroid specificCapability = new CaptureCapabilityAndroid();
			specificCapability.width = 352;
			specificCapability.height = 288;
			specificCapability.maxFPS = 15;
			AddDeviceSpecificCapability(specificCapability);

			specificCapability = new CaptureCapabilityAndroid();
			specificCapability.width = 176;
			specificCapability.height = 144;
			specificCapability.maxFPS = 15;
			AddDeviceSpecificCapability(specificCapability);

			specificCapability = new CaptureCapabilityAndroid();
			specificCapability.width = 320;
			specificCapability.height = 240;
			specificCapability.maxFPS = 15;
			AddDeviceSpecificCapability(specificCapability);
		}

		if(android.os.Build.DEVICE.equalsIgnoreCase("msm8974"))
		{
			CaptureCapabilityAndroid specificCapability = new CaptureCapabilityAndroid();
			specificCapability.width = 640;
			specificCapability.height = 480;
			specificCapability.maxFPS = 15;
			AddDeviceSpecificCapability(specificCapability);
		}
		// Motorola Milestone Camera server does not work at 30fps
		// even though it reports that it can
		// Motorola Hubble Camera server does work only at 30fps
		if (android.os.Build.MANUFACTURER.equalsIgnoreCase("motorola")) {
			if (android.os.Build.DEVICE.equals("umts_sholes")) {
				for (AndroidVideoCaptureDevice device : deviceList)
					for (CaptureCapabilityAndroid capability : device.captureCapabilies)
						capability.maxFPS = 15;
			} else if (android.os.Build.DEVICE.equals("wifi_hubble")) {
				for (AndroidVideoCaptureDevice device : deviceList)
					for (CaptureCapabilityAndroid capability : device.captureCapabilies)
						capability.maxFPS = 30;
			}
		}
	}

	private void AddDeviceSpecificCapability(CaptureCapabilityAndroid specificCapability) {
		for (AndroidVideoCaptureDevice device : deviceList) {
			boolean foundCapability = false;
			for (CaptureCapabilityAndroid capability : device.captureCapabilies) {
				if (capability.width == specificCapability.width && capability.height == specificCapability.height) {
					foundCapability = true;
					break;
				}
			}
			if (foundCapability == false) {
				CaptureCapabilityAndroid newCaptureCapabilies[] = new CaptureCapabilityAndroid[device.captureCapabilies.length + 1];
				for (int i = 0; i < device.captureCapabilies.length; ++i) {
					newCaptureCapabilies[i + 1] = device.captureCapabilies[i];
				}
				newCaptureCapabilies[0] = specificCapability;
				device.captureCapabilies = newCaptureCapabilies;
			}
		}
	}

	// Returns the number of Capture devices that is supported
	public int NumberOfDevices() {
		return deviceList.size();
	}

	public String GetDeviceUniqueName(int deviceNumber) {

		if (deviceNumber < 0) {

			Print.w(TAG, "deviceNumber < 0 or deviceNumber =" + deviceNumber + "deviceList.size()=" + deviceList.size());
			return null;
		}

		if(deviceNumber >= deviceList.size())
			deviceNumber =0;

		return deviceList.get(deviceNumber).deviceUniqueName;
	}

	public CaptureCapabilityAndroid[] GetCapabilityArray(String deviceUniqueId) {
		for (AndroidVideoCaptureDevice device : deviceList) {
			if (device.deviceUniqueName.equals(deviceUniqueId)) {
				return (CaptureCapabilityAndroid[]) device.captureCapabilies;
			}
		}
		return null;
	}

	// Returns the camera orientation as described by
	// android.hardware.Camera.CameraInfo.orientation
	public int GetOrientation(String deviceUniqueId) {
		for (AndroidVideoCaptureDevice device : deviceList) {
			if (device.deviceUniqueName.equals(deviceUniqueId)) {
				return device.orientation;
			}
		}
		return -1;
	}

	public int switchDevice(int deviceNumber)
	{
		String deviceUniqueId = GetDeviceUniqueName(deviceNumber);
		for (AndroidVideoCaptureDevice device : deviceList)
		{
			if (device.deviceUniqueName.equals(deviceUniqueId))
			{
				if(vca != null)
				{
					vca.setCurrentDevice(device);
					return device.orientation;
				}else
					return 0;
			}

		}

		return 0;
	}

	public static void getCamerInfo(int width, int height, int frameRate)
	{
		width = m_width;
		height = m_height;
		frameRate = m_frameRate;
	}

	public void computerCamerInfor(Camera camera)
	{
		try
		{
			if (null == camera)
			{
				Print.e(TAG, "Camera is not found.");
				return;
			}
			Parameters parameters = camera.getParameters();
			// ///////////
			List<Integer> frameRates = parameters.getSupportedPreviewFrameRates();
			int baseFps = 15;
			int fpsdifference = Integer.MAX_VALUE;
			for (Integer frameRate : frameRates)
			{
				if (Math.abs(frameRate - baseFps) < fpsdifference)
				{
					m_frameRate = frameRate;
					fpsdifference = Math.abs(frameRate - baseFps);
				}
				else if (Math.abs(frameRate - baseFps) == fpsdifference)
				{
					// 如果有 14 和 16 那么选择14
					if (frameRate < baseFps)
					{
						m_frameRate = frameRate;
					}
				}
			}

			if (m_frameRate > 15)
			{
				m_frameRate = 15;
			}

			// /////////
			int base_resolution = VWIDTH * VHEIGH; // 基准分辨率
			int difference = Integer.MAX_VALUE;// 差值
			List<Size> sizes = parameters.getSupportedPreviewSizes();
			for (Size size : sizes)
			{
				Print.v(TAG, "width = " + size.width + ", height = " + size.height);
				int area = size.width * size.height;
				if (Math.abs(area - base_resolution) < difference)
				{
					m_height = size.height;
					m_width = size.width;
					difference = Math.abs(area - base_resolution);
				}
				else if (Math.abs(area - base_resolution) == difference)
				{
					// 上下限的问题
					if (area < base_resolution)
					{
						m_height = size.height;
						m_width = size.width;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	// Returns an instance of VideoCaptureAndroid.
	public VideoCaptureAndroid AllocateCamera(int id, long context, String deviceUniqueId)
	{
		try {
			if (DEBUG)
				Print.d("*WEBRTC*", "AllocateCamera " + deviceUniqueId);

			Camera camera = null;
			AndroidVideoCaptureDevice deviceToUse = null;
			for (AndroidVideoCaptureDevice device : deviceList) {
				if (device.deviceUniqueName.equals(deviceUniqueId)) {
					// Found the wanted camera
					deviceToUse = device;
					switch (device.frontCameraType) {
					case GalaxyS:
						camera = AllocateGalaxySFrontCamera();
						break;
					case HTCEvo:
						camera = AllocateEVOFrontFacingCamera();
						break;
					default:
						// From Android 2.3 and onwards)
						if (android.os.Build.VERSION.SDK_INT > 8)
							camera = Camera.open(device.index);
						else
							camera = Camera.open(); // Default camera
					}
				}
			}

			if (camera == null) {
				return null;
			}
			if (VERBOSE) {
				Print.v("*WEBRTC*", "AllocateCamera - creating VideoCaptureAndroid");
			}

			//computer camer info for sdk
			computerCamerInfor(camera);

			vca= new VideoCaptureAndroid(id, context, camera, deviceToUse,deviceList,mActivity);
			vca.enable();
			return vca;

		} catch (Exception ex) {
			Print.w("*WEBRTC*", "AllocateCamera Failed to open camera- ex " + ex.getLocalizedMessage());
		}
		return null;
	}

	// Searches for a front facing camera device. This is device specific code.
	private Camera.Parameters SearchOldFrontFacingCameras(AndroidVideoCaptureDevice newDevice)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException, ClassNotFoundException,
            IllegalAccessException, InvocationTargetException {
		// Check the id of the opened camera device
		// Returns null on X10 and 1 on Samsung Galaxy S.
		Camera camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		String cameraId = parameters.get("camera-id");
		if (cameraId != null && cameraId.equals("1")) {
			// This might be a Samsung Galaxy S with a front facing camera.
			try {
				parameters.set("camera-id", 2);
				camera.setParameters(parameters);
				parameters = camera.getParameters();
				newDevice.frontCameraType = FrontFacingCameraType.GalaxyS;
				newDevice.orientation = 0;
				camera.release();
				return parameters;
			} catch (Exception ex) {
				// Nope - it did not work.
				Print.w("*WEBRTC*", "Init Failed to open front camera camera - ex " + ex.getLocalizedMessage());
			}
		}
		camera.release();

		// Check for Evo front facing camera
		File file = new File("/system/framework/com.htc.hardware.twinCamDevice.jar");
		boolean exists = file.exists();
		if (!exists) {
			file = new File("/system/framework/com.sprint.hardware.twinCamDevice.jar");
			exists = file.exists();
		}
		if (exists) {
			newDevice.frontCameraType = FrontFacingCameraType.HTCEvo;
			newDevice.orientation = 0;
			Camera evCamera = AllocateEVOFrontFacingCamera();
			parameters = evCamera.getParameters();
			evCamera.release();
			return parameters;
		}
		return null;
	}

	// Returns a handle to HTC front facing camera.
	// The caller is responsible to release it on completion.
	private Camera AllocateEVOFrontFacingCamera() throws SecurityException, NoSuchMethodException,
            ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String classPath = null;
		File file = new File("/system/framework/com.htc.hardware.twinCamDevice.jar");
		classPath = "com.htc.hardware.twinCamDevice.FrontFacingCamera";
		boolean exists = file.exists();
		if (!exists) {
			file = new File("/system/framework/com.sprint.hardware.twinCamDevice.jar");
			classPath = "com.sprint.hardware.twinCamDevice.FrontFacingCamera";
			exists = file.exists();
		}
		if (!exists) {
			return null;
		}

		String dexOutputDir = "";
		if (mActivity != null) {
			dexOutputDir = mActivity.getFilesDir().getAbsolutePath();
			File mFilesDir = new File(dexOutputDir, "dexfiles");
			if (!mFilesDir.exists()) {
				// Log.e("*WEBRTCN*", "Directory doesn't exists");
				if (!mFilesDir.mkdirs()) {
					// Log.e("*WEBRTCN*", "Unable to create files directory");
				}
			}
		}

		dexOutputDir += "/dexfiles";

		DexClassLoader loader = new DexClassLoader(file.getAbsolutePath(), dexOutputDir, null,
				ClassLoader.getSystemClassLoader());

		Method method = loader.loadClass(classPath).getDeclaredMethod("getFrontFacingCamera", (Class[]) null);
		Camera camera = (Camera) method.invoke((Object[]) null, (Object[]) null);
		return camera;
	}

	// Returns a handle to Galaxy S front camera.
	// The caller is responsible to release it on completion.
	private Camera AllocateGalaxySFrontCamera() {
		Camera camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		parameters.set("camera-id", 2);
		camera.setParameters(parameters);
		return camera;
	}

}
