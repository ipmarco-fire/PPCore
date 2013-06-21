package com.ipmacro.ppcore;

import android.content.Context;
import android.view.SurfaceHolder;

public abstract class BasePlayer {

	public enum State {
		apsInit, apsWait, apsBuffer, apsPlay, apsPause, apsEof, apsFinish, apsEol, apsFaild,
	};

	public static String getStateName(State st) {
		switch (st) {
		case apsInit:
			return "I";
		case apsWait:
			return "W";
		case apsBuffer:
			return "B";
		case apsPlay:
			return "P";
		case apsPause:
			return "T";
		case apsEof:
			return "E";
		case apsFinish:
			return "F";
		case apsEol:
			return "L";
		case apsFaild:
			return "X";
		}
		return "";
	}

	public static interface Callback {
		void stateChanged(State st);
	}

	private Callback mCallback;

	public void setCallback(Callback cb) {
		mCallback = cb;
	};

	void fireCallback(State st) {
		if (mCallback != null) {
			mCallback.stateChanged(st);
		}
	}

	Context mContext;

	public BasePlayer(Context context) {
		this.mContext = context;
	};

	public abstract void start(String url, int mode);

	public abstract void release();

	public abstract void play();

	public abstract void pause();

	public abstract void dump();

	public abstract void setHolder(SurfaceHolder mHolder);
}
