package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.Channel;
import com.jianchi.fsp.buddhismnetworkradio.model.ChannelListResult;
import com.jianchi.fsp.buddhismnetworkradio.model.Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.LanguageUtils;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SelectProgramActivity extends AppCompatActivity {

    ExpandableListView lv;
    BApplication app;

    ChannelListResult channelListResult;
    HashMap<Integer, List<Program>> programListResultHashMap;
    SelectProgramAdapter selectProgramAdapter;
    ProgressBar proBar;

    Program selectedProgramListItem = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        Tools.changeAppLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_program);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //获取自定义APP，APP内存在着数据，若为旋转屏幕，此处记录以前的内容
        app = (BApplication)getApplication();

        //加载时显示进度
        proBar = (ProgressBar) findViewById(R.id.mp3ProBar);

        lv = (ExpandableListView) findViewById(R.id.lv_mp3);
        lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                selectedProgramListItem = (Program) v.getTag();
                selectProgramAdapter.notifyDataSetChanged();
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
                    if(programListResultHashMap.containsKey(groupPosition)){
                        return false;
                    } else {
                        //显示对话框
                        proBar.setVisibility(View.VISIBLE);
                        Channel channel = (Channel)selectProgramAdapter.getGroup(groupPosition);
                        AmtbApi<com.jianchi.fsp.buddhismnetworkradio.model.ProgramListResult> api = new AmtbApi<>(UrlHelper.takeProgramsUrl(channel.amtbid)
                                , new AmtbApiCallBack<com.jianchi.fsp.buddhismnetworkradio.model.ProgramListResult>() {
                            @Override
                            public void callBack(com.jianchi.fsp.buddhismnetworkradio.model.ProgramListResult obj) {
                                proBar.setVisibility(View.GONE);
                                if(obj.isSucess) {
                                    if(obj.programs.size()>0) {
                                        programListResultHashMap.put(groupPosition, obj.programs);
                                        lv.expandGroup(groupPosition, true);
                                    } else {
                                        Toast.makeText(SelectProgramActivity.this, R.string.load_nothing, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(SelectProgramActivity.this, obj.msg, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        api.execute(com.jianchi.fsp.buddhismnetworkradio.model.ProgramListResult.class);
                        return true;
                    }
                }
                //返回false表示系统自己处理展开和关闭事件 返回true表示调用者自己处理展开和关闭事件
                return true;
            }
        });
        programListResultHashMap = new HashMap<>();
        //显示对话框
        proBar.setVisibility(View.VISIBLE);
        AmtbApi<ChannelListResult> api = new AmtbApi<>(UrlHelper.takeChannelsUrl(), new AmtbApiCallBack<ChannelListResult>() {
            @Override
            public void callBack(ChannelListResult obj) {
                proBar.setVisibility(View.GONE);
                if(obj.isSucess) {
                    SelectProgramActivity.this.channelListResult = obj;
                    selectProgramAdapter = new SelectProgramAdapter();
                    lv.setAdapter(selectProgramAdapter);
                } else {
                    Toast.makeText(SelectProgramActivity.this, obj.msg, Toast.LENGTH_LONG).show();
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
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if(selectedProgramListItem!=null) {
                Intent receivedIntent = getIntent();
                receivedIntent.putExtra("data", new Gson().toJson(selectedProgramListItem));
                setResult(RESULT_OK, receivedIntent);
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class SelectProgramAdapter extends BaseExpandableListAdapter {
        LayoutInflater mInflater = LayoutInflater.from(SelectProgramActivity.this);

        @Override
        public int getGroupCount() {
            return channelListResult.channels.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return programListResultHashMap.get(groupPosition).size();
        }

        public boolean containsGroup(int groupPosition){
            return programListResultHashMap.containsKey(groupPosition);
        }

        @Override
        public Object getGroup(int groupPosition) {
            return channelListResult.channels.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return programListResultHashMap.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }


        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.group_channel, null);
            }
            TextView txt = (TextView) convertView.findViewById(R.id.txt);
            txt.setText(TW2CN.getInstance(SelectProgramActivity.this).toLocal(channelListResult.channels.get(groupPosition).name));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Program programListItem = programListResultHashMap.get(groupPosition).get(childPosition);

            if(convertView==null)
                convertView = mInflater.inflate(R.layout.item_mp3_program_manager, null);

            convertView.setTag(programListItem);

            TextView txt = (TextView) convertView.findViewById(R.id.txt);
            txt.setText(TW2CN.getInstance(SelectProgramActivity.this).toLocal(programListItem.name));

            TextView info = (TextView) convertView.findViewById(R.id.info);
            info.setText(TW2CN.getInstance(SelectProgramActivity.this).toLocal(programListItem.name));

            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            if(programListItem == selectedProgramListItem){ //未添加到数据库，即未被选择，选择的必定已经添加到了数据库
                checkBox.setChecked(true);
                convertView.setBackgroundResource(R.color.bootstrap_brand_warning);
            } else {
                checkBox.setChecked(false);
                convertView.setBackgroundResource(R.color.bootstrap_gray_lightest);
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
