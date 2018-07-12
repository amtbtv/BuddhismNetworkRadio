package com.jianchi.fsp.buddhismnetworkradio.tools;

import com.jianchi.fsp.buddhismnetworkradio.model.Result;

public interface AmtbApiCallBack<T extends Result> {
    void callBack(T obj);
}
