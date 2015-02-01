package com.antilost.app.network;

import android.util.Log;

/**
 * Created by Tan on 2015/2/1.
 */
public class UnbindCommand extends Command {

    private final int mUid;
    private final String mAddress;

    public UnbindCommand(int uid, String address) {
        mUid = uid;
        mAddress = address;
    }

    @Override
    protected String makeRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd:delete").append(LINE_SPLITTER)
                .append("uid:").append(mUid).append(LINE_SPLITTER)
                .append("losserid:").append(mAddress).append(LINE_SPLITTER);
        String result = sb.toString();
        Log.v(LOG_TAG, "request string is " + result);
        return result;
    }
}
