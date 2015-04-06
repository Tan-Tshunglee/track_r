package com.antilost.app.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Tan on 2015/3/7.
 */
public class Utils {

    public static final String DEVICE_NAME = "TrackR";
    public static final boolean isValidMacAddress(String mac) {
        if(TextUtils.isEmpty(mac)) {
            return false;
        }

        if(mac.length() != 17) {
            return false;
        }

        if(!mac.contains(":")) {
            return false;
        }

        String[] addressBits = mac.split(":");
        if(addressBits.length != 6) {
            return false;
        }

        boolean valid = true;
        for(String bit: addressBits) {
            try {
                Integer.parseInt(bit, 16);
            } catch (NumberFormatException e) {
                valid = false;
                e.printStackTrace();
            }
        }

        return valid;
    }

    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);

            byte[] cert = info.signatures[0].toByteArray();

            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            return hexString.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String convertTimeStampToLiteral(long timeStamp) {

        if(timeStamp  == -1) {
            return null;
        }
        Date date = new Date(timeStamp);
        return DATE_FORMAT.format(date);
    }

    public static final long convertTimeStrToLongTime(String dateStr) {

        try {
            return  DATE_FORMAT.parse(dateStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
