package com.jianchi.fsp.buddhismnetworkradio.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/1/23.
 */

public class RecDBManager {
    public SQLiteDatabase db;
    public String recType = "rec";

    public RecDBManager(Context context) {
        DBHelper helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    public void close(){
        db.close();
        db = null;
    }

    public long count()
    {
        // 调用查找书库代码并返回数据源
        Cursor cursor = db.rawQuery("select count(*) from rec where type='"+recType+"'",null);
        //游标移到第一条记录准备获取数据
        cursor.moveToFirst();
        // 获取数据中的LONG类型数据
        return cursor.getLong(0);
    }

    /**
     * add recs
     * @param rec Rec
     */
    public int add(Rec rec) {
        int _id= -1;

        db.beginTransaction();	//开始事务
        try {
            db.execSQL("INSERT INTO rec VALUES(null, ?, ?, ?, ?, ?)", new Object[]{rec.type, rec.key1, rec.key2, rec.info, rec.updateTime});
            Cursor cursor = db.rawQuery("select last_insert_rowid() from rec",null);
            if(cursor.moveToFirst())
                _id = cursor.getInt(0);
            db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
            db.endTransaction();	//结束事务
        }
        return _id;
    }

    public void delOverMax(int maxRec){
        long c = count();
        if(c>maxRec) {
            RecQueryParam param = new RecQueryParam();
            param.skip=maxRec;
            param.limit=1;
            param.type=recType;
            List<Rec> recs = query(param);
            if(recs.size()>0) {
                db.execSQL("delete from rec where type='"+recType+"' and _id<"+String.valueOf(recs.get(0)._id));
            }
        }
    }

    /**
     * add recList
     * @param recList List<Rec>
     */
    public void add(List<Rec> recList) {
        db.beginTransaction();	//开始事务
        try {
            for(Rec rec: recList){
                db.execSQL("INSERT INTO rec VALUES(null, ?, ?, ?, ?, ?)", new Object[]{rec.type, rec.key1, rec.key2, rec.info, rec.updateTime});
            }
            db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
            db.endTransaction();	//结束事务
        }
    }

    /**
     * update rec's age
     * @param rec Rec
     */
    public void updateInfo(Rec rec) {
        ContentValues cv = new ContentValues();
        cv.put("info", rec.info);
        cv.put("type", rec.type);
        cv.put("key1", rec.key1);
        cv.put("key2", rec.key2);
        cv.put("updateTime", rec.updateTime);
        db.update("rec", cv, "_id = ?", new String[]{String.valueOf(rec._id)});
    }

    /**
     * delete old rec
     * @param _id id
     */
    public void deleteRec(int _id) {
        db.delete("rec", "_id = ?", new String[]{String.valueOf(_id)});
    }

    /**
     * delete old rec
     */
    public void cleanRec() {
        db.delete("rec", "type=?", new String[]{recType});
    }

    /**
     * query all recs, return list
     * @return List<rec>
     */
    public List<Rec> query(RecQueryParam param) {
        ArrayList<Rec> recs = new ArrayList<>();
        if(param==null)
            param=new RecQueryParam();
        Cursor c = queryTheCursor(param);
        while (c.moveToNext()) {
            Rec rec = new Rec();
            rec._id = c.getInt(c.getColumnIndex("_id"));
            rec.type = c.getString(c.getColumnIndex("type"));
            rec.key1 = c.getString(c.getColumnIndex("key1"));
            rec.key2 = c.getString(c.getColumnIndex("key2"));
            rec.info = c.getString(c.getColumnIndex("info"));
            recs.add(rec);
        }
        c.close();
        return recs;
    }


    /**
     * query all recs, return cursor
     * @return	Cursor
     */
    public Cursor queryTheCursor(RecQueryParam param) {
        String selectionStr = "";
        String orderBy = "updateTime desc";
        List<String> selectionParam = new ArrayList<>();

        if(param.id>0){
            selectionStr = "_id=?";
            selectionParam.add(String.valueOf(param.id));
        } else {
            if(!param.type.isEmpty()){
                selectionStr+="type=?";
                selectionParam.add(param.type);
            }

            if(!param.key1.isEmpty()){
                selectionParam.add(param.key1);
                if(!selectionStr.isEmpty())
                    selectionStr+=" and key1=?";
                else
                    selectionStr+="key1=?";
            }

            if(!param.key2.isEmpty()){
                selectionParam.add(param.key2);
                if(!selectionStr.isEmpty())
                    selectionStr+=" and key2=?";
                else
                    selectionStr+="key2=?";
            }

            if(!param.orderBy.isEmpty()){
                orderBy = param.orderBy+" "+param.sort;
            }
        }

        String limitStr="";
        if(param.limit!=-1)
            limitStr = String.valueOf(param.skip) + ","+String.valueOf(param.limit);

        String[] paramValues = selectionParam.toArray(new String[0]);
        Cursor cursor = db.query(
                "rec",
                null,
                selectionStr,
                paramValues,
                null,
                null,
                orderBy,
                limitStr
        );

        return cursor;
    }
}
