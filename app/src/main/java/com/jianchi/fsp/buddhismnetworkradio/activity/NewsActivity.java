package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.NewsListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewsActivity extends AppCompatActivity {
    public static final String newsListUrl  = "http://www.amtb.tw/tvchannel/show_marquee.asp";
    BApplication app;
    ListView lv_news;
    List<String> news;
    ProgressDialog proDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (BApplication)getApplication();
        lv_news = (ListView) findViewById(R.id.lv_news);

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        proDialog = ProgressDialog.show(NewsActivity.this, getString(R.string.zrsj), getString(R.string.sjjzz));
                    }
                });
                try {
                    news = GetNewsList();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (proDialog != null) proDialog.dismiss();
                            lv_news.setAdapter(new NewsListAdapter(NewsActivity.this, news));
                            setListViewHeightBasedOnChildren(lv_news);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (proDialog != null) proDialog.dismiss();
                            Toast.makeText(NewsActivity.this, R.string.sjhcsb, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();


/*
        TextView faYuTxt = (TextView) findViewById(R.id.faYuTxt);
        faYuTxt.setText(app.data.getFaYu());
        */
    }
    /**
     * 计算弄表高度
     * @param listView
     */
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }


    public List<String> GetNewsList() throws Exception {
        //<div id='bul_1'><target='_top' class='usetext' onMouseOver="this.className='applettext2';" onMouseOut="this.className='applettext1';">8月10日起重播《淨土大經科註（第四回）》。</a></div><div id='bul_2'><a href='../news/news_content.asp?web_index=398&web_select_type=6' target='_blank' target='_top' class='usetext' onMouseOver="this.className='applettext2';" onMouseOut="this.className='applettext1';">微雲、百度網盤下載：講座影音、文字、菁華短片、卡片圖檔、電子書等（4月12日更新）。</a></div>

        Pattern newsListPattern = Pattern.compile("<div id='bul_\\d'>(.*?)</div>");
        Pattern htmlTagPattern = Pattern.compile("<[^>]*>");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(newsListUrl)
                .build();

        Call call = client.newCall(request);

        try {
            Response response = call.execute();
            String html = new String(response.body().bytes(), "big5");


            Matcher m = newsListPattern.matcher(html);
            List<String> newsList = new ArrayList<String>();
            while (m.find()) {
                String nm = m.group(1);
                if (nm.startsWith("<")) {
                    Matcher hm = htmlTagPattern.matcher(nm);
                    nm = hm.replaceAll("");
                }
                newsList.add(nm);
            }
            if (newsList.size() == 0)
                MyLog.w("GetNewsList onResponse", html);
            return newsList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
