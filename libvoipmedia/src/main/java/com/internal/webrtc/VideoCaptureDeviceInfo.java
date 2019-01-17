package com.internal.webrtc;

public class VideoCaptureDeviceInfo {
	
	  public enum FrontFacingCameraType {
	    None, // This is not a front facing camera
	    GalaxyS, // Galaxy S front facing camera.
	    HTCEvo, // HTC Evo front facing camera
	    Android23, // Android 2.3 front facing camera.
	    Android22, 
	  }
	  // Private class with info about all available cameras and the capabilities
	  public class AndroidVideoCaptureDevice {
		  AndroidVideoCaptureDevice() {
	      frontCameraType = FrontFacingCameraType.None;
	      index = 0;
	    }

	    public String deviceUniqueName;
	    public CaptureCapabilityAndroid captureCapabilies[];
	    public FrontFacingCameraType frontCameraType;

	    // Orientation of camera as described in
	    // android.hardware.Camera.CameraInfo.Orientation
	    public int orientation;
	    // Camera index used in Camera.Open on Android 2.3 and onwards
	    public int index;
	  }
}
