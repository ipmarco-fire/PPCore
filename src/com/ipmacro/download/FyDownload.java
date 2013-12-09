package com.ipmacro.download;

import android.content.Context;
import android.util.Log;

public class FyDownload extends FlvDownload {

	public FyDownload(Context context) {
		super(context);
	}

	@Override
	public void start(String playUrl) {
		String[] arrs = playUrl.split("WNAS");
		if(arrs.length == 4){
			playUrl = arrs[0]+ randomNum(Integer.parseInt(arrs[1]),Integer.parseInt(arrs[2])) + arrs[3];
		}
		String tmp = playUrl.replace("http://", "fy://"); 
		super.start(tmp);
		mPlayUrl = playUrl;
	}
	
	public static int randomNum(int start,int end){
   	 int r = start + (int) (Math.random() * (end - start));
   	 return r;
    }
}
