package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.VolListItem;

import java.util.List;

public class VolRecyclerViewAdapter extends RecyclerView.Adapter<VolHolder>  {
    private Context context;
    private List<VolListItem> volListItemList;
    private VolListItem curVolListItem = null;
    private VolRecyclerItemClickerListener volRecyclerItemClickerListener;
    public VolRecyclerViewAdapter(Context context, List<VolListItem> volListItemList, VolRecyclerItemClickerListener volRecyclerItemClickerListener){
        this.context = context;
        this.volListItemList = volListItemList;
        this.volRecyclerItemClickerListener = volRecyclerItemClickerListener;
    }

    @Override
    public VolHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vol, parent, false);
        view.setOnClickListener(volOnClickListener);
        return new VolHolder(view);
    }

    private View.OnClickListener volOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != volRecyclerItemClickerListener && null != v) {
                TextView txt = (TextView) v.findViewById(R.id.txt);
                curVolListItem = (VolListItem) txt.getTag();
                volRecyclerItemClickerListener.onVolRecyclerItemClick(curVolListItem);
            }
        }
    };

    @Override
    public void onBindViewHolder(VolHolder holder, int position) {
        VolListItem item = volListItemList.get(position);
        holder.textView.setText(item.getItem().getVolno());
        holder.textView.setTag(item);
        if(curVolListItem == item){
            holder.textView.setTextColor(context.getResources().getColor(R.color.bootstrap_brand_primary));
        } else {
            holder.textView.setTextColor(context.getResources().getColor(R.color.bootstrap_gray_dark));
        }
    }

    @Override
    public int getItemCount() {
        return volListItemList.size();
    }
}
