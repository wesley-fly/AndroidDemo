/*
 *  Copyright (c) 2011 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.internal.webrtc;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.internal.webrtc.VideoCaptureDeviceInfo.AndroidVideoCaptureDevice;
import com.internal.webrtc.VideoCaptureDeviceInfo.FrontFacingCameraType;
import com.internal.voipmedia.util.Print;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;


public class VideoCaptureAndroid extends OrientationEventListener implements PreviewCallback, Callback {

    private Camera camera;
    private AndroidVideoCaptureDevice currentDevice = null;
    public ReentrantLock previewBufferLock = new ReentrantLock();
    private int PIXEL_FORMAT = ImageFormat.NV21;
    PixelFormat pixelFormat = new PixelFormat();
    // True when the C++ layer has ordered the camera to be started.
    private boolean isRunning = false;

    private final int numCaptureBuffers = 3;
    private int expectedFrameSize = 0;
    private int deviceOrientation = 0;
    private int deviceInfo = 0;

    private int id = 0;
    // C++ callback context variable.
    private long context = 0;
    private SurfaceHolder localPreview = null;
    private SurfaceView localView = null;
    // True if this class owns the preview video buffers.
    private boolean ownsBuffers = false;

    // Set this to 2 for VERBOSE logging. 1 for DEBUG
    private static int LOGLEVEL = 1;
    private static boolean VERBOSE = LOGLEVEL >= 2;
    private static boolean DEBUG = LOGLEVEL >= 1;

    private static final String TAG = "WEBRTC-JAVA";

    private boolean isSetPreviewDisplay = false;
    private CaptureCapabilityAndroid currentCapability = new CaptureCapabilityAndroid();

    private Context mActivity;
    private int viewWidth = 1;
    private int viewHeight = 1;
    private int layoutRatition = 0;

    private boolean isStartProvide = false;
    private boolean isPreviewVisable = true;
    private List<AndroidVideoCaptureDevice> mDevices;

    public static ReentrantLock LOCK = new ReentrantLock();

    public static void DeleteVideoCaptureAndroid(VideoCaptureAndroid captureAndroid) {
        if (DEBUG)
            Print.i(TAG, "DeleteVideoCaptureAndroid start");
        LOCK.lock();
        try {
            if (captureAndroid.camera != null) {
                captureAndroid.camera.release();
                captureAndroid.camera = null;
            }
            captureAndroid.context = 0;
            captureAndroid.disable();
        } finally {
            LOCK.unlock();
        }

        if (DEBUG)
            Print.i(TAG, "DeleteVideoCaptureAndroid ended");

    }

    public VideoCaptureAndroid(int in_id, long in_context, Camera in_camera, AndroidVideoCaptureDevice in_device,
                               List<AndroidVideoCaptureDevice> devices, Context activity) {



        super(activity);
        id = in_id;
        context = in_context;
        camera = in_camera;
        currentDevice = in_device;
        currentCapability = currentDevice.captureCapabilies[0];
        mActivity = activity;
        mDevices = devices;

    }

    public void setCurrentDevice(AndroidVideoCaptureDevice in_device) {
        currentDevice = in_device;
    }

    public int EnablePreview(int isEnable) {

        try {
            if (camera != null) {
                switch (isEnable) {
                    case 0:
                        LOCK.lock();
                        try {
                            StopPreview();
                            camera.release();
                            camera = null;
                        } finally {
                            LOCK.unlock();
                        }

                        break;
                    case 1:
                        int width = currentDevice.captureCapabilies[0].width;
                        int height = currentDevice.captureCapabilies[0].height;
                        int framerate = currentDevice.captureCapabilies[0].maxFPS;
                        SetRotation(currentDevice.orientation);
                        StartPreview(width, height, framerate, isPreviewVisable);
                        break;
            /*
            case 2:
				isPreviewVisable = false;
				SetPreviewVisable(isPreviewVisable);
				break;
			case 3:
				isPreviewVisable = true;
				SetPreviewVisable(isPreviewVisable);
				break;
			*/
                }

            } else {
                if (isEnable == 0)
                    return 0;

                int width = currentDevice.captureCapabilies[0].width;
                int height = currentDevice.captureCapabilies[0].height;
                int framerate = currentDevice.captureCapabilies[0].maxFPS;

                if (android.os.Build.VERSION.SDK_INT > 8) {
                    camera = Camera.open(currentDevice.index);
                } else
                    camera = Camera.open(); // Default camera

                SetRotation(currentDevice.orientation);
                StartPreview(width, height, framerate, isPreviewVisable);
            }
        } catch (Exception ex) {
            Print.w("*WEBRTC*", "Failed to init connect camera ex:" + ex.getLocalizedMessage());
            return -1;
        }
        return 0;
    }

    /*
     * visable = false 隐藏
     * visable = true 显示
     */
    public int SetPreviewVisable(boolean isVisable) {
        Print.i(TAG, "SetPreviewVisable(" + isVisable + ")");
        previewBufferLock.lock();
        final boolean running = isRunning;
        previewBufferLock.unlock();
        int width = 0;
        int height = 0;
        int framerate = 0;

        if (camera != null) {
            if (running) {
                width = currentCapability.width;
                height = currentCapability.height;
                framerate = currentCapability.maxFPS;
                StopPreview();
                StartPreview(width, height, framerate, isVisable);
            }
        }


        return 0;
    }

    public int SetVideoOrientation(int layout) {

        //int rotation = 0;
        switch (layout) {
            case 0://横屏
                //rotation = 0;
                for (int i = 0; i < mDevices.size(); i++) {
                    //AndroidVideoCaptureDevice Device = mDevices.get(i);
//				if(Device.frontCameraType==FrontFacingCameraType.None)//后置
//				{
//					if(Device.orientation != 180)
//						Device.orientation = 0;
//				}
//				else
//					Device.orientation = 0;
                    if (mDevices.get(i).orientation != 180)
                        mDevices.get(i).orientation = 0;
                }
                break;
            case 1://竖屏
                for (int i = 0; i < mDevices.size(); i++) {
                    AndroidVideoCaptureDevice Device = mDevices.get(i);
                    if (Device.frontCameraType == FrontFacingCameraType.None)//后置
                        mDevices.get(i).orientation = 90;
                    else
                        mDevices.get(i).orientation = 270;
                }
                break;
        }

        return 0;
    }


    public int SetCamera(int device) {
        Print.e(TAG, "SetCamera(" + device + ")");
        int width = 0;
        int height = 0;
        int framerate = 0;

        if (camera != null) {

            previewBufferLock.lock();
            final boolean running = isRunning;
            previewBufferLock.unlock();
            //if (running)
            {
                if (device == -1) {
                    StopPreview();
                    LOCK.lock();
                    try {
                        camera.release();
                        camera = null;
                    } finally {
                        LOCK.unlock();
                    }

                } else {
                    width = currentCapability.width;
                    height = currentCapability.height;
                    framerate = currentCapability.maxFPS;

                    StopPreview();
                    LOCK.lock();
                    try {
                        camera.release();
                        camera = null;
                    } finally {
                        LOCK.unlock();
                    }

                    // From Android 2.3 and onwards)
                    if (android.os.Build.VERSION.SDK_INT > 8) {
                        camera = Camera.open(device);
                    } else
                        camera = Camera.open(); // Default camera

                    isSetPreviewDisplay = false;

                    //set Rotation
                    SetRotation(currentDevice.orientation);
                    StartPreview(width, height, framerate, isPreviewVisable);

                }

            }
        } else {
            if (device == -1)
                return 0;

            width = currentCapability.width;
            height = currentCapability.height;
            framerate = currentCapability.maxFPS;

            // From Android 2.3 and onwards)
            if (android.os.Build.VERSION.SDK_INT > 8) {
                camera = Camera.open(device);
            } else
                camera = Camera.open(); // Default camera

            isSetPreviewDisplay = false;

            //set Rotation
            SetRotation(currentDevice.orientation);
            StartPreview(width, height, framerate, isPreviewVisable);
        }

        return 0;
    }

    private int StartPreview(int width, int height, int frameRate, boolean isVisable) {
        if (DEBUG)
            Print.d(TAG, "StartPreview width" + width + " height " + height + " frame rate " + frameRate);

        try {
            if (camera == null) {
                Print.i(TAG, String.format(Locale.US, "Camera not initialized %d", id));
                return -1;
            }

            currentCapability.width = width;
            currentCapability.height = height;
            currentCapability.maxFPS = frameRate;
            PixelFormat.getPixelFormatInfo(PIXEL_FORMAT, pixelFormat);

            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(currentCapability.width, currentCapability.height);
            parameters.setPreviewFormat(PIXEL_FORMAT);
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setPreviewFrameRate(currentCapability.maxFPS);
            camera.setParameters(parameters);

            // Get the local preview SurfaceHolder from the static render class
            {
                localPreview = ViERenderer.GetLocalRenderer();

                if (localPreview != null) {
                    if (!isSetPreviewDisplay) {
                        camera.setPreviewDisplay(localPreview);
                        isSetPreviewDisplay = true;//false
                    }
                    localPreview.addCallback(this);
                }
            }

            int bufSize = width * height * pixelFormat.bitsPerPixel / 8;
            if (android.os.Build.VERSION.SDK_INT >= 7) {
                // According to Doc addCallbackBuffer belongs to API level 8.
                // But it seems like it works on Android 2.1 as well.
                // At least SE X10 and Milestone
                byte[] buffer = null;
                for (int i = 0; i < numCaptureBuffers; i++) {
                    buffer = new byte[bufSize];
                    camera.addCallbackBuffer(buffer);
                }

                camera.setPreviewCallbackWithBuffer(this);
                ownsBuffers = true;
            } else {
                camera.setPreviewCallback(this);
            }

            camera.startPreview();
            expectedFrameSize = bufSize;

        } catch (Exception ex) {
            Print.i(TAG, "Failed to start camera");
            return -1;
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
            return -1;
        }
        return 0;
    }

    private int StopPreview() {
        Print.i(TAG, "StopCapture");

        if (camera != null) {
            try {
                camera.stopPreview();
                if (android.os.Build.VERSION.SDK_INT > 7)
                    camera.setPreviewCallbackWithBuffer(null);
                else
                    camera.setPreviewCallback(null);
            } catch (Exception ex) {
                Print.i(TAG, "Failed to stop camera");
                return -1;
            } finally {
                //SetPreviewRotation(0);
                isSetPreviewDisplay = false;
                layoutRatition = 0;
            }
        }

        if (DEBUG)
            Print.i(TAG, "StopCapture ended");
        else
            Print.i(TAG, "End StopCapture");

        return 0;
    }

    public int StartCapture(int width, int height, int frameRate) {
        isStartProvide = true;
        if (!isRunning) {
            int ret = StartPreview(width, height, frameRate, isPreviewVisable);
            if (ret == 0) {
                previewBufferLock.lock();
                isRunning = true;
                previewBufferLock.unlock();
            }
            return ret;
        }
        return 0;

    }

    public int StopCapture() {
        isStartProvide = false;
        int ret = 0;
        if (isRunning) {
            previewBufferLock.lock();
            isRunning = false;
            previewBufferLock.unlock();
            if (!isRunning) {
                ret = StopPreview();
            }
            return ret;
        }
        return 0;
    }

    native void ProvideCameraFrame(byte[] data, int length, long captureObject,int orientation, int deviceinfo);

    public void onPreviewFrame(byte[] data, Camera camera) {

        previewBufferLock.lock();

        if (VERBOSE) {
            Print.v(TAG, String.format(Locale.US, "preview frame length %d context %x", data.length, context));
        }
        if (isRunning) {
            if (data != null)
                if (data.length == expectedFrameSize) {
                    if (isStartProvide) {
                        ProvideCameraFrame(data, expectedFrameSize, context, deviceOrientation,deviceInfo);
                    }

                    if (VERBOSE) {
                        Print.v(TAG, String.format(Locale.US, "frame delivered"));
                    }
                    if (ownsBuffers) {
                        // Give the video buffer to the camera service again.
                        camera.addCallbackBuffer(data);
                    }
                }

        }
        previewBufferLock.unlock();
    }

    public void SetRotation(int rotation) {
        if (camera != null) {
            previewBufferLock.lock();

            int resultRotation = 0;

            String model = Build.MODEL.replaceAll(" +", "");
            String brand = android.os.Build.BRAND;
            Print.e(TAG, "model:" + model + ", brand:" + brand);
            if (currentDevice.frontCameraType == VideoCaptureDeviceInfo.FrontFacingCameraType.Android23) {
                if("Nexus6P".equals(model)) {
                    resultRotation = (360 - rotation) % 360 + 180; // compensate the
                    deviceInfo = 2;
                }else{
                    resultRotation = (360 - rotation) % 360;
                    deviceInfo = 0;
                }
            } else {
                if ("Nexus5X".equals(model)){
                    resultRotation = (rotation - 180 + 360) % 360;
                    deviceInfo = 1;
                }else {
                    resultRotation = rotation;
                    deviceInfo = 0;
                }

            }


            if (android.os.Build.VERSION.SDK_INT > 7) {
                camera.setDisplayOrientation(resultRotation);
            } else {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setRotation(resultRotation);
                camera.setParameters(parameters);
            }

            previewBufferLock.unlock();
        }
    }

    // Sets the rotation of the preview render window.
    // Does not affect the captured video image.
    public void SetPreviewRotation(int rotation) {
        if (camera != null) {
            previewBufferLock.lock();
            final boolean running = isRunning;
            int width = 0;
            int height = 0;
            int framerate = 0;

            if (running) {
                width = currentCapability.width;
                height = currentCapability.height;
                framerate = currentCapability.maxFPS;

                StopPreview();
            }

            int resultRotation = 0;
            if (currentDevice.frontCameraType == VideoCaptureDeviceInfo.FrontFacingCameraType.Android23) {
                resultRotation = (360 - rotation) % 360; // compensate the
            } else {
                resultRotation = rotation;
            }

            if (android.os.Build.VERSION.SDK_INT > 7) {
                camera.setDisplayOrientation(resultRotation);
            } else {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setRotation(resultRotation);
                camera.setParameters(parameters);
            }

            if (running) {
                StartPreview(width, height, framerate, isPreviewVisable);
            }

            previewBufferLock.unlock();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        //if(isSetPreviewDisplay)
        //	return;

        LOCK.lock();
        try {
            if (camera != null) {
                camera.setPreviewDisplay(holder);// localPreview
                //isSetPreviewDisplay = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Print.i(TAG, String.format(Locale.US, "Failed to set Local preview. " + e.getMessage()));
        } finally {
            LOCK.unlock();
        }

    }

    public void surfaceCreated(SurfaceHolder holder) {
        Print.i(TAG, "VideoCaptureAndroid::surfaceCreated");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Print.i(TAG, "VideoCaptureAndroid::surfaceDestroyed");
        isSetPreviewDisplay = false;
    }

    @Override
    public void onOrientationChanged(int orientation) {

        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }

        //保证只返回四个方向
        deviceOrientation = ((orientation + 45) / 90 * 90) % 360;
        //Print.e(TAG, "onOrientationChanged : deviceOrientation" + deviceOrientation);
    }
}
