package com.antilost.app.network;

import android.text.TextUtils;
import android.util.Log;

import com.antilost.app.model.TrackR;
import com.antilost.app.util.Utils;

import java.util.HashMap;

/**
 * Created by Tan on 2015/3/6.
 */
public class FetchAllTrackRCommand extends Command {

    private final int mUid;


    private final HashMap<String, TrackR> mAddressesToTrackRMap = new HashMap<String, TrackR>();
    public FetchAllTrackRCommand(int uid) {
        mUid = uid;
    }
    @Override
    protected String makeRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd:getlosserid").append(LINE_SPLITTER);
        sb.append("uid:").append(mUid).append(LINE_SPLITTER);
        return sb.toString();
    }

    public HashMap<String, TrackR> getBoundTrackRs() {
        if(success()) {
            String addressesStr = mResultMap.get("losserid");
            if(!TextUtils.isEmpty(addressesStr)) {
                String[] trackrs = addressesStr.split(",");

                for(String t: trackrs) {
                    Log.i(LOG_TAG, "t is " + t);
                    //format address|name|type
                    String[] trackInfo = t.split("\\|");
                    String address = trackInfo[1];

                    if(!Utils.isValidMacAddress(address)) {
                        Log.e(LOG_TAG, "get one invalid mac address " + address);
                        continue;
                    }
                    String name = trackInfo[0];
                    String typeStr = trackInfo[2];
                    int type = 0;

                    try {
                        type = Integer.parseInt(typeStr);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    TrackR track = new TrackR();
                    track.type = type;
                    track.address = address;
                    track.name = name;

                    Log.d(LOG_TAG, "Fetch one mac address is " + address);
                    mAddressesToTrackRMap.put(address, track);
                }
                return mAddressesToTrackRMap;
            } else {
                Log.e(LOG_TAG, "losserid data payload is empty.");
            }
            return null;
        } else {
            dumpDebugInfo();
        }
        return null;
    }

    public boolean parseResponse(String entity) {
        try {
            String[] lines = entity.split("\r\n");
            mResultMap = new HashMap<String, String>();
            for(String line: lines) {
                String[] keyValues = line.split(":");
                if(keyValues.length == 2) {
                    mResultMap.put(keyValues[0], keyValues[1]);
                } else if(line.startsWith("losserid:")) {
                    String data = line.substring("losserid:".length());
                    mResultMap.put("losserid", data);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            mStatusBad = true;
        }
        return false;
    }
}
