package com.ipmacro.ppcore;

import com.ipmacro.AndPlayer;
import com.ipmacro.PPCore;
import com.ipmacro.app.MyApplication;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;

public class PPCorePlayer extends BasePlayer implements AndPlayer.Callback {
	AndPlayer mPlayer;

	public PPCorePlayer(Context context) {
		super(context);

		mPlayer = new AndPlayer();
		mPlayer.setCallback(this);
	}

	@Override
	public void setHolder(SurfaceHolder holder) {
		mPlayer.setHolder(holder);
	}

	@Override
	public void start(String url, int mode) {
		mPlayer.release();
		mPlayer.start(url, mode);
		mPlayer.play();
	}

	@Override
	public void release() {
		mPlayer.release();
	}

	@Override
	public void play() {
		mPlayer.play();
	}

	public void pause() {
		mPlayer.pause();
	}

	@Override
	public void dump() {
		mPlayer.dump();
	}

	public State getState() {
		return stateToState(mPlayer.getState());
	}

	public void stateChanged(AndPlayer.State st) {
		fireCallback(stateToState(st));
	}

	private State stateToState(AndPlayer.State st) {
		switch (st) {
		case apsInit:
			return State.apsInit;
		case apsWait:
			return State.apsWait;
		case apsBuffer:
			return State.apsBuffer;
		case apsPlay:
			return State.apsPlay;
		case apsPause:
			return State.apsPause;
		case apsEof:
			return State.apsEof;
		case apsFinish:
			return State.apsFinish;
		case apsEol:
			return State.apsEol;
		case apsFaild:
			return State.apsFaild;
		}
		return null;
	}
}
