package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;
import com.mikepenz.iconics.view.IconicsButton;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.List;

/**
 * Created by fsp on 16-7-6.
 */
public class Mp3ChannelListAdapter extends BaseAdapter {

    public interface InnerItemOnclickListener {
        void itemClick(int position);
    }
    public InnerItemOnclickListener _PlayMp3OnClickListener;
    public InnerItemOnclickListener _PlayMp4OnClickListener;

    List<Mp3Program> mp3Programs;
    private LayoutInflater mInflater;
    Context context;
    public Mp3ChannelListAdapter(Context context, List<Mp3Program> mp3Programs){
        this.context=context;
        this.mp3Programs=mp3Programs;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setMp3Programs(List<Mp3Program> mp3Programs){
        this.mp3Programs = mp3Programs;
    }

    @Override
    public int getCount() {
        return mp3Programs.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Programs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    View.OnClickListener playMp3OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            if(_PlayMp3OnClickListener!=null)
                _PlayMp3OnClickListener.itemClick(position);
        }
    };
    View.OnClickListener playMp4OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            if(_PlayMp4OnClickListener!=null)
                _PlayMp4OnClickListener.itemClick(position);
        }
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Mp3Program mp3Program = mp3Programs.get(position);
        //观察convertView随ListView滚动情况
        if(convertView==null)
            convertView = mInflater.inflate(R.layout.item_mp3_program, null);

        convertView.setTag(mp3Program);

        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        txt.setText(TW2CN.getInstance(context).toLocal(mp3Program.programListItem.name));

        TextView info = (TextView) convertView.findViewById(R.id.info);
        info.setText(TW2CN.getInstance(context).toLocal(mp3Program.programListItem.recDate));

        IconicsButton icon_play_mp3 = (IconicsButton) convertView.findViewById(R.id.icon_play_mp3);
        icon_play_mp3.setVisibility(mp3Program.programListItem.mp3.equals("0") ? View.INVISIBLE : View.VISIBLE);
        IconicsButton icon_play_video = (IconicsButton) convertView.findViewById(R.id.icon_play_video);
        icon_play_video.setVisibility(mp3Program.programListItem.mp4.equals("0") ? View.INVISIBLE : View.VISIBLE);
        icon_play_mp3.setTag(position);
        icon_play_video.setTag(position);
        icon_play_mp3.setOnClickListener(playMp3OnClickListener);
        icon_play_video.setOnClickListener(playMp4OnClickListener);

        return convertView;
    }
}
