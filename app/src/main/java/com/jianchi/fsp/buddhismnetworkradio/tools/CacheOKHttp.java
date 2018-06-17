package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/3/1.
 */

public class CacheOKHttp {
    OkHttpClient client;
    public CacheOKHttp(Context context){
        File cacheDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cacheDir = new File(context.getExternalCacheDir(), "bcache");
        } else {
            cacheDir = new File(context.getCacheDir(), "bcache");
        }
        //缓存文件夹
        File cacheFile = new File(cacheDir.toString(),"bcache");
        //缓存大小为10M
        int cacheSize = 10 * 1024 * 1024;
        //创建缓存对象
        Cache cache = new Cache(cacheFile,cacheSize);

        client = new OkHttpClient.Builder()
                .cache(cache)
                .build();
    }

    /**
     * 将下载并图片存入文件缓存
     */
    public String takeXML(String url)
    {
        String xmlStr = "";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
                if (response.body() != null) {
                    xmlStr = new String(response.body().bytes(), "BIG5");
                }
            }
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xmlStr;
    }
}
