/*
 *  Copyright (c) 2013 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

// The functions in this file are called from native code. They can still be
// accessed even though they are declared private.

package com.internal.webrtc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;

import com.internal.voipmedia.util.Print;

import java.util.Locale;

class AudioManagerAndroid {
  // Most of Google lead devices use 44.1K as the default sampling rate, 44.1K
  // is also widely used on other android devices.
  private static final int DEFAULT_SAMPLING_RATE = 16000;//44100;
  // Randomly picked up frame size which is close to return value on N4.
  // Return this default value when
  // getProperty(PROPERTY_OUTPUT_FRAMES_PER_BUFFER) fails.
  private static final int DEFAULT_FRAMES_PER_BUFFER = 960;//256

  private int mNativeOutputSampleRate;
  private boolean mAudioLowLatencySupported;
  private int mAudioLowLatencyOutputFrameSize;
  
  private static int STREAM_TYPE = AudioManager.STREAM_VOICE_CALL;

  @SuppressLint("NewApi")
  private AudioManagerAndroid(Context context) {
    AudioManager audioManager = (AudioManager)
    context.getSystemService(Context.AUDIO_SERVICE);

    mNativeOutputSampleRate = DEFAULT_SAMPLING_RATE;
    mAudioLowLatencyOutputFrameSize = DEFAULT_FRAMES_PER_BUFFER;
    if (android.os.Build.VERSION.SDK_INT >=android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
    {
      String sampleRateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
      if (sampleRateString != null) {
    	  int outputsamplerate = Integer.parseInt(sampleRateString);
    	  //if(mNativeOutputSampleRate >outputsamplerate)
    		  mNativeOutputSampleRate = outputsamplerate;
      }
      String framesPerBuffer = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
      if (framesPerBuffer != null) {
    	  int outputframesize = Integer.parseInt(framesPerBuffer);
    	  //if(mAudioLowLatencyOutputFrameSize > outputframesize)
    		  mAudioLowLatencyOutputFrameSize = outputframesize;
      }
    }
    mAudioLowLatencySupported = context.getPackageManager().hasSystemFeature(
        PackageManager.FEATURE_AUDIO_LOW_LATENCY);
  }

    @SuppressWarnings("unused")
    private int getNativeOutputSampleRate() {
      return mNativeOutputSampleRate;
    }

    @SuppressWarnings("unused")
    private boolean isAudioLowLatencySupported() {
        return mAudioLowLatencySupported;
    }

    @SuppressWarnings("unused")
    private int getAudioLowLatencyOutputFrameSize() {
        return mAudioLowLatencyOutputFrameSize;
    }

	/**
	 * 返回当前设备播放声音采用的媒体类型
	 *
	 * @return 0走通话音量，1走openSL默认的 媒体音量
	 */
	public int getStreamType()
	{
		Print.i("getStreamType", "=============getStreamType================");
		String model = Build.MODEL.replaceAll(" +", "");
		String brand = android.os.Build.BRAND;
		if (model != null)
			model = model.toLowerCase(Locale.getDefault());
		else
			model = "unknown";

		if (brand != null)
			brand = brand.toLowerCase(Locale.getDefault());
		else
			brand = "unknown";
		int streamType = 0;

		Print.i("getStreamType", "model:" + model + ", brand:" + brand);
		// //////////////
		if ("xiaomi".equals(brand))
		{
			streamType = 1;
		} else if ("sony".equals(brand))
		{
			if ("l36h".equals(model))
			{
				streamType = 1;
			}
		} else if ("huawei".equals(brand))
		{
			if ("huaweig520-0000".equals(model))
			{
				streamType = 1;
			}
		}
		// else if ("samsung".equals(brand)) {
		// if ("gt-i9300".equals(model))
		// streamType = 1;
		//
		// }

		Print.i("getStreamType", "streamType = " + streamType);

		if (1 == streamType)
		{
			STREAM_TYPE = AudioManager.STREAM_MUSIC;
		}

		return streamType;
	}
}