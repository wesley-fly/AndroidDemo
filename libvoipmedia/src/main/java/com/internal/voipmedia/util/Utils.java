package com.internal.voipmedia.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Random;

public class Utils
{
	/**
	 * 生成n位的随机数
	 *
	 * @param n
	 * @return string类型的随机数
	 */
	public static String generatePassword(int n) {
		Random rand = new Random();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < n; i++) {
			char c = (char) (0x30 + rand.nextInt(10));
			sb.append(c);
		}
		return sb.toString();
	}
	/**
	 * 进行{@link java.net.URLDecoder#decode(String, String)}
	 * 
	 * @param content
	 *            要URLDecoder的内容
	 * @return
	 */
	public static String decode(String content)
	{
		try
		{
			if (!TextUtils.isEmpty(content))
			{
				return URLDecoder.decode(content, "UTF-8");
			}
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * 关闭输出流（如果关闭的时候有异常，那么把异常吃掉）
	 * 
	 * @param stream
	 *            流
	 */
	public static void safeClose(Closeable stream)
	{
		if (stream != null)
		{
			try
			{
				stream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 检查参数是否有为空的字符串
	 * 
	 * @param parameters
	 * @return 如果没有空的字符串返回true，其他返回false
	 */
	public static boolean notEmpty(String... parameters)
	{
		if (null == parameters || 0 == parameters.length)
		{
			return false;
		}
		int len = parameters.length;
		for (int i = 0; i < len; i++)
		{
			if (TextUtils.isEmpty(parameters[i]))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断网络是否可用
	 * 
	 * @param context
	 *            {@link Context}  android上下文引用
	 * @return 如果网络可用true，false其他情况
	 */
	public static synchronized boolean isNetworkAvaliable(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetInfo = cm.getActiveNetworkInfo();
		if (null == activeNetInfo || !activeNetInfo.isAvailable() || !activeNetInfo.isConnected())
		{
			return false;
		}
		return true;
	}
}