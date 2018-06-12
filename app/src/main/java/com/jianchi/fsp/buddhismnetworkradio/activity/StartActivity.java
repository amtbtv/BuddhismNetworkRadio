package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.Mp3ChannelListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.adapter.TvChannelListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.api.Channel;
import com.jianchi.fsp.buddhismnetworkradio.api.ChannelList;
import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Constant;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 起始活动窗口
 * 包含两个节目列表：电视节目，随机点播
 * 处理启动事务，根据传过来的参数 StartWith 判断是否要直接跳转，并在启动本地或远程播放器时传入参数 StartWith
 *
 */
public class StartActivity extends AppCompatActivity {
    /**
     * 接收返回管理点播MP3列表的标记
     */
    public static final int MANAGER_MP3_RESULT = 2548;
    BApplication app;//全局应用
    ListView lv_channel;//视频列表

    BootstrapButton bt_tv;//切换为视频按扭
    BootstrapButton bt_mp3;//切换为音频点播列表按扭

    List<Mp3Program> mp3Programs;//音频节目列表
    ChannelList channelList;//视频节目列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //获取自定义APP，APP内存在着数据，若为旋转屏幕，此处记录以前的内容
        app = (BApplication)getApplication();

        //音频和视频的列表视频，listview，在点击时判断点的是音频还是视频
        lv_channel = (ListView) findViewById(R.id.lv_channel);
        lv_channel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if(app.isNetworkConnected())
                {
                    if (lv_channel.getAdapter() instanceof TvChannelListAdapter) {
                        Channel programType = (Channel) view.getTag();
                        channelList.selectedChannelTitle = programType.title;
                        Intent intent = new Intent(StartActivity.this, MainActivity.class);
                        intent.putExtra("title", programType.title);
                        intent.putExtra("listUrl", programType.listUrl);
                        intent.putExtra("tvUrl", programType.tvUrl);
                        startActivity(intent);
                    } else {
                        Mp3Program mp3Program = (Mp3Program) view.getTag();
                        Intent intent = new Intent(StartActivity.this, Mp3PlayerActivity.class);
                        //启动时判断是否已经开始播放音频节目了，并传入不同的参数
                        intent.putExtra("dbRecId", mp3Program.dbRecId);
                        startActivity(intent);
                    }
                }
                //else
                {
                //    Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_LONG).show();
                }
            }
        });


        //载入音频节目列表数据，并排序
        Mp3RecDBManager db = new Mp3RecDBManager();
        mp3Programs = db.getAllMp3Rec();
        /*
        Collections.sort(mp3Programs, new Comparator<Mp3Program>() {
            @Override
            public int compare(Mp3Program o1, Mp3Program o2) {
                Integer o1n = o1.num;
                return -o1n.compareTo(o2.num);
            }
        });
        */

        //载入视频节目列表
        channelList = new ChannelList();
        String json = Tools.readRawFile(R.raw.channels, this);
        channelList.channels= new Gson().fromJson(json, new TypeToken<List<Channel>>(){}.getType());
        channelList.selectedChannelTitle =channelList.channels.get(0).title;

        //默认初始为视频节目
        TvChannelListAdapter tvChannelListAdapter = new TvChannelListAdapter(StartActivity.this, channelList, app);
        lv_channel.setAdapter(tvChannelListAdapter);

        //切换音频视频
        bt_tv = (BootstrapButton) findViewById(R.id.bt_tv);
        bt_mp3 = (BootstrapButton) findViewById(R.id.bt_mp3);
        bt_tv.setOnCheckedChangedListener(new BootstrapButton.OnCheckedChangedListener() {
            @Override
            public void OnCheckedChanged(BootstrapButton bootstrapButton, boolean isChecked) {
                if(isChecked){
                    TvChannelListAdapter tvChannelListAdapter = new TvChannelListAdapter(StartActivity.this, channelList, app);
                    lv_channel.setAdapter(tvChannelListAdapter);
                }
            }
        });
        bt_mp3.setOnCheckedChangedListener(new BootstrapButton.OnCheckedChangedListener() {
            @Override
            public void OnCheckedChanged(BootstrapButton bootstrapButton, boolean isChecked) {
                if(isChecked) {
                    Mp3ChannelListAdapter mp3ChannelListAdapter = new Mp3ChannelListAdapter(StartActivity.this, mp3Programs);
                    lv_channel.setAdapter(mp3ChannelListAdapter);

                    if(mp3ChannelListAdapter.getCount()==0){
                        //显示对话框，要求添加音频
                        AlertDialog dialog = new AlertDialog.Builder(StartActivity.this)
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
                                        Intent intent = new Intent(StartActivity.this, Mp3ManagerActivity.class);
                                        startActivityForResult(intent, MANAGER_MP3_RESULT);
                                    }
                                }).create();
                        dialog.show();
                    }
                }
            }
        });

        //判断是不是由点击状态栏启动，判断传进来的参数
        /*
        Intent intent = getIntent();
        String startWith = intent.getStringExtra("StartWith");
        if(startWith.equals(Constant.StartWith_MP3_SERVICE)) {
            Intent mp3Intent = new Intent(StartActivity.this, Mp3PlayerActivity.class);
            mp3Intent.putExtra("StartWith", Constant.StartWith_MP3_SERVICE);
            startActivity(mp3Intent);
        }
        */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, ContactActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_news) {
            if(app.isNetworkConnected()) {
                Intent intent = new Intent(this, NewsActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (id == R.id.action_manager_mp3){
            Intent intent = new Intent(this, Mp3ManagerActivity.class);
            startActivityForResult(intent, MANAGER_MP3_RESULT);
            return true;
        } else if (id == R.id.action_download){
            Intent intent = new Intent(this, DownLoadActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * 在Mp3ManagerActivity返回时，判断是否需要更新数据
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MANAGER_MP3_RESULT) {
            //重新载入MP3 list
            Mp3RecDBManager db = new Mp3RecDBManager();
            mp3Programs = db.getAllMp3Rec();
            Mp3ChannelListAdapter mp3ChannelListAdapter = new Mp3ChannelListAdapter(StartActivity.this, mp3Programs);
            lv_channel.setAdapter(mp3ChannelListAdapter);
        }
    }
}
