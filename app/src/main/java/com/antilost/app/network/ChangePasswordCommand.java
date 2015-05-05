package com.antilost.app.network;

/**
 * Created by Tan on 2015/5/4.
 */
public class ChangePasswordCommand extends Command {

    private final String mNewPass;
    private final String mOldPass;
    private final long mUid;

    public ChangePasswordCommand(long uid, String newPass, String oldPass) {
        mUid = uid;
        mNewPass = newPass;
        mOldPass = oldPass;
    }
    @Override
    protected String makeRequestString() {
        setCommand("changepasswd");
        addLine("uid:" + mUid);
        addLine("oldpasswd:" + mOldPass);
        addLine("newpasswd:" + mNewPass);
        return mRequestBuffer.toString();
    }
}
