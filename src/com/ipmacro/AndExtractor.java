package com.ipmacro;

import java.util.ArrayList;
import java.util.LinkedList;

import android.util.Log;

class AndExtractor {

	AndExtractor() {

	}

	AndExtractor(String Url, int Mode) {
		nativeSetup(Url, Mode);
	}

	boolean setUrl(String Url, int Mode) {
		release();
		return nativeSetup(Url, Mode);
	}

	void release() {
		nativeRelease();
		mTrackList.clear();
		mAudioList.clear();
		mVideoList.clear();
	}

	public void finalize() {
		nativeRelease();
	}

	boolean next() {
		if (!nativeNext()) {
			return false;
		}
		mTrackList.clear();
		mAudioList.clear();
		mVideoList.clear();
		return true;
	}

	static final long INVALID_PTS = 0x8000000000000000L;

	static class Frame {
		public byte[] data;
		public long pts, dts;
		public boolean key;
	}

	class Track {
		public int index = 0, queue = 0;
		public float fps = 25;
		public String mime;
		public byte[] extra;
		private boolean enable = false;
		LinkedList<Frame> framelist = new LinkedList<Frame>();

		int getParam(String Name) {
			return nativeGetTrackParam(index, Name);
		}

		void update() {
			int q = nativeGetTrackQueue(index);
			queue = framelist.size() + q;
			long d = nativeGetTrackDuration(index);
			if (d > 500000) {
				fps = q * 1000000 / d;
			}
		}

		float getBuf() {
			return queue / fps;
		}

		void release() {
			framelist.clear();
			enable = false;
		}

		void setup() {
			enable = true;
		}

		boolean isEof() {
			return (nativeGetState() == aesEof)
					&& (nativeGetTrackQueue(index) == 0);
		}
	}

	ArrayList<Track> mTrackList = new ArrayList<Track>();
	ArrayList<Track> mVideoList = new ArrayList<Track>();
	ArrayList<Track> mAudioList = new ArrayList<Track>();

	void prepareTrack() {
		if (!mTrackList.isEmpty()) {
			return;
		}
		int n = nativeGetTrackCount();
		for (int i = 0; i < n; ++i) {
			Track t = new Track();
			t.index = i;
			t.mime = nativeGetTrackMime(i);
			t.extra = nativeGetTrackExtra(i);
			t.queue = nativeGetTrackQueue(i);
			Log.w("AndExtractor", t.mime);

			if (t.mime.startsWith("audio/")) {
				mAudioList.add(t);
				t.fps = 40;
			} else if (t.mime.startsWith("video/")) {
				mVideoList.add(t);
				t.fps = 25;
			}
			mTrackList.add(t);
		}
	}

	void updateTrack() {
		int n = mTrackList.size();
		for (int i = 0; i < n; ++i) {
			Track t = mTrackList.get(i);
			t.update();
		}
	}

	void decode() {
		int j = -1;
		for (Track t : mTrackList) {
			if (!t.enable) {
				continue;
			}
			if (t.framelist.size() < t.fps) {
				j = t.index;
				break;
			}
		}
		if (j == -1) {
			return;
		}
		int k = 0;
		while (k < 40) {
			int i = nativeGetFrameIndex();
			if (i < 0) {
				return;
			}
			if (!mTrackList.get(i).enable) {
				nativePopFrame();
				continue;
			}
			Frame f = new Frame();
			// f.data = getFrameData();
			// f.dts = getFrameDts();
			// f.pts = getFramePts();
			nativeGetFrame(f);
			nativePopFrame();
			Track t = mTrackList.get(i);
			t.framelist.add(f);
			++k;
		}
	}

	private int mNativeContext;

	private native boolean nativeSetup(String Url, int Mode);

	private native void nativeRelease();

	static final int aesInit = 0;
	static final int aesFaild = 1;

	static final int aesWait = 2;
	static final int aesReady = 3;
	static final int aesEof = 4;

	static final int aesEol = 5;

	native int nativeGetState();

	private native boolean nativeNext();

	private native int nativeGetTrackCount();

	private native String nativeGetTrackMime(int Index);

	private native byte[] nativeGetTrackExtra(int Index);

	private native int nativeGetTrackQueue(int Index);

	private native long nativeGetTrackDuration(int Index);

	private native int nativeGetTrackParam(int Index, String Name);

	private native int nativeGetFrameIndex();

	// private native byte[] nativeGetFrameData();
	// private native long nativeGetFramePts();
	// private native long nativeGetFrameDts();

	private native void nativePopFrame();

	private native int nativeGetFrame(Frame f);

	private static native void nativeInit();

	static {
		System.loadLibrary("AndExtractor");
		nativeInit();
	}

}
