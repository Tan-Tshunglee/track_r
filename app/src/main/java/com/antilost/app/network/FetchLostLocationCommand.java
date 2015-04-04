package com.antilost.app.network;

import android.text.TextUtils;

import com.antilost.app.util.Utils;

import org.w3c.dom.Text;

/**
 * Created by Tan on 2015/3/29.
 */
public class FetchLostLocationCommand extends Command {


    private final int mUid;
    private final String mAddress;

    public FetchLostLocationCommand(String address, int uid) {
        mAddress = address;
        mUid = uid;
    }

    @Override
    protected String makeRequestString() {

        setCommand("getgps");
        addLine("uid:" + mUid);
        addLine("losserid:" + mAddress);
        return mRequestBuffer.toString();
    }

    public double getLongitude() {
        return Float.valueOf(mResultMap.get("log"));
    }

    public double getLatitude() {
        return Float.valueOf(mResultMap.get("lat"));
    }

    public long getLostTime() {
        String timeStr = mResultMap.get(TIME);
        if(TextUtils.isEmpty(timeStr)) {
            return -1;
        }
        return Utils.convertTimeStrToLongTime(timeStr);
    }
}
