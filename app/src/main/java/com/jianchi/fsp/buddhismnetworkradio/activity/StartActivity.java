package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.Mp3ChannelListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.adapter.TvChannelListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.model.Live;
import com.jianchi.fsp.buddhismnetworkradio.model.LiveListResult;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.mp3service.BMp3Service;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.SharedPreferencesHelper;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

import org.lzh.framework.updatepluginlib.UpdateBuilder;

import java.util.List;

/**
 * 起始活动窗口
 * 包含两个节目列表：电视节目，随机点播
 * 处理启动事务，根据传过来的参数 StartWith 判断是否要直接跳转，并在启动本地或远程播放器时传入参数 StartWith
 *
 */
public class StartActivity extends BaseActivity {
    private static boolean updateChecked = false;
    /**
     * 接收返回管理点播MP3列表的标记
     */
    public static final int MANAGER_MP3_RESULT = 2548;

    ListView lv_channel;//视频列表
    ListView lv_mp3;//音频列表

    ViewPager viewPager;
    private TabHost mTabHost;
    private TabWidget mTabWidget;

    List<Mp3Program> mp3Programs;//音频节目列表
    LiveListResult liveListResult;//视频节目列表

    TvChannelListAdapter tvChannelListAdapter;
    Mp3ChannelListAdapter mp3ChannelListAdapter;

    ProgressBar progressBar;


    @Override
    int getContentView() {
        return R.layout.activity_start;
    }

    @Override
    void onCreateDo() {
        setTitle(R.string.app_name);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if(app.isNetworkConnected()){
            progressBar.setVisibility(View.VISIBLE);
            AmtbApi<LiveListResult> api = new AmtbApi<>(UrlHelper.takeLivesUrl(), new AmtbApiCallBack<LiveListResult>() {
                @Override
                public void callBack(LiveListResult obj) {
                    progressBar.setVisibility(View.GONE);
                    if(obj.isSucess) {
                        liveListResult = obj;
                        setUi();
                    } else {
                        Toast.makeText(getThisActivity(), obj.msg, Toast.LENGTH_LONG).show();
                    }
                }
            });
            api.execute(LiveListResult.class);
        } else {
            Toast.makeText(getThisActivity(), R.string.no_network, Toast.LENGTH_LONG).show();
        }

        //恢复之前的播放界面
        String startWith = getIntent().getStringExtra("StartWith");
        if(startWith!=null && startWith.equals("StartWith_MP3_SERVICE")){
            startActivity(new Intent(getThisActivity(), Mp3PlayerActivity.class));
        }

        if(!updateChecked) {
            updateChecked = true;
            //每次打开首页检测更新
            UpdateBuilder.create().check();// 启动更新任务
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    void setUi(){
        mTabHost = (TabHost) findViewById(R.id.tabhost);
        mTabHost.setup();
        mTabWidget = mTabHost.getTabWidget();
        mTabHost.addTab(
                mTabHost.newTabSpec(
                        "lv_channel")
                        .setContent(R.id.tab1)
                        .setIndicator(getString(R.string.bt_label_spzb))
        );
        mTabHost.addTab(
                mTabHost.newTabSpec("lv_mp3")
                        .setContent(R.id.tab2)
                        .setIndicator(getString(R.string.bt_label_ypdb))
        );

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        //音频和视频的列表视频，listview，在点击时判断点的是音频还是视频
        lv_channel = (ListView) getLayoutInflater().inflate(R.layout.channel_list_view, null);
        lv_mp3 = (ListView) getLayoutInflater().inflate(R.layout.channel_list_view, null);

        lv_channel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Live programType = (Live) view.getTag();
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("name", programType.name);
                intent.putExtra("listUrl", programType.listUrl);
                intent.putExtra("mediaUrl", programType.mediaUrl);
                startActivity(intent);
            }
        });

        lv_mp3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Mp3Program mp3Program = (Mp3Program) view.getTag();

                //启动service
                Intent startIntent = new Intent(StartActivity.this, BMp3Service.class);
                startIntent.putExtra("dbRecId", mp3Program.dbRecId);
                ComponentName name = startService(startIntent);

                //启动播放器
                Intent intent = new Intent(StartActivity.this, Mp3PlayerActivity.class);
                startActivity(intent);
            }
        });

        //载入音频节目列表数据，并排序
        Mp3RecDBManager db = new Mp3RecDBManager(this);
        mp3Programs = db.getAllMp3Rec();
        db.close();
        mp3ChannelListAdapter = new Mp3ChannelListAdapter(StartActivity.this, mp3Programs);
        lv_mp3.setAdapter(mp3ChannelListAdapter);

        //默认初始为视频节目
        tvChannelListAdapter = new TvChannelListAdapter(StartActivity.this, liveListResult);
        lv_channel.setAdapter(tvChannelListAdapter);

        //设置viewPager的监听器
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            //当 滑动 切换时
            @Override
            public void onPageSelected(int position) {
                mTabWidget.setCurrentTab(position);
                if(position == 1){
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
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //TabHost的监听事件
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if(tabId.equals("lv_channel")){
                    viewPager.setCurrentItem(0, true);
                }else{
                    viewPager.setCurrentItem(1, true);
                }
            }
        });

        viewPager.setAdapter(mPagerAdapter);
        viewPager.setCurrentItem(0);
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
            Intent intent = new Intent(this, NewsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_manager_mp3){
            if(lv_mp3!=null) {
                Intent intent = new Intent(this, Mp3ManagerActivity.class);
                startActivityForResult(intent, MANAGER_MP3_RESULT);
            }
            return true;
        } else if (id == R.id.action_download){
            Intent intent = new Intent(this, DownLoadActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_zh_tw){
            //切换语言
            if(BApplication.country.equals("ZH"))
                BApplication.country = "TW";
            else
                BApplication.country = "ZH";
            new SharedPreferencesHelper(this, "setting").putString("local", BApplication.country);
            Tools.changeAppLanguage(this);
            recreate();//刷新界面
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * 在Mp3ManagerActivity返回时，更新数据
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MANAGER_MP3_RESULT) {
            //当没有网络时，mp3ChannelListAdapter = null
            if(mp3ChannelListAdapter!=null) {
                //重新载入MP3 list
                Mp3RecDBManager db = new Mp3RecDBManager(this);
                mp3Programs = db.getAllMp3Rec();
                db.close();
                mp3ChannelListAdapter.setMp3Programs(mp3Programs);
                mp3ChannelListAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * pager adapter
     */
    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            View view = position==0 ? lv_channel : lv_mp3;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, params);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(position==0 ? lv_channel : lv_mp3);
        }
    };
}
