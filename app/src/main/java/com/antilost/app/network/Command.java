package com.antilost.app.network;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

public abstract class Command {

    public static final String SERVER_URL = "http://www.ieasytec.com/communication";
    public static final String LOG_TAG = "Command";

    public static final String COMMAND_ABBR_PREFIX = "cmd:";
    public static final String LINE_SPLITTER = "\r\n";
    public static final String STATUS = "status";
    public static final String SUCCESS = "success";
    public static final String ERROR = "err";
    public static final String TIME = "time";

    protected boolean mStatusBad = false;
    protected boolean mNetworkError = false;
    protected HashMap<String, String> mResultMap = new HashMap<String, String>();
    protected StringBuilder mRequestBuffer = new StringBuilder();


    protected abstract String makeRequestString();

    public boolean execTask() {
        boolean result = false;
        String request = makeRequestString();
        if(TextUtils.isEmpty(request)) {
            Log.e(LOG_TAG, "execTask get empty request");
        } else {
            String entity = HttpRequest.sendPost(SERVER_URL, request);
            if(!TextUtils.isEmpty(entity)) {
                result = parseResponse(entity);
            }
        }
        postAction();
        return result;
    }

    public boolean parseResponse(String entity) {
        try {
            String[] lines = entity.split("\r\n");
            mResultMap = new HashMap<String, String>();
            for(String line: lines) {
                String[] keyValues = line.split(":");
                if(keyValues.length == 2) {
                    mResultMap.put(keyValues[0], keyValues[1]);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            mStatusBad = true;
        }
        return false;
    }

    public boolean success() {
        if(mResultMap == null) {
            return false;
        }

        String status = mResultMap.get(STATUS);
        return SUCCESS.equals(status);

    }

    public boolean resultError() {
        if(mResultMap == null) {
            return false;
        }

        String status = mResultMap.get(STATUS);
        return ERROR.equals(status);
    }

    public boolean isNetworkError() {
        return mNetworkError;
    }

    public boolean isStatusBad() {
        return  mStatusBad;
    }

    public void dumpDebugInfo() {
        if(isNetworkError()) {
            Log.v(LOG_TAG, "network error");
        } else if(isStatusBad()) {
            Log.v(LOG_TAG, "status bad");
        } else if(resultError()) {
            Log.v(LOG_TAG, "result error");
        } else {
            Log.v(LOG_TAG, "unknown error.");
        }
    }

    protected void setCommand(String command) {
        mRequestBuffer.setLength(0);
        mRequestBuffer
                .append(COMMAND_ABBR_PREFIX)
                .append(command)
                .append(LINE_SPLITTER);

    }

    protected void addLine(String line) {
        mRequestBuffer.append(line).append(LINE_SPLITTER);
    }

    protected void postAction() {
        //do nothing
    }

}

