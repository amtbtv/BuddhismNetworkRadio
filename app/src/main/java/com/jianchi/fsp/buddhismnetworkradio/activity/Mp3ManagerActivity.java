package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.Mp3ManagerAdapter;
import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.model.Channel;
import com.jianchi.fsp.buddhismnetworkradio.model.ChannelListResult;
import com.jianchi.fsp.buddhismnetworkradio.model.ProgramListResult;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.LanguageUtils;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

import java.util.List;
import java.util.Locale;

public class Mp3ManagerActivity extends AppCompatActivity {

    ExpandableListView lv;
    BApplication app;
    Mp3ManagerAdapter mp3ManagerAdapter;
    List<Mp3Program> checkedMpsPrograms;
    ProgressBar proBar;

    @Override
    protected void attachBaseContext(Context newBase) {
        Tools.changeAppLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //获取自定义APP，APP内存在着数据，若为旋转屏幕，此处记录以前的内容
        app = (BApplication)getApplication();

        //加载时显示进度
        proBar = (ProgressBar) findViewById(R.id.mp3ProBar);

        Mp3RecDBManager db = new Mp3RecDBManager(this);
        checkedMpsPrograms = db.getAllMp3Rec();
        db.close();

        lv = (ExpandableListView) findViewById(R.id.lv_mp3);
        lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Mp3Program mp3Program = (Mp3Program) v.getTag();
                if(checkedMpsPrograms.contains(mp3Program)){
                    //删除
                    checkedMpsPrograms.remove(mp3Program);
                    Mp3RecDBManager db = new Mp3RecDBManager(Mp3ManagerActivity.this);
                    db.delMp3(mp3Program);
                    db.close();
                } else {
                    //添加
                    Mp3RecDBManager db = new Mp3RecDBManager(Mp3ManagerActivity.this);
                    mp3Program.dbRecId = db.add(mp3Program);
                    db.close();
                    checkedMpsPrograms.add(mp3Program);
                }
                mp3ManagerAdapter.notifyDataSetChanged();
                return false;
            }
        });

        lv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, final int groupPosition, long id) {
                //如果分组被打开 直接关闭
                if (lv.isGroupExpanded(groupPosition) ) {
                    lv.collapseGroup(groupPosition);
                } else {
                    if(mp3ManagerAdapter.containsGroup(groupPosition)){
                        return false;
                    } else {
                        //显示对话框
                        proBar.setVisibility(View.VISIBLE);
                        Channel channel = (Channel)mp3ManagerAdapter.getGroup(groupPosition);
                        AmtbApi<ProgramListResult> api = new AmtbApi<>(UrlHelper.takeProgramsUrl(channel.amtbid), new AmtbApiCallBack<ProgramListResult>() {
                            @Override
                            public void callBack(ProgramListResult obj) {
                                proBar.setVisibility(View.GONE);
                                if(obj.isSucess) {
                                    if(obj.programs.size()>0) {
                                        mp3ManagerAdapter.putProgramListItemList(groupPosition, obj.programs);
                                        //mp3ManagerAdapter.notifyDataSetChanged();
                                        //打开分组
                                        lv.expandGroup(groupPosition, true);
                                    } else {
                                        Toast.makeText(Mp3ManagerActivity.this, obj.msg, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(Mp3ManagerActivity.this, R.string.load_fail, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        api.execute(ProgramListResult.class);
                        return true;
                    }
                }
                //返回false表示系统自己处理展开和关闭事件 返回true表示调用者自己处理展开和关闭事件
                return true;
            }
        });

        //显示对话框
        proBar.setVisibility(View.VISIBLE);
        AmtbApi<ChannelListResult> api = new AmtbApi<>(UrlHelper.takeChannelsUrl(), new AmtbApiCallBack<ChannelListResult>() {
            @Override
            public void callBack(ChannelListResult obj) {
                proBar.setVisibility(View.GONE);
                if(obj.isSucess) {
                        mp3ManagerAdapter = new Mp3ManagerAdapter(Mp3ManagerActivity.this, checkedMpsPrograms, obj);
                        lv.setAdapter(mp3ManagerAdapter);
                } else {
                    Toast.makeText(Mp3ManagerActivity.this, obj.msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        api.execute(ChannelListResult.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
