package com.ipmacro.ppcore;

import java.util.Date;
import java.util.LinkedList;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PPCore {

	private PPCore() {

	}

	public static boolean checkCdKey(String CdKey) {
		return nativeCheckCdKey(CdKey);
	}

	private static boolean mInited = false;

	public static boolean init(Context Ctx) {
		if (mInited) {
			return true;
		}
		if (!detectWifi(Ctx)) {
			return false;
		}
		mInited = nativeInit(Ctx);
		return mInited;
	}

	public static String getPwd() {
		if (!mInited) {
			return "";
		}
		return nativeGetPwd();
	}

	static byte[] mMutex = new byte[1];
	private static boolean mRun;

	private static class TaskThread extends Thread {
		public void run() {
			while (mRun) {
				synchronized (mMutex) {
					oneClick();
				}
				try {
					sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			nativeLogout();
			Log.i("PPCore", "Logout OK");
		}
	};

	private static TaskThread mThread;

	public static boolean login(String Token) {
		if (mRun) {
			return true;
		}
		if (!nativeLogin3(Token)) {
			return false;
		}
		Log.i("PPCore", "Login OK");
		Log.i("HttpUrl", getHttpUrl());
		Log.i("RtspUrl", getRtspUrl());
		Date d = new Date();
		d.setTime(getExpire() * 1000);
		Log.i("Expire", d.toString());
		Log.i("Life", "" + getLife());
		mRun = true;
		mThread = new TaskThread();
		mThread.setName("PPCore");
		mThread.start();
		return true;
	}

	public static int getSN() {
		if (!mRun) {
			return 0;
		}
		return nativeGetSN();
	}

	public static int getLife() {
		if (!mRun) {
			return 0;
		}
		return nativeGetLife();
	}

	public static int getExpire() {
		if (!mRun) {
			return 0;
		}
		return nativeGetExpire();
	}

	public static String getRtspUrl() {
		if (!mRun) {
			return "";
		}
		return nativeGetRtspUrl();
	}

	public static String getHttpUrl() {
		if (!mRun) {
			return "";
		}
		return nativeGetHttpUrl();
	}

	public static boolean logout() {
		if (!mRun) {
			return false;
		}
		Log.i("PPCore", "Logout Pending");
		mRun = false;
		try {
			mThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (mThread.isAlive()) {
			Log.e("PPCore", "Thread Still Running");
		}
		mThread = null;
		return true;
	}

	private static String mMac;

	private static boolean detectWifi(Context Ctx) {
		if (mMac != null && !mMac.equals("")) {
			return true;
		}
		WifiManager wifiManager = (WifiManager) Ctx
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String mMac = wifiInfo.getMacAddress();
		if (mMac != null && !mMac.equals("")) {
			return true;
		}
		boolean last = wifiManager.isWifiEnabled();
		if (!last) {
			Log.i("PPCore", "Opening Wifi");
			wifiManager.setWifiEnabled(true);
		}
		int count = 0;
		long time = System.currentTimeMillis();
		while (count < 50) {
			wifiInfo = wifiManager.getConnectionInfo();
			mMac = wifiInfo.getMacAddress();
			if (mMac != null && !mMac.equals("")) {
				time = System.currentTimeMillis() - time;
				Log.i("PPCore", "Wifi Ready Time=" + time + " Count=" + count);
				break;
			}
			++count;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!last) {
			Log.i("PPCore", "Closing Wifi");
			wifiManager.setWifiEnabled(false);
		}
		if (mMac != null && !mMac.equals("")) {
			return true;
		}
		return false;
	}

	static abstract class Task {
		abstract void oneClick();
	}

	static LinkedList<Task> mTaskList = new LinkedList<Task>();

	private static void oneClick() {
		nativeOneClick();
		for (Task task : mTaskList) {
			task.oneClick();
		}
	}

	static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
		}
	};

	private static native boolean nativeInit(Context Ctx);

	private static native boolean nativeCheckCdKey(String CdKey);

	private static native String nativeGetPwd();

	private static native boolean nativeLogin3(String Token);

	private static native boolean nativeLogout();

	private static native boolean nativeOneClick();

	private static native int nativeGetSN();

	private static native int nativeGetLife();

	private static native int nativeGetExpire();

	private static native String nativeGetRtspUrl();

	private static native String nativeGetHttpUrl();

	static {
		System.loadLibrary("PPCoreJni");
	}
}
