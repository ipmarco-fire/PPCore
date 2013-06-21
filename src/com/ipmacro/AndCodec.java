package com.ipmacro;

import android.util.Log;

abstract class AndCodec {

	String mName;
	AndExtractor.Track mTrack;
	AndPlayer mPlayer;

	AndCodec(AndExtractor.Track Track, String Name, AndPlayer Player) {
		mTrack = Track;
		mName = Name;
		mPlayer = Player;
	}

	boolean setup() {
		return true;
	}

	void release() {
		mTrack = null;
		mPlayer = null;
	}

	void play() {
	}

	void pause() {
	}

	void flush() {
		mPts = INVALID_PTS;
	}

	static final long INVALID_PTS = 0x8000000000000000l;
	long mPts = INVALID_PTS;

	long getPts() {
		if (mPts == INVALID_PTS) {
			return 0;
		}
		return mPts - getDelay();
	}

	long getDelay() {
		return 0;
	}

	abstract void render(long stc);

	abstract void decode();

	abstract boolean isEof();

	void dump(long stc) {
		Log.i(mName, "pts=" + mPts / 1000000f + " delay=" + (stc - mPts)
				/ 1000000f);
	}

}
