package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.News;
import com.jianchi.fsp.buddhismnetworkradio.model.NewsPager;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;

import java.util.List;

/**
 * Created by fsp on 16-7-6.
 */
public class NewsListAdapter extends BaseAdapter {
    NewsPager newsPager;
    private LayoutInflater mInflater;
    Context context;
    public NewsListAdapter(Context context, NewsPager newsPager){
        this.context=context;
        this.newsPager=newsPager;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return newsPager.newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsPager.newsList.get(position);
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
        News holder = newsPager.newsList.get(position);
        //观察convertView随ListView滚动情况
        if(convertView==null)
            convertView = mInflater.inflate(R.layout.item_news, null);

        convertView.setTag(holder);

        TextView txt = (TextView) convertView.findViewById(R.id.txt);
        String localTitle = TW2CN.getInstance(context).toLocal(holder.title.rendered);

        //因为当为单行文本时，上标显示不全，所以加上 smalll 标签，这样就可以显示全了
        localTitle = localTitle.replace("<sup>上</sup>", "<sup><small>上</small></sup>");
        localTitle = localTitle.replace("<sup>下</sup>", "<sup><small>下</small></sup>");
        Spanned spanned = Html.fromHtml(localTitle);
        txt.setText(spanned);

        TextView time = (TextView) convertView.findViewById(R.id.time);
        time.setText(holder.date);

        return convertView;
    }
}
