package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.Mp3ChannelListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.mp3service.BMp3Service;

import java.util.List;

public class ProgramFragment extends Fragment {

    /**
     * 接收返回管理点播MP3列表的标记
     */
    public static final int MANAGER_MP3_RESULT = 2548;
    ListView lv_mp3;//音频列表
    List<Mp3Program> mp3Programs;//音频节目列表
    Mp3ChannelListAdapter mp3ChannelListAdapter;
    ProgressBar proBar;
    FloatingActionButton fab;
    /*
     * 在Mp3ManagerActivity返回时，更新数据
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGER_MP3_RESULT) {
            //当没有网络时，mp3ChannelListAdapter = null
            if(mp3ChannelListAdapter!=null) {
                //重新载入MP3 list
                Mp3RecDBManager db = new Mp3RecDBManager(getActivity());
                mp3Programs = db.getAllMp3Rec();
                db.close();
                mp3ChannelListAdapter.setMp3Programs(mp3Programs);
                mp3ChannelListAdapter.notifyDataSetChanged();
            }
        }
    }

    public ProgramFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_program, container, false);
        proBar =  view.findViewById(R.id.proBar);
        lv_mp3 = view.findViewById(R.id.lv);
        fab = view.findViewById(R.id.fab);

        //载入音频节目列表数据，并排序
        Mp3RecDBManager db = new Mp3RecDBManager(getActivity());
        mp3Programs = db.getAllMp3Rec();
        db.close();

        mp3ChannelListAdapter = new Mp3ChannelListAdapter(getActivity(), mp3Programs);
        mp3ChannelListAdapter._PlayMp3OnClickListener = new Mp3ChannelListAdapter.InnerItemOnclickListener() {
            @Override
            public void itemClick(int position) {
                Mp3Program mp3Program = mp3Programs.get(position);
                //启动service
                Intent startIntent = new Intent(getActivity(), BMp3Service.class);
                startIntent.putExtra("dbRecId", mp3Program.dbRecId);
                ComponentName name = getActivity().startService(startIntent);

                //启动播放器
                Intent intent = new Intent(getActivity(), Mp3PlayerActivity.class);
                startActivity(intent);
            }
        };
        mp3ChannelListAdapter._PlayMp4OnClickListener = new Mp3ChannelListAdapter.InnerItemOnclickListener() {
            @Override
            public void itemClick(int position) {
                Mp3Program mp3Program = mp3Programs.get(position);
                //启动播放器
                Intent intent = new Intent(getActivity(), Mp4PlayerActivity.class);
                intent.putExtra("dbRecId", mp3Program.dbRecId);
                startActivity(intent);
            }
        };

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Mp3ManagerActivity.class);
                startActivityForResult(intent, MANAGER_MP3_RESULT);
            }
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        lv_mp3.setAdapter(mp3ChannelListAdapter);

        if(mp3ChannelListAdapter.getCount()==0){
            //显示对话框，要求添加音频
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setIcon(R.mipmap.ic_launcher)//设置标题的图片
                    .setTitle(R.string.title_activity_mp3_manager)//设置对话框的标题
                    .setMessage(R.string.open_mp3_manager_msg)//设置对话框的内容
                    //设置对话框的按钮
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(getActivity(), Mp3ManagerActivity.class);
                            startActivityForResult(intent, MANAGER_MP3_RESULT);
                        }
                    }).create();
            dialog.show();
        }
    }
}