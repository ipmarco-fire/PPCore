package com.ipmacro.ppcore;

import android.content.Context;

public class M3U8Download extends TsDownload {

	public M3U8Download(Context context) {
		super(context);
	}

	@Override
	public void start(String playUrl) {
		String tmp = playUrl.replace("http://", "m3u8://");
		super.start(tmp);
		mPlayUrl = playUrl;
	}
}
