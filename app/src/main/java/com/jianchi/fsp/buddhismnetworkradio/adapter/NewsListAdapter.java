package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;

import java.util.List;

/**
 * Created by fsp on 16-7-6.
 */
public class NewsListAdapter extends BaseAdapter {
    List<String> news;
    private LayoutInflater mInflater;
    Context context;
    public NewsListAdapter(Context context, List<String> news){
        this.context=context;
        this.news=news;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return news.size();
    }

    @Override
    public Object getItem(int position) {
        return news.get(position);
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
        String holder = news.get(position);
        //观察convertView随ListView滚动情况
        if(convertView==null)
            convertView = mInflater.inflate(R.layout.item_news, null);

        convertView.setTag(holder);

        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        txt.setText(TW2CN.getInstance(context).toLocal(holder));

        return convertView;
    }
}
