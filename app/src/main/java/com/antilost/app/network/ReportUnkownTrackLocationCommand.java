package com.antilost.app.network;

/**
 * Created by Tan on 2015/3/29.
 */
public class ReportUnkownTrackLocationCommand extends Command {

    private final String mAddress;
    private final String mLongitude;
    private final String mLatitude;

    public ReportUnkownTrackLocationCommand(String address, String log, String lat) {
        mAddress = address;
        mLongitude = log;
        mLatitude = lat;
    }
    @Override
    protected String makeRequestString() {
        setCommand("setothergps");
        addLine("losserid:" + mAddress);
        addLine("log:" + mLatitude);
        return mRequestBuffer.toString();
    }
}
