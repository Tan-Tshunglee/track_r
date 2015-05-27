package com.antilost.app.network;

import android.text.TextUtils;

import com.antilost.app.util.Utils;

import org.w3c.dom.Text;

import java.util.HashMap;

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
        appendEncodePassword(mRequestBuffer);
        return mRequestBuffer.toString();
    }

    public boolean parseResponse(String entity) {
        try {
            String[] lines = entity.split("\r\n");
            mResultMap = new HashMap<String, String>();
            for(String line: lines) {
                String[] keyValues = line.split(":");
                if(keyValues.length == 2) {
                    mResultMap.put(keyValues[0], keyValues[1]);
                } else if(line.startsWith("time:")) {
                    String timeStr = line.substring("time:".length());
                    mResultMap.put("time", timeStr);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            mStatusBad = true;
        }
        return false;
    }

    public double getLongitude() throws NumberFormatException {
        return Float.valueOf(mResultMap.get("log"));
    }

    public double getLatitude() throws NumberFormatException {
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
