package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class SharedPreferencesHelper {
    private SharedPreferences sharedPreferences;
    /*
     * 保存手机里面的名字
     */private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context, String FILE_NAME) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * 存储
     */
    public void put(String key, Object object) {
        editor.putString(key, new Gson().toJson(object));
        editor.commit();
    }

    public void putString(String key, String msg) {
        editor.putString(key, msg);
        editor.commit();
    }

    public String getString(String key){
        return sharedPreferences.getString(key, "");
    }

    /**
     * 获取保存的数据
     */
    public <T> T getSharedPreference(String key, Class<T> _class) {
        String json = sharedPreferences.getString(key, null);
        if(json==null)
            return null;
        else
            return new Gson().fromJson(json, _class);
    }

    /**
     * 移除某个key值已经对应的值
     */
    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        editor.clear();
        editor.commit();
    }

    /**
     * 查询某个key是否存在
     */
    public Boolean contain(String key) {
        return sharedPreferences.contains(key);
    }


    /**
     * 返回所有的键值对
     */
    public <T> Map<String, T> getAll(Class<T> _class) {
        Map<String, T> map = new HashMap<>();
        Map<String, ?> getMap = sharedPreferences.getAll();
        Gson gson = new Gson();
        for(String k : getMap.keySet()){
            String json = (String) getMap.get(k);
            map.put(k, gson.fromJson(json, _class));
        }
        return map;
    }
}
