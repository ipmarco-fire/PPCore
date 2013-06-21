package com.ipmacro.Test;

import java.util.ArrayList;
import java.util.List;

import com.ipmacro.AndPlayer;
import com.ipmacro.AndPlayer.State;
import com.ipmacro.ppcore.BaseDownload;
import com.ipmacro.ppcore.BasePlayer;
import com.ipmacro.ppcore.PPCorePlayer;
import com.ipmacro.ppcore.RlpDownload;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LiveActivity extends Activity {
	private static final String TAG = "Live";
	private final int MESSAGE_BUFFER_UPDATE = 2; // ¸üÐÂ»º³å×´Ì¬

	Context mContext;
	ListView listView;
	BaseDownload download;
	SurfaceView mSurface;
	SurfaceHolder mHolder;

	BasePlayer mPlayer;

	private List<String> getData() {
		List<String> data = new ArrayList<String>();
		data.add("rlp://1356841525919A742D4877B8EBEC1385?tracker=10000:69.64.42.216:8000&prefetch=209.239.112.139:3344:83adddaa1aa359a835b0f1804054503b");
		data.add("rlp://13568415259350F9EADACDAF5636FAA1?tracker=10000:69.64.42.216:8000&prefetch=209.239.112.139:3344:83adddaa1aa359a835b0f1804054503b");
		data.add("rlp://135684198120404628FC4FB355F7EAB8?tracker=10000:69.64.42.216:8000");
		return data;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_BUFFER_UPDATE:
				if (download == null) {
					return;
				}

				int p = download.getProgress();
				int r = download.getRate();
				Log.i(TAG, "p:" + p + " r:" + r);
				if (p >= 10) {
					mPlayer.release();
					mPlayer.start(download.getPlayUrl(), 0);
					mPlayer.play();
					return;
				}
				Message msg1 = mHandler.obtainMessage(MESSAGE_BUFFER_UPDATE);
				mHandler.sendMessageDelayed(msg1, 500);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live);

		mContext = this;
		mPlayer = new PPCorePlayer(this);

		listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, getData()));
		listView.setOnItemClickListener(listener);

		mSurface = (SurfaceView) findViewById(R.id.surfaceView1);
		mHolder = mSurface.getHolder();

		mPlayer.setHolder(mHolder);
		mPlayer.setCallback(new BasePlayer.Callback() {
			@Override
			public void stateChanged(BasePlayer.State st) {
				// TODO Auto-generated method stub

			}
		});
	}

	OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			String url = (String) listView.getAdapter().getItem(position);
			startDownload(url);
		}
	};

	@Override
	protected void onPause() {
		mPlayer.release();
		if (download != null) {
			download.stop();
			download = null;
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mPlayer.play();
		super.onResume();
	}

	public void startDownload(String url) {
		if (download != null) {
			download.stop();
			download = null;
		}
		download = new RlpDownload(mContext);
		download.start(url);
		Message msg1 = mHandler.obtainMessage(MESSAGE_BUFFER_UPDATE);
		mHandler.sendMessageDelayed(msg1, 300);
	}

	public void dump(View v) {
		mPlayer.dump();
	}
}
