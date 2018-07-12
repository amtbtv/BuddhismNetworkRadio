package com.jianchi.fsp.buddhismnetworkradio.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/1/23.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rec.db";
    private static final int DATABASE_VERSION = 3;
    private static DBHelper helper;
    //private Context context;

    private DBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //this.context = context;
    }

    public static void init(Context context){
        helper = new DBHelper(context);
    }

    public static DBHelper getHelper(){ return helper;}

    /*
    数据库第一次被创建时onCreate会被调用
    用这一个数据库保存所有数据
    */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS [rec]" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, type VARCHAR, key1 VARCHAR, key2 VARCHAR, info TEXT, updateTime INT64)");
    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("CREATE TABLE IF NOT EXISTS [rec]" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, type VARCHAR, key1 VARCHAR, key2 VARCHAR, info TEXT, updateTime INT64)");
        db.execSQL("DELETE FROM [rec];");
    }
}
