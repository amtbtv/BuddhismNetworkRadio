package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.content.Context;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.api.IpInfo;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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


    private String downHtml(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).removeHeader("User-Agent").addHeader("User-Agent",
                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0").build();
        Call call = client.newCall(request);

        try {
            Response response = call.execute();
            String html = new String(response.body().bytes(), "utf-8");
            return html;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public IpInfo getIpInof(){
        String html = downHtml("http://ip.taobao.com/service/getIpInfo2.php?ip=myip");
        IpInfo ipInfo = new Gson().fromJson(html, IpInfo.class);
        /*{
    "code": 0,
    "data": {
        "country": "中国", 台湾 日本 德国
        "country_id": "CN",
        "area": "华中",
        "area_id": "400000",
        "region": "河南省", 江苏  山东
        "region_id": "410000",
        "city": "焦作市",
        "city_id": "410800",
        "county": "",
        "county_id": "-1",
        "isp": "联通",
        "isp_id": "100026",
        "ip": "42.234.54.167"
    }
}*/
        return ipInfo;
    }
}
