package com.jianchi.fsp.buddhismnetworkradio.model;

import com.jianchi.fsp.buddhismnetworkradio.adapter.NewsListAdapter;

import java.util.ArrayList;

public class NewsPager {
    public int pager = 0; //
    public ArrayList<News> newsList = new ArrayList<>();
    public boolean hasNext = true;
    public boolean isLoading = false;
    public NewsCategorie nc;
    public NewsListAdapter adapter;

    public NewsPager(NewsCategorie nc) {
        this.nc = nc;
    }
}
