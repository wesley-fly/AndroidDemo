package com.internal.voipmedia.util;

import android.util.Log;

public class Print {

	private static boolean IS_DEBUG = false;

	private static boolean ENABLE_E = true;
	private static boolean ENABLE_D = IS_DEBUG;
	private static boolean ENABLE_I = IS_DEBUG;
	private static boolean ENABLE_V = IS_DEBUG;
	private static boolean ENABLE_W = IS_DEBUG;

	public static void e(String tag, Object log) {
		if (ENABLE_E) {
			Log.e(tag, String.valueOf(log));
			// FileLog.log(tag + "_E", log);
		}
	}

	public static void d(String tag, Object log) {
		if (ENABLE_D) {
			Log.d(tag, String.valueOf(log));
			// FileLog.log(tag + "_D", log);
		}
	}

	public static void i(String tag, Object log) {
		if (ENABLE_I) {
			Log.i(tag, String.valueOf(log));
			// FileLog.log(tag + "_I", log);
		}
	}

	public static void v(String tag, Object log) {
		if (ENABLE_V) {
			Log.v(tag, String.valueOf(log));
			// FileLog.log(tag + "_V", log);
		}
	}

	public static void w(String tag, Object log) {
		if (ENABLE_W) {
			Log.w(tag, String.valueOf(log));
			// FileLog.log(tag + "_W", log);
		}

	}

	public static void e(String tag, Object log, Throwable tr) {
		if (ENABLE_E) {
			Log.e(tag, String.valueOf(log), tr);
			// FileLog.log(tag + "_E", log + tr);
		}
	}

	public static void d(String tag, Object log, Throwable tr) {
		if (ENABLE_D) {
			Log.d(tag, String.valueOf(log), tr);
			// FileLog.log(tag + "_D", log + tr);
		}
	}

	public static void i(String tag, Object log, Throwable tr) {
		if (ENABLE_I) {
			Log.i(tag, String.valueOf(log), tr);
			// FileLog.log(tag + "_I", log + tr);
		}
	}

	public static void v(String tag, Object log, Throwable tr) {
		if (ENABLE_V) {
			Log.v(tag, String.valueOf(log), tr);
			// FileLog.log(tag + "_V", log + tr);
		}
	}

	public static void w(String tag, Object log, Throwable tr) {
		if (ENABLE_W) {
			Log.w(tag, String.valueOf(log), tr);
			// FileLog.log(tag + "_W", log + tr);
		}

	}

}
