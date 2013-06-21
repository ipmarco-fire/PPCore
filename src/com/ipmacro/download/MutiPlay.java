package com.ipmacro.download;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MutiPlay {
	public static final int MAX_MUTIPLAY = 3;
	private static final int TYPE_M3U8 = 8;
	private static final int TYPE_FLV = 9;
	private static final int TYPE_TS = 5;

	private Context context;
	List<BaseDownload> list;
	BaseDownload curDb;
	

	public MutiPlay(Context c) {
		context = c;
		list = new ArrayList<BaseDownload>();

	}
	
	public int getProgress(int id){
		if(list.size()>id){
			return list.get(id).getProgress();
		}
		return 0;
	}
	public int getProgress(){
		int p = 0;
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress(); Log.e("ppp","i:"+i+"  p:"+p1);
			if(p1>p){
				p = p1;
			}
		}
		return p;
	}
	
	public int[] getAllProgress(){
		int[] arr = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i).getProgress();
		}
		return arr;
	}
	
	public int getRate(int id){
		if(list.size()>id){
			return list.get(id).getRate();
		}
		return 0;
	}
	
	public int getRate(){
		int p = 0,r=0;
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			if(p1>p){
				p = p1;
				r = list.get(i).getRate();
			}
		}
		return r;
	}
	
	public int[] getAllRate(){
		int[] arr = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i).getRate();
		}
		return arr;
	}
	
	public String getSourceUrl(int id){
		if(list.size() > id){
			return list.get(id).getSourceUrl();
		}
		return "";
	}
	
	public String getSourceUrl(){
		int p = 0;String sourceUrl = "";
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			if(p1>p){
				p = p1;
				sourceUrl = list.get(i).getSourceUrl();
			}
		}
		return sourceUrl;
	}
	
	public String[] getAllSourceUrl(){
		String[] arr = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i).getSourceUrl();
		}
		return arr;
	}
	
	public String getPlayUrl(int id){
		if(list.size() > id){
			return list.get(id).getPlayUrl();
		}
		return "";
	}
	
	public String getPlayUrl(){
		if(curDb!=null){
			return curDb.getPlayUrl();
		}
		int p = 0;String playUrl = "";
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			if(p1>p){
				p = p1;
				playUrl = list.get(i).getPlayUrl();
			}
		}
		return playUrl;
	}
	
	public String[] getAllPlayUrl(){
		String[] arr = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i).getPlayUrl();
		}
		return arr;
	}
	
	public int setMaxDBAndStopOthers(){
		int id = 0,p=0;
		for (int i = 0; i < list.size(); i++) {
			int p1 = list.get(i).getProgress();
			if(p1>p){
				p = p1;
				id = i;
			}
		}
		curDb = list.get(id);
		for (int i = 0; i < list.size(); i++) {
			if(i!=id){
				list.get(i).stop();
			}
		}
		return id;
	}
	
	public void stop() {
		for (int i = 0; i < list.size(); i++) {
			 list.get(i).stop();
		}
		list.clear();
		curDb = null;
	}
	
	public int getSize(){
		return list.size();
	}
	
	public void addUrl(String playUrl, int mode) {
		if(list.size()>=MAX_MUTIPLAY){
			return ;
		}
		
		BaseDownload db = null;
		switch (mode) {
		case TYPE_M3U8:
			db = new M3U8Download(context);
			break;
		case TYPE_FLV:
			db = new M3U8Download(context);
			break;
		case TYPE_TS:
			db = new TsDownload(context);
		default:
			break;
		}
		if (db != null) {
			db.start(playUrl);
			list.add(db);
		}
	}

}
