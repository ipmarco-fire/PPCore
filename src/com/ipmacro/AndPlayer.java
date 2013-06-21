package com.ipmacro;

import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

public class AndPlayer extends PPCore.Task {

	public static interface Callback {
		void stateChanged(State st);
	}

	private Callback mCallback;

	public void setCallback(Callback cb) {
		synchronized (PPCore.mMutex) {
			mCallback = cb;
		}
	}

	void OnVideoFormat(AndCodec Codec, int width, int height, int format) {
		final int w = width, h = height;
		PPCore.mHandler.post(new Runnable() {
			public void run() {
				mHolder.setFixedSize(w, h);
			}
		});
	}

	void OnAudioFormat(AndCodec Codec, int sample, int channel) {
		mAudioRender = new AudioRender();
		mAudioRender.setup(sample, channel);
		if (mPlaying) {
			mAudioRender.play();
		}
	}

	void OnAudioFrame(AndCodec Codec, byte[] Frame) {
		if (mAudioRender == null) {
			return;
		}
		mAudioRender.write(Frame);
	}

	long getAudioDelay() {
		if (mAudioRender == null) {
			return 0;
		}
		return mAudioRender.getDelay();
	}

	public float mBufRequire = 3;// buffer require for play, in second

	private float mBufMin = 1;// buffer not enough will change to buffing
	private float mBufMax = 10;// buffer too many will force playing

	SurfaceHolder mHolder;

	public void setHolder(SurfaceHolder holder) {
		synchronized (PPCore.mMutex) {
			_setHolder(holder);
		}
	}

	private void _setHolder(SurfaceHolder holder) {
		mHolder = holder;
		if (mHolder == null) {
			if (mVideoTrack != null) {
				mVideoLast = mVideoTrack;
			}
			switchVideo(null);
		} else if (mVideoLast != null) {
			switchVideo(mVideoLast);
			mVideoLast = null;
		}
	}

	private boolean mPlay = true, mPlaying = false;

	public void play() {
		synchronized (PPCore.mMutex) {
			mPlay = true;
		}
	}

	private void _play() {
		if (mPlaying) {
			return;
		}
		mPlaying = true;
		mStc.play();
		if (mAudioCodec != null) {
			mAudioCodec.play();
		}
		if (mAudioRender != null) {
			mAudioRender.play();
		}
		if (mVideoCodec != null) {
			mVideoCodec.play();
		}
	}

	public void pause() {
		synchronized (PPCore.mMutex) {
			mPlay = false;
		}
	}

	private void _pause() {
		if (!mPlaying) {
			return;
		}
		mPlaying = false;
		mStc.stop();
		if (mAudioCodec != null) {
			mAudioCodec.pause();
		}
		if (mAudioRender != null) {
			mAudioRender.pause();
		}
		if (mVideoCodec != null) {
			mVideoCodec.pause();
		}
	}

	private AndExtractor mExtractor;

	private AndStc mStc = new AndStc();

	public boolean start(String Url, int Mode) {
		synchronized (PPCore.mMutex) {
			return _start(Url, Mode);
		}
	}

	private boolean _start(String Url, int Mode) {
		_release();
		mExtractor = new AndExtractor();
		if (!mExtractor.setUrl(Url, Mode)) {
			mExtractor = null;
			return false;
		}
		setState(State.apsWait);
		PPCore.mTaskList.push(this);
		return true;
	}

	public void release() {
		synchronized (PPCore.mMutex) {
			_release();
		}
	}

	private void _release() {
		PPCore.mTaskList.remove(this);
		if (mExtractor != null) {
			mExtractor.release();
			mExtractor = null;
		}
		if (mAudioCodec != null) {
			mAudioCodec.release();
			mAudioCodec = null;
		}
		if (mAudioRender != null) {
			mAudioRender.release();
			mAudioRender = null;
		}
		if (mVideoCodec != null) {
			mVideoCodec.release();
			mVideoCodec = null;
		}
		mAudioTrack = null;
		mVideoTrack = null;
		mVideoLast = null;
		mPlay = false;
		mPlaying = false;
		mStc.stop();
		mStc.setTime(0);
		setState(State.apsInit);
	}

	public enum State {
		apsInit, apsWait, apsBuffer, apsPlay, apsPause, apsEof, apsFinish, apsEol, apsFaild,
	};

	private State mState = State.apsInit;

	public State getState() {
		synchronized (PPCore.mMutex) {
			return mState;
		}
	}

	private void setState(State st) {
		if (mState == st) {
			return;
		}
		mState = st;
		_dump();
		PPCore.mHandler.post(new Runnable() {
			public void run() {
				if (mCallback != null) {
					mCallback.stateChanged(mState);
				}
			}
		});

	}

	private void updateState() {
		if (mExtractor == null) {
			return;
		}
		if (mState == State.apsWait) {
			int st = mExtractor.nativeGetState();
			if (st == AndExtractor.aesFaild) {
				setState(State.apsFaild);
				return;
			}
			if (st == AndExtractor.aesWait) {
				return;
			}
			if (st == AndExtractor.aesEof) {
				mExtractor.next();
				return;
			}
			if (st == AndExtractor.aesEol) {
				setState(State.apsEol);
				return;
			}
			if (st != AndExtractor.aesReady) {
				return;
			}
			mExtractor.prepareTrack();

			AndExtractor.Track video = chooseVideo();
			if ((video != null) && (mHolder == null)) {
				return;
			}
			AndExtractor.Track audio = chooseAudio();
			boolean wait_audio = (audio == null)
					&& !mExtractor.mAudioList.isEmpty();
			boolean wait_video = (video == null)
					&& !mExtractor.mVideoList.isEmpty();
			if (wait_audio && wait_video) {
				return;
			}
			boolean force = false;
			if (audio != null && audio.getBuf() > mBufMax) {
				force = true;
			}
			if (video != null && video.getBuf() > mBufMax) {
				force = true;
			}

			if ((wait_audio || wait_video) && !force) {
				return;
			}
			switchAudio(audio);
			switchVideo(video);
			mStc.setTime(0);
			setState(State.apsBuffer);
		}

		if (mState == State.apsBuffer) {
			if (!mPlay) {
				return;
			}
			int st = mExtractor.nativeGetState();
			if (st == AndExtractor.aesEof) {
				setState(State.apsEof);
				_play();
				return;
			}
			boolean play = true, force = false;
			if (mAudioTrack != null) {
				mAudioTrack.update();
				if (mAudioTrack.getBuf() < mBufRequire) {
					play = false;
				}
				if (mAudioTrack.getBuf() > mBufMax) {
					force = true;
				}
			}
			if (mVideoTrack != null) {
				mVideoTrack.update();
				if (mVideoTrack.getBuf() < mBufRequire) {
					play = false;
				}
				if (mVideoTrack.getBuf() > mBufMax) {
					force = true;
				}
			}
			if (!play && !force) {
				return;
			}
			// long stc = AndCodec.INVALID_PTS;
			// if (mAudioCodec != null) {
			// stc = mAudioCodec.mPts;
			// }
			// if (mVideoCodec != null) {
			// long pts = mVideoCodec.mPts;
			// if (pts != AndCodec.INVALID_PTS
			// && ((stc == AndCodec.INVALID_PTS) || stc > pts)) {
			// stc = pts;
			// }
			// }
			// if (stc == AndCodec.INVALID_PTS) {
			// return;
			// }
			// mStc.setTime(stc);
			setState(State.apsPlay);
			_play();
			return;
		}
		if (mState == State.apsPlay) {
			if (!mPlay) {
				_pause();
				setState(State.apsPause);
				return;
			}
			int st = mExtractor.nativeGetState();
			if (st == AndExtractor.aesEof) {
				setState(State.apsEof);
				return;
			}
			boolean buf = false, force = false;
			if (mAudioTrack != null) {
				mAudioTrack.update();
				if (mAudioTrack.getBuf() < mBufMin) {
					buf = true;
				}
				if (mAudioTrack.getBuf() > mBufMax) {
					force = true;
				}
			}
			if (mVideoTrack != null) {
				mVideoTrack.update();
				if (mVideoTrack.getBuf() < mBufMin) {
					buf = true;
				}
				if (mVideoTrack.getBuf() > mBufMax) {
					force = true;
				}
			}
			if (!buf || force) {
				return;
			}
			_pause();
			setState(State.apsBuffer);
			return;
		}
		if (mState == State.apsEof) {
			if (!mPlay) {
				_pause();
				setState(State.apsPause);
				return;
			}
			if (mAudioCodec != null && !mAudioCodec.isEof()) {
				return;
			}
			if (mVideoCodec != null && !mVideoCodec.isEof()) {
				return;
			}
			setState(State.apsFinish);
			return;
		}
		if (mState == State.apsPause) {
			if (mPlay) {
				setState(State.apsPlay);
				_play();
				return;
			}
		}
		if (mState == State.apsFinish) {
			switchAudio(null);
			switchVideo(null);
			mExtractor.next();
			setState(State.apsWait);
			return;
		}
	}

