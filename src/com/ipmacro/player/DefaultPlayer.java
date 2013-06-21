package com.ipmacro.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.Log;
import android.view.SurfaceHolder;

public class DefaultPlayer extends BasePlayer {
	private static String TAG = "DefaultPlayer";
	MediaPlayer mMediaPlayer;
	SurfaceHolder mHolder;

	public DefaultPlayer(Context context) {
		super(context);
	}

	@Override
	public void start(String url, int mode) {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
		}
		try {
			mMediaPlayer = new MediaPlayer(); // 创建MediaPlayer对象
			mMediaPlayer.setDataSource(url);
			mMediaPlayer.setDisplay(mHolder);
			mMediaPlayer.setOnPreparedListener(new MyPreparedListener());
			mMediaPlayer.setOnCompletionListener(new MyCompletionListener());
			mMediaPlayer
					.setOnVideoSizeChangedListener(new MyVideoSizeChangedListener());

			mMediaPlayer.prepareAsync();
		} catch (Exception e) {
			Log.e(TAG, "226:error: " + e.getMessage(), e);
			fireCallback(State.apsFaild);

		}
	}

	@Override
	public void release() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	@Override
	public void play() {

	}

	@Override
	public void dump() {
	}

	public void setHolder(SurfaceHolder mHolder) {
		this.mHolder = mHolder;
	}

	class MyPreparedListener implements OnPreparedListener {
		public void onPrepared(MediaPlayer mp) {
			Log.d(TAG, "onPrepared called");
			mp.start();
			fireCallback(State.apsPlay);

		}
	}

	class MyCompletionListener implements OnCompletionListener {
		public void onCompletion(MediaPlayer mp) {
			fireCallback(State.apsEol);
		}
	}

	class MyVideoSizeChangedListener implements OnVideoSizeChangedListener {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			Log.i(TAG, "onVideoSizeChanged(width=" + width + " ,height="
					+ height + ")");
			mHolder.setFixedSize(width, height);
			fireCallback(State.apsWait);
		}
	}

	@Override
	public void pause() {

	}
}
