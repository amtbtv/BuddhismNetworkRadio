package com.jianchi.fsp.buddhismnetworkradio.db;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/23.
 */

public class Rec {
    public int _id;
    public String type = "rec";
    public String key1 = "";
    public String key2 = "";
    public String info = "";
    public Long updateTime = new Date().getTime();
}
