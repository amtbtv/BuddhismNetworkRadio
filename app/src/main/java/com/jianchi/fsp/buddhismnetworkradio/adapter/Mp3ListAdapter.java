package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.FileItem;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.List;

/**
 * 在Mp3PlayerActivity的播放列表中用到
 */
public class Mp3ListAdapter extends BaseAdapter {
    //volid  mp3列表
    public List<FileItem> mediaList;
    private LayoutInflater mInflater;
    Context context;
    public int curMediaIdx;

    IconicsDrawable iconMusic;
    IconicsDrawable iconPlay;

    public Mp3ListAdapter(Context context, List<FileItem> mediaList, int curMediaIdx){
        this.context=context;
        this.mediaList = mediaList;
        this.curMediaIdx = curMediaIdx;
        this.mInflater = LayoutInflater.from(context);
        iconMusic = new IconicsDrawable(context, FontAwesome.Icon.faw_music);
        iconPlay = new IconicsDrawable(context, FontAwesome.Icon.faw_play);
    }

    public FileItem getCurFileItem() {
        return mediaList.get(curMediaIdx);
    }

    public void setData(List<FileItem> mp3s, int curMediaIdx) {
        this.mediaList = mp3s;
        this.curMediaIdx = curMediaIdx;
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }

    @Override
    public Object getItem(int position) {
        return mediaList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileItem mp3 = mediaList.get(position);
        //观察convertView随ListView滚动情况
        if(convertView==null)
            convertView = mInflater.inflate(R.layout.item_mp3, null);

        convertView.setTag(mp3);

        IconicsImageView icon_play = (IconicsImageView) convertView.findViewById(R.id.icon_play);
        if(position == curMediaIdx){
            icon_play.setIcon(iconMusic);
            convertView.setBackgroundResource(R.color.tab_background);
        } else {
            icon_play.setIcon(iconPlay);
            convertView.setBackgroundResource(R.color.design_default_color_background);
        }

        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        txt.setText(mp3.file);

        return convertView;
    }
}
