package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.EndlessRecyclerOnScrollListener;
import com.jianchi.fsp.buddhismnetworkradio.adapter.FaYinLoadMoreAdapter;
import com.jianchi.fsp.buddhismnetworkradio.adapter.FaYinOnClickListener;
import com.jianchi.fsp.buddhismnetworkradio.model.FaYin;
import com.jianchi.fsp.buddhismnetworkradio.model.FaYinListResult;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.FaYinApi;

import java.util.ArrayList;
import java.util.List;

public class WordPressFragment extends Fragment {

    private String mediaUrl;
    private String jsonUrl;

    RecyclerView rv_fayin; //法音宣流
    List<FaYin> faYinList = new ArrayList<>();//法音宣流数据
    boolean faYinLoadAllOver = false; //全部载入
    int faYinCurPageId = 0;

    FaYinLoadMoreAdapter faYinLoadMoreAdapter;
    ProgressBar proBar;
    public WordPressFragment() {
        // Required empty public constructor
    }

    public WordPressFragment(String jsonUrl, String mediaUrl){
        this.jsonUrl = jsonUrl;
        this.mediaUrl = mediaUrl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wordpress, container, false);
        proBar = view.findViewById(R.id.proBar);
        rv_fayin = view.findViewById(R.id.rv);
        rv_fayin.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        faYinLoadMoreAdapter = new FaYinLoadMoreAdapter(faYinList, mediaUrl);
        faYinLoadMoreAdapter.setOnItemClickListener(new FaYinOnClickListener() {
            @Override
            public void onClick(View v, FaYin faYin) {
                /* 跳出在浏览器中打开
                Uri uri = Uri.parse(faYin.link);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
                */
                //自己解析content，来播放视频
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra("fayin", new Gson().toJson(faYin));
                startActivity(intent);
            }
        });
        rv_fayin.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                faYinLoadMoreAdapter.setLoadState(faYinLoadMoreAdapter.LOADING);
                if (!faYinLoadAllOver) {
                    loadMoreFaYin();
                } else {
                    // 显示加载到底的提示
                    faYinLoadMoreAdapter.setLoadState(faYinLoadMoreAdapter.LOADING_END);
                }
            }
        });
        rv_fayin.setAdapter(faYinLoadMoreAdapter);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(faYinList.size()==0) {
            loadMoreFaYin();
        }
    }

    public void loadMoreFaYin() {
        proBar.setVisibility(View.VISIBLE);
        faYinCurPageId++;
        //https://www.amtb.tw/blog/wp-json/wp/v2/posts?page=1
        FaYinApi<FaYinListResult> api = new FaYinApi<>(
                jsonUrl + faYinCurPageId,
                new AmtbApiCallBack<FaYinListResult>() {
                    @Override
                    public void callBack(FaYinListResult obj) {
                        proBar.setVisibility(View.GONE);
                        if (obj.isSucess) {
                            faYinList.addAll(obj.faYinList);
                            if (faYinLoadMoreAdapter != null)
                                faYinLoadMoreAdapter.setLoadState(faYinLoadMoreAdapter.LOADING_COMPLETE);
                        } else {
                            if (obj.msg == "loadover") {
                                faYinLoadAllOver = true;
                            } else {
                                Toast.makeText(getActivity(), obj.msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
        api.execute(FaYinListResult.class);
    }

}