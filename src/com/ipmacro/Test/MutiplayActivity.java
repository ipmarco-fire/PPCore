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
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ipmacro.ppcore.MutiPlay;

class Item {
	String url;
	int mode;

	public Item(String u, int m) {
		url = u;
		mode = m;
	}
}

class PlayInfo {
	String name;
	List<Item> list;

	public PlayInfo(String n) {
		this.name = n;
		list = new ArrayList<Item>();
	}

	public void addUrlItem(String u, int m) {
		list.add(new Item(u, m));
	}
}



public class MutiplayActivity extends Activity {

	private static final String LIST_URL = "http://192.168.1.20:8080/mutiplay.js";
	private static final int MSG_INIT = 0;
	ArrayAdapter<String> adapterUrl;
	Context mContext;

	Spinner spinnerUrl;
	List<PlayInfo> dataList = new ArrayList<PlayInfo>();;
	MutiPlay mutiPlay;
	MediaPlayer mPlayer = null;
	SurfaceView mSurface;
	SurfaceHolder mHolder;
	LinearLayout layoutBox;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(msg.what == MSG_INIT){
				int p = mutiPlay.getProgress();
				if(p>=100){   //已经有一个达到最大值
					int id = mutiPlay.setMaxDBAndStopOthers();
					View child = layoutBox.getChildAt(id);
					ProgressBar progressBar = (ProgressBar) child.findViewById(R.id.progressBar);
					progressBar.setProgress(mutiPlay.getProgress(id));
					TextView txtPrecent = (TextView) child.findViewById(R.id.txtPrecent);
					txtPrecent.setText(mutiPlay.getProgress(id) + "%");
					TextView txtRate = (TextView) child.findViewById(R.id.txtRate);
					txtRate.setText(mutiPlay.getRate(id)/ 1024 + "KB");
					
					String playUrl = mutiPlay.getPlayUrl();
					openPlay(playUrl);
				}else{
					int[] progressArr = mutiPlay.getAllProgress();
					int[] rateArr = mutiPlay.getAllRate();
					int size = mutiPlay.getSize();
					
					for(int i = 0;i<size;i++){
						View child = layoutBox.getChildAt(i);
						ProgressBar progressBar = (ProgressBar) child.findViewById(R.id.progressBar);
						progressBar.setProgress(progressArr[i]);
						TextView txtPrecent = (TextView) child.findViewById(R.id.txtPrecent);
						txtPrecent.setText(progressArr[i] + "%");
						TextView txtRate = (TextView) child.findViewById(R.id.txtRate);
						txtRate.setText(rateArr[i] / 1024 + "KB");
					}
					
					mHandler.sendEmptyMessageDelayed(MSG_INIT, 100);
				}
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mutipaly);

		mContext = this;
		mutiPlay = new MutiPlay(this);

		spinnerUrl = (Spinner) findViewById(R.id.spinnerUrl);
		spinnerUrl.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		

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
		layoutBox = (LinearLayout) findViewById(R.id.layoutBox);
		Button open = (Button) findViewById(R.id.open);
		open.requestFocus();

		initData();
	}

	public void stop(View v){
		
		mutiPlay.stop();
		releasePlay();
	}
	
	public void play(View v){
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mutiPlay.stop();
		releasePlay();
	}

	private void initData() {
		PlayTask task = new PlayTask();
		task.execute(LIST_URL);
	}

	private void refreshView() {
		adapterUrl = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		for (PlayInfo info : dataList)
			adapterUrl.add(info.name);
		spinnerUrl.setAdapter(adapterUrl);
	}

	private void openPlay(String url) {
		releasePlay();
		
		mPlayer = new MediaPlayer();
		mPlayer.setSurface(mHolder.getSurface());
		mPlayer.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
			public void onVideoSizeChanged(MediaPlayer mp, int width,
					int height) {
				Toast.makeText(mContext, width + "x" + height,
						Toast.LENGTH_SHORT).show();
				Log.v("MediaPlayer", "Size=" + width + "x" + height);
			}
		});
		mPlayer.setOnBufferingUpdateListener(new android.media.MediaPlayer.OnBufferingUpdateListener() {
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				Log.v("MediaPlayer", "Precent=" + percent);
			}
		});
		mPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				Log.v("MediaPlayer", "Complete");
			}
		});
		mPlayer.setOnInfoListener(new OnInfoListener() {
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				Log.v("MediaPlayer", "Info=" + what + ":" + extra);
				return false;
			}
		});
		mPlayer.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				Log.v("MediaPlayer", "Prepared");
				mPlayer.start();
			}
		});
		mPlayer.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.v("MediaPlayer", "Error=" + what + ":" + extra);
				return false;
			}
		});
		
		try {
			mPlayer.setDataSource(url);
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

	private void releasePlay() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	public void open(View v) {
		close(null);
		int selId = spinnerUrl.getSelectedItemPosition();
		PlayInfo info = dataList.get(selId);

		for (int i = 0; i < info.list.size(); i++) {
			Item item = info.list.get(i);
			mutiPlay.addUrl(item.url, item.mode);
		}
		
		layoutBox.removeAllViews();
		
		LayoutInflater mInflater = getLayoutInflater();
		int size = mutiPlay.getSize();
		for(int i = 0;i<size;i++){
			View par = mInflater.inflate(R.layout.activity_mutiplay_item, null);
			TextView txtSource = (TextView) par.findViewById(R.id.txtSource);
			txtSource.setText(mutiPlay.getSourceUrl(i));
			layoutBox.addView(par);
		}
		mHandler.sendEmptyMessageDelayed(MSG_INIT, 100);
	}

	public void close(View v) {
		mutiPlay.stop();
		mHandler.removeMessages(MSG_INIT);
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

			try {
				JSONArray root = new JSONArray(result);
				dataList.clear();
				for (int i = 0; i < root.length(); i++) {
					JSONObject obj = root.getJSONObject(i);
					String n = obj.getString("name");
					PlayInfo info = new PlayInfo(n);
					JSONArray listArray = obj.getJSONArray("list");
					for (int j = 0; j < listArray.length(); j++) {
						JSONObject o = listArray.getJSONObject(j);
						info.addUrlItem(o.getString("url"), o.getInt("mode"));
					}
					dataList.add(info);
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
