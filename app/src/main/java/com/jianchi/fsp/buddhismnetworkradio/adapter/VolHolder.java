package com.jianchi.fsp.buddhismnetworkradio.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jianchi.fsp.buddhismnetworkradio.R;

public class VolHolder extends RecyclerView.ViewHolder  {
    TextView textView;
    public VolHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.txt);
    }
}