package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.TvChannelListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.model.Live;
import com.jianchi.fsp.buddhismnetworkradio.model.LiveListResult;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

public class LiveFragment extends Fragment {
    ListView lv_channel;//视频列表
    LiveListResult liveListResult;//视频节目列表
    TvChannelListAdapter tvChannelListAdapter;
    ProgressBar proBar;
    public LiveFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        proBar =  view.findViewById(R.id.proBar);
        lv_channel = view.findViewById(R.id.lv);
        lv_channel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Live programType = (Live) view.getTag();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("name", programType.name);
                intent.putExtra("listUrl", programType.listUrl);
                intent.putExtra("mediaUrl", programType.mediaUrl);
                startActivity(intent);
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(liveListResult==null){
            downLiveList();
        } else {
            setAdapter();
        }
    }

    void downLiveList(){
        proBar.setVisibility(View.VISIBLE);
        AmtbApi<LiveListResult> api = new AmtbApi<>(UrlHelper.takeLivesUrl(), new AmtbApiCallBack<LiveListResult>() {
            @Override
            public void callBack(LiveListResult obj) {
                proBar.setVisibility(View.GONE);
                if(obj.isSucess) {
                    liveListResult = obj;
                    setAdapter();
                } else {
                    Toast.makeText(getActivity(), obj.msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        api.execute(LiveListResult.class);
    }

    void setAdapter(){
        //默认初始为视频节目
        tvChannelListAdapter = new TvChannelListAdapter(getActivity(), liveListResult);
        lv_channel.setAdapter(tvChannelListAdapter);
    }
}