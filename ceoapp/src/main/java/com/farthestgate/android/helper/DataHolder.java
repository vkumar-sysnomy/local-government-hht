package com.farthestgate.android.helper;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 3/28/2018.
 */

public class DataHolder {

    private static DataHolder instance;

    private int sync = 0;

    private List<?> listData;
    private JSONObject jsonObject;

    private DataHolder(){}

    public synchronized static DataHolder get() {
        if (instance == null) {
            instance = new DataHolder ();
        }
        return instance;
    }

    public int setListData(List<?> listData) {
        this.listData = listData;
        return ++sync;
    }

    public List<?> getListData(int request) {
        return (request == sync) ? listData : null;
    }

    public int setJSONObject(JSONObject jsonObject){
        this.jsonObject = jsonObject;
        return ++sync;
    }

    public JSONObject getJsonObject(int request){
        return (request == sync) ? jsonObject : new JSONObject();
    }

}
