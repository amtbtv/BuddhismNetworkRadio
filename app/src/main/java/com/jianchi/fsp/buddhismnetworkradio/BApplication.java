package com.jianchi.fsp.buddhismnetworkradio;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.jianchi.fsp.buddhismnetworkradio.tools.CacheOKHttp;
import com.jianchi.fsp.buddhismnetworkradio.tools.SharedPreferencesHelper;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;

import java.util.Locale;

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
    public static String country;

    @Override
    protected void attachBaseContext(Context base) {
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(base, "setting");
        country = sharedPreferencesHelper.getString("local");
        if(country==null || country.isEmpty()){
            Resources resources = base.getResources();
            //在7.0以上和7.0一下获取国家的方式有点不一样
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //  大于等于24即为7.0及以上执行内容
                country = resources.getConfiguration().getLocales().get(0).getCountry();
            } else {
                //  低于24即为7.0以下执行内容
                country = resources.getConfiguration().locale.getCountry();
            }
            sharedPreferencesHelper.putString("local", country);
        }
        Tools.changeAppLanguage(base);
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = BApplication.country.equals("ZH") ? Locale.SIMPLIFIED_CHINESE : Locale.TRADITIONAL_CHINESE;
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);

        sApp = this;
        http = new CacheOKHttp(this);

        TypefaceProvider.registerDefaultIconSets();

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

    public String getResourceString(int id){
        Resources resources = getResources();
        return resources.getString(id);
    }
}
