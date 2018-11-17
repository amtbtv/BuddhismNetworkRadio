package com.jianchi.fsp.buddhismnetworkradio.db;

import android.content.Context;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;

import java.util.ArrayList;
import java.util.List;

/**
 public int id;
 public String name;
 public String content;
 public String type;
 public String date;
 */

public class Mp3RecDBManager extends RecDBManager {
    public Mp3RecDBManager(Context context) {
        super(context);
        recType="mp3rec";
    }

    /**
     * add recs
     * @param newsMessage NewsMessage
     */
    public int add(Mp3Program newsMessage) {
        return add(mp3ToRec(newsMessage));
    }


    public void update(Mp3Program mp3Program){
        updateInfo(mp3ToRec(mp3Program));
    }

    private Rec mp3ToRec(Mp3Program mp3Program){
        Rec rec = new Rec();
        rec._id = mp3Program.dbRecId;
        rec.type=recType;
        rec.key1=String.valueOf(mp3Program.programListItem.identifier);
        rec.key2=mp3Program.programListItem.name;
        rec.info= new Gson().toJson(mp3Program);
        return rec;
    }

    public List<Mp3Program> getAllMp3Rec() {
        RecQueryParam param = new RecQueryParam();
        param.type = recType;
        return queryMp3Rec(param);
    }

    public List<Mp3Program> queryMp3Rec(RecQueryParam param) {
        List<Mp3Program> newss = new ArrayList<>();
        param.type = recType;
        param.orderBy = "_id";
        param.sort = "ASC";
        List<Rec> recs = query(param);
        for(Rec rec : recs){
            Mp3Program mp3Program = new Gson().fromJson(rec.info, Mp3Program.class);
            mp3Program.dbRecId = rec._id;
            newss.add(mp3Program);
        }
        return newss;
    }

    public Mp3Program getMp3RecByDbRecId(int dbRecId) {
        RecQueryParam param = new RecQueryParam();
        param.type = recType;
        param.id = dbRecId;
        List<Rec> recs = query(param);
        if(recs.size()>0){
            Rec rec = recs.get(0);
            Mp3Program mp3Program = new Gson().fromJson(rec.info, Mp3Program.class);
            mp3Program.dbRecId = rec._id;
            return mp3Program;
        } else {
            return null;
        }
    }

    public void delMp3(Mp3Program mp3){
        String[] args = {String.valueOf(mp3.dbRecId)};
        db.delete("rec", "_id=?",args);
    }

    public void addMp3s(List<Mp3Program> addMpsPrograms) {
        List<Rec> recs = new ArrayList<>();
        for(Mp3Program mp3 : addMpsPrograms){
            recs.add(mp3ToRec(mp3));
        }
        add(recs);
    }
}
