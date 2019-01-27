package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;

import java.util.Locale;

public class LanguageUtils {
    public static Context attachBaseContext(Context context) {
        applyLanguage(context);
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createConfigurationResources(context);
        } else {
            return context;
        }
        */
        return null;
    }

    /**
     * 7.1.1以下设置语言的方式
     *
     * @param context
     */
    public static void applyLanguage(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = BApplication.country.equals("ZH") ? Locale.SIMPLIFIED_CHINESE : Locale.TRADITIONAL_CHINESE;
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);
    }

    /**
     * 7.1.1以上设置语言的方式
     *
     * @param context
     * @return
     */

    @TargetApi(Build.VERSION_CODES.N)
    private static Context createConfigurationResources(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(BApplication.country.equals("ZH") ? Locale.SIMPLIFIED_CHINESE : Locale.TRADITIONAL_CHINESE);
        return context.createConfigurationContext(configuration);
    }
}
