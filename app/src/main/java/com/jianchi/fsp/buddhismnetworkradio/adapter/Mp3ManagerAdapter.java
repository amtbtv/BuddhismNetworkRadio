package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.CategoryListResult;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.ProgramListItem;

import java.util.HashMap;
import java.util.List;

/**
 * Created by fsp on 16-7-6.
 */
public class Mp3ManagerAdapter extends BaseExpandableListAdapter {
    List<Mp3Program> checkedMpsPrograms;
    HashMap<Integer, List<ProgramListItem>> programListResultHashMap;
    CategoryListResult categoryListResult;
    private LayoutInflater mInflater;
    Context context;

    public Mp3ManagerAdapter(Context context, List<Mp3Program> checkedMpsPrograms, CategoryListResult categoryListResult){
        this.context=context;
        this.checkedMpsPrograms =checkedMpsPrograms;
        this.categoryListResult = categoryListResult;
        this.mInflater = LayoutInflater.from(context);
        programListResultHashMap = new HashMap<>();
    }


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
        txt.setText(TW2CN.getInstance(context).toLocal(categoryListResult.getList().getItem().get(groupPosition).getName()));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ProgramListItem programListItem = programListResultHashMap.get(groupPosition).get(childPosition);
        //获取Mp3Program，若存在直接获取，不存在生成一个
        Mp3Program holder = checkMp3Program(programListItem);

        //观察convertView随ListView滚动情况
        if(convertView==null)
            convertView = mInflater.inflate(R.layout.item_mp3_program_manager, null);

        convertView.setTag(holder);

        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        txt.setText(TW2CN.getInstance(context).toLocal(holder.programListItem.getLecturename()));

        TextView info = (TextView) convertView.findViewById(R.id.info);
        info.setText(TW2CN.getInstance(context).toLocal(holder.programListItem.getLecturedate()));

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

        if(holder.dbRecId!=-1){ //未添加到数据库，即未被选择，选择的必定已经添加到了数据库
            checkBox.setChecked(true);
            convertView.setBackgroundResource(R.color.bootstrap_brand_warning);
        } else {
            checkBox.setChecked(false);
            convertView.setBackgroundResource(R.color.bootstrap_gray_lightest);
        }

        return convertView;
    }

    private Mp3Program checkMp3Program(ProgramListItem programListItem){
        int lectureid = programListItem.getLectureid();
        for(Mp3Program mp : checkedMpsPrograms){
            if(mp.programListItem.getLectureid() == lectureid)
                return mp;
        }

        Mp3Program mp3Program = new Mp3Program();
        mp3Program.programListItem = programListItem;
        return mp3Program;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public void putProgramListItemList(int groupPosition, List<ProgramListItem> programListItems) {
        programListResultHashMap.put(groupPosition, programListItems);
    }
}
