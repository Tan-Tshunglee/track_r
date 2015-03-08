package com.antilost.app.network;

import android.text.TextUtils;
import android.util.Log;

import com.antilost.app.model.TrackR;
import com.antilost.app.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tan on 2015/3/6.
 */
public class FetchAllTrackRCommand extends Command {

    private final int mUid;

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

    public List<TrackR> getBoundTrackRs() {
        if(success()) {
            String addressesStr = mResultMap.get("losserid");
            ArrayList<String> addressedArrayList = new ArrayList<String>();
            if(!TextUtils.isEmpty(addressesStr)) {
                String[] trackrs = addressesStr.split(",");
                ArrayList<TrackR> trackRs = new ArrayList<TrackR>();
                for(String t: trackrs) {
                    //format address|name|type
                    String[] trackInfo = t.split("|");
                    String address = trackInfo[0];

                    if(!Utils.isValidMacAddress(address)) {
                        Log.e(LOG_TAG, "get one invalid mac address");
                        continue;
                    }
                    String name = trackInfo[1];
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

//                    if(Utils.isValidMacAddress(address)) {
//                        addressedArrayList.add(address);
//                        Log.d(LOG_TAG, "Fetch one mac address is " + address);
//                    }
                    trackRs.add(track);
                }
                return trackRs;
            }
            return null;
        } else {
            dumpDebugInfo();
        }
        return null;
    }
}
