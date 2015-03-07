package com.antilost.app.util;

import android.text.TextUtils;

/**
 * Created by Tan on 2015/3/7.
 */
public class Utils {

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
}
