package com.antilost.app.network;

import com.antilost.app.util.Utils;

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
        addLine("oldpasswd:" + Utils.base64EncodeStr2Str(mOldPass));
        addLine("newpasswd:" + Utils.base64EncodeStr2Str(mNewPass));
        return mRequestBuffer.toString();
    }
}
