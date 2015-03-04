package com.antilost.app.model;

import java.io.Serializable;

/**
 * 动作对象
 * @author liuyang
 */
public class UserdataBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1657248999697611222L;

    /** ID */
    private int muserdataId = 0;

    /** 名字 */
    private String muserdataName = null;

    /** 时间 */
    private String mimage = null;

    /** 经度 */
    private String malarmtime =null;


    /** 纬度 */
    private String mnickname =null;
    //
    private String mbirthday =null;

    private String mbloodType=null;

    private String mHobby =null;

    private String mSignature =null;

    private String mHomePage =null;


    public UserdataBean() {
    }

    public UserdataBean(String muserdataName) {
        this.muserdataName = muserdataName;
    }

    /**
     *  用来查询用的构建函数
     * @param muserdataId
     * @param muserdataName
     * @param mimage
     * @param mnickname
     */
    public UserdataBean(int muserdataId, String muserdataName, String mimage,
                        String mnickname, String malarmtime, String mbirthday, String mbloodType, String mHobby, String mSignature , String mHomePage ) {
        this.muserdataId = muserdataId;
        this.muserdataName = muserdataName;
        this.mimage = mimage;
        this.mnickname = mnickname;
        this.malarmtime = malarmtime;
        this.mbirthday =mbirthday;
        this.mbloodType =mbloodType;
        this.mHobby =mHobby;
        this.mSignature =mSignature;
        this.mHomePage =mHomePage;

    }
    /**
     * 用来插入一条数据时候用的
     * @param muserdataName
     * @param mimage
     * @param mnickname
     */
    public UserdataBean(String muserdataName, String mimage,
                        String mnickname, String malarmtime, String mbirthday, String mbloodType, String mHobby, String mSignature , String mHomePage ) {
        this.muserdataName = muserdataName;
        this.mimage = mimage;
        this.mnickname = mnickname;
        this.malarmtime = malarmtime;
        this.mbirthday =mbirthday;
        this.mbloodType =mbloodType;
        this.mHobby =mHobby;
        this.mSignature =mSignature;
        this.mHomePage =mHomePage;

    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getMuserdataId() {
        return muserdataId;
    }

    public void setMuserdataId(int muserdataId) {
        this.muserdataId = muserdataId;
    }

    public String getMuserdataName() {
        return muserdataName;
    }

    public void setMuserdataName(String muserdataName) {
        this.muserdataName = muserdataName;
    }

    public String getMimage() {
        return mimage;
    }

    public void setMimage(String mimage) {
        this.mimage = mimage;
    }

    public String getMalarmtime() {
        return malarmtime;
    }

    public void setMalarmtime(String malarmtime) {
        this.malarmtime = malarmtime;
    }

    public String getMnickname() {
        return mnickname;
    }

    public void setMnickname(String mnickname) {
        this.mnickname = mnickname;
    }

    public String getMbirthday() {
        return mbirthday;
    }

    public void setMbirthday(String mbirthday) {
        this.mbirthday = mbirthday;
    }

    public String getMbloodType() {
        return mbloodType;
    }

    public void setMbloodType(String mbloodType) {
        this.mbloodType = mbloodType;
    }

    public String getmHobby() {
        return mHobby;
    }

    public void setmHobby(String mHobby) {
        this.mHobby = mHobby;
    }

    public String getmSignature() {
        return mSignature;
    }

    public void setmSignature(String mSignature) {
        this.mSignature = mSignature;
    }

    public String getmHomePage() {
        return mHomePage;
    }

    public void setmHomePage(String mHomePage) {
        this.mHomePage = mHomePage;
    }
}
