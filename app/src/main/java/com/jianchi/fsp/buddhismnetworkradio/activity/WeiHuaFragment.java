package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jianchi.fsp.buddhismnetworkradio.LoadMoreListView;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.NewsListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.model.News;
import com.jianchi.fsp.buddhismnetworkradio.model.NewsCategorie;
import com.jianchi.fsp.buddhismnetworkradio.model.NewsPager;
import com.jianchi.fsp.buddhismnetworkradio.model.StringResult;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

import java.util.ArrayList;
import java.util.List;

public class WeiHuaFragment extends Fragment {

    ProgressBar proBar;
    LoadMoreListView loadMoreListView;

    NewsPager newsPager;

    public WeiHuaFragment() {
        newsPager = new NewsPager(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wei_hua, container, false);
        proBar =  view.findViewById(R.id.proBar);
        loadMoreListView = view.findViewById(R.id.lv);
        loadMoreListView.setTag(newsPager);
        loadMoreListView.setOnLoadMoreListener(loadMoreListener);
        loadMoreListView.setOnItemClickListener(itemClickListener);
        NewsListAdapter adapter = new NewsListAdapter(getActivity(), newsPager);
        loadMoreListView.setAdapter(adapter);
        newsPager.adapter = adapter;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (newsPager.pager == 0 && newsPager.hasNext && !newsPager.isLoading) {
            loadMoreListView.showFooterView();
            loadMoreListView.loadMore();
        }
    }
    LoadMoreListView.OnLoadMoreListener loadMoreListener = new LoadMoreListView.OnLoadMoreListener() {
        @Override
        public void onLoadMore(LoadMoreListView loadMoreListView) {
            NewsPager newsPager = (NewsPager)loadMoreListView.getTag();
            newsPager.isLoading = true;
            int per_page = 10;
            String url = UrlHelper.getRsdNewsUrl(newsPager.pager + 1, per_page);
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
}