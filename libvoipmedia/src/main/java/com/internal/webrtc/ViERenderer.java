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
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ViERenderer {

  // View used for local rendering that Cameras can use for Video Overlay.
  private static SurfaceHolder g_localRenderer = null;
  private static SurfaceView g_localView = null;
  private static SurfaceHolder g_remoteRenderer = null;
  private static SurfaceView g_remoteView = null;

  public static SurfaceView CreateRenderer(Context context, boolean useOpenGLES2)
  {
    if(useOpenGLES2 == true && ViEAndroidGLES20.IsSupported(context))
    	return new ViEAndroidGLES20(context);
    else
    	return  new SurfaceView(context);
    
  }

  // Creates a SurfaceView to be used by Android Camera
  // service to display a local preview.
  // This needs to be used on Android prior to version 2.1
  // in order to run the camera.
  // Call this function before ViECapture::StartCapture.
  // The created view needs to be added to a visible layout
  // after a camera has been allocated
  // (with the call ViECapture::AllocateCaptureDevice).
  // IE.
  // CreateLocalRenderer
  // ViECapture::AllocateCaptureDevice
  // LinearLayout.addview
  // ViECapture::StartCapture
  public static SurfaceView CreateLocalRenderer(Context context) {
	if(g_localView == null)
	{
		g_localView = CreateRenderer(context,false);
	    g_localRenderer = g_localView.getHolder();
	    g_localRenderer.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
    return  g_localView;
  }

  public static SurfaceView CreateRemoteRenderer(Context context, boolean useOpenGLES2)
  {
	    if(g_remoteView == null)
	    {
		    g_remoteView = CreateRenderer(context,useOpenGLES2);
		    g_remoteRenderer = g_remoteView.getHolder();
	    }
	    return  g_remoteView;
 }
  
  public static SurfaceHolder GetLocalRenderer() {
    return g_localRenderer;
  }

  public static SurfaceHolder GetRemoteRenderer(){
	  
	return   g_remoteRenderer;
  }
  
  public static SurfaceView GetLocalView(){
	  return g_localView;
  }
  
  public static SurfaceView GetRemoteView(){
	  return g_remoteView;
  }
  
  public static void RenderDestory()
  {
	  g_localRenderer = null;
	  g_localView = null;
	  g_remoteRenderer = null;
	  g_remoteView = null;
  }
}
