package com.ipmacro;

class AndStc {

	private boolean mRun = false;
	private long mTime = 0;

	private static long now() {
		return System.currentTimeMillis() * 1000;
	}

	long getTime() {
		if (!mRun) {
			return mTime;
		}
		return now() - mTime;
	}

	void setTime(long Time) {
		if (!mRun) {
			mTime = Time;
			return;
		}
		mTime = now() - Time;
	}

	void play() {
		if (mRun) {
			return;
		}
		mTime = now() - mTime;
		mRun = true;
	}

	void stop() {
		if (!mRun) {
			return;
		}
		mTime = now() - mTime;
		mRun = false;
	}
}
