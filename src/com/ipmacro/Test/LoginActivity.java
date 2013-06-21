package com.ipmacro.Test;

import java.util.Date;

import com.ipmacro.PPCore;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	TextView mCdKey, mPwd, mToken, mSN, mLife, mExpire, mRtspUrl, mHttpUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mCdKey = (TextView) findViewById(R.id.cdKey);
		String cdkey = "B0668988B87BD81F3A3E6D4CFE7F45DF";
		mCdKey.setText(cdkey);

		mPwd = (TextView) findViewById(R.id.pwd);

		mToken = (TextView) findViewById(R.id.token);
		String authKeyGS2 = "C6C1164261C140DBB97B44CB83A2082B4A3F02CFEEFCF5FD";// Aya
		String authKeyU30GT = "54DD51474ECC0DE46300891411612EE88F3AACE2492B6168";// U300GT
		String authKeyRK = "4E33BC5F9A66ABADF7FC55C6424B67DADB885EE5633E5FB1";// RK
		String authKeyTest = "TinyToon";
		mToken.setText(authKeyU30GT);

		mSN = (TextView) findViewById(R.id.sn);
		mLife = (TextView) findViewById(R.id.life);
		mHttpUrl = (TextView) findViewById(R.id.httpUrl);
		mRtspUrl = (TextView) findViewById(R.id.rtspUrl);
		mExpire = (TextView) findViewById(R.id.expire);
		getAll();
	}

	private void showMsg(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	public void checkCdKey(View v) {
		String cdkey = mCdKey.getText().toString();
		if (PPCore.checkCdKey(cdkey)) {
			showMsg("CdKey OK");
		} else {
			showMsg("CdKey Error");
		}
	}

	public void getPwd(View v) {
		String pwd = PPCore.getPwd();
		mPwd.setText(pwd);
		String cdkey = mCdKey.getText().toString();
		String url = "http://stb.ipmacro.com/genAuth?cdkey=" + cdkey
				+ "&password=" + pwd;
		Log.i("GetAuth", url);
		showMsg(url);
	}

	public void login(View v) {
		String token = mToken.getText().toString();
		if (PPCore.login(token)) {
			showMsg("Login Success");
			getAll();
		} else {
			showMsg("Login Faild");
		}
	}

	void getAll() {
		mSN.setText(PPCore.getSN() + "");
		mLife.setText(PPCore.getLife() + "");
		Date d = new Date();
		d.setTime(PPCore.getExpire() * 1000);
		mExpire.setText(d.toString());
		mRtspUrl.setText(PPCore.getRtspUrl());
		mHttpUrl.setText(PPCore.getHttpUrl());
	}

	public void getSN(View v) {
		mSN.setText(PPCore.getSN() + "");
	}

	public void getLife(View v) {
		mLife.setText(PPCore.getLife() + "");
	}

	public void getExpire(View v) {
		Date d = new Date();
		d.setTime(PPCore.getExpire() * 1000);
		mExpire.setText(d.toString());
	}

	public void getRtspUrl(View v) {
		mRtspUrl.setText(PPCore.getRtspUrl());
	}

	public void getHttpUrl(View v) {
		mHttpUrl.setText(PPCore.getHttpUrl());
	}

	public void logout(View v) {
		PPCore.logout();
	}
}
