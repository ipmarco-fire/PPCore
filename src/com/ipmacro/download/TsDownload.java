package com.ipmacro.download;

import android.content.Context;

import com.ipmacro.ppcore.TsApple;

public class TsDownload extends BaseDownload {

	TsApple mTs;

	public TsDownload(Context context) {
		super(context);
	}

	public void start(String playUrl) {
		super.start(playUrl);
		if (mTs != null) {
			mTs.release();
			mTs = null;
		}
		mTs = new TsApple(playUrl);
	}

	@Override
	public int getProgress() {
		if (mTs != null) {
			return mTs.getProgress();
		}
		return 0;
	}

	@Override
	public int getRate() {
		if (mTs != null) {
			return mTs.getRate();
		}
		return 0;
	}

	@Override
	public void stop() {
		if (mTs != null) {
			mTs.release();
			mTs = null;
		}
	}

	@Override
	public String getPlayUrl() {
		if (mTs != null) {
			return mTs.getUrl();
		}
		return null;
	}

}
