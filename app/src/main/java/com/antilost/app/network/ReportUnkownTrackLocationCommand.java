package com.antilost.app.network;

import android.location.Location;

import com.antilost.app.util.Utils;

/**
 * Created by Tan on 2015/3/29.
 */
public class ReportUnkownTrackLocationCommand extends Command {

    private final String mAddress;
    private final String mLongitude;
    private final String mLatitude;

    public ReportUnkownTrackLocationCommand(String address, Location loc) {
        mAddress = address;
        mLongitude = String.valueOf(loc.getLongitude());
        mLatitude = String.valueOf(loc.getLatitude());
    }
    @Override
    protected String makeRequestString() {
        setCommand("setothergps");
        addLine("losserid:" + mAddress);
        addLine("log:" + mLongitude);
        addLine("lat:" + mLatitude);
        addLine("time:" + Utils.convertTimeStampToLiteral(System.currentTimeMillis()));
        return mRequestBuffer.toString();
    }
}
