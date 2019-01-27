package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.NewsListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.model.StringResult;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsActivity extends AppCompatActivity {
    BApplication app;
    ListView lv_news;
    List<String> news;
    ProgressDialog proDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        Tools.changeAppLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (BApplication)getApplication();
        lv_news = (ListView) findViewById(R.id.lv_news);

        proDialog = ProgressDialog.show(NewsActivity.this, getString(R.string.zrsj), getString(R.string.sjjzz));
        AmtbApi<StringResult> api = new AmtbApi<StringResult>(UrlHelper.getNewsUrl(),
                "big5",
                new AmtbApiCallBack<StringResult>(){
            @Override
            public void callBack(StringResult obj) {
                if (proDialog != null) proDialog.dismiss();
                if(obj.isSucess){
                    news = getNewsList(obj.string);

                    lv_news.setAdapter(new NewsListAdapter(NewsActivity.this, news));
                    setListViewHeightBasedOnChildren(lv_news);
                } else {
                    Toast.makeText(NewsActivity.this, obj.msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        api.execute(StringResult.class);
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


    public List<String> getNewsList(String html) {
        //<div id='bul_1'><target='_top' class='usetext' onMouseOver="this.className='applettext2';" onMouseOut="this.className='applettext1';">8月10日起重播《淨土大經科註（第四回）》。</a></div><div id='bul_2'><a href='../news/news_content.asp?web_index=398&web_select_type=6' target='_blank' target='_top' class='usetext' onMouseOver="this.className='applettext2';" onMouseOut="this.className='applettext1';">微雲、百度網盤下載：講座影音、文字、菁華短片、卡片圖檔、電子書等（4月12日更新）。</a></div>

        Pattern newsListPattern = Pattern.compile("<div id='bul_\\d'>(.*?)</div>");
        Pattern htmlTagPattern = Pattern.compile("<[^>]*>");
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
    }
}
