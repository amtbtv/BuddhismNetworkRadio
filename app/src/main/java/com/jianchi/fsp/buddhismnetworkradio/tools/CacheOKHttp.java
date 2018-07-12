package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/3/1.
 */

public class CacheOKHttp {

    OkHttpClient client;
    CacheControl cacheControl;

    public static Headers headers = new Headers.Builder()
            .add("Connection", "keep-alive")
            .add("Accept", "application/json, text/javascript, */*; q=0.01")
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
            .add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .add("Accept-Language", "en-US,en;q=0.8")
            .build();

    public CacheOKHttp(Context context){
        File cacheDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cacheDir = new File(context.getExternalCacheDir(), "tvcache");
        } else {
            cacheDir = new File(context.getCacheDir(), "tvcache");
        }
        //缓存文件夹
        File cacheFile = new File(cacheDir.toString(),"tvcache");
        //缓存大小为10M
        int cacheSize = 20 * 1024 * 1024;
        //创建缓存对象
        Cache cache = new Cache(cacheFile,cacheSize);

        cacheControl = new CacheControl.Builder()
                .maxStale(5, TimeUnit.DAYS)
                .build();
        client = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

    }

    public String take(String url) {
        Request request = new Request.Builder().headers(headers).cacheControl(cacheControl).url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 将下载并图片存入文件缓存
     */
    public String takeXML(String url)
    {
        String xmlStr = "";
        Request request = new Request.Builder().headers(headers).cacheControl(cacheControl).url(url).build();
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
