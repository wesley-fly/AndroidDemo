package com.internal.voipmedia.util;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

public class FileLog {
	private static final String TAG = FileLog.class.getSimpleName();
	
	private static boolean IS_DEBUG = false;
	
	// 一天对应的毫秒数
	private static final long DAY_OF_MILLISECONDS = 24 * 60 * 60 * 1000;

	/** 10天对应的毫秒数 */
	private static final long EXPIRED_TIME = 10 * DAY_OF_MILLISECONDS;

	private static final ReentrantLock m_lock = new ReentrantLock(true);

	/** 日志存储的文件夹 */
	private static final String LOG_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/voipsdk/log/";

	private static FileOutputStream m_systemLog = null;
	private static final ThreadLocal<DateFormat> m_formatter = new ThreadLocal<DateFormat>() {
		protected DateFormat initialValue() {
			return new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
		};
	};

	private static boolean m_intialized = false;

	private static void intialize() throws IOException {
		try {
			DateFormat date = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			// /初始化新的日志文件/////////
			File logFile = new File(LOG_FOLDER + date.format(System.currentTimeMillis()) + ".log");
			if (!logFile.exists()) {
				// ////删除久的日志文件///////
				deleteOlderLogFile(date);
				// /////////
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
			}
			m_systemLog = new FileOutputStream(logFile, true);
			m_intialized = true;
		} catch (Exception e) {
			// e.printStackTrace();
		}

	}

	/**
	 * 从现在开始算，删除10天之前的日志文件
	 * 
	 * @param format
	 */
  
	private static void deleteOlderLogFile(DateFormat format) {
		final DateFormat date = format;
		// 这里启线程的原因是，这个类可能会在UI线程第一次调用，为了避免阻塞UI，所以启动一个线程来删除不用的日志文件
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				File logFileDirect = new File(LOG_FOLDER);
				if (!logFileDirect.exists()) {
					Print.i(TAG, "LogFileDirect was not exists.");
					return; 
				}

				File[] logs = logFileDirect.listFiles();
				if (null != logs) {
					long nowTime = System.currentTimeMillis();
					for (File f : logs) {
						String name = f.getName();
						Print.i(TAG, "name = " + name);
						int index = name.lastIndexOf('.');
						if (index > 0) {
							String dateStr = name.substring(0, index);
							try {
								Date d = date.parse(dateStr);
								// 如果日志的时间大于10天那么删除
								if (Math.abs((nowTime - d.getTime())) > EXPIRED_TIME) {
									Print.w(TAG, "file log " + name + " was expired, so delete.");
									f.delete();
								}

							} catch (Exception e) {
								Print.w(TAG, "Parse old file log ", e);
								f.delete();
							}
						} else {
							f.delete();
						}
					}
				}
			}
		});
		thread.setName("Delete_Old_Log_File");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	public static void log(String tag, Object str) {
		str = String.valueOf(str);
		log(String.format("[%s] (%s): %s\n", m_formatter.get().format(new Date()), tag, str).getBytes());
		Print.i(tag, str);
	}

	public static void log_w(String tag, String msg) {
		log(String.format("[%s] (%s): %s\n", m_formatter.get().format(new Date()), tag, msg).getBytes());
		Print.w(tag, msg);
	}

	public static void log_e(String tag, String msg, Throwable e) {
		log(String.format("[%s] (%s): %s\n", m_formatter.get().format(new Date()), tag, msg).getBytes());
		Print.e(tag, msg, e);
	}

	private static void log(final byte[] buffer) {
		if(!IS_DEBUG)
			return;
		if (!m_intialized) {
			try {
				intialize();
			} catch (Exception ex) {
				// Print.w(TAG, "Intialize FileLog failed", ex);
			}
		}
		if (m_systemLog == null) {
			m_intialized = false;
			return;
		}

		FileLock lock = null;
		m_lock.lock();
		try {
			// 这个锁的用处是为了防止多个进程操作一个文件，用这把锁来同步。
			lock = m_systemLog.getChannel().lock();
			// ///所有的线程 在这里排队等待锁//////
			if (m_systemLog == null) {
				m_intialized = false;
				return;
			}
			m_systemLog.write(buffer);
		} catch (Exception e1) {
			e1.printStackTrace();
			close();
		} finally {
			if (null != lock) {
				try {
					lock.release();
				} catch (IOException e) {
					// ignore
				}
			}
			m_lock.unlock();
		}
	}

	private static void close() {
		m_intialized = false;
		Utils.safeClose(m_systemLog);
		m_systemLog = null;
	}

}
