package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.NewsListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.model.News;
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
    List<News> news;
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

        lv_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                News news = (News) view.getTag();
                //https://www.hwadzan.tv/news/all_news.html
                String url = news.url.startsWith("/") ? "https://www.hwadzan.tv"+news.url : news.url;
                Uri uri = Uri.parse(url);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
            }
        });

        proDialog = ProgressDialog.show(NewsActivity.this, getString(R.string.zrsj), getString(R.string.sjjzz));
        AmtbApi<StringResult> api = new AmtbApi<StringResult>(UrlHelper.getNewsUrl(),
                "utf-8",
                new AmtbApiCallBack<StringResult>(){
            @Override
            public void callBack(StringResult obj) {
                if (proDialog != null) proDialog.dismiss();
                if(obj.isSucess){
                    news = getNewsList(obj.string);

                    lv_news.setAdapter(new NewsListAdapter(NewsActivity.this, news));
                    //setListViewHeightBasedOnChildren(lv_news);
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
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + dp2px(480);
        listView.setLayoutParams(params);
    }

    int dp2px(float dpValue) { final float scale = getResources().getDisplayMetrics().density; return (int) (dpValue * scale + 0.5f); }

    public List<News> getNewsList(String html) {
        //<li><span>2019-06-03</span><a href='https://edu.hwadzan.tv/livetv' title='6月3日起<sup>上</sup>淨<sup>下</sup>空老和尚暫停講經'
        Pattern newsListPattern = Pattern.compile("<li><span>([^<]*)</span><a href='([^']*)' title='([^']*)'");
        Matcher m = newsListPattern.matcher(html);
        List<News> newsList = new ArrayList<>();
        while (m.find()) {
            News news = new News();
            news.time = m.group(1);
            news.url = m.group(2);
            news.title = m.group(3);
            newsList.add(news);
        }
        if (newsList.size() == 0)
            MyLog.w("GetNewsList onResponse", html);
        return newsList;
    }
}
