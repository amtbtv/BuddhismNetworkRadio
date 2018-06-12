package com.jianchi.fsp.buddhismnetworkradio.xmlbean;
import java.util.List;

public class MediaListResult {
    private int result;
    private String name;
    private String subname;
    private String lecturename;
    private String lectureno;

    private List<VolListItem> vollist;

    private MediaList list;

    private int voltotal;
    private String thisvolno;

    private int lectureid;
    //获取后设置
    private int amtbid;
    private int subamtbid;

    public List<VolListItem> getVollist() {
        return vollist;
    }

    public void setVollist(List<VolListItem> vollist) {
        this.vollist = vollist;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubname() {
        return subname;
    }

    public void setSubname(String subname) {
        this.subname = subname;
    }

    public String getLecturename() {
        return lecturename;
    }

    public void setLecturename(String lecturename) {
        this.lecturename = lecturename;
    }

    public String getLectureno() {
        return lectureno;
    }

    public void setLectureno(String lectureno) {
        this.lectureno = lectureno;
    }

    public MediaList getList() {
        return list;
    }

    public void setList(MediaList list) {
        this.list = list;
    }

    public int getVoltotal() {
        return voltotal;
    }

    public void setVoltotal(int voltotal) {
        this.voltotal = voltotal;
    }

    public String getThisvolno() {
        return thisvolno;
    }

    public void setThisvolno(String thisvolno) {
        this.thisvolno = thisvolno;
    }

    public int getLectureid() {
        return lectureid;
    }

    public void setLectureid(int lectureid) {
        this.lectureid = lectureid;
    }

    public int getAmtbid() {
        return amtbid;
    }

    public void setAmtbid(int amtbid) {
        this.amtbid = amtbid;
    }

    public int getSubamtbid() {
        return subamtbid;
    }

    public void setSubamtbid(int subamtbid) {
        this.subamtbid = subamtbid;
    }
}
