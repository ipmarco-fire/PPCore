package com.ipmacro;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import com.ipmacro.AndExtractor.Track;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

abstract class JBCodec extends AndCodec {

	JBCodec(Track Track, String Name, AndPlayer Player) {
		super(Track, Name, Player);
	}

	MediaCodec mCodec;

	ByteBuffer[] mInputBuffers;
	ByteBuffer[] mOutputBuffers;
	boolean mInited = false, mEof = false;

	abstract MediaFormat getFormat();

	boolean setup(Surface surface) {
		mCodec = MediaCodec.createDecoderByType(mTrack.mime);
		mCodec.configure(getFormat(), surface, null, 0);
		mCodec.start();
		mInputBuffers = mCodec.getInputBuffers();
		mOutputBuffers = mCodec.getOutputBuffers();
		return true;
	}

	void flush() {
		if (mCodec != null) {
			mCodec.flush();
			mInited = false;
		}

	}

	void release() {
		super.release();
		if (mCodec != null) {
			mCodec.release();
			mCodec = null;
		}
		mRenderQueue.clear();
		mInputBuffers = null;
		mOutputBuffers = null;
	}

	static class Frame {
		int index;
		MediaCodec.BufferInfo bi;
	}

	LinkedList<Frame> mRenderQueue = new LinkedList<Frame>();

	abstract void setFormat(MediaFormat format);

	int sendExtra(ByteBuffer bb, AndExtractor.Frame f) {
		if (mTrack.extra.length != 0) {
			bb.put(mTrack.extra);
		}
		return mTrack.extra.length;
	}

	int sendFrame(ByteBuffer bb, AndExtractor.Frame f) {
		bb.put(f.data);
		return f.data.length;
	}

	void decode() {
		if (mCodec == null) {
			return;
		}
		BufferInfo bi = new BufferInfo();
		int n = 0;
		while (n < 10) {
			++n;
			int index = mCodec.dequeueOutputBuffer(bi, 0);
			if (index >= 0) {
				Frame f = new Frame();
				f.index = index;
				f.bi = bi;
				mRenderQueue.add(f);
				bi = new BufferInfo();
			} else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				mOutputBuffers = mCodec.getOutputBuffers();
			} else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				setFormat(mCodec.getOutputFormat());
			} else {
				break;
			}
		}
		if (mTrack == null) {
			return;
		}
		n = 0;
		while (n < 10) {
			++n;
			AndExtractor.Frame f = mTrack.framelist.poll();
			if (f == null) {
				if (!mTrack.isEof()) {
					return;
				}
				if (mEof) {
					return;
				}
			}
			int index = mCodec.dequeueInputBuffer(0);
			if (index < 0) {
				if (f != null) {
					mTrack.framelist.push(f);
				}
				return;
			}
			if (f == null) {
				mEof = true;
				mCodec.queueInputBuffer(index, 0, 0, 0,
						MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				return;
			}
			ByteBuffer bb = mInputBuffers[index];
			bb.clear();
			if (!mInited) {
				mInited = true;
				int len = sendExtra(bb, f);
				if (len != 0) {
					mCodec.queueInputBuffer(index, 0, len, 0,
							MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
					mTrack.framelist.push(f);
					continue;
				}
			}
			int len = sendFrame(bb, f);
			mCodec.queueInputBuffer(index, 0, len, f.pts,
					f.key ? MediaCodec.BUFFER_FLAG_SYNC_FRAME : 0);
		}
	}

	void dump(long stc) {
		long pts = getPts();
		Log.i(mName, "Pts=" + pts / 1000000f + " Delay=" + (stc - pts)
				/ 1000000f + " Render=" + getDelay() / 1000000f + " Queue="
				+ mRenderQueue.size() + "/" + mOutputBuffers.length + " Frame="
				+ mTrack.framelist.size() + "/" + mTrack.queue + " Buf="
				+ mTrack.getBuf() + " Fps=" + mTrack.fps);
	}

	boolean isEof() {
		if (!mEof) {
			return false;
		}
		return mEof;
	}
}
