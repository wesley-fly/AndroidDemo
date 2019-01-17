/*
 *  Copyright (c) 2013 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.internal.webrtc;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Build;
import android.util.Log;

import com.internal.webrtc.adapter.AudioEffectOperatDevice;
import com.internal.webrtc.adapter.LocalAudioCapabilityFactory;
import com.internal.webrtc.adapter.LocalAudioDetector;
import com.internal.voipmedia.util.FileLog;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

class WebRtcAudioRecord {
    private String TAG = getClass().getSimpleName();
    private AudioRecord _audioRecord = null;

    private Context _context;

    private ByteBuffer _recBuffer;
    private byte[] _tempBufRec;

    private final ReentrantLock _recLock = new ReentrantLock();

    private boolean _doRecInit = true;
    private boolean _isRecording = false;

    private int _bufferedRecSamples = 0;

    private AudioDeviceEffect _audioEff = new AudioDeviceEffect();
	private static AudioEffectOperatDevice m_audioEffectOperatDevice;

	/** 设置机型兼容性 */
	public static void setAudioEffectOperatDevice(AudioEffectOperatDevice audioEffectOperatDevice)
	{
		m_audioEffectOperatDevice = audioEffectOperatDevice;
	}
    WebRtcAudioRecord() {
        try {
            _recBuffer = ByteBuffer.allocateDirect(2 * 480); // Max 10 ms @ 48
                                                             // kHz
        } catch (Exception e) {
            DoLog(e.getMessage());
        }

        _tempBufRec = new byte[2 * 480];
    }

    @SuppressWarnings("unused")
    private int InitRecording(int audioSource, int sampleRate) {
        audioSource = AudioSource.VOICE_COMMUNICATION;
        // get the minimum buffer size that can be used
        int minRecBufSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

        // DoLog("min rec buf size is " + minRecBufSize);

        // double size to be more safe
        int recBufSize = minRecBufSize * 2;
        // On average half of the samples have been recorded/buffered and the
        // recording interval is 1/100s.
        _bufferedRecSamples = (5 * sampleRate)/ 200;
        // DoLog("rough rec delay set to " + _bufferedRecSamples);

        // release the object
        if (_audioRecord != null) {
            _audioRecord.release();
            _audioRecord = null;
        }

        try {
            _audioRecord = new AudioRecord(
                            audioSource,
                            sampleRate,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            recBufSize);

        } catch (Exception e) {
            DoLog(e.getMessage());
            return -1;
        }

        // check that the audioRecord is ready to be used
        if (_audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            // DoLog("rec not initialized " + sampleRate);
            return -1;
        }

        // DoLog("rec sample rate set to " + sampleRate);

        return _bufferedRecSamples;
    }

    @SuppressWarnings("unused")
    private int StartRecording() {
        // start recording
        try {
            _audioRecord.startRecording();
            if (Build.VERSION.SDK_INT > 13)
			{
				// 如果4.0以上的手机走java采集，那么默认关闭AGC，AEC和NS
				int sessionId = _audioEff.getAudioSessionId(_audioRecord);
				if (-1 != sessionId)
				{
					FileLog.log(TAG, "audioSessionId = " + sessionId);
					LocalAudioDetector detect = LocalAudioCapabilityFactory.create();
					if (null != detect)
					{
						if (detect.haveAGC())
						{
							if (null != m_audioEffectOperatDevice && m_audioEffectOperatDevice.isOpenAGC())
							{
								FileLog.log(TAG, "Open AGC.");
								_audioEff.open(AudioDeviceEffect.EFFECT_TYPE_AGC, sessionId);
							} else
							{
								FileLog.log(TAG, "Close AGC.");
								_audioEff.close(AudioDeviceEffect.EFFECT_TYPE_AGC, sessionId);
							}
						}

						if (detect.haveAEC())
						{

							if (null != m_audioEffectOperatDevice && m_audioEffectOperatDevice.isOpenAEC())
							{
								FileLog.log(TAG, "Open AEC.");
								_audioEff.open(AudioDeviceEffect.EFFECT_TYPE_AEC, sessionId);
							} else
							{
								FileLog.log(TAG, "Close AEC.");
								_audioEff.close(AudioDeviceEffect.EFFECT_TYPE_AEC, sessionId);
							}
						}

						if (detect.haveNS())
						{
							if (null != m_audioEffectOperatDevice && m_audioEffectOperatDevice.isOpenAEC())
							{
								FileLog.log(TAG, "Open NS.");
								_audioEff.open(AudioDeviceEffect.EFFECT_TYPE_NS, sessionId);
							} else
							{
								FileLog.log(TAG, "Close NS.");
								_audioEff.close(AudioDeviceEffect.EFFECT_TYPE_NS, sessionId);
							}
						}
					}
				} else
				{
					FileLog.log_w(TAG, "get audio session failed.");
				}
			}

        } catch (IllegalStateException e) {
            e.printStackTrace();
            return -1;
        }

        _isRecording = true;
        return 0;
    }

    @SuppressWarnings("unused")
    private int StopRecording() {
        _recLock.lock();
        try {
            // only stop if we are recording
            if (_audioRecord.getRecordingState() ==
              AudioRecord.RECORDSTATE_RECORDING) {
                // stop recording
                try {
                    _audioRecord.stop();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return -1;
                }
            }

            // release the object
            _audioRecord.release();
            _audioRecord = null;

        } finally {
            // Ensure we always unlock, both for success, exception or error
            // return.
            _doRecInit = true;
            _recLock.unlock();
        }

        _isRecording = false;
        return 0;
    }

    @SuppressWarnings("unused")
    private int RecordAudio(int lengthInBytes) {
        _recLock.lock();

        try {
            if (_audioRecord == null) {
                return -2; // We have probably closed down while waiting for rec
                           // lock
            }

            // Set priority, only do once
            if (_doRecInit == true) {
                try {
                    android.os.Process.setThreadPriority(
                        android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                } catch (Exception e) {
                    DoLog("Set rec thread priority failed: " + e.getMessage());
                }
                _doRecInit = false;
            }

            int readBytes = 0;
            _recBuffer.rewind(); // Reset the position to start of buffer
            readBytes = _audioRecord.read(_tempBufRec, 0, lengthInBytes);
            // DoLog("read " + readBytes + "from SC");
            _recBuffer.put(_tempBufRec);

            if (readBytes != lengthInBytes) {
                // DoLog("Could not read all data from sc (read = " + readBytes
                // + ", length = " + lengthInBytes + ")");
                return -1;
            }

        } catch (Exception e) {
            DoLogErr("RecordAudio try failed: " + e.getMessage());

        } finally {
            // Ensure we always unlock, both for success, exception or error
            // return.
            _recLock.unlock();
        }

        return _bufferedRecSamples;
    }

    final String logTag = "WebRTC AD java";

    private void DoLog(String msg) {
        Log.d(logTag, msg);
    }

    private void DoLogErr(String msg) {
        Log.e(logTag, msg);
    }
}
