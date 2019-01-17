package com.internal.voipmedia.util;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RuntimeExecu
{
	/** 前台进程名称 */
	public static final String UI_APPLICATION_PROCESS_NAME = "com.browan.freeppmobile.android";
	/** 后台进程名称 */
	public static final String DAEMON_APPLICATION_PROCESS_NAME = "com.browan.freeppmobile.daemon";

	private static final String TAG = RuntimeExecu.class.getSimpleName();

	/***
	 * cpu一些信息
	 * 
	 * @author Zhao
	 * 
	 */
	public static class CpuInfo
	{
		public boolean isArmV7;
		public boolean haveVfP;
		public String hardware;

		@Override
		public String toString()
		{
			return "isArmV7:" + isArmV7 + ", haveVfp:" + haveVfP + ", hardware:" + hardware;
		}
	}

	/**
	 * 判断当前设备的CPU是否支持armV7
	 */
	public static boolean isArmeabiV7a()
	{
		File cpuInfo = new File("/proc/cpuinfo");

		if (null != cpuInfo && cpuInfo.exists() && cpuInfo.canRead())
		{
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(cpuInfo), "UTF-8"), 8192);
				String line = reader.readLine();
				if (!TextUtils.isEmpty(line))
				{
					Print.d(TAG, "line = " + line);
					String content = line.toLowerCase(Locale.US).replace(" +", "");
					Print.d(TAG, "content = " + content);
					if (content.contains("armv7"))
					{
						return true;
					}

				}
			} catch (Exception e)
			{
				e.printStackTrace();
			} finally
			{
				if (null != reader)
				{
					try
					{
						reader.close();
					} catch (IOException e)
					{
					}
					reader = null;
				}
			}
		}
		return false;
	}

	public static void execute(String cmd)
	{
		Print.i(TAG, "execute cmd : " + cmd);
		java.lang.Process process = null;
		try
		{
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"),8192);
			String line = null;
			while (null != (line = reader.readLine()))
			{
				Print.i(TAG, line);
			}

		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != process)
			{
				process.destroy();
			}
		}

	}

	/**
	 * 获取CPU性能信息
	 * 
	 * @return {@link CpuInfo}
	 */
	public static CpuInfo getCpuInfo()
	{
		CpuInfo info = new CpuInfo();
		Map<String, String> map = new HashMap<String, String>();
		File cpuInfo = new File("/proc/cpuinfo");
		if (null != cpuInfo && cpuInfo.exists() && cpuInfo.canRead())
		{
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(cpuInfo), "UTF-8"), 8192);
				String line = null;
				boolean isFirst = true;
				while (null != (line = reader.readLine()))
				{
					String content = line.toLowerCase(Locale.US).trim();
					Print.d(TAG, content);
					if (isFirst)
					{
						isFirst = false;
						if (content.contains("armv7"))
						{
							info.isArmV7 = true;
						}
					}
					String[] str = content.split(":");
					if (null != str && 2 == str.length)
					{
						map.put(str[0].trim(), str[1].trim());
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			} finally
			{
				if (null != reader)
				{
					try
					{
						reader.close();
					} catch (IOException e)
					{
					}
					reader = null;
				}
			}
		}

		String features = map.get("features");
		if (!TextUtils.isEmpty(features))
		{
			if (features.contains("vfp"))
			{
				info.haveVfP = true;
			}
		}
		info.hardware = map.get("hardware");
		return info;
	}

	/**
	 * 当前进程是不是UI进程
	 * 
	 * @return true是UI进程，false其他
	 */
	public static boolean isUiApplicationProcess(Context c)
	{
		// String content = readFileContent("/proc/" + Process.myPid() +
		// "/cmdline").trim();
		String content = getCurProcessName(c);
		Print.d(TAG, "content = " + content);
		if (UI_APPLICATION_PROCESS_NAME.equals(content))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/** 获取当前进程名称 */
	public static String getCurProcessName(Context context)
	{
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses())
		{
			if (appProcess.pid == pid)
			{
				Print.e(TAG, "processName = " + appProcess.processName);

				return appProcess.processName;
			}
		}
		return null;
	}

	/**
	 * 获取指定路径文件的内容
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 文件里面的内容（如果文件不存在，或者路径不合法那么返回为null）
	 */
	public static String readFileContent(String filePath)
	{

		if (TextUtils.isEmpty(filePath))
		{
			Print.w(TAG, "readFileContent faile ,because filepath was empty.");
			return null;
		}
		File file = new File(filePath);
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = null;
			while (null != (line = reader.readLine()))
			{
				if (builder.length() > 0)
				{
					builder.append("\n");
				}
				builder.append(line);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != reader)
			{
				try
				{
					reader.close();
				} catch (IOException e)
				{
				}
				reader = null;
			}
		}
		return builder.toString();
	}

}