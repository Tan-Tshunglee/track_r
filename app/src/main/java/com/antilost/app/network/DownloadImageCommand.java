package com.antilost.app.network;

import android.util.Base64;
import android.util.Log;

/**
 * Created by Tan on 2015/3/6.
 */
public class DownloadImageCommand extends Command {

    private final String mAddress;
    private final int mUid;

    public DownloadImageCommand(int uid, String address) {
        mUid  = uid;
        mAddress = address;
    }

    @Override
    protected String makeRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd:getpic").append(LINE_SPLITTER);
        sb.append("uid:").append(mUid).append(LINE_SPLITTER);
        sb.append("losserid:").append(mAddress).append(LINE_SPLITTER);
        return sb.toString();
    }

    public byte[] getRawImageData() {
        if(success()) {
            String base64EncodedImageData = mResultMap.get("pic");
            return Base64.decode(base64EncodedImageData, Base64.NO_WRAP);
        } else {
            Log.v(LOG_TAG, "download image data failed.");
        }
        return null;
    }
}
