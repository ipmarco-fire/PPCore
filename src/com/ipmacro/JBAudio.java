package com.ipmacro;

import java.nio.ByteBuffer;

import android.media.MediaFormat;
import android.util.Log;

import com.ipmacro.AndExtractor.Track;

class JBAudio extends JBCodec {

	JBAudio(Track Track, AndPlayer Player) {
		super(Track, "JBAudio", Player);
	}

	public boolean setup() {
		return super.setup(null);
	}

	MediaFormat getFormat() {
		int sample = mTrack.getParam("sample");
		int channel = mTrack.getParam("channel");
		if (sample == 0) {
			sample = 44100;
		}
		if (channel == 0) {
			channel = 2;
		}
		return MediaFormat.createAudioFormat(mTrack.mime, sample, channel);
	}

	void setFormat(MediaFormat format) {
		int sample = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
		int channel = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
		Log.w("JBAudio", "sample=" + sample + " channel=" + channel);
		if (mPlayer != null) {
			mPlayer.OnAudioFormat(this, sample, channel);
		}
	}

	void render(long stc) {
		if (mCodec == null) {
			return;
		}
		if (mPlayer == null) {
			return;
		}
		int n = 0;
		while (n < 10) {
			++n;
			if (getDelay() > 700000) {
				return;
			}
			Frame f = mRenderQueue.poll();
			if (f == null) {
				return;
			}
			mPts = f.bi.presentationTimeUs;
			if (mPts > stc + 600000) {
				mRenderQueue.push(f);
				return;
			}

			ByteBuffer bb = mOutputBuffers[f.index];
			byte[] buf = new byte[f.bi.size];
			bb.get(buf);
			bb.clear();
			mPlayer.OnAudioFrame(this, buf);
			mCodec.releaseOutputBuffer(f.index, false);
		}
	}

	long getDelay() {
		if (mPlayer == null) {
			return 0;
		}
		return mPlayer.getAudioDelay();
	}

	private boolean mAdts = false;

	int sendExtra(ByteBuffer bb, AndExtractor.Frame f) {
		if (mTrack.extra.length != 0) {
			bb.put(mTrack.extra);
			return mTrack.extra.length;
		}
		if (!mTrack.mime.equals("audio/mp4a-latm")) {
			return 0;
		}
		mAdts = true;
		byte[] b = nativeAdts2Latm(f.data);
		bb.put(b);
		return b.length;
	}

	int sendFrame(ByteBuffer bb, AndExtractor.Frame f) {
		if (!mAdts) {
			bb.put(f.data);
			return f.data.length;
		}

		int l = nativeSkipAdts(f.data);
		bb.put(f.data, l, f.data.length - l);
		return f.data.length - l;
	}

	private static native byte[] nativeAdts2Latm(byte[] src);

	private static native int nativeSkipAdts(byte[] src);

}
