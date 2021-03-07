package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.FaYin;
import com.jianchi.fsp.buddhismnetworkradio.model.FaYinListResult;
import com.jianchi.fsp.buddhismnetworkradio.model.Result;
import com.jianchi.fsp.buddhismnetworkradio.model.StringResult;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class FaYinApi<T extends Result> extends AsyncTask<Class<T>, Integer, T> {

    AmtbApiCallBack<T> amtbApiCallBack;
    String url;
    String charset;

    public FaYinApi(String url, AmtbApiCallBack<T> amtbApiCallBack){
        this(url, "UTF-8", amtbApiCallBack);
    }

    public FaYinApi(String url, String charset, AmtbApiCallBack<T> amtbApiCallBack){
        this.url = url;
        this.amtbApiCallBack = amtbApiCallBack;
        this.charset = charset;
    }

    @Override
    protected void onPostExecute(T obj) {
        amtbApiCallBack.callBack(obj);
        super.onPostExecute(obj);
    }

    @Override
    protected T doInBackground(Class<T>... parms) {
        Class<T> _class = parms[0];
        try {
            String json = BApplication.getInstance().http.take(url, charset);
            if (!json.isEmpty()) {
                try {
                    T val = _class.newInstance();
                    if(json.startsWith("{\"code\":\"rest_post_invalid_page_number\"")){
                        val.isSucess = false;
                        val.msg = "loadover";
                    } else {
                        val.isSucess = true;
                        val.msg = BApplication.getInstance().getResourceString(R.string.api_msg_download_success);
                        Type type = new TypeToken<ArrayList<FaYin>>(){}.getType();
                        ((FaYinListResult) val).faYinList = new Gson().fromJson(json, type);
                    }
                    return val;
                } catch (Exception e){
                    return createFailT(_class, BApplication.getInstance().getResourceString(R.string.api_msg_parse_fail));
                }
            } else {
                return createFailT(_class, BApplication.getInstance().getResourceString(R.string.api_msg_download_fail));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return createFailT(_class, BApplication.getInstance().getResourceString(R.string.api_msg_network_fail));
        } catch (MyHttpExpception myHttpExpception) {
            myHttpExpception.printStackTrace();
            return createFailT(_class, myHttpExpception.getMessage());
        } catch (Exception en){
            return createFailT(_class, BApplication.getInstance().getResourceString(R.string.api_unknow_fail));
        }
    }

    private T createFailT(Class<T> _class, String msg){
        try {
            T val  = _class.newInstance();
            val.isSucess = false;
            val.msg = msg;
            return val;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
