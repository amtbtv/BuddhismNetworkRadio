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

import java.util.List;

/**
 * Created by fsp on 16-7-6.
 */
public class Mp3ChannelListAdapter extends BaseAdapter {
    List<Mp3Program> mp3Programs;
    private LayoutInflater mInflater;
    Context context;
    public Mp3ChannelListAdapter(Context context, List<Mp3Program> mp3Programs){
        this.context=context;
        this.mp3Programs=mp3Programs;
        this.mInflater = LayoutInflater.from(context);
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Mp3Program holder = mp3Programs.get(position);
        //观察convertView随ListView滚动情况
        if(convertView==null)
            convertView = mInflater.inflate(R.layout.item_mp3_program, null);

        convertView.setTag(holder);

        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        txt.setText(TW2CN.getInstance(context).toLocal(holder.programListItem.name));

        TextView info = (TextView) convertView.findViewById(R.id.info);
        info.setText(TW2CN.getInstance(context).toLocal(holder.programListItem.recDate));

        return convertView;
    }
}
