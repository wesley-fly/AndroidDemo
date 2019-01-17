package com.internal.webrtc;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.AudioEffect.OnControlStatusChangeListener;
import android.media.audiofx.AudioEffect.OnEnableStatusChangeListener;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;

import com.internal.voipmedia.util.Print;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class AudioDeviceEffect {

	private String TAG = getClass().getSimpleName();
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
	
	private AudioEffect agc;
	private AudioEffect aec;
	private AudioEffect ns;
	
	@SuppressLint("NewApi")
	public void open(UUID type, int sessionID)
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
	public void close(UUID type, int sessionID)
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

	@SuppressLint("NewApi")
	public void releaseAudioEfect()
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
					Print.i(TAG, "SessionID = " + object.toString());
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

}
