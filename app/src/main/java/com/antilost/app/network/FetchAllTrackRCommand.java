package com.antilost.app.network;

import android.text.TextUtils;
import android.util.Log;

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

    public List<String> getBoundTrackRAddress() {
        if(success()) {
            String addressesStr = mResultMap.get("losserid");
            ArrayList<String> addressedArrayList = new ArrayList<String>();
            if(!TextUtils.isEmpty(addressesStr)) {
                String[] addresses = addressesStr.split(",");
                for(String address: addresses) {
                    if(Utils.isValidMacAddress(address)) {
                        addressedArrayList.add(address);
                        Log.d(LOG_TAG, "fetch one mac address is " + address);
                    }
                }
                return addressedArrayList;
            }
            return null;
        } else {
            dumpDebugInfo();
        }
        return null;
    }
}
