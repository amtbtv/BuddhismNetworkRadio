package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.activity.ScheduleActivity;
import com.jianchi.fsp.buddhismnetworkradio.model.Live;
import com.jianchi.fsp.buddhismnetworkradio.model.LiveListResult;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;

/**
 * Created by fsp on 16-7-6.
 */
public class TvChannelListAdapter extends BaseAdapter {
    LiveListResult channelList;
    private LayoutInflater mInflater;
    Context context;
    public TvChannelListAdapter(Context context, LiveListResult channelList){
        this.context=context;
        this.channelList=channelList;
        this.mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return channelList.lives.size();
    }

    @Override
    public Object getItem(int position) {
        return channelList.lives.get(position);
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
        Live holder = channelList.lives.get(position);
        //观察convertView随ListView滚动情况
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.item_tv_channel, null);

        convertView.setTag(holder);
        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        txt.setText(TW2CN.getInstance(context).toLocal(holder.name));
        AwesomeTextView bt_showSchedule = (AwesomeTextView) convertView.findViewById(R.id.bt_showSchedule);
        bt_showSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Live holder = channelList.lives.get(position);
                Intent intent = new Intent(context, ScheduleActivity.class);
                intent.putExtra("name", holder.name);
                intent.putExtra("listUrl", holder.listUrl);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

}
