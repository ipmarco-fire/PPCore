package com.ipmacro.Test;

import com.ipmacro.PPCore;
import com.ipmacro.ppcore.BaseDownload;
import com.ipmacro.ppcore.RfpDownload;
import com.ipmacro.ppcore.RlpDownload;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class RlpDownloadActivity extends Activity {
	private final int MESSAGE_BUFFER_UPDATE = 2; // 更新缓冲状态
	private static final String TAG = "RlpDownload";
	String RLP_URL = "rlp://1356841525919A742D4877B8EBEC1385?tracker=10000:69.64.42.216:8000&prefetch=209.239.112.139:3344:83adddaa1aa359a835b0f1804054503b";
	String RFP_URL = "rfp://A7BFEB92BA96A66EDF734345058C9035?tracker=18888:174.36.169.143:8888";

	BaseDownload download;
	EditText etxtRlp, etxtRfp;
	TextView txtResult;

	Context mContext;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_BUFFER_UPDATE:
				if (download == null) {
					return;
				}
				int p = download.getProgress();
				int r = download.getRate();

				txtResult.setText("进度:" + p + "  下载速度:" + r + "kb/s");

				Message msg1 = mHandler.obtainMessage(MESSAGE_BUFFER_UPDATE);
				mHandler.sendMessageDelayed(msg1, 500);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rlp_download);

		mContext = this;
		PPCore.init(this);
		boolean login_ok = PPCore.login("TinyToon");

		etxtRlp = (EditText) findViewById(R.id.etxtRlp);
		etxtRfp = (EditText) findViewById(R.id.etxtRfp);
		txtResult = (TextView) findViewById(R.id.txtResult);

		etxtRlp.setText(RLP_URL);
		etxtRfp.setText(RFP_URL);
	}

	public void onClickListener(View view) {
		switch (view.getId()) {
		case R.id.rlp_start:
			Log.i(TAG, "rlp start");
			if (download != null) {
				download.stop();
			}
			download = new RlpDownload(mContext);
			download.start(etxtRlp.getText().toString());
			Message msg1 = mHandler.obtainMessage(MESSAGE_BUFFER_UPDATE);
			mHandler.sendMessageDelayed(msg1, 300);
			break;
		case R.id.rfp_start:
			Log.i(TAG, "rfp start");
			if (download != null) {
				download.stop();
			}
			download = new RfpDownload(mContext);
			download.start(etxtRfp.getText().toString());
			Message msg2 = mHandler.obtainMessage(MESSAGE_BUFFER_UPDATE);
			mHandler.sendMessageDelayed(msg2, 300);
			break;
		case R.id.stop:
			Log.i(TAG, "stop");
			if (download != null) {
				download.stop();
				download = null;
			}
			break;
		case R.id.close:
			Log.i(TAG, "close");
			download.stop();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onPause() {
		if (download != null) {
			download.stop();
			download = null;
		}
		super.onPause();
	}
}
