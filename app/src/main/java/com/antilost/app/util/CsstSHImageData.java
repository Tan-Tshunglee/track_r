package com.antilost.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.android.camera.CropImageIntentBuilder;
import com.antilost.app.common.ICsstSHConstant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片处理工具类
 *
 * @author liuyang
 */
public final class CsstSHImageData {

    public static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
    public static final String TRACKR_IMAGE_FOLDER = SDCARD + File.separator + "iTrack";
    private static final String LOG_TAG = "CsstSHImageData";
    /**
     * 拍照临时文件
     */
    private static File DEVICE_TAKE_TEMP = null;

    /**
     * 临时文件
     *
     * @return
     */
    public static final File deviceIconTempFile() {
        if (null == DEVICE_TAKE_TEMP) {
            DEVICE_TAKE_TEMP = new File(TRACKR_IMAGE_FOLDER, "device.jpg");
        }
        return DEVICE_TAKE_TEMP;
    }

    /**
     * 缩放临时文件
     *
     * @return
     */
    public static final File zoomIconTempFile() {
        return new File(ICsstSHConstant.DEVICE_ICON_PATH, CsstSHDateUtil.deviceIconName());
    }

    /**
     * 设备封面文件
     *
     * @return
     */
    public static final File deviceIconFile(Context context) {
        return new File(context.getFilesDir(), CsstSHDateUtil.deviceIconName());
    }

    /**
     * 通过专辑选择图片
     *
     * @param context
     * @param requestCode
     */
    public static final void pickAlbum(Activity context, int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 复制bitmap到本地文件
     *
     * @param bitmap
     * @param file
     */
    public static final void copyBitmapToLocal(Bitmap bitmap, String file) {
        FileOutputStream ous = null;
        try {
            ous = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 0, ous);
            ous.flush();
            ous.close();
            ous = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != ous) {
                    ous.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拷贝缩放后的文件到程序中
     *
     * @param source
     * @param target
     */
    public static final void copyZoomToApp(String source, File target) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int index = 0;
            while ((index = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, index);
            }
            fos.flush();
            fos.close();
            fos = null;
            fis.close();
            fis = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fis) {
                    fis.close();
                }
                if (null != fos) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 缩放图片
     *
     * @param source    源图片
     *  newWidth  缩放宽度
     *  newHeight 缩放高度
     * @return
     */
    public static final Bitmap zoomBitmap(Bitmap source, String file) {
        // 获取这个图片的宽和高
        int width = source.getWidth();
        int height = source.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) ICsstSHConstant.DEVICE_ICON_WIDTH) / width;
        float scaleHeight = ((float) ICsstSHConstant.DEVICE_ICON_HEIGHT) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
        source.recycle();
        source = null;
        copyBitmapToLocal(bitmap, file);
        return bitmap;
    }

    /**
     * 拍照
     *
     * @param context     上下文
     * @param file        拍照保持文件路径
     * @param requestCode 请求骂
     */
    public static final void takePhoto(Activity context, File file, int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 剪切设备封面图片
     *
     * @param context     上下文
     * @param sourceImageUri   剪切图片源文件Uri
     * @param savedImageUri 保存剪切处理之后图片文件的Uri
     * @param requestCode 请求code
     */
    public static final void cropDeviceIconPhoto(Activity context, Uri sourceImageUri, Uri savedImageUri, int requestCode) {
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(uri, "image/*");
//        // 设置裁剪
//        intent.putExtra("crop", "true");
//        // aspectX aspectY 是宽高的比例
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        // outputX outputY 是裁剪图片宽高
//        intent.putExtra("outputX", ICsstSHConstant.DEVICE_ICON_WIDTH);
//        intent.putExtra("outputY", ICsstSHConstant.DEVICE_ICON_HEIGHT);
//        intent.putExtra("return-data", true);
//        context.startActivityForResult(intent, requestCode);

        CropImageIntentBuilder cropImageIntentBuilder =
                new CropImageIntentBuilder(
                        1,//Horizontal aspect ratio.
                        1,//Vertical  aspect ratio.
                        ICsstSHConstant.DEVICE_ICON_WIDTH,//Output vertical size in pixels.
                        ICsstSHConstant.DEVICE_ICON_HEIGHT,//Output horizontal size in pixels.
                        savedImageUri//
                );
        cropImageIntentBuilder.setOutlineColor(0xFF03A9F4);
        cropImageIntentBuilder.setSourceImage(sourceImageUri);
        cropImageIntentBuilder.setCircleCrop(true);
        context.startActivityForResult(cropImageIntentBuilder.getIntent(context), requestCode);
    }


    public static final Uri getIconImageUri(String address) {
        File imageFile = new File(TRACKR_IMAGE_FOLDER, address);
        if(imageFile.exists()) {
            return Uri.fromFile(imageFile);
        }
        return null;
    }
    public static final String getIconImageString(String address) {
        File imageFile = new File(TRACKR_IMAGE_FOLDER, address);
        if(imageFile.exists()) {
            return imageFile.toString();
        }
        return null;
    }

    public static final File getIconFile(String address) {
        return new File(TRACKR_IMAGE_FOLDER, address);
    }

    public static Bitmap toRoundCorner(String customIconUri) {

        Bitmap bitmap = BitmapFactory.decodeFile(customIconUri);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 100.f;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();
        return output;
    }

    public static Bitmap toRoundCorner(Bitmap bitmap) {

//        Bitmap bitmap = BitmapFactory.decodeFile(customIconUri);
        System.out.println("图片是否变成圆角模式了+++++++++++++");
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
//        final float roundPx = pixels;
        final float roundPx = 99.f;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
//        System.out.println("pixels+++++++" + pixels);

        return output;
    }


    public static void removePhoto(String address) {
        File imageFile = new File(TRACKR_IMAGE_FOLDER, address);
        boolean result = imageFile.delete();
        Log.d(LOG_TAG, "remove track 's photo" + result );
    }
}
