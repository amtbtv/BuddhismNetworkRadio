package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;


/**
 * Created by fsp on 17-8-17.
 */

public class Tools {
    /// <summary>
    ///解析html成 普通文本
    /// </summary>
    /// <param name="str">string</param>
    /// <returns>string</returns>
    public static String Decode(String str)
    {
        str = str.replace("<br>","\n");
        str = str.replace("&gt;",">");
        str = str.replace("&lt;","<");
        str = str.replace("&nbsp;"," ");
        str = str.replace("&quot;","\"");
        str = str.replace("&#039;","'");
        str = str.replace("&amp;","&");
        return str;
    }

    public static void changeAppLanguage(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        // app locale
        Locale locale = BApplication.country.equals("ZH") ? Locale.SIMPLIFIED_CHINESE : Locale.TRADITIONAL_CHINESE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        // updateConfiguration
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);
    }

    public static String readRawFile(int rawId, Context context)
    {
        String tag = "readRawFile";
        String content=null;
        Resources resources=context.getResources();
        InputStream is=null;
        try{
            is=resources.openRawResource(rawId);
            byte buffer[]=new byte[is.available()];
            is.read(buffer);
            content=new String(buffer);
            MyLog.i(tag, "read:"+content);
        }
        catch(IOException e)
        {
            MyLog.e(tag, e.getMessage());
        }
        finally
        {
            if(is!=null)
            {
                try{
                    is.close();
                }catch(IOException e)
                {
                    MyLog.e(tag, e.getMessage());
                }
            }
        }
        return content;
    }

}
