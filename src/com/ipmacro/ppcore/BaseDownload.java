package com.ipmacro.ppcore;

import android.content.Context;

public class BaseDownload {
	Context mContext;
	String mPlayUrl;

	public BaseDownload(Context context) {
		mContext = context;
	}

	public int getProgress() {
		return 100;
	}

	public void start(String playUrl) {
		mPlayUrl = playUrl;
	}

	public int getRate() {
		return 0;
	}

	public void stop() {

	}

	public String getSourceUrl(){
		return mPlayUrl;
	}
	
	public String getPlayUrl() {
		return mPlayUrl;
	}
}
