package com.antilost.app.network;

import android.util.Log;

/**
 * Created by Administrator on 2015/1/26.
 */
public class BindCommand extends Command {

    private final String mUid;
    private final String mTrackName;
    private final String mTrackId;
    private final String mCustomType;

    public BindCommand(String uid, String trackName, String trackId, String customType) {
        mUid = uid;
        mTrackName = trackName;
        mTrackId = trackId;
        mCustomType = customType;
    }
    @Override
    protected String makeRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd:bind").append(LINE_SPLITTER)
                .append("uid:").append(mUid).append(LINE_SPLITTER)
                .append("type:").append(mCustomType).append(LINE_SPLITTER)
                .append("lossername:").append(mTrackName).append(LINE_SPLITTER)
                .append("losserid:").append(mTrackId).append(LINE_SPLITTER);
        String result = sb.toString();
        Log.v(LOG_TAG, "request string is " + result);
        return result;
    }
}
