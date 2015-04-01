package com.antilost.app.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.antilost.app.model.LocationBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 动作
 * @author liuyang
 */
public class LocationTable {
    public static final String TAG = "LocationTable";
    /** 表名 */
    public static final String GEN_TABLE_NAME = "location";
    /** 设备id */
    public static final String GEN_LOCATION_ID = "location_id";
    /** 设备名字 */
    public static final String GEN_LOCATION_NAME = "location_name";
    /** 图片封面是否内置 */
    public static final String GEN_LOCATION_TIME = "location_time";
    /** 设备封面 */
    public static final String GEN_LOCATION_LONGITUDE = "location_longitude";
    /** 设备状态 */
    public static final String GEN_LOCATION_LATITUDE = "location_latitude";

    /** 建表SQL语句 */
    public static String GEN_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + GEN_TABLE_NAME
            + "(" + GEN_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + GEN_LOCATION_NAME + " TEXT,"
            + GEN_LOCATION_TIME + " TEXT,"
            + GEN_LOCATION_LONGITUDE + " FLOAT,"
            + GEN_LOCATION_LATITUDE + " FLOAT"
            + ")";
    /** 删除表 */
    public static String GEN_DROP_TABLE = "DROP TABLE IF EXISTS " + LocationTable.GEN_TABLE_NAME;
    private static LocationTable mInstance = null;

    private LocationTable(){

    }

    public static final LocationTable getInstance(){
        if (null == mInstance){
            mInstance = new LocationTable();
        }
        return mInstance;
    }

    public long insert(SQLiteDatabase db, LocationBean arg0)
            throws RuntimeException {
        System.out.println(" LocationTable Action insert IN");
        String table = GEN_TABLE_NAME;
        ContentValues cv = new ContentValues();
        cv.put(GEN_LOCATION_NAME, arg0.getmLocationName());
        cv.put(GEN_LOCATION_TIME,arg0.getmLocationTime());
        cv.put(GEN_LOCATION_LONGITUDE, arg0.getMlongitude());
        cv.put(GEN_LOCATION_LATITUDE, arg0.getMlatitude());
        return db.insert(table, null, cv);
    }

    public long update(SQLiteDatabase db, LocationBean arg0)
            throws RuntimeException {
        String table = GEN_TABLE_NAME;
        String whereClause = GEN_LOCATION_ID + "=?";
        String[] whereArgs = {Integer.toString(arg0.getMlocationId())};
        ContentValues cv = new ContentValues();
        cv.put(GEN_LOCATION_NAME, arg0.getmLocationName());
        cv.put(GEN_LOCATION_TIME, arg0.getmLocationTime());
        cv.put(GEN_LOCATION_LONGITUDE, arg0.getMlongitude());
        cv.put(GEN_LOCATION_LATITUDE, arg0.getMlatitude());
        return db.update(table, cv, whereClause, whereArgs);
    }
    public List<LocationBean> query(SQLiteDatabase db) {
        String table = GEN_TABLE_NAME;
        Cursor c = db.query(table, null, null, null, null, null, null);
        List<LocationBean> list = null;
        if (c != null && c.moveToFirst()){
            list = new ArrayList<LocationBean>();
            int locationId;
            String locationName;
            String locationTime;
            float locationlongitude,locationlatitude;
            do{
                locationId = c.getInt(c.getColumnIndexOrThrow(GEN_LOCATION_ID));
                locationName = c.getString(c.getColumnIndexOrThrow(GEN_LOCATION_NAME));
                locationTime = c.getString(c.getColumnIndexOrThrow(GEN_LOCATION_TIME));
                locationlongitude = c.getFloat(c.getColumnIndexOrThrow(GEN_LOCATION_LONGITUDE));
                locationlatitude = c.getFloat(c.getColumnIndexOrThrow(GEN_LOCATION_LATITUDE));

                list.add(new LocationBean(locationId, locationName, locationTime, locationlongitude, locationlatitude));
            }while(c.moveToNext());
        }
        c.close();
        return list;
    }



}
