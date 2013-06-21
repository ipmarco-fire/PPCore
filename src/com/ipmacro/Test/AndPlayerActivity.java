package com.ipmacro.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.ipmacro.AndPlayer;
import com.ipmacro.AndPlayer.State;
import com.ipmacro.ppcore.BasePlayer;
import com.ipmacro.ppcore.DefaultPlayer;
import com.ipmacro.ppcore.PPCorePlayer;

public class AndPlayerActivity extends Activity implements Handler.Callback {
	public static final String TAG = "Player";
	EditText mUrl, mMode;
	CheckBox mSys;
	SurfaceView mSurface;
	SurfaceHolder mHolder;
	Handler mHandler = new Handler();
	Spinner mSpinner;

	BasePlayer mPlayer;

	static class _Url {
		String url;
		int mode;
	};

	List<String> list = new ArrayList<String>();
	List<_Url> list2 = new ArrayList<_Url>();

	public boolean handleMessage(Message msg) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		mSpinner.setAdapter(adapter);
		mSpinner.invalidate();
		return true;
	}

	AsyncTask<Void, Void, Void> myTask = new AsyncTask<Void, Void, Void>() {
		protected Void doInBackground(Void... paramArrayOfParams) {
			URL url;
			StringBuffer sb = new StringBuffer();
			String line = null;
			BufferedReader bufReader = null;
			String rst = null;
			try {
				url = new URL("http://192.168.1.20:8080/list.js");
				HttpURLConnection urlConn = (HttpURLConnection) url
						.openConnection();
				urlConn.setRequestProperty("Connection", "close");
				urlConn.connect();
				bufReader = new BufferedReader(new InputStreamReader(
						urlConn.getInputStream()));
				while ((line = bufReader.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
				rst = sb.toString();
			} catch (MalformedURLException e) {
				System.out.println("MalformedURLException");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IOException");
				e.printStackTrace();
			}
			rst = "{list:" + rst.substring(11) + "}";
			try {
				JSONObject object = new JSONObject(rst);
				JSONArray array = object.getJSONArray("list");
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = (JSONObject) array.opt(i);
					_Url u = new _Url();
					u.url = obj.getString("u");
					u.mode = obj.getInt("m");

					list.add(u.url);
					list2.add(u);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mHandler.sendEmptyMessage(0);
			return null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_andplay);

		mUrl = (EditText) findViewById(R.id.url);
		mMode = (EditText) findViewById(R.id.mode);
		mSys = (CheckBox) findViewById(R.id.sys);
		// mUrl.setText("flv://edgews.yicai.com/channels/601/400.flv/live");
		mSurface = (SurfaceView) findViewById(R.id.surfaceView1);
		mHolder = mSurface.getHolder();
		mSpinner = (Spinner) findViewById(R.id.spinner1);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				_Url u = list2.get(position);
				mUrl.setText(u.url);
				mMode.setText(String.valueOf(u.mode));
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		mHandler = new Handler(this);
		myTask.execute();
	}

	@Override
	protected void onPause() {
		pause(null);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		close(null);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		play(null);
		super.onResume();
	}

	long start = 0;

	long getTime() {
		return System.currentTimeMillis() - start;
	}

	String title = "";

	void showTime(String T) {
		title = title + " " + T + ":" + getTime();
		setTitle(title);
	}

	public void setUrl(View v) {
		if (mPlayer != null) {
			mPlayer.release();
		}
		if (mSys.isChecked()) {
			mPlayer = new DefaultPlayer(this);
		} else {
			mPlayer = new PPCorePlayer(this);
		}
		mPlayer.setHolder(mHolder);
		mPlayer.setCallback(new BasePlayer.Callback() {
			public void stateChanged(BasePlayer.State st) {
				Log.e(TAG, "State=" + st);
				showTime(BasePlayer.getStateName(st));
			}
		});

		start = System.currentTimeMillis();
		title = "";
		mPlayer.start(mUrl.getText().toString(),
				Integer.parseInt(mMode.getText().toString()));
	}

	public void pause(View v) {
		if (mPlayer != null) {
			mPlayer.pause();
		}
	}

	public void play(View v) {
		if (mPlayer != null) {
			mPlayer.play();
		}
	}

	public void dump(View v) {
		if (mPlayer != null) {
			mPlayer.dump();
		}
	}

	public void close(View v) {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
