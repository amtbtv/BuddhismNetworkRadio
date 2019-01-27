package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.Result;

import java.io.IOException;

public class AmtbApi<T extends Result> extends AsyncTask<Class<T>, Integer, T> {

    AmtbApiCallBack<T> amtbApiCallBack;
    String url;
    String charset;

    public AmtbApi(String url, AmtbApiCallBack<T> amtbApiCallBack){
        this(url, "UTF-8", amtbApiCallBack);
    }

    public AmtbApi(String url, String charset, AmtbApiCallBack<T> amtbApiCallBack){
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
                    T val = new Gson().fromJson(json, parms[0]);
                    val.isSucess = true;
                    val.msg = BApplication.getInstance().getResourceString(R.string.api_msg_download_success);
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
