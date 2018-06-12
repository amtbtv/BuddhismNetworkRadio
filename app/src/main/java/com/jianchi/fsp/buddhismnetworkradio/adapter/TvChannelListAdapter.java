package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.activity.ScheduleActivity;
import com.jianchi.fsp.buddhismnetworkradio.api.Channel;
import com.jianchi.fsp.buddhismnetworkradio.api.ChannelList;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;

/**
 * Created by fsp on 16-7-6.
 */
public class TvChannelListAdapter extends BaseAdapter {
    ChannelList channelList;
    private LayoutInflater mInflater;
    Context context;
    BApplication app;
    public TvChannelListAdapter(Context context, ChannelList channelList, BApplication app){
        this.context=context;
        this.channelList=channelList;
        this.mInflater = LayoutInflater.from(context);
        this.app = app;
    }
    @Override
    public int getCount() {
        return channelList.channels.size();
    }

    @Override
    public Object getItem(int position) {
        return channelList.channels.get(position);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        Channel holder = channelList.channels.get(position);
        //观察convertView随ListView滚动情况
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.item_tv_channel, null);

        convertView.setTag(holder);
        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        txt.setText(TW2CN.getInstance(context).toLocal(holder.title));
        AwesomeTextView bt_showSchedule = (AwesomeTextView) convertView.findViewById(R.id.bt_showSchedule);
        bt_showSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Channel holder = channelList.channels.get(position);
                Intent intent = new Intent(context, ScheduleActivity.class);
                intent.putExtra("title", holder.title);
                intent.putExtra("listUrl", holder.listUrl);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

}
