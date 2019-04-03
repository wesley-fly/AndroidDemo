/*
 *  Copyright (c) 2011 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

/*
 *  Android audio device test app
 */

package com.internal.webrtc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.AudioEffect.OnControlStatusChangeListener;
import android.media.audiofx.AudioEffect.OnEnableStatusChangeListener;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.util.Log;

import com.internal.voipmedia.util.DeviceUtil;
import com.internal.webrtc.adapter.AudioEffectOperatDevice;
import com.internal.webrtc.adapter.LocalAudioCapabilityFactory;
import com.internal.webrtc.adapter.LocalAudioDetector;
import com.internal.voipmedia.util.FileLog;
import com.internal.voipmedia.util.Print;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class AudioDeviceAndroid
{

	/**
	 * UUID for Automatic Gain Control (AGC) audio pre-processing
	 */
	static final UUID EFFECT_TYPE_AGC = UUID.fromString("0a8abfe0-654c-11e0-ba26-0002a5d5c51b");

	/**
	 * UUID for Acoustic Echo Canceler (AEC) audio pre-processing
	 * 
	 */
	static final UUID EFFECT_TYPE_AEC = UUID.fromString("7b491460-8d4d-11e0-bd61-0002a5d5c51b");

	/**
	 * UUID for Noise Suppressor (NS) audio pre-processing
	 * 
	 */
	static final UUID EFFECT_TYPE_NS = UUID.fromString("58b4b260-8e06-11e0-aa8e-0002a5d5c51b");

	/**
	 * Null effect UUID. Used when the UUID for effect type of
	 * 
	 */
	static final UUID EFFECT_TYPE_NULL = UUID.fromString("ec7178ec-e5e1-4432-a3f4-4657e6795210");

	private AudioTrack _audioTrack = null;
	private AudioRecord _audioRecord = null;
	private Context _context;
	private AudioManager _audioManager;

	private ByteBuffer _playBuffer;
	private ByteBuffer _recBuffer;
	private byte[] _tempBufPlay;
	private byte[] _tempBufRec;

	private final ReentrantLock _playLock = new ReentrantLock();
	private final ReentrantLock _recLock = new ReentrantLock();

	private boolean _doPlayInit = true;
	private boolean _doRecInit = true;
	private boolean _isRecording = false;
	private boolean _isPlaying = false;

	private int _bufferedRecSamples = 0;
	private int _bufferedPlaySamples = 0;
	private int _playPosition = 0;

	private AudioEffect agc;
	private AudioEffect aec;
	private AudioEffect ns;

	private static int STREAM_TYPE = AudioManager.STREAM_VOICE_CALL;
	
	private static AudioEffectOperatDevice m_audioEffectOperatDevice;

	/** 设置机型兼容性 */
	public static void setAudioEffectOperatDevice(AudioEffectOperatDevice audioEffectOperatDevice)
	{
		m_audioEffectOperatDevice = audioEffectOperatDevice;
	}
	
	AudioDeviceAndroid()
	{
		try
		{
			_playBuffer = ByteBuffer.allocateDirect(2 * 480); // Max 10 ms @ 48
																// kHz
			_recBuffer = ByteBuffer.allocateDirect(2 * 480); // Max 10 ms @ 48
																// kHz
		} catch (Exception e)
		{
			DoLog(e.getMessage());
		}

		_tempBufPlay = new byte[2 * 480];
		_tempBufRec = new byte[2 * 480];
	}

	public int InitRecording(int audioSource, int sampleRate)
	{
		DoLog("==========InitRecording============");
		// get the minimum buffer size that can be used
		int minRecBufSize = AudioRecord.getMinBufferSize(sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);

		int recBufSize = minRecBufSize * 2;
		_bufferedRecSamples = (5 * sampleRate) / 200;

		// release the object
		if (_audioRecord != null)
		{
			_audioRecord.release();
			_audioRecord = null;
		}

		try
		{
			_audioRecord = new AudioRecord(audioSource, sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					recBufSize);

		} catch (Exception e)
		{
			DoLog(e.getMessage());
			return -1;
		}

		if (_audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
		{
			return -1;
		}

		return _bufferedRecSamples;
	}

	@SuppressLint("NewApi")
	public int getAudioSessionId(AudioRecord recoder)
	{
		int id = -1;
		try
		{
			if (Build.VERSION.SDK_INT >= 16)
			{
				id = recoder.getAudioSessionId();
			} else if (Build.VERSION.SDK_INT > 13)
			{

				Method method = recoder.getClass().getMethod("getAudioSessionId");
				Object object = method.invoke(recoder);
				if (null != object)
				{
					DoLog("SessionID = " + object.toString());
					id = Integer.parseInt(object.toString());
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		
		return id;
	}

	public int StartRecording()
	{
		DoLog("==========StartRecording============");
		if (_isPlaying == false)
		{
			SetAudioMode(true);
		}

		try
		{
			_audioRecord.startRecording();

			if (Build.VERSION.SDK_INT > 13)
			{
				int sessionId = getAudioSessionId(_audioRecord);
				if (-1 != sessionId)
				{
					DoLog( "audioSessionId = " + sessionId);
					LocalAudioDetector detect = LocalAudioCapabilityFactory.create();
					if (null != detect)
					{
						if (detect.haveAGC())
						{
							if (null != m_audioEffectOperatDevice && m_audioEffectOperatDevice.isOpenAGC())
							{
								DoLog("Open AGC.");
								open(EFFECT_TYPE_AGC, sessionId);
							}
							else
							{
								DoLog( "Close AGC.");
								close(EFFECT_TYPE_AGC, sessionId);
							}
						}

						if (detect.haveAEC())
						{

							if (null != m_audioEffectOperatDevice && m_audioEffectOperatDevice.isOpenAEC())
							{
								DoLog("Open AEC.");
								open(EFFECT_TYPE_AEC, sessionId);
							}
							else
							{
								DoLog("Close AEC.");
								close(EFFECT_TYPE_AEC, sessionId);
							}
						}

						if (detect.haveNS())
						{
							if (null != m_audioEffectOperatDevice && m_audioEffectOperatDevice.isOpenAEC())
							{
								DoLog("Open NS.");
								open(EFFECT_TYPE_NS, sessionId);
							}
							else
							{
								DoLog("Close NS.");
								close(EFFECT_TYPE_NS, sessionId);
							}
						}
					}
				}
				else
				{
					DoLog("get audio session failed.");
				}
			}
		} catch (IllegalStateException e)
		{
			e.printStackTrace();
			return -1;
		}

		_isRecording = true;
		return 0;
	}

	public int InitPlayback(int sampleRate)
	{
		DoLog("==========InitPlayback============");
		// get the minimum buffer size that can be used
		int minPlayBufSize = AudioTrack.getMinBufferSize(sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);

		int playBufSize = minPlayBufSize;
		if (playBufSize < 6000)
		{
			playBufSize *= 2;
		}
		_bufferedPlaySamples = 0;

		// release the object
		if (_audioTrack != null)
		{
			_audioTrack.release();
			_audioTrack = null;
		}

		try
		{
			_audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
					sampleRate, 
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					playBufSize, 
					AudioTrack.MODE_STREAM);
		} catch (Exception e)
		{
			DoLog(e.getMessage());
			return -1;
		}

		// check that the audioRecord is ready to be used
		if (_audioTrack.getState() != AudioTrack.STATE_INITIALIZED)
		{
			DoLog("play not initialized " + sampleRate);
			try
			{
				_audioTrack.release();
			} catch (Exception e)
			{
			}
			DoLog("InitAudioTrack StreamVoiceCall failed, so try Stream_Music");
			try
			{
				//_audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);
				_audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);
			} catch (Exception e)
			{
				DoLog(e.getMessage());
				return -1;
			}

			if (_audioTrack.getState() != AudioTrack.STATE_INITIALIZED)
			{
				DoLog("InitAudioTrack try Stream_Music failed!");
				return -1;
			}
		}

		if (_audioManager == null && _context != null)
		{
			_audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
		}

		if (_audioManager == null)
		{
			// Don't know the max volume but still init is OK for playout,
			// so we should not return error.
			return 0;
		}
		
		_audioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
		_audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		return _audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
	}

	@SuppressWarnings("unused")
	private int StartPlayback()
	{
		DoLog("==========StartPlayback============");
		if (_isRecording == false)
		{
			SetAudioMode(true);
		}

		// start playout
		try
		{
			_audioTrack.play();

		} catch (IllegalStateException e)
		{
			e.printStackTrace();
			return -1;
		}

		_isPlaying = true;
		return 0;
	}

	@SuppressWarnings("unused")
	private int StopRecording()
	{
		DoLog("==========StopRecording============");
		_recLock.lock();
		try
		{
			// only stop if we are recording
			if (_audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
			{
				// stop recording
				try
				{
					Print.d(TAG, "begin stop audiorecord");
					_audioRecord.stop();
					Print.d(TAG, "end stop audiorecord");
				} catch (IllegalStateException e)
				{
					e.printStackTrace();
					return -1;
				}
				releaseAudioEfect();
			}

			// release the object
			_audioRecord.release();
			_audioRecord = null;

		} finally
		{
			// Ensure we always unlock, both for success, exception or error
			// return.
			_doRecInit = true;
			_recLock.unlock();
		}

		if (_isPlaying == false)
		{
			SetAudioMode(false);
		}

		_isRecording = false;
		return 0;
	}

	@SuppressWarnings("unused")
	private int StopPlayback()
	{
		DoLog("==========StopPlayback============");
		_playLock.lock();
		try
		{
			// only stop if we are playing
			if (_audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
			{
				// stop playout
				try
				{
					_audioTrack.stop();
				} catch (IllegalStateException e)
				{
					e.printStackTrace();
					return -1;
				}

				// flush the buffers
				_audioTrack.flush();
			}

			// release the object
			_audioTrack.release();
			_audioTrack = null;

		} finally
		{
			// Ensure we always unlock, both for success, exception or error
			// return.
			_doPlayInit = true;
			_playLock.unlock();
		}

		if (_isRecording == false)
		{
			SetAudioMode(false);
		}

		_isPlaying = false;
		return 0;
	}

	@SuppressWarnings("unused")
	private int PlayAudio(int lengthInBytes)
	{
		int bufferedSamples = 0;

		_playLock.lock();
		try
		{
			if (_audioTrack == null)
			{
				return -2; // We have probably closed down while waiting for
							// play lock
			}

			// Set priority, only do once
			if (_doPlayInit == true)
			{
				try
				{
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				} catch (Exception e)
				{
					DoLog("Set play thread priority failed: " + e.getMessage());
				}
				_doPlayInit = false;
			}

			int written = 0;
			_playBuffer.get(_tempBufPlay);
			written = _audioTrack.write(_tempBufPlay, 0, lengthInBytes);
			_playBuffer.rewind(); // Reset the position to start of buffer

			// DoLog("Wrote data to sndCard");

			// increase by number of written samples
			_bufferedPlaySamples += (written >> 1);

			// decrease by number of played samples
			int pos = _audioTrack.getPlaybackHeadPosition();
			if (pos < _playPosition)
			{ // wrap or reset by driver
				_playPosition = 0; // reset
			}
			_bufferedPlaySamples -= (pos - _playPosition);
			_playPosition = pos;

			if (!_isRecording)
			{
				bufferedSamples = _bufferedPlaySamples;
			}

			if (written != lengthInBytes)
			{
				// DoLog("Could not write all data to sc (written = " + written
				// + ", length = " + lengthInBytes + ")");
				return -1;
			}

		} finally
		{
			// Ensure we always unlock, both for success, exception or error
			// return.
			_playLock.unlock();
		}

		return bufferedSamples;
	}

	@SuppressWarnings("unused")
	private int RecordAudio(int lengthInBytes)
	{
		_recLock.lock();

		try{
			if (_audioRecord == null)
			{
				return -2; // We have probably closed down while waiting for rec
							// lock
			}

			// Set priority, only do once
			if (_doRecInit == true)
			{
				try
				{
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				} catch (Exception e)
				{
					DoLog("Set rec thread priority failed: " + e.getMessage());
				}
				_doRecInit = false;
			}

			int readBytes = 0;
			_recBuffer.rewind(); // Reset the position to start of buffer
			readBytes = _audioRecord.read(_tempBufRec, 0, lengthInBytes);

			// DoLog("read " + readBytes + "from SC");
			_recBuffer.put(_tempBufRec);

			if (readBytes != lengthInBytes)
			{
				// DoLog("Could not read all data from sc (read = " + readBytes
				// + ", length = " + lengthInBytes + ")");
				return -1;
			}

		}catch (Exception e)
		{
			DoLog("RecordAudio try failed: " + e.getMessage());

		}finally
		{
			// Ensure we always unlock, both for success, exception or error
			// return.
			_recLock.unlock();
		}

		return (_bufferedPlaySamples);
	}

	private void SetAudioMode(boolean startCall)
	{
		 DoLog("==========SetAudioMode============ startCall = " + startCall);
		 if (startCall) {
		 	// 进入通话
		 	if (_audioManager == null && _context != null) {
		 		_audioManager = (AudioManager)
		 		_context.getSystemService(Context.AUDIO_SERVICE);
		 	}

		 	if (_audioManager == null) {
				DoLog("Could not set audio mode - no audio manager");
		 		return;
		 	}


		 	int audioMode = DeviceUtil.getDeviceAudioMode();

		 	DoLog("==========SetAudioMode============ mode = " + audioMode);
		 	if (audioMode == _audioManager.getMode()) {
				DoLog("current mode is " + audioMode + ", so not set.");
		 	} else {
		 		_audioManager.setMode(audioMode);
			}
		 }
		 else
		 {
		 	// 通话结束 这不需要在把通话模式设置成 一般正常模式 会在状态机里面统一做处理。
		 	if (_audioManager == null && _context != null) {
		 		_audioManager = (AudioManager)
		 		_context.getSystemService(Context.AUDIO_SERVICE);
		 	}

			if (_audioManager == null) {
				DoLog("Could not set audio mode - no audio manager");
		 		return;
		 	}
		 	_audioManager.setMode(AudioManager.MODE_NORMAL);
		 	_audioManager.setSpeakerphoneOn(false);
		 }
	}

	final String logTag = "WebRTC AD java";

	private void DoLog(String msg)
	{
		Log.e(logTag, msg);
	}

	/**
	 * 返回当前设备播放声音采用的媒体类型
	 * 
	 * @return 0走通话音量，1走openSL默认的 媒体音量
	 */
	public int getStreamType()
	{
		String model = Build.MODEL.replaceAll(" +", "");
		String brand = android.os.Build.BRAND;

		int streamType = 0;

		if (model != null)
			model = model.toLowerCase(Locale.getDefault());
		else
			model = "unknown";

		if (brand != null)
			brand = brand.toLowerCase(Locale.getDefault());
		else
			brand = "unknown";

		DoLog( "model:" + model + ", brand:" + brand);

		/*if ("xiaomi".equals(brand))
		{
			streamType = 1;
		}
		else if ("sony".equals(brand))
		{
			if ("l36h".equals(model))
			{
				streamType = 1;
			}
		}
		else if ("huawei".equals(brand))
		{
			if ("huaweig520-0000".equals(model))
			{
				streamType = 1;
			}
		}
		else if ("samsung".equals(brand)) {
			if ("gt-i9300".equals(model))
				streamType = 1;
		}*/

		DoLog( "streamType = " + streamType);

		if (1 == streamType)
		{
			STREAM_TYPE = AudioManager.STREAM_MUSIC;
		}

		return streamType;
	}

	public static int getStreamControlType()
	{
		return STREAM_TYPE;
	}

	private String TAG = getClass().getSimpleName();

	@SuppressLint("NewApi")
	private void releaseAudioEfect()
	{
		Print.d(TAG, "start releaseAudioEfect.");
		if (null != agc)
		{
			Print.i(TAG, "release agc");
			agc.release();
			agc = null;
		}
		if (null != aec)
		{
			Print.i(TAG, "release aec");
			aec.release();
			aec = null;
		}

		if (null != ns)
		{
			Print.i(TAG, "release ns");
			ns.release();
			ns = null;
		}
		Print.d(TAG, "end releaseAudioEfect.");
	}

	@SuppressLint("NewApi")
	private void close(UUID type, int sessionID)
	{
		if (Build.VERSION.SDK_INT > 15)
		{
			closeNewApi(type, sessionID);
			return;
		}

		Class<?> demo = null;
		try
		{
			demo = Class.forName("android.media.audiofx.AudioEffect");

			Constructor<?> cons[] = demo.getConstructors();
			Print.i(TAG, "cons.length = " + cons.length);
			AudioEffect efect = (AudioEffect) cons[0].newInstance(type, EFFECT_TYPE_NULL, 0, sessionID);
			if (null != efect)
			{

				efect.setControlStatusListener(new OnControlStatusChangeListener()
				{
					@Override
					public void onControlStatusChange(AudioEffect effect, boolean controlGranted)
					{
						Print.i(TAG, "onControlStatusChange id = " + effect.getId() + ", controlGranted = " + controlGranted);
					}
				});

				efect.setEnableStatusListener(new OnEnableStatusChangeListener()
				{
					@Override
					public void onEnableStatusChange(AudioEffect effect, boolean enabled)
					{
						// TODO Auto-generated method stub
						Print.i(TAG, "onEnableStatusChange id = " + effect.getId() + ", enabled = " + enabled);
					}
				});

				boolean enable = efect.getEnabled();
				Print.i(TAG, "default enable = " + enable);
				Print.i(TAG, "id = " + efect.getId());
				// if (!enable) {
				int result = efect.setEnabled(false);
				if (AudioEffect.SUCCESS == result)
				{
					Print.i(TAG, "Close success!");
				} else
				{
					Print.i(TAG, "Close failed result  = " + result);
				}

				if (EFFECT_TYPE_AGC.equals(type))
				{
					agc = efect;
				} else if (EFFECT_TYPE_AEC.equals(type))
				{
					aec = efect;
				} else if (EFFECT_TYPE_NS.equals(type))
				{
					ns = efect;
				}
			}
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	private void open(UUID type, int sessionID)
	{
		if (Build.VERSION.SDK_INT > 15)
		{
			openNewApi(type, sessionID);
			return;
		}

		Class<?> demo = null;
		try
		{
			demo = Class.forName("android.media.audiofx.AudioEffect");

			Constructor<?> cons[] = demo.getConstructors();
			Print.i(TAG, "cons.length = " + cons.length);
			AudioEffect efect = (AudioEffect) cons[0].newInstance(type, EFFECT_TYPE_NULL, 0, sessionID);
			if (null != efect)
			{

				efect.setControlStatusListener(new OnControlStatusChangeListener()
				{

					@Override
					public void onControlStatusChange(AudioEffect effect, boolean controlGranted)
					{
						// TODO Auto-generated method stub

						Print.i(TAG, "onControlStatusChange id = " + effect.getId() + ", controlGranted = " + controlGranted);

					}
				});

				efect.setEnableStatusListener(new OnEnableStatusChangeListener()
				{

					@Override
					public void onEnableStatusChange(AudioEffect effect, boolean enabled)
					{
						// TODO Auto-generated method stub
						Print.i(TAG, "onEnableStatusChange id = " + effect.getId() + ", enabled = " + enabled);
					}
				});

				boolean enable = efect.getEnabled();
				Print.i(TAG, "default enable = " + enable);
				Print.i(TAG, "id = " + efect.getId());
				// if (!enable) {
				int result = efect.setEnabled(true);
				if (AudioEffect.SUCCESS == result)
				{
					Print.i(TAG, "open success");
				} else
				{
					Print.i(TAG, "open failed result  = " + result);
				}

				if (EFFECT_TYPE_AGC.equals(type))
				{
					agc = efect;
				} else if (EFFECT_TYPE_AEC.equals(type))
				{
					aec = efect;
				} else if (EFFECT_TYPE_NS.equals(type))
				{
					ns = efect;
				}
				// }
			}
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	public void openNewApi(UUID type, int sessionID)
	{
		AudioEffect efect = null;
		if (EFFECT_TYPE_AGC.equals(type))
		{
			efect = AutomaticGainControl.create(sessionID);
			agc = efect;
		} else if (EFFECT_TYPE_AEC.equals(type))
		{
			efect = AcousticEchoCanceler.create(sessionID);
			aec = efect;
		} else if (EFFECT_TYPE_NS.equals(type))
		{
			efect = NoiseSuppressor.create(sessionID);
			ns = efect;
		}
		if (null != efect)
		{

			efect.setControlStatusListener(new OnControlStatusChangeListener()
			{

				@Override
				public void onControlStatusChange(AudioEffect effect, boolean controlGranted)
				{
					// TODO Auto-generated method stub
					Print.i(TAG, "onControlStatusChange id = " + effect.getId() + ", controlGranted = " + controlGranted);

				}
			});

			efect.setEnableStatusListener(new OnEnableStatusChangeListener()
			{

				@Override
				public void onEnableStatusChange(AudioEffect effect, boolean enabled)
				{
					// TODO Auto-generated method stub
					Print.i(TAG, "onEnableStatusChange id = " + effect.getId() + ", enabled = " + enabled);
				}
			});

			boolean enable = efect.getEnabled();
			Print.i(TAG, "default enable = " + enable);
			Print.i(TAG, "id = " + efect.getId());
			// if (!enable) {
			int openResult = efect.setEnabled(true);
			if (AudioEffect.SUCCESS == openResult)
			{
				Print.i(TAG, "Open Sucess!");
			} else
			{
				Print.i(TAG, "Open Failed, result = " + openResult);
			}
			// }
		}
	}

	@SuppressLint("NewApi")
	public void closeNewApi(UUID type, int sessionID)
	{
		AudioEffect efect = null;
		if (EFFECT_TYPE_AGC.equals(type))
		{
			efect = AutomaticGainControl.create(sessionID);
			agc = efect;
		} else if (EFFECT_TYPE_AEC.equals(type))
		{
			efect = AcousticEchoCanceler.create(sessionID);
			aec = efect;
		} else if (EFFECT_TYPE_NS.equals(type))
		{
			efect = NoiseSuppressor.create(sessionID);
			ns = efect;
		}
		if (null != efect)
		{

			efect.setControlStatusListener(new OnControlStatusChangeListener()
			{

				@Override
				public void onControlStatusChange(AudioEffect effect, boolean controlGranted)
				{
					// TODO Auto-generated method stub

					Print.i(TAG, "onControlStatusChange id = " + effect.getId() + ", controlGranted = " + controlGranted);

				}
			});

			efect.setEnableStatusListener(new OnEnableStatusChangeListener()
			{

				@Override
				public void onEnableStatusChange(AudioEffect effect, boolean enabled)
				{
					// TODO Auto-generated method stub
					Print.i(TAG, "onEnableStatusChange id = " + effect.getId() + ", enabled = " + enabled);
				}
			});

			boolean enable = efect.getEnabled();
			Print.i(TAG, "default enable = " + enable);
			Print.i(TAG, "id = " + efect.getId());
			// if (!enable) {
			int openResult = efect.setEnabled(false);
			if (AudioEffect.SUCCESS == openResult)
			{
				Print.i(TAG, "Close Sucess!");
			} else
			{
				Print.i(TAG, "Close Failed , Result = " + openResult);
			}
			// }
		}
	}
}
