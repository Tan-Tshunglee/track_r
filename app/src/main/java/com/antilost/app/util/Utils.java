package com.antilost.app.util;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.view.Gravity;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.activity.ManualAddLocationActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Created by Tan on 2015/3/7.
 */
public class Utils {

    public static final String DEVICE_NAME = "TrackR";
    public static final String DEVICE_KEY_PRESSED_NAME = "Track0";

    public static final String NAME_NEED_VERSION = "version";
    public static final byte[] VERIFY_CODE = new byte[]{(byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4};

    public static final boolean isValidMacAddress(String mac) {
        if (TextUtils.isEmpty(mac)) {
            return false;
        }

        if (mac.length() != 17) {
            return false;
        }

        if (!mac.contains(":")) {
            return false;
        }

        String[] addressBits = mac.split(":");
        if (addressBits.length != 6) {
            return false;
        }

        boolean valid = true;
        for (String bit : addressBits) {
            try {
                Integer.parseInt(bit, 16);
            } catch (NumberFormatException e) {
                valid = false;
                e.printStackTrace();
            }
        }

        return valid;
    }

    public static boolean isValidEmailAddress(String email) {
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(email);
        return matcher.matches();
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

        if (timeStamp == -1) {
            return null;
        }
        Date date = new Date(timeStamp);
        return DATE_FORMAT.format(date);
    }

    public static final long convertTimeStrToLongTime(String dateStr) {

        try {
            return DATE_FORMAT.parse(dateStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static final BluetoothGatt connectBluetoothGatt(BluetoothDevice device,
                                                           Context ctx,
                                                           BluetoothGattCallback callback) {
        if (isLollipop()) {
            // Little hack with reflect to use the connect gatt with defined transport in Lollipop
            Method connectGattMethod = null;

            try {
                connectGattMethod = device.getClass().getMethod("connectGatt", Context.class, boolean.class, BluetoothGattCallback.class, int.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            try {
                return (BluetoothGatt) connectGattMethod.invoke(device, ctx, false, callback, 2);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return device.connectGatt(ctx, false, callback);
        }
    }

    public static boolean isLollipop() {
        //
        //return Build.VERSION.SDK_INT >= 20;
        return false;
    }


    public static int neededRotation(File imageFile) {
        try {

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270;
            }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180;
            }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90;
            }
            return 0;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static Bitmap scaleBitmap(String originFilePath, float scale) {
        Bitmap origin = BitmapFactory.decodeFile(originFilePath);
        return Bitmap.createScaledBitmap(origin, (int) (origin.getWidth() * scale), (int) (origin.getHeight() * scale), true);
    }

    public static Bitmap scaleBitmap(Bitmap origin, float scale) {
        return Bitmap.createScaledBitmap(origin, (int) (origin.getWidth() * scale), (int) (origin.getHeight() * scale), true);
    }

    public static void showShortToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showShortToast(ManualAddLocationActivity ctx, int msgResId) {
        Toast.makeText(ctx, msgResId, Toast.LENGTH_SHORT).show();
    }

    public static String base64EncodeStr2Str(String origin) {
        return Base64.encodeToString(origin.getBytes(), Base64.NO_WRAP);
    }
}
