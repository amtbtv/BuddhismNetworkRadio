package com.jianchi.fsp.buddhismnetworkradio.xmlbean;

public class ProgramListResult {
    private int result;

    private String name;
    private String subname;

    private int amtbid;
    private int subamtbid;

    private ProgramList list;

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

    public ProgramList getList() {
        return list;
    }

    public void setList(ProgramList list) {
        this.list = list;
    }

    public int getSubamtbid() {
        return subamtbid;
    }

    public void setSubamtbid(int subamtbid) {
        this.subamtbid = subamtbid;
    }

    public int getAmtbid() {
        return amtbid;
    }

    public void setAmtbid(int amtbid) {
        this.amtbid = amtbid;
    }
}
