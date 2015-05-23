package com.antilost.app.network;

import android.util.Base64;
import android.util.Log;

import com.antilost.app.util.CsstSHImageData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tan on 2015/3/6.
 */
public class UpdateTrackImageCommand extends Command {

    private final String mAddress;
    private final int mUid;

    public UpdateTrackImageCommand(int uid, String address) {
        mUid  = uid;
        mAddress = address;
    }
    @Override
    protected String makeRequestString() {
        File iconFile = CsstSHImageData.getIconFile(mAddress);
        if(iconFile.exists() && iconFile.length() > 0) {
            long length = iconFile.length();
            ByteArrayOutputStream out = new ByteArrayOutputStream((int) length);

            try {
                InputStream in = new FileInputStream(iconFile);
                byte[] buffer = new byte[2048];
                int readCount;
                while((readCount = in.read(buffer)) != -1) {
                    out.write(buffer, 0, readCount);
                }
                in.close();
                byte[] rawData = out.toByteArray();
                String encodedData = Base64.encodeToString(rawData, Base64.NO_WRAP);
                out.close();

                StringBuilder sb = new StringBuilder(encodedData.length() + 30);
                sb.append("cmd:setpic").append(LINE_SPLITTER);
                sb.append("uid:").append(mUid).append(LINE_SPLITTER);
                sb.append("losserid:").append(mAddress).append(LINE_SPLITTER);
                sb.append("pic:").append(encodedData).append(LINE_SPLITTER);
                appendEncodePassword(sb);
                return sb.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //TODO: user delete custom icon;
            Log.d(LOG_TAG, "Image not set delete photo on server");
            StringBuilder sb = new StringBuilder(30);
            sb.append("cmd:setpic").append(LINE_SPLITTER);
            sb.append("uid:").append(mUid).append(LINE_SPLITTER);
            sb.append("losserid:").append(mAddress).append(LINE_SPLITTER);
            sb.append("pic:").append(LINE_SPLITTER);
            return sb.toString();
        }
        return null;
    }
}
