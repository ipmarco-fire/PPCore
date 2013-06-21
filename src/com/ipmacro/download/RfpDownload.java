package com.ipmacro.download;

import android.content.Context;

import com.ipmacro.ppcore.PPCore;
import com.ipmacro.ppcore.Rfp;

public class RfpDownload extends BaseDownload {
	String mFileId;

	public RfpDownload(Context context) {
		super(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ipmacro.ppcore.BaseDownload#start(java.lang.String)
	 */
	@Override
	public void start(String playUrl) {
		super.start(playUrl);
		String[] arr1 = playUrl.split("\\?");
		mFileId = arr1[0].substring(6);

		String[] arr2 = arr1[1].split("&");
		String[] trackerArr = arr2[0].split("=");
		String[] tracker = trackerArr[1].split(":");

		Rfp.start(mFileId);
		Rfp.addPeer(mFileId, Integer.parseInt(tracker[0]), "Tracker",
				tracker[1], Short.parseShort(tracker[2]), false);
	}

	@Override
	public int getProgress() {
		return Rfp.getProgress(mFileId);
	}

	@Override
	public int getRate() {
		return Rfp.getRate(mFileId) / 1024; // ∂‡…Ÿk
	}

	@Override
	public void stop() {
		Rfp.stop(mFileId);
	}

	@Override
	public String getPlayUrl() {
		return PPCore.getHttpUrl() + "RfpCast/" + mFileId;
	}
}
