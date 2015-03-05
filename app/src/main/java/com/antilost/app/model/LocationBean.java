package com.antilost.app.model;

import java.io.Serializable;

/**
 * 动作对象
 * @author liuyang
 */
public class LocationBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1657248999697611222L;

    /** ID */
    private int mlocationId = 0;

    /** 名字 */
    private String mLocationName = null;

    /** 时间 */
    private String mLocationTime = null;

    /** 经度 */
    private float mlongitude = 0;


    /** 纬度 */
    private float mlatitude = 0;


    public LocationBean() {
    }

    public LocationBean(String mLocationName) {
        this.mLocationName = mLocationName;
    }

    /**
     *  用来查询用的构建函数
     * @param mlocationId
     * @param mLocationName
     * @param mLocationTime
     * @param mlatitude
     */
    public LocationBean( int mlocationId,String mLocationName, String mLocationTime,
                        float mlatitude,float mlongitude) {
        this.mlocationId = mlocationId;
        this.mLocationName = mLocationName;
        this.mLocationTime = mLocationTime;
        this.mlatitude = mlatitude;
        this.mlongitude = mlongitude;
    }
    /**
     * 用来插入一条数据时候用的
     * @param mLocationName
     * @param mLocationTime
     * @param mlatitude
     */
    public LocationBean(String mLocationName, String mLocationTime,
                        float mlatitude,float mlongitude) {
        this.mLocationName = mLocationName;
        this.mLocationTime = mLocationTime;
        this.mlatitude = mlatitude;
        this.mlongitude = mlongitude;
    }

    public int getMlocationId() {
        return mlocationId;
    }

    public void setMlocationId(int mlocationId) {
        this.mlocationId = mlocationId;
    }

    public String getmLocationName() {
        return mLocationName;
    }

    public void setmLocationName(String mLocationName) {
        this.mLocationName = mLocationName;
    }

    public String getmLocationTime() {
        return mLocationTime;
    }

    public void setmLocationTime(String mLocationTime) {
        this.mLocationTime = mLocationTime;
    }

    public float getMlongitude() {
        return mlongitude;
    }

    public void setMlongitude(float mlongitude) {
        this.mlongitude = mlongitude;
    }

    public float getMlatitude() {
        return mlatitude;
    }

    public void setMlatitude(float mlatitude) {
        this.mlatitude = mlatitude;
    }
}
