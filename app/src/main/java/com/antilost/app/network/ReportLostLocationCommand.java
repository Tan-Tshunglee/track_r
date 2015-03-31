package com.antilost.app.network;

import android.location.Location;

import com.antilost.app.util.Utils;

/**
 * Created by Tan on 2015/4/1.
 */
public class ReportLostLocationCommand extends Command {

    private final int mUid;
    private final Location mLocation;
    private final String mAddress;

    public ReportLostLocationCommand(int uid, Location loc, String address) {
        mUid = uid;
        mLocation = loc;
        mAddress = address;
    }
    @Override
    protected String makeRequestString() {
        setCommand("setgps");
        addLine("uid:" + mUid);
        addLine("losserid:" + mAddress);
        addLine("log:" + mLocation.getLongitude());
        addLine("lat:" + mLocation.getLatitude());
        addLine("time:" + Utils.convertTimeStampToLiteral(System.currentTimeMillis()));
        return mRequestBuffer.toString();
    }
}
