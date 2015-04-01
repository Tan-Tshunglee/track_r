package com.antilost.app.camera;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tan on 2015/3/21.
 */
public class PhotoSaver extends  Thread {
    public static final String PARENT_FOLDER =  Environment.getExternalStorageDirectory()+ File.separator + "DCIM" + File.separator + "TrackR";
    public static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_TAG = "PhotoSaver";
    private final Context mContext;
    private  byte[] mJpgData;

    public PhotoSaver(byte[] jpgData, Context ctx) {
        mContext = ctx;
        mJpgData = jpgData;
    }

    @Override
    public void run() {
        ensureParentFolder();
        String fileName = FILE_NAME_FORMAT.format(new Date()) + ".jpg";
        File jpg = new File(PARENT_FOLDER, fileName);
        try {
            FileOutputStream out = new FileOutputStream(jpg);
            BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
            bufferedOut.write(mJpgData);
            mJpgData = null;
            bufferedOut.close();
            Log.d(LOG_TAG, "save jpg data to " + fileName);
            final Activity activity = (Activity) mContext;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Photo Taken And Saved.", Toast.LENGTH_SHORT).show();
                }
            });
            addImageToGallery(jpg.getAbsolutePath(), mContext);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureParentFolder() {
        File parent = new File(PARENT_FOLDER);
        if(!parent.exists()) {
            parent.mkdir();
            return;
        }

        if(parent.isFile()) {
            parent.delete();
            parent.mkdir();
            return;
        }
    }

    public static void save(byte[] bytes, Context context) {
        Thread t = new PhotoSaver(bytes, context);
        t.start();
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent view = new Intent(Intent.ACTION_VIEW);
                view.setData(uri);
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(view);

    }
}
