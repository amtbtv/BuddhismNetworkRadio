package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.MediaList;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.MediaListItem;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.MediaListResult;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.VolListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 在Mp3PlayerActivity的播放列表中用到
 */
public class Mp3ListAdapter extends BaseExpandableListAdapter {
    //volid  mp3列表
    public HashMap<Integer, MediaList> mediaListHashMap;
    public List<VolListItem> volList;

    public Mp3Program mp3Program;
    private LayoutInflater mInflater;
    Context context;

    public Mp3ListAdapter(Context context, Mp3Program mp3Program, List<VolListItem> volList, int volidIdx, MediaList mediaList){
        this.context=context;
        this.volList = volList;
        mediaListHashMap = new HashMap<>();
        mediaListHashMap.put(volidIdx, mediaList);
        this.mp3Program = mp3Program;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getGroupCount() {
        return volList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mediaListHashMap.get(groupPosition).getItem().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return volList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mediaListHashMap.get(groupPosition).getItem().get(childPosition);
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
        txt.setText(volList.get(groupPosition).getItem().getVolno());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        MediaListItem holder =(MediaListItem)getChild(groupPosition, childPosition);
        //观察convertView随ListView滚动情况
        if(convertView==null)
            convertView = mInflater.inflate(R.layout.item_mp3, null);

        convertView.setTag(holder);

        AwesomeTextView icon_play = (AwesomeTextView) convertView.findViewById(R.id.icon_play);
        if(childPosition == mp3Program.curMediaIdx && groupPosition == mp3Program.curVolIdx){
            icon_play.setFontAwesomeIcon("fa-music");
            convertView.setBackgroundResource(R.color.bootstrap_brand_warning);
        } else {
            icon_play.setFontAwesomeIcon("fa_play");
            convertView.setBackgroundResource(R.color.bootstrap_gray_lightest);
        }

        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        txt.setText(TW2CN.getInstance(context).toLocal(holder.getFileurl().split("/")[2]));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
