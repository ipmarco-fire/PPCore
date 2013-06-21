package com.ipmacro;

import java.util.LinkedList;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

class AudioRender extends Thread {

	private AudioTrack mAudioTrack;
	private int mSample = 44100;
	private int mChannel = 2;

	void setup(int sample, int channel) {
		mSample = sample;
		mChannel = channel;
		int layout = getLayout();
		int bs = AudioTrack.getMinBufferSize(mSample, layout,
				AudioFormat.ENCODING_PCM_16BIT);
		if (mAudioTrack != null) {
			mAudioTrack.release();
			mAudioTrack = null;
		}
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSample,
				layout, AudioFormat.ENCODING_PCM_16BIT, bs,
				AudioTrack.MODE_STREAM);
		mPlay = false;
		mPlaying = false;
		if (!isAlive()) {
			mRunning = true;
			setName("AudioRender");
			start();
		}
	}

	private boolean mPlay = false, mPlaying = false;

	void play() {
		mPlay = true;
	}

	void pause() {
		mPlay = false;
	}

	void flush() {
		mRenderQueue.clear();
		mPad = null;
		mLen = mOff = 0;
	}

	private LinkedList<byte[]> mRenderQueue = new LinkedList<byte[]>();

	long getDelay() {
		if (mSample == 0) {
			return 0;
		}
		long len = 0;
		synchronized (mRenderQueue) {
			int n = mRenderQueue.size();
			for (int i = 0; i < n; ++i) {
				byte b[] = mRenderQueue.get(i);
				len += b.length;
			}
		}
		return len / 2 / mChannel * 1000000 / mSample;
	}

	void write(byte[] buf) {
		synchronized (mRenderQueue) {
			mRenderQueue.add(buf);
		}
	}

	private byte[] mPad;
	private int mOff, mLen;

	boolean render() {
		if (!mPlay) {
			if (mPlaying) {
				mPlaying = false;
				mAudioTrack.pause();
			}
			return false;
		}

		if (mLen != 0) {
			int len = mAudioTrack.write(mPad, mOff, mLen);
			mOff += len;
			mLen -= len;
			return true;
		}
		synchronized (mRenderQueue) {
			if (!mPlaying) {
				if (getDelay() < 500000) {
					return false;
				}
			}
			mPad = mRenderQueue.poll();
		}
		if (mPad == null) {
			mPlaying = false;
			mAudioTrack.pause();
			return false;
		}
		mOff = 0;
		mLen = mPad.length;
		int len = mAudioTrack.write(mPad, mOff, mLen);
		mOff += len;
		mLen -= len;
		if (!mPlaying) {
			mPlaying = true;
			mAudioTrack.play();
		}
		return true;
	}

	boolean mRunning = true;

	public void run() {
		while (mRunning) {
			if (render()) {
				continue;
			}
			try {
				sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	void release() {
		if (isAlive()) {
			mRunning = false;
			try {
				join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (mAudioTrack != null) {
			mAudioTrack.release();
			mAudioTrack = null;
		}
		flush();
	}

	private int getLayout() {
		switch (mChannel) {
		case 1:
			return AudioFormat.CHANNEL_OUT_MONO;
		case 2:
			return AudioFormat.CHANNEL_OUT_STEREO;
		case 3:
			return AudioFormat.CHANNEL_OUT_STEREO
					| AudioFormat.CHANNEL_OUT_FRONT_CENTER;
		case 4:
			return AudioFormat.CHANNEL_OUT_QUAD;
		case 5:
			return AudioFormat.CHANNEL_OUT_QUAD
					| AudioFormat.CHANNEL_OUT_FRONT_CENTER;
		case 6:
			return AudioFormat.CHANNEL_OUT_5POINT1;
		case 8:
			return AudioFormat.CHANNEL_OUT_7POINT1;
		}
		return 0;
	}
}