	private AndExtractor.Track mAudioTrack, mVideoTrack;
	private AndExtractor.Track mVideoLast;

	private AndExtractor.Track chooseAudio() {
		AndExtractor.Track choose = null;
		for (AndExtractor.Track t : mExtractor.mAudioList) {
			if (!t.mime.equals("audio/mp4a-latm")
					&& !t.mime.equals("audio/mpeg")) {

				continue;
			}
			t.update();
			if (t.getBuf() < mBufMin) {
				continue;
			}
			if ((choose == null) || t.queue > choose.queue) {
				choose = t;
			}
		}
		return choose;
	}

	private AndExtractor.Track chooseVideo() {
		AndExtractor.Track choose = null;
		for (AndExtractor.Track t : mExtractor.mVideoList) {
			if (!t.mime.equals("video/avc") && !t.mime.equals("video/mpeg2")) {
				continue;
			}
			t.update();
			if (t.getBuf() < mBufMin) {
				continue;
			}
			if ((choose == null) || t.queue > choose.queue) {
				choose = t;
			}
		}
		return choose;
	}

	private void switchVideo(AndExtractor.Track video) {
		if (mVideoTrack == video) {
			return;
		}
		if (mVideoTrack != null) {
			mVideoTrack.release();
		}
		if (mVideoCodec != null) {
			mVideoCodec.release();
			mVideoCodec = null;
		}
		mVideoTrack = video;
		if (mVideoTrack == null) {
			return;
		}
		mVideoTrack.setup();
		JBVideo codec = new JBVideo(mVideoTrack, this);
		codec.setup();
		mVideoCodec = codec;
	}

	private void switchAudio(AndExtractor.Track audio) {
		if (mAudioTrack == audio) {
			return;
		}
		if (mAudioTrack != null) {
			mAudioTrack.release();
		}
		if (mAudioCodec != null) {
			mAudioCodec.release();
			mAudioCodec = null;
		}
		if (mAudioRender != null) {
			mAudioRender.release();
			mAudioRender = null;
		}
		mAudioTrack = audio;
		if (mAudioTrack == null) {
			return;
		}
		mAudioTrack.setup();
		JBAudio codec = new JBAudio(mAudioTrack, this);
		codec.setup();
		mAudioCodec = codec;
	}

	private AndCodec mAudioCodec;
	private AndCodec mVideoCodec;
	private AudioRender mAudioRender;

	private void decode() {
		if (mExtractor != null) {
			mExtractor.decode();
		}
		if (mAudioCodec != null) {
			mAudioCodec.decode();
		}
		if (mVideoCodec != null) {
			mVideoCodec.decode();
		}
	}

	private void render() {
		long stc = mStc.getTime();
		if (mAudioCodec != null) {
			mAudioCodec.render(stc);
			long pts = mAudioCodec.getPts();
			if (pts != AndCodec.INVALID_PTS && !mAudioCodec.isEof()) {
				long diff = stc - pts;
				if (diff > 1000000 || diff < -1000000) {
					Log.e("AndPlayer", "Fix Stc=" + stc / 1000000f + " -> Pts="
							+ pts / 1000000f + " Diff=" + diff / 1000000f);
					mStc.setTime(pts);
				}
				stc = pts;
			}
		}
		if (mVideoCodec != null) {
			mVideoCodec.render(stc);
		}
	}

	void oneClick() {
		updateState();
		if ((mState != State.apsBuffer) && (mState != State.apsPlay)
				&& (mState != State.apsEof) && (mState != State.apsPause)) {
			return;
		}
		decode();
		// if (mState != State.apsPlay) {
		// return;
		// }
		render();

	}

	public void dump() {
		synchronized (PPCore.mMutex) {
			_dump();
		}
	}

	private void _dump() {
		long stc = mStc.getTime();
		Log.i("AndPlayer", "State=" + mState + " Stc=" + stc / 1000000f);
		if (mAudioTrack != null) {
			mAudioTrack.update();
		}
		if (mVideoTrack != null) {
			mVideoTrack.update();

		}
		if (mAudioCodec != null) {
			mAudioCodec.dump(stc);
		}
		if (mVideoCodec != null) {
			mVideoCodec.dump(stc);
		}
	}

}
