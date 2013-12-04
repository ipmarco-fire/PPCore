package com.ipmacro.download;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class MutiPlay {
	private Handler handler = new Handler();
	public static boolean log = true;

	public static final String TAG = "ppcore";
	public static final int MAX_MUTIPLAY = 3;
	private static final int TYPE_M3U8 = 8;
	private static final int TYPE_FLV = 9;
	private static final int TYPE_TS = 5;

	private Context context;
	List<BaseDownload> list;
	BaseDownload curDb;
	BaseDownload oldDb;

	public MutiPlay(Context c) {
		context = c;
		list = new ArrayList<BaseDownload>();

	}

	public int getProgress(int id) {
		if (list.size() > id) {
			return list.get(id).getProgress();
		}
		return 0;
	}

	public int getProgress() {
		int p = 0;
		String tip = "";
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			int r = list.get(i).getRate() / 1024;

			String s = list.get(i).getSourceUrl();
			tip += "   i:" + i + "  p:" + p1 + "  r:" + r + " s:" + s;
			if (p1 > p) {
				p = p1;
			}
		}
		i(tip + "   Progress:" + p);
		return p;
	}

	public int[] getAllProgress() {
		int[] arr = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i).getProgress();
		}
		return arr;
	}

	public int getRate(int id) {
		if (list.size() > id) {
			return list.get(id).getRate();
		}
		return 0;
	}

	public int getRate() {
		int p = 0, r = 0;
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			if (p1 > p) {
				p = p1;
				r = list.get(i).getRate();
			}
		}
		return r;
	}

	public int[] getAllRate() {
		int[] arr = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i).getRate();
		}
		return arr;
	}

	public String getSourceUrl(int id) {
		if (list.size() > id) {
			return list.get(id).getSourceUrl();
		}
		return "";
	}

	public String getSourceUrl() {
		if (curDb != null) {
			return curDb.getSourceUrl();
		}
		int p = 0;
		String sourceUrl = "";
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			if (p1 > p) {
				p = p1;
				sourceUrl = list.get(i).getSourceUrl();
			}
		}
		return sourceUrl;
	}

	public String[] getAllSourceUrl() {
		String[] arr = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i).getSourceUrl();
		}
		return arr;
	}

	public String getPlayUrl(int id) {
		if (list.size() > id) {
			return list.get(id).getPlayUrl();
		}
		return "";
	}

	public String getPlayUrlAndStopOthers(){
		if(oldDb != null){
			oldDb.stop();oldDb = null;
		}
		if (curDb != null) {
			return curDb.getPlayUrl();
		}
		int p = 0,max = 0;
		String playUrl = "";
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			if (p1 > p) {
				p = p1;
				max = i;
			}
		}
		for (int i = 0; i < list.size(); i++) {
			if(max == i){
				curDb = list.get(i);
				playUrl = list.get(i).getPlayUrl();
			}else{
				list.get(i).stop();
			}
		}
		
		return playUrl;
	}
	
	public String getPlayUrl() {
		if (curDb != null) {
			return curDb.getPlayUrl();
		}
		int p = 0;
		String playUrl = "";
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			if (p1 > p) {
				p = p1;
				playUrl = list.get(i).getPlayUrl();
			}
		}
		return playUrl;
	}

	public String[] getAllPlayUrl() {
		String[] arr = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i).getPlayUrl();
		}
		return arr;
	}

	public int setMaxDBAndStopOthers() {
		int id = 0, p = 0;
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			if (p1 > p) {
				p = p1;
				id = i;
			}
		}
		curDb = list.get(id);
		for (int i = 0; i < list.size(); i++) {
			if (i != id) {
				list.get(i).stop();
			}
		}
		return id;
	}

	public void setCurDBAndStopOthers(int id) {
		if (curDb != null) {
			curDb = list.get(id);
			for (int i = 0; i < list.size(); i++) {
				if (i != id) {
					list.get(i).stop();
				}
			}
		}
	}

	public void stopExceptCur(){
		if(oldDb!=null){
			oldDb.stop();
			oldDb = null;
		}
		for (int i = 0; i < list.size(); i++) {
			if(list.get(i) != curDb){
				list.get(i).stop();
			}else{
				oldDb = curDb;
			}
		}
		list.clear();
		curDb = null;
	}
	
	public void stop() {
		if(oldDb != null){
			oldDb.stop();oldDb = null;
		}
		for (int i = 0; i < list.size(); i++) {
			list.get(i).stop();
		}
		list.clear();
		curDb = null;
	}

	public int getSize() {
		return list.size();
	}

	public void addUrl(String playUrl, int mode) {

		if (list.size() >= MAX_MUTIPLAY) {
			return;
		}
		w("mode:" + mode + ":playUrl:" + playUrl);
		BaseDownload db = null;
		switch (mode) {
		case TYPE_M3U8:
			db = new M3U8Download(context);
			break;
		case TYPE_FLV:
			db = new FlvDownload(context);

			if (playUrl.indexOf(".tv.sohu.") > -1) {
				parserSOHU(playUrl);
			}
			break;
		case TYPE_TS:
			db = new TsDownload(context);
			break;
		default:
			break;
		}
		if (db != null) {
			db.start(playUrl);
			list.add(db);
		}
	}

	// 解析sohu的播放地址:
	private void parserSOHU(final String url) {
		Log.e(TAG, url);
	}

	private void i(String msg) {
		if (log)
			Log.i(TAG, msg);
	}

	private void w(String msg) {
		if (log)
			Log.i(TAG, msg);
	}
	
	public List<State> getStates(){
		List<State> stateList = new ArrayList<State>();
		if(oldDb!=null){
			stateList.add(new State(-1,oldDb.getProgress(),oldDb.getRate(),oldDb.getSourceUrl()));
		}
		for (int i = 0; i < list.size(); i++) {
			BaseDownload bd = list.get(i);
			stateList.add(new State(i,bd.getProgress(),bd.getRate(),bd.getSourceUrl()));
		}
		return stateList;
	}
}
