package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.LoadMoreListView;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.NewsListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.model.News;
import com.jianchi.fsp.buddhismnetworkradio.model.NewsCategorie;
import com.jianchi.fsp.buddhismnetworkradio.model.NewsPager;
import com.jianchi.fsp.buddhismnetworkradio.model.StringResult;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewsActivity extends AppCompatActivity {
    BApplication app;

    ViewPager viewPager;
    TabLayout tabLayout;
    ProgressBar proBar;

    List<NewsCategorie> newsCategorieList;
    HashMap<NewsCategorie, NewsPager> newsPagerHashMap;

    List<View> viewList;

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

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        proBar = findViewById(R.id.proBar);

        proBar.setVisibility(View.VISIBLE);
        loadNewsCategorieList();
    }

    void loadNewsCategorieList(){
        String url = UrlHelper.getNewsCategorieUrl();
        AmtbApi<StringResult> api = new AmtbApi<StringResult>(url,
                "utf-8",
                new AmtbApiCallBack<StringResult>(){
                    @Override
                    public void callBack(StringResult obj) {
                        proBar.setVisibility(View.INVISIBLE);
                        if(obj.isSucess){
                            String newsCategoriesJson = obj.string;
                            newsCategorieList = new Gson().fromJson(newsCategoriesJson, new TypeToken<List<NewsCategorie>>() {}.getType());
                            viewList = new ArrayList<>();

                            newsPagerHashMap = new HashMap<>();
                            LayoutInflater mInflater = LayoutInflater.from(NewsActivity.this);
                            for(NewsCategorie nc : newsCategorieList){
                                NewsPager newsPager = new NewsPager(nc);
                                View v = mInflater.inflate(R.layout.load_more_listview, null);
                                LoadMoreListView loadMoreListView = v.findViewById(R.id.lv);
                                loadMoreListView.setTag(newsPager);
                                loadMoreListView.setOnLoadMoreListener(loadMoreListener);
                                loadMoreListView.setOnItemClickListener(itemClickListener);
                                NewsListAdapter adapter = new NewsListAdapter(NewsActivity.this, newsPager);
                                loadMoreListView.setAdapter(adapter);
                                newsPager.adapter = adapter;
                                viewList.add(v);
                            }
                            ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(viewList);
                            viewPager.setAdapter(viewPagerAdapter);
                            tabLayout.setupWithViewPager(viewPager);
                        }
                    }
                });
        api.execute(StringResult.class);
    }

    LoadMoreListView.OnLoadMoreListener loadMoreListener = new LoadMoreListView.OnLoadMoreListener() {
        @Override
        public void onLoadMore(LoadMoreListView loadMoreListView) {
            NewsPager newsPager = (NewsPager)loadMoreListView.getTag();
            newsPager.isLoading = true;
            int per_page = 10;
            String url = UrlHelper.getNewsUrl(newsPager.nc.id, newsPager.pager + 1, per_page);
            AmtbApi<StringResult> api = new AmtbApi<StringResult>(url,
                    "utf-8",
                    new AmtbApiCallBack<StringResult>(){
                        @Override
                        public void callBack(StringResult obj) {
                            if(obj.isSucess){
                                newsPager.pager += 1;
                                List<News> news = getNewsList(obj.string);
                                if(news.size() < per_page){
                                    loadMoreListView.onLoadMoreComplete();
                                } else {
                                    loadMoreListView.onLoadMoreFinish();
                                }
                                if(news.size()>0){
                                    newsPager.newsList.addAll(news);
                                    newsPager.adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
            api.execute(StringResult.class);
        }
    };

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(view.getTag()==null)
                return;
            News news = (News) view.getTag();
            //https://www.hwadzan.tv/news/all_news.html
            String url = news.link;
            Uri uri = Uri.parse(url);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(uri);
            startActivity(intent);
        }
    };

    public List<News> getNewsList(String json) {
        if(json.contains("rest_post_invalid_page_number")){
            return new ArrayList<>();
        } else {
            return new Gson().fromJson(json, new TypeToken<List<News>>() {}.getType());
        }
    }


    class ViewPagerAdapter extends PagerAdapter {
        private List<View> viewList;

        public ViewPagerAdapter(List<View> viewList) {
            this.viewList=viewList;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = viewList.get(position);
            LoadMoreListView loadMoreListView = v.findViewById(R.id.lv);
            NewsPager newsPager = (NewsPager)loadMoreListView.getTag();
            if(newsPager.pager == 0 && newsPager.hasNext && !newsPager.isLoading){
                loadMoreListView.showFooterView();
                loadMoreListView.loadMore();
            }
            container.addView(v);
            return v;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            View v = viewList.get(position);
            LoadMoreListView loadMoreListView = v.findViewById(R.id.lv);
            NewsPager newsPager = (NewsPager)loadMoreListView.getTag();
            return newsPager.nc.title;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }
    }
}
