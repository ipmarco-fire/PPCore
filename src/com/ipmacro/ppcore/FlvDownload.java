package com.ipmacro.ppcore;

import com.ipmacro.FlvApple;

import android.content.Context;

public class FlvDownload extends BaseDownload {

	FlvApple mFlv;

	public FlvDownload(Context context) {
		super(context);
	}

	public void start(String playUrl) {
		super.start(playUrl);
		if (mFlv != null) {
			mFlv.release();
			mFlv = null;
		}
		mFlv = new FlvApple(playUrl);
	}

	@Override
	public int getProgress() {
		if (mFlv != null) {
			return mFlv.getProgress();
		}
		return 0;
	}

	@Override
	public int getRate() {
		if (mFlv != null) {
			return mFlv.getRate();
		}
		return 0;
	}

	@Override
	public void stop() {
		if (mFlv != null) {
			mFlv.release();
			mFlv = null;
		}
	}

	@Override
	public String getPlayUrl() {
		if (mFlv != null) {
			return mFlv.getUrl();
		}
		return null;
	}

}
