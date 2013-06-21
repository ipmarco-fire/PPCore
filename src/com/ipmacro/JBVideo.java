package com.ipmacro;

import java.nio.ByteBuffer;

import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.ipmacro.AndExtractor.Track;

class JBVideo extends JBCodec {

	JBVideo(Track Track, AndPlayer Player) {
		super(Track, "JBVideo", Player);
	}

	public boolean setup() {
		return super.setup(mPlayer.mHolder.getSurface());
	}

	MediaFormat getFormat() {
		int width = mTrack.getParam("width");
		int height = mTrack.getParam("height");
		if (width == 0) {
			width = 1920;
		}
		if (height == 0) {
			height = 1080;
		}
		// if ((width == 0) || (height == 0)) {
		// MediaFormat format = new MediaFormat();
		// format.setString(MediaFormat.KEY_MIME, mTrack.mime);
		// return format;
		// }
		return MediaFormat.createVideoFormat(mTrack.mime, width, height);
	}

	void setFormat(MediaFormat format) {
		int width = format.getInteger(MediaFormat.KEY_WIDTH);
		int height = format.getInteger(MediaFormat.KEY_HEIGHT);
		int color = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
		Log.w("JBVideo", "width=" + width + " height=" + height + " color="
				+ color);
		// nativeFixSurface(mSurface, width, height, color);
		if (mPlayer != null) {
			mPlayer.OnVideoFormat(this, width, height, color);
		}
	}

	boolean mStep = true;

	void render(long stc) {
		if (mCodec == null) {
			return;
		}
		if (mPts != INVALID_PTS && mPts > stc) {
			return;
		}
		Frame f = mRenderQueue.poll();
		if (f == null) {
			return;
		}
		mPts = f.bi.presentationTimeUs;
		if (mPts > stc && !mStep) {
			mRenderQueue.push(f);
			return;
		}
		mStep = false;
		mCodec.releaseOutputBuffer(f.index, true);
	}

	private int mAvc = 0;
	private static final byte[] mH264Nal = new byte[4];
	static {
		mH264Nal[3] = 1;
	}

	int sendExtra(ByteBuffer bb, AndExtractor.Frame f) {
		if (mTrack.extra.length == 0) {
			return 0;
		}
		if (mTrack.mime.equals("video/avc") && (mTrack.extra[0] == 1)) {
			mAvc = (mTrack.extra[4] & 0x3) + 1;

			int total = 0;
			int off = 6;
			int n = (mTrack.extra[5] & 0x1F);
			for (int i = 0; i < n; ++i) {
				int len = (((short) mTrack.extra[off] + 256) & 0xFF) * 256
						+ (((short) mTrack.extra[off + 1] + 256) & 0xFF);
				if (off + 2 + len > mTrack.extra.length) {
					return total;
				}
				bb.put(mH264Nal);
				bb.put(mTrack.extra, off + 2, len);
				off += 2 + len;
				total += 4 + len;
			}
			n = ((short) mTrack.extra[off] + 256) & 0xFF;
			++off;
			for (int i = 0; i < n; ++i) {
				int len = (((short) mTrack.extra[off] + 256) & 0xFF) * 256
						+ (((short) mTrack.extra[off + 1] + 256) & 0xFF);
				if (off + 2 + len > mTrack.extra.length) {
					return total;
				}
				bb.put(mH264Nal);
				bb.put(mTrack.extra, off + 2, len);
				off += 2 + len;
				total += 4 + len;
			}
			return total;
		}
		bb.put(mTrack.extra);
		return mTrack.extra.length;
	}

	int sendFrame(ByteBuffer bb, AndExtractor.Frame f) {
		if (mAvc == 0) {
			if (f.data[0] != 0 || f.data[1] != 0 || f.data[2] != 0
					|| f.data[3] != 1) {
				int a;
				a=1;
				a=a+2;
			}
			bb.put(f.data);
			return f.data.length;
		}
		int total = 0;
		int off = 0;
		while (off + mAvc < f.data.length) {
			int len = 0;
			for (int i = 0; i < mAvc; ++i) {
				len = len * 256 + (((short) f.data[off + i] + 256) & 0xFF);
			}
			if (off + mAvc + len > f.data.length) {
				return total;
			}
			bb.put(mH264Nal);
			bb.put(f.data, off + mAvc, len);
			total += 4 + len;
			off += mAvc + len;
		}
		return total;
	}

	private static native void nativeFixSurface(Surface surface, int width,
			int height, int Format);
}
