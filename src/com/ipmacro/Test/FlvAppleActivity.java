package com.ipmacro.Test;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;

import com.ipmacro.FlvApple;

public class FlvAppleActivity extends Activity {

	EditText mUrl;
	SurfaceView mSurface;
	SurfaceHolder mHolder;
	FlvApple mFlv;
	boolean mPlaying = false;
	boolean mAutoPlay = false;
	MediaPlayer mPlayer = null;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (mFlv != null && mPlayer != null && !mPlaying && mAutoPlay) {
				int p = mFlv.getProgress();
				Log.v("Flv", "progress=" + p);
				if (p >= 100) {
					String u = mFlv.getUrl();
					Log.v("Flv", "url=" + u);
					mPlaying = true;
					try {
						mPlayer.setDataSource(u);
						mPlayer.prepare();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mPlayer.start();

				}
			}
			mHandler.sendEmptyMessageDelayed(0, 100);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flv_apple);
		mUrl = (EditText) findViewById(R.id.url);
		mUrl.setText("http://zb.v.qq.com:1863/?progid=1039787969");
		//mUrl.setText("http://gslb.tv.sohu.com/live?cid=36&sig=c2-ZcF_O0gwcvXVy7CBVag..");
		mSurface = (SurfaceView) findViewById(R.id.surfaceView1);
		mHolder = mSurface.getHolder();
		mHolder.addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				stop(null);
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				play(null);
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
		});

		mHandler.sendEmptyMessageDelayed(0, 100);
	}

	public void close(View v) {
		if (mFlv != null) {
			mFlv.release();
			mFlv = null;
		}
		stop(v);
	}

	@Override
	protected void onDestroy() {
		close(null);
		super.onDestroy();
	}

	public void play(View v) {

		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
			mPlayer.setSurface(mHolder.getSurface());
		}
	}

	public void stop(View v) {
		if (mPlaying) {
			mPlaying = false;
			mPlayer.release();
			mPlayer = null;
		}
	}

	public void open(View v) {
		close(null);
		mFlv = new FlvApple(mUrl.getText().toString());
		mAutoPlay = true;
		play(null);
	}

}
