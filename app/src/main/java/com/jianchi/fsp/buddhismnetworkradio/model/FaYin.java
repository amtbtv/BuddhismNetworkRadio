package com.jianchi.fsp.buddhismnetworkradio.model;

public class FaYin {
    public class Rendered {
        public String rendered;
    }
    public int id;
    public String date;
    public Rendered title;
    public Rendered content;
    public Rendered excerpt;
    public String link;

    //由 https://www.amtb.tw/blog/wp-json/wp/v2/media/ + featured_media 获取数据
    //在其中找到 "guid":{"rendered":"https:\/\/new.amtb.tw\/blog\/wp-content\/uploads\/2020\/12\/\u91d1\u525b\u7d93\u7684\u667a\u8a00\u6167\u8a9e-\u6de8\u7a7a\u8001\u548c\u5c1a\u958b\u793a.jpg"}
    //上面就是图片链接
    public int featured_media;

    //在这里可以找到图片链接  <meta property=\"og:image\" content=\"https://www.amtb.tw/blog/wp-content/uploads/2020/12/金剛經的智言慧語-淨空老和尚開示.jpg\" />
    //public String yoast_head;

}
