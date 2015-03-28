package com.antilost.app.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import com.antilost.app.R;
import com.antilost.app.network.VersionCheckCommand;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UpdateService extends Service {

    public static boolean isDownloading = false;
    public static final String EXTRA_KEY_NEW_VERSION_URL = "new_version_url";
    public static final int DOWNLOAD_NOTIFICATION_ID = 1;

    private static final String LOG_TAG = "UpdateService";
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Downloading iTrack...")
                .setSmallIcon(R.drawable.downloading);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String newApkUrl = intent.getStringExtra(EXTRA_KEY_NEW_VERSION_URL);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    if(isDownloading) {
                        Log.e(LOG_TAG, "download already start.");
                        return;
                    }

                    isDownloading = true;
                    if(TextUtils.isEmpty(newApkUrl)) {
                        Log.e(LOG_TAG, "Get empty new apk url.");
                        isDownloading = false;
                        return;
                    }
                    URL newVersionApkUrl = new URL(newApkUrl);
                    URLConnection connection = newVersionApkUrl.openConnection();
                    InputStream urlInput = (InputStream) connection.getInputStream();
                    if(urlInput == null) {
                        Log.e(LOG_TAG, "input stream of url content is null");
                        isDownloading = false;
                        return;
                    }
                    int fileSize = connection.getContentLength();
                    Log.d(LOG_TAG, "apk file size is " + Formatter.formatFileSize(UpdateService.this, fileSize));
                    File outputParentFolder = new File(Environment.getExternalStorageDirectory(), "iTrack");
                    if(!outputParentFolder.exists()) {
                        outputParentFolder.mkdir();
                    } else {
                        if(outputParentFolder.isFile()) {
                            outputParentFolder.delete();
                            outputParentFolder.mkdir();
                        }
                    }

                    File newVersionApkFile = new File(outputParentFolder, "temp");

                    FileOutputStream fileOut = new FileOutputStream(newVersionApkFile);
                    byte[] buffer = new byte[2048];
                    int downloadCount = 0;
                    int readCount;

                    while((readCount = urlInput.read(buffer)) > 0) {
                        downloadCount += readCount;
                        if(fileSize > 0) {
                            mBuilder.setProgress(fileSize, downloadCount, false);
                        } else {
                            String humanReadableDownloadSize = Formatter.formatFileSize(UpdateService.this, downloadCount);
                            mBuilder.setContentText(getString(R.string.format_downloaded, humanReadableDownloadSize));
                        }
                        mNotifyManager.notify(DOWNLOAD_NOTIFICATION_ID, mBuilder.build());
                        fileOut.write(buffer, 0, readCount);
                    }
                    isDownloading = false;
                    fileOut.close();
                    urlInput.close();
                    installFile(newVersionApkFile);

                    mNotifyManager.cancel(DOWNLOAD_NOTIFICATION_ID);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        return START_NOT_STICKY;
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
        startActivity(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
