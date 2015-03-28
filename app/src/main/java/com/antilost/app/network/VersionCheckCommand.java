package com.antilost.app.network;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Tan on 2015/3/28.
 */
public class VersionCheckCommand extends Command {

    private final Integer mMajorVersion;
    private final Integer mMinorVersion;
    private final Context mContext;
    public static boolean isUpdating = false;
    private final boolean mAutoUpdate;

    /**
     * Version check command , which can check and  update self;
     * @param context
     * @param autoUpdate auto update or not
     * @throws IllegalArgumentException
     * @throws NumberFormatException
     * @throws PackageManager.NameNotFoundException
     */
    public VersionCheckCommand(Context context, boolean autoUpdate) throws IllegalArgumentException, NumberFormatException, PackageManager.NameNotFoundException {

        mContext = context;
        mAutoUpdate = autoUpdate;
        PackageManager packageManager = context.getPackageManager();

        PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        String currentVersion = packageInfo.versionName;

        if(TextUtils.isEmpty(currentVersion)) {
            throw new IllegalArgumentException("version string must not be empty");
        }

        String[] versions = currentVersion.split("\\.");
        if(versions.length != 2) {
            throw new IllegalArgumentException("wrong version string format.");
        }

        mMajorVersion = Integer.valueOf(versions[0]);
        mMinorVersion = Integer.valueOf(versions[1]);
    }

    @Override
    protected String makeRequestString() {
        setCommand("version");
        return mRequestBuffer.toString();
    }

    @Override
    public boolean execTask() {
        if(!isUpdating) {
            isUpdating = true;
            return super.execTask();
        } else {
            Log.w(LOG_TAG, "try make another update thread.");
            return false;
        }

    }

    @Override
    protected void postAction() {
//        mResultMap.put("status","success");
//        mResultMap.put("version", "3.3");
//        mResultMap.put("url", "http://dl.wandoujia.com/files/phoenix/latest/wandoujia-wandoujia_web.apk?timestamp=1409388568830");

        if(mAutoUpdate && hasNewVersion()) {
            downloadAndInstallNewVersionApk();
        }

        isUpdating = false;
    }

    public String newVersionName() {
        return mResultMap.get("version");
    }

    public boolean hasNewVersion() {

        //version string format XX.XX which contain
        //major and minor version number;
        String newVersion = newVersionName();
        if(TextUtils.isEmpty(newVersion)) {
            return false;
        }

        if(newVersion.indexOf('.') == -1) {
            return false;
        }
        String[] newVerNums = newVersion.split("\\.");
        if(newVerNums.length != 2) {
            return false;
        }

        int newMajorVersion = 0;
        try {
            newMajorVersion = Integer.valueOf(newVerNums[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

        int newMinorVersion = 0;

        try {
            newMinorVersion = Integer.valueOf(newVerNums[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

        if(newMajorVersion > mMajorVersion) {
            Log.i(LOG_TAG, "find new major  version update + " + newVersionUrl());
            return true;
        } else if(newMajorVersion == mMajorVersion) {
            if(newMinorVersion > mMinorVersion) {
                Log.i(LOG_TAG, "find new minor version update + " + newVersionUrl());
                return true;
            }
        }
        Log.i(LOG_TAG, "no new version found.");

        return false;
    }

    public String newVersionUrl() {
        return mResultMap.get("url");
    }


    public boolean downloadAndInstallNewVersionApk() {
        try {
            URL newVersionApkUrl = new URL(newVersionUrl());
            InputStream urlInput = (InputStream) newVersionApkUrl.getContent();
            if(urlInput == null) {
                return false;
            }
            File outputParentFolder = new File(Environment.getExternalStorageDirectory(), "iTrack");
            if(!outputParentFolder.exists()) {
                outputParentFolder.mkdir();
            } else {
                if(outputParentFolder.isFile()) {
                    outputParentFolder.delete();
                    outputParentFolder.mkdir();
                }
            }

            File newVersionApkFile = new File(outputParentFolder, "iTrack" + newVersionName());

            FileOutputStream fileOut = new FileOutputStream(newVersionApkFile);
            byte[] buffer = new byte[2048];
            int readCount;
            while((readCount = urlInput.read(buffer)) > 0) {
                fileOut.write(buffer, 0, readCount);
            }

            fileOut.close();
            urlInput.close();
            installFile(newVersionApkFile);
            return true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * install APK code
     */
    private void installFile(File apkFile){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

}
