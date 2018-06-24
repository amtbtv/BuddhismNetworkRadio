package com.jianchi.fsp.buddhismnetworkradio.tools;

import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class CacheInterceptor  implements Interceptor {

    static int cacheTime = 24*60*60*3;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originResponse = chain.proceed(chain.request());
        //设置缓存时间为60秒，并移除了pragma消息头，移除它的原因是因为pragma也是控制缓存的一个消息头属性
        return originResponse.newBuilder().removeHeader("pragma")
                .header("Cache-Control","max-age="+cacheTime).build();
    }
}