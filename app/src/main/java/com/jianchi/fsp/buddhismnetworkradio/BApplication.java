package com.jianchi.fsp.buddhismnetworkradio;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.jianchi.fsp.buddhismnetworkradio.db.DBHelper;
import com.jianchi.fsp.buddhismnetworkradio.tools.CacheOKHttp;
import com.tencent.bugly.Bugly;

/**
 * Created by fsp on 16-7-13.
 * 保存状态，以便在屏幕旋转后使用
 */
public class BApplication extends Application {
    private static BApplication sApp;
    public CacheOKHttp http;
    public static BApplication getInstance() {
        return sApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        http = new CacheOKHttp(this);

        //腾讯错误收集和自动升级服务注册
        Bugly.init(getApplicationContext(), "c833a75af3", false);

        TypefaceProvider.registerDefaultIconSets();

        DBHelper.init(this);

    }

    /**
     * 检测网络是否可用
     * @return boolean
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = null;
        if (cm != null) {
            ni = cm.getActiveNetworkInfo();
        }
        return ni != null && ni.isConnected();
    }
}
