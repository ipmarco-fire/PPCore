package com.ipmacro.app;

import com.ipmacro.PPCore;

import android.app.Application;

public class MyApplication extends Application {
	private String autokey = "TinyToon";
	public boolean initState = false;

	@Override
	public void onCreate() {
		super.onCreate();
		//initState = PPCore.init(this);
		login();
	}

	public boolean login() {
		return PPCore.login(autokey);
	}

	public boolean logout() {
		return PPCore.logout();
	}

	@Override
	public void onTerminate() {

		super.onTerminate();
	}

}
