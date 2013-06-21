package com.ipmacro.ppcore;

public class TsApple {

	public TsApple(String Url) {
		synchronized (PPCore.mMutex) {
			nativeSetup(Url);
		}
	}

	public int getProgress() {
		synchronized (PPCore.mMutex) {
			return nativeGetProgress();
		}
	}

	public int getRate() {
		synchronized (PPCore.mMutex) {
			return nativeGetRate();
		}
	}

	public void finalize() {
		synchronized (PPCore.mMutex) {
			nativeRelease();
		}
	}

	public void release() {
		synchronized (PPCore.mMutex) {
			nativeRelease();
		}
	}

	public String getUrl() {
		synchronized (PPCore.mMutex) {
			return PPCore.getHttpUrl() + "TsApple/" + mNativeContext
					+ "/playlist.m3u8";
		}
	}

	private int mNativeContext;

	private native boolean nativeSetup(String Url);

	private native void nativeRelease();

	private native int nativeGetProgress();

	private native int nativeGetRate();

	private static native void nativeInit();

	static {
		nativeInit();
	}
}
