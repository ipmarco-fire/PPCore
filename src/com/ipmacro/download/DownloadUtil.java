package com.ipmacro.download;

import android.content.Context;

public class DownloadUtil {
    public static final int TYPE_M3U8 = 8;
    public static final int TYPE_FLV = 9;
    public static final int TYPE_TS = 5;
    public static final int TYPE_FY = 10;
    
    private Context context;
    
    public DownloadUtil(Context context){
        this.context = context;
    }
    
    public  BaseDownload createDB(int mode){
        BaseDownload db = null;
        switch (mode) {
        case TYPE_M3U8:
            db = new M3U8Download(context);
            break;
        case TYPE_FLV:
            db = new FlvDownload(context);
            break;
        case TYPE_TS:
            db = new TsDownload(context);
            break;
        case TYPE_FY:
            db = new FyDownload(context);
        default:
            break;
        }
        return db;
    }
}
