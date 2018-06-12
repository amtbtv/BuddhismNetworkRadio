package com.jianchi.fsp.buddhismnetworkradio.db;

import com.jianchi.fsp.buddhismnetworkradio.mp3.DownloadTaskInfo;

import java.util.ArrayList;
import java.util.List;

/**
 public String fileName;
 public String state;
 public String checked;
 */

public class DownloadTaskInfoDBManager extends RecDBManager {
    public DownloadTaskInfoDBManager() {
        super();
        recType="download";
    }

    /**
     * add recs
     * @param downloadTaskInfo downloadTaskInfo
     */
    public int add(DownloadTaskInfo downloadTaskInfo) {
        return add(downloadTaskInfoToRec(downloadTaskInfo));
    }


    public void update(DownloadTaskInfo taskInfo){
        updateInfo(downloadTaskInfoToRec(taskInfo));
    }

    private Rec downloadTaskInfoToRec(DownloadTaskInfo downloadTaskInfo){
        Rec rec = new Rec();
        rec._id = downloadTaskInfo.dbRecId;
        rec.type=recType;
        rec.key1=downloadTaskInfo.fileName;
        rec.key2=downloadTaskInfo.state;
        rec.info= downloadTaskInfo.checked;
        return rec;
    }

    public List<DownloadTaskInfo> getAll() {
        RecQueryParam param = new RecQueryParam();
        param.type = recType;
        return queryDownloadTaskInfo(param);
    }

    public List<DownloadTaskInfo> queryDownloadTaskInfo(RecQueryParam param) {
        List<DownloadTaskInfo> downloadTaskInfos = new ArrayList<>();
        param.type = recType;
        param.orderBy = "_id";
        param.sort = "ASC";
        List<Rec> recs = query(param);
        for(Rec rec : recs){
            DownloadTaskInfo d = new DownloadTaskInfo();
            d.dbRecId = rec._id;
            d.fileName = rec.key1;
            d.state = rec.key2;
            d.checked = rec.info;
            downloadTaskInfos.add(d);
        }
        return downloadTaskInfos;
    }

    public DownloadTaskInfo getRecId(int dbRecId) {
        RecQueryParam param = new RecQueryParam();
        param.type = recType;
        param.id = dbRecId;
        List<Rec> recs = query(param);
        if(recs.size()>0){
            Rec rec = recs.get(0);
            DownloadTaskInfo d = new DownloadTaskInfo();
            d.dbRecId = rec._id;
            d.fileName = rec.key1;
            d.state = rec.key2;
            d.checked = rec.info;
            return d;
        } else {
            return null;
        }
    }

    public void del(DownloadTaskInfo d){
        String[] args = {String.valueOf(d.dbRecId)};
        db.delete("rec", "_id=?",args);
    }

    public void clean(){
        String[] args = {String.valueOf(recType)};
        db.delete("rec", "type=?", args);
    }

    public void addDownloadTaskInfos(List<DownloadTaskInfo> downloadTaskInfos) {
        List<Rec> recs = new ArrayList<>();
        for(DownloadTaskInfo d : downloadTaskInfos){
            recs.add(downloadTaskInfoToRec(d));
        }
        add(recs);
    }
}
