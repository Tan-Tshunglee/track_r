package com.antilost.app.network;

/**
 * Created by Tan on 2015/3/29.
 */
public class LostDeclareCommand extends Command {
    private final int mUid;
    private final String mTrackAddress;
    private final int mEnabled;

    /**
     * Command  declare lost and withdraw the declare,
     * @param uid user id
     * @param trackAddress the address of track
     * @param enabled 1 for declare lost 0 for withdraw the declare
     */
        public  LostDeclareCommand(int uid, String trackAddress, int enabled) {
        mUid = uid;
        mTrackAddress = trackAddress;
        mEnabled = enabled;
    }
    @Override
    protected String makeRequestString() {
        setCommand("declare");
        addLine("type:" + mEnabled);
        addLine("uid:" + mUid);
        addLine("track:" + mTrackAddress);
        return mRequestBuffer.toString();
    }
}
