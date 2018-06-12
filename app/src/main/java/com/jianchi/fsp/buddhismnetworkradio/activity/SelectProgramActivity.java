package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.jianchi.fsp.buddhismnetworkradio.adapter.Mp3ManagerAdapter;
import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbQuery;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.CategoryListItem;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.CategoryListResult;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.ProgramListItem;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.ProgramListResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectProgramActivity extends AppCompatActivity {

    ExpandableListView lv;
    BApplication app;

    CategoryListResult categoryListResult;
    HashMap<Integer, List<ProgramListItem>> programListResultHashMap;
    SelectProgramAdapter selectProgramAdapter;
    ProgressBar proBar;

    ProgramListItem selectedProgramListItem = null;

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
                selectedProgramListItem = (ProgramListItem) v.getTag();
                selectProgramAdapter.notifyDataSetChanged();
                return false;
            }
        });

        lv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                //如果分组被打开 直接关闭
                if (lv.isGroupExpanded(groupPosition) ) {
                    lv.collapseGroup(groupPosition);
                } else {
                    if(programListResultHashMap.containsKey(groupPosition)){
                        return false;
                    } else {
                        //显示对话框
                        proBar.setVisibility(View.VISIBLE);
                        new LoadProgramList(groupPosition).execute();
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
        new LoadCategoryList().execute();
    }

    class LoadProgramList extends AsyncTask<Integer, Integer, List<ProgramListResult>> {
        int groupPosition;
        public LoadProgramList(int groupPosition){
            this.groupPosition = groupPosition;
        }
        @Override
        protected List<ProgramListResult> doInBackground(Integer... integers) {
            CategoryListItem categoryListItem = categoryListResult.getList().getItem().get(groupPosition);
            return AmtbQuery.queryProgramListResult(categoryListItem.getAmtbid());
        }

        @Override
        protected void onPostExecute(List<ProgramListResult> programListResultList) {
            super.onPostExecute(programListResultList);
            List<ProgramListItem> programListItems = new ArrayList<>();
            for(ProgramListResult result : programListResultList){
                programListItems.addAll(result.getList().getItem());
            }

            if(programListItems.size()>0) {
                programListResultHashMap.put(groupPosition, programListItems);
                lv.expandGroup(groupPosition, true);
            } else {
                Toast.makeText(SelectProgramActivity.this, R.string.load_nothing, Toast.LENGTH_LONG).show();
            }
            //异步加载
            proBar.setVisibility(View.INVISIBLE);
        }
    }

    class LoadCategoryList extends AsyncTask<Integer, Integer, CategoryListResult>{

        @Override
        protected CategoryListResult doInBackground(Integer... integers) {
            return AmtbQuery.queryCategoryListResult();
        }

        @Override
        protected void onPostExecute(CategoryListResult categoryListResult) {
            super.onPostExecute(categoryListResult);

            //异步加载
            proBar.setVisibility(View.INVISIBLE);
            if(categoryListResult==null){
                Toast.makeText(SelectProgramActivity.this, R.string.load_fail, Toast.LENGTH_LONG).show();
            } else {
                SelectProgramActivity.this.categoryListResult = categoryListResult;
                selectProgramAdapter = new SelectProgramAdapter();
                lv.setAdapter(selectProgramAdapter);
            }
        }
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
            return categoryListResult.getList().getItem().size();
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
            return categoryListResult.getList().getItem().get(groupPosition);
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
            txt.setText(TW2CN.getInstance(SelectProgramActivity.this).toLocal(categoryListResult.getList().getItem().get(groupPosition).getName()));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ProgramListItem programListItem = programListResultHashMap.get(groupPosition).get(childPosition);

            if(convertView==null)
                convertView = mInflater.inflate(R.layout.item_mp3_program_manager, null);

            convertView.setTag(programListItem);

            TextView txt = (TextView) convertView.findViewById(R.id.txt);
            txt.setText(TW2CN.getInstance(SelectProgramActivity.this).toLocal(programListItem.getLecturename()));

            TextView info = (TextView) convertView.findViewById(R.id.info);
            info.setText(TW2CN.getInstance(SelectProgramActivity.this).toLocal(programListItem.getLecturedate()));

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
