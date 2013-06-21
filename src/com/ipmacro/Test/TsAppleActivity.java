package com.ipmacro.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.TimedText;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ipmacro.ppcore.BaseDownload;
import com.ipmacro.ppcore.FlvDownload;
import com.ipmacro.ppcore.M3U8Download;
import com.ipmacro.ppcore.TsDownload;

class PlayUrl {
	String name;
	String url;
	int mode;

	public PlayUrl(String n, String u, int m) {
		name = n;
		url = u;
		mode = m;
	}
};

public class TsAppleActivity extends Activity {

	SurfaceView mSurface;
	SurfaceHolder mHolder;
	BaseDownload mDownload;
	boolean mPlaying = false;
	boolean mPlay = false;
	boolean mShowProgress = false;
	MediaPlayer mPlayer = null;
	Spinner mUrlSpinner, mModeSpinner;
	ProgressBar progressBar;
	TextView mRate, mMode, mPrecent;
	Context mContext;

	private static final String LIST_URL = "http://192.168.1.20:8080/list.js";
	ArrayAdapter<String> mUrlAdapter, mModeAdapter;
	List<PlayUrl> mUrlList = new ArrayList<PlayUrl>();;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (mDownload != null) {
				int p = mDownload.getProgress();
				int r = mDownload.getRate();
				progressBar.setProgress(p);
				mRate.setText(r / 1024 + "KB");
				mPrecent.setText(p + "%");
				if (mPlay) {
					Log.v("Download", "Progress=" + p);
					if (p >= 100) {
						mPlay = false;
						String u = mDownload.getPlayUrl();
						Log.v("Download", "Url=" + u);
						if (mPlayer != null && !mPlaying) {
							mPlaying = true;
							try {
								mPlayer.setDataSource(u);
								mPlayer.prepareAsync();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (SecurityException e) {
								e.printStackTrace();
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			mHandler.sendEmptyMessageDelayed(0, 100);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ts_apple);
		mContext = this;

		mUrlSpinner = (Spinner) findViewById(R.id.spinnerUrl);
		mUrlSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mModeSpinner.setSelection(mUrlList.get(position).mode);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		mModeSpinner = (Spinner) findViewById(R.id.spinnerMode);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(100);
		// progressBar.setProgress(50); // 设置进度
		mRate = (TextView) findViewById(R.id.txtRate);
		mRate.setText("0K");
		mPrecent = (TextView) findViewById(R.id.txtPrecent);

		mSurface = (SurfaceView) findViewById(R.id.surfaceView1);
		mHolder = mSurface.getHolder();
		mHolder.addCallback(new SurfaceHolder.Callback() {
			public void surfaceDestroyed(SurfaceHolder holder) {
				stop(null);
			}

			public void surfaceCreated(SurfaceHolder holder) {
				play(null);
			}

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
		});

		mHandler.sendEmptyMessageDelayed(0, 100);

		Button open = (Button) findViewById(R.id.open);
		open.requestFocus();

		initData();
	}

	private void refreshView() {
		mUrlAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		for (PlayUrl playUrl : mUrlList)
			mUrlAdapter.add(playUrl.url);
		mUrlSpinner.setAdapter(mUrlAdapter);

		mModeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mModeAdapter.add("asf");
		mModeAdapter.add("");
		mModeAdapter.add("");
		mModeAdapter.add("");
		mModeAdapter.add("other");
		mModeAdapter.add("ts");
		mModeAdapter.add("");// savp
		mModeAdapter.add("");// icy
		mModeAdapter.add("m3u8");
		mModeAdapter.add("flv");
		mModeAdapter.add("raw");
		mModeSpinner.setAdapter(mModeAdapter);
	}

	private void initData() {
		PlayTask task = new PlayTask();
		task.execute(LIST_URL);
	}

	public void close(View v) {
		if (mDownload != null) {
			mDownload.stop();
			mDownload = null;
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
			mPlay = true;
			mPlayer = new MediaPlayer();
			mPlayer.setSurface(mHolder.getSurface());
			mPlayer.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
				public void onVideoSizeChanged(MediaPlayer mp, int width,
						int height) {
					Toast.makeText(mContext, width + "x" + height,
							Toast.LENGTH_SHORT).show();
					Log.v("Player", "Size=" + width + "x" + height);
				}
			});
			mPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					Log.v("Player", "Precent=" + percent);
				}
			});
			mPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					Log.v("Player", "Complete");
				}
			});
			mPlayer.setOnInfoListener(new OnInfoListener() {
				public boolean onInfo(MediaPlayer mp, int what, int extra) {
					Log.v("Player", "Info=" + what + ":" + extra);
					return false;
				}
			});
			mPlayer.setOnPreparedListener(new OnPreparedListener() {
				public void onPrepared(MediaPlayer mp) {
					Log.v("Player", "Prepared");
					mPlayer.start();
				}
			});
			mPlayer.setOnErrorListener(new OnErrorListener() {
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.v("Player", "Error=" + what + ":" + extra);
					return false;
				}
			});
			mPlayer.setOnTimedTextListener(new OnTimedTextListener() {
				public void onTimedText(MediaPlayer mp, TimedText text) {
					Rect r = text.getBounds();
					Log.v("Player", "TimeText=" + text.getText() + " Rect=("
							+ r.left + "," + r.top + ")-(" + r.right + ","
							+ r.bottom + ")");
				}
			});
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

		String mode = mModeAdapter.getItem(mModeSpinner
				.getSelectedItemPosition());
		if (mode == "ts") {
			mDownload = new TsDownload(this);
		} else if (mode == "m3u8") {
			mDownload = new M3U8Download(this);
		} else if (mode == "flv") {
			mDownload = new FlvDownload(this);
		} else if (mode == "raw") {
			mDownload = new BaseDownload(this);
		}
		if (mDownload != null) {
			PlayUrl url = mUrlList.get(mUrlSpinner.getSelectedItemPosition());
			mDownload.start(url.url);
			play(null);
		}

	}

	class PlayTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			String con = downloadHTML(url);
			return con;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				return;
			}
			int index = result.indexOf("[");
			String json = result.substring(index);
			Log.i("json", json);

			try {
				JSONArray root = new JSONArray(json);
				mUrlList.clear();
				for (int i = 0; i < root.length(); i++) {
					JSONObject obj = root.getJSONObject(i);
					String n = obj.getString("n");
					String u = obj.getString("u");
					int m = obj.getInt("m");
					mUrlList.add(new PlayUrl(n, u, m));
				}
				refreshView();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		protected String downloadHTML(String htmlUrl) {
			Log.i("downloadHTML", htmlUrl);
			StringBuilder pageHTML = new StringBuilder();
			try {
				URL url = new URL(htmlUrl);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setRequestProperty("User-Agent", "MSIE 7.0");
				BufferedReader br = new BufferedReader(new InputStreamReader(
						connection.getInputStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					pageHTML.append(line);
					pageHTML.append("\r\n");
				}
				connection.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String html = pageHTML.toString();
			return html;
		}

	}
}
