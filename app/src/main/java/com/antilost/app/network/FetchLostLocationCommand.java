package com.antilost.app.network;

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
}
