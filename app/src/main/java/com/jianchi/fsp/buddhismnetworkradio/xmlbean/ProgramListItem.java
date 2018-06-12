package com.jianchi.fsp.buddhismnetworkradio.xmlbean;

public class ProgramListItem {
    //int amtbid, int subamtbid, int lectureid, int volid
    private String lectureno;
    private String lecturename;
    private String lecturedate;
    private String lectureaddr;
    private int lecturevol;
    private int lectureid;

    //获取后设置
    private int amtbid;
    private int subamtbid;

    public String getLectureno() {
        return lectureno;
    }

    public void setLectureno(String lectureno) {
        this.lectureno = lectureno;
    }

    public String getLecturename() {
        return lecturename;
    }

    public void setLecturename(String lecturename) {
        this.lecturename = lecturename;
    }

    public String getLecturedate() {
        return lecturedate;
    }

    public void setLecturedate(String lecturedate) {
        this.lecturedate = lecturedate;
    }

    public int getLecturevol() {
        return lecturevol;
    }

    public void setLecturevol(int lecturevol) {
        this.lecturevol = lecturevol;
    }

    public String getLectureaddr() {
        return lectureaddr;
    }

    public void setLectureaddr(String lectureaddr) {
        this.lectureaddr = lectureaddr;
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
