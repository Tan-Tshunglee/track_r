package com.antilost.app.network;

import android.os.Handler;
import android.util.Log;

/**
 * Created by Administrator on 2015/3/8.
 */
public class CommandPerformer extends Thread {
    private static final String LOG_TAG = "CommandPerformer";
    private final Command mCommand;
    private Handler mHandler;
    private Runnable mRunnable;

    public CommandPerformer(Command command) {
        mCommand = command;
    }


    public void setPostExective(Handler handler, Runnable runnable) {
        mHandler = handler;
        mRunnable = runnable;
    }
    @Override
    public void run() {
        mCommand.execTask();
        if(mCommand.success()) {
            Log.i(LOG_TAG, "command perform successful.");
        } else {
            mCommand.dumpDebugInfo();
        }

        if(mHandler != null && mRunnable != null) {
            mHandler.post(mRunnable);
        }
    }
}
