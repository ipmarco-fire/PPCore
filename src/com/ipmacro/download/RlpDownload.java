package com.ipmacro.download;

import android.content.Context;

import com.ipmacro.ppcore.PPCore;
import com.ipmacro.ppcore.Rlp;

public class RlpDownload extends BaseDownload {
	String mFileID;

	public RlpDownload(Context context) {
		super(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ipmacro.ppcore.BaseDownload#start(java.lang.String) url格式:
	 * playUrl = 'rlp://'+ fileId+
	 * '?tracker='+peerId+':'+ip+':'+port&prefetch='+accelerationIp+':'+accelerationPort+':'+accelerationKey
	 */
	@Override
	public void start(String playUrl) {
		super.start(playUrl);
		String[] arr1 = playUrl.split("\\?");
		mFileID = arr1[0].substring(6);

		String[] arr2 = arr1[1].split("&");
		String[] trackerArr = arr2[0].split("=");
		String[] tracker = trackerArr[1].split(":");

		Rlp.start(mFileID);
		Rlp.addPeer(mFileID, Integer.parseInt(tracker[0]), "Tracker",
				tracker[1], Short.parseShort(tracker[2]), false);
		if (arr2.length == 2) {
			String[] prefetchArr = arr2[1].split("=");
			String[] prefetch = prefetchArr[1].split(":");
			Rlp.preFetch(mFileID, prefetch[0], Short.parseShort(prefetch[1]),
					prefetch[2]);
		}
	}

	@Override
	public int getProgress() {
		return Rlp.getProgress(mFileID);
	}

	@Override
	public int getRate() {
		return Rlp.getRate(mFileID) / 1024; // 多少k
	}

	@Override
	public void stop() {
		Rlp.stop(mFileID);
	}

	@Override
	public String getPlayUrl() {
		String playUrl = PPCore.getHttpUrl() + "Rlp/" + mFileID;
		return playUrl;
	}
}
