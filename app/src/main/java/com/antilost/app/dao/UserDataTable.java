package com.antilost.app.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.antilost.app.model.LocationBean;
import com.antilost.app.model.UserdataBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 动作
 * @author liuyang
 */
public class UserDataTable {
    public static final String TAG = "userDateTable";
    /** 表名 */
    public static final String GEN_TABLE_NAME = "userDate";
    /** 设备id */
    public static final String GEN_USERDATA_ID = "userDate_id";
    /** 设备名字 */
    public static final String GEN_USERDATA_NAME = "userDate_name";
    /** 图片封面是否内置 */
    public static final String GEN_USERDATA_IMAGE = "userDate_imge";
    /** 设备封面 */
    public static final String GEN_USERDATA_ALARMTIME = "userDate_alartime";
    /** 设备状态 */
    public static final String GEN_USERDATA_NICKNAME = "userDate_nickname";

    public static final String GEN_USERDATA_BIRTHDAY = "userDate_birthday";

    public static final String GEN_USERDATA_BLOODTYPE = "userDate_bloodtype";

    public static final String GEN_USERDATA_HOBBY = "userDate_hobby";

    public static final String GEN_USERDATA_SIGNATURE = "userDate_signature";

    public static final String GEN_USERDATA_HOMEPAGE = "userDate_homepage";

    /** 建表SQL语句 */
    public static String GEN_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + GEN_TABLE_NAME
            + "(" + GEN_USERDATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + GEN_USERDATA_NAME + " TEXT,"
            + GEN_USERDATA_IMAGE + " TEXT,"
            + GEN_USERDATA_ALARMTIME + " TEXT,"
            + GEN_USERDATA_NICKNAME + " TEXT,"
            + GEN_USERDATA_BIRTHDAY + " TEXT,"
            + GEN_USERDATA_BLOODTYPE + " TEXT,"
            + GEN_USERDATA_HOBBY + " TEXT,"
            + GEN_USERDATA_SIGNATURE + " TEXT,"
            + GEN_USERDATA_HOMEPAGE + " TEXT"
            + ")";
    /** 删除表 */
    public static String GEN_DROP_TABLE = "DROP TABLE IF EXISTS " + UserDataTable.GEN_TABLE_NAME;
    private static UserDataTable mInstance = null;

    private UserDataTable(){

    }

    public static final UserDataTable getInstance(){
        if (null == mInstance){
            mInstance = new UserDataTable();
        }
        return mInstance;
    }
    public long insert(SQLiteDatabase db, UserdataBean arg0)
            throws RuntimeException {
        Log.d(TAG, " UserDataTable  insert IN");
        String table = GEN_TABLE_NAME;
        ContentValues cv = new ContentValues();
        cv.put(GEN_USERDATA_NAME, arg0.getMuserdataName());
        cv.put(GEN_USERDATA_IMAGE,arg0.getMimage());
        cv.put(GEN_USERDATA_ALARMTIME, arg0.getMalarmtime());
        cv.put(GEN_USERDATA_NICKNAME, arg0.getMnickname());
        cv.put(GEN_USERDATA_BIRTHDAY, arg0.getMbirthday());
        cv.put(GEN_USERDATA_BLOODTYPE, arg0.getMbloodType());
        cv.put(GEN_USERDATA_HOBBY, arg0.getmHobby());
        cv.put(GEN_USERDATA_SIGNATURE, arg0.getmSignature());
        cv.put(GEN_USERDATA_HOMEPAGE, arg0.getmHomePage());

        return db.insert(table, null, cv);
    }

    public long update(SQLiteDatabase db, UserdataBean arg0)
            throws RuntimeException {
        String table = GEN_TABLE_NAME;
        String whereClause = GEN_USERDATA_ID + "=?";
        String[] whereArgs = {Integer.toString(arg0.getMuserdataId())};
        ContentValues cv = new ContentValues();
        cv.put(GEN_USERDATA_NAME, arg0.getMuserdataName());
        cv.put(GEN_USERDATA_IMAGE,arg0.getMimage());
        cv.put(GEN_USERDATA_ALARMTIME, arg0.getMalarmtime());
        cv.put(GEN_USERDATA_NICKNAME, arg0.getMnickname());
        cv.put(GEN_USERDATA_BIRTHDAY, arg0.getMbirthday());
        cv.put(GEN_USERDATA_BLOODTYPE, arg0.getMbloodType());
        cv.put(GEN_USERDATA_HOBBY, arg0.getmHobby());
        cv.put(GEN_USERDATA_SIGNATURE, arg0.getmSignature());
        cv.put(GEN_USERDATA_HOMEPAGE, arg0.getmHomePage());
        return db.update(table, cv, whereClause, whereArgs);
    }


    public int countRecord(SQLiteDatabase db) {
        String table = GEN_TABLE_NAME;
        String[] columns = {"count(*)"};
        Cursor c = db.query(table, columns, null, null, null, null, null);
        if (c != null && c.moveToFirst()){
            return c.getInt(0);
        }
        return 0;
    }

    public boolean isEmpty(SQLiteDatabase db) {
        String table = GEN_TABLE_NAME;
        Cursor c = db.query(table, null, null, null, null, null, null);
        if (c != null){
            return false;
        }else{
            return true;
        }

    }
    public UserdataBean query(SQLiteDatabase db) {
        String table = GEN_TABLE_NAME;
        Cursor c = db.query(table, null, null, null, null, null, null);
        UserdataBean userDataBean = null;
        if (c != null && c.moveToFirst()){
            int userDataId;
             String muserdataName,  mimage,  mnickname,  malarmtime,  mbirthday,  mbloodType,  mHobby,  mSignature ,  mHomePage;
            userDataId = c.getInt(c.getColumnIndexOrThrow(GEN_USERDATA_ID));
            muserdataName = c.getString(c.getColumnIndexOrThrow(GEN_USERDATA_NAME));
            mimage = c.getString(c.getColumnIndexOrThrow(GEN_USERDATA_IMAGE));
            mnickname = c.getString(c.getColumnIndexOrThrow(GEN_USERDATA_NICKNAME));
            malarmtime = c.getString(c.getColumnIndexOrThrow(GEN_USERDATA_ALARMTIME));
            mbirthday = c.getString(c.getColumnIndexOrThrow(GEN_USERDATA_BIRTHDAY));
            mbloodType = c.getString(c.getColumnIndexOrThrow(GEN_USERDATA_BLOODTYPE));
            mHobby = c.getString(c.getColumnIndexOrThrow(GEN_USERDATA_HOBBY));
            mSignature = c.getString(c.getColumnIndexOrThrow(GEN_USERDATA_SIGNATURE));
            mHomePage = c.getString(c.getColumnIndexOrThrow(GEN_USERDATA_HOMEPAGE));
            userDataBean = new UserdataBean(userDataId, muserdataName, mimage, mnickname, malarmtime,mbirthday,mbloodType,mHobby,mSignature,mHomePage);
        }
        c.close();
        return userDataBean;
    }

}
