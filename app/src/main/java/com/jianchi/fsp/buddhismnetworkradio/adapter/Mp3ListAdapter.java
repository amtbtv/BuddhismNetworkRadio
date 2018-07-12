package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;

import java.util.List;

/**
 * 在Mp3PlayerActivity的播放列表中用到
 */
public class Mp3ListAdapter extends BaseAdapter {
    //volid  mp3列表
    public List<String> mediaList;
    public Mp3Program mp3Program;
    private LayoutInflater mInflater;
    Context context;

    public Mp3ListAdapter(Context context, Mp3Program mp3Program, List<String> mediaList){
        this.context=context;
        this.mediaList = mediaList;
        this.mp3Program = mp3Program;
        this.mInflater = LayoutInflater.from(context);
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
        String mp3 = mediaList.get(position);
        //观察convertView随ListView滚动情况
        if(convertView==null)
            convertView = mInflater.inflate(R.layout.item_mp3, null);

        convertView.setTag(mp3);

        AwesomeTextView icon_play = (AwesomeTextView) convertView.findViewById(R.id.icon_play);
        if(position == mp3Program.curMediaIdx){
            icon_play.setFontAwesomeIcon("fa-music");
            convertView.setBackgroundResource(R.color.bootstrap_brand_warning);
        } else {
            icon_play.setFontAwesomeIcon("fa_play");
            convertView.setBackgroundResource(R.color.bootstrap_gray_lightest);
        }

        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        txt.setText(mp3);

        return convertView;
    }
}
