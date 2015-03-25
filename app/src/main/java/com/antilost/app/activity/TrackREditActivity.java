package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.model.TrackR;
import com.antilost.app.network.BindCommand;
import com.antilost.app.network.UploadImageCommand;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.CsstSHImageData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TrackREditActivity extends Activity implements View.OnClickListener {

    public static final int REQUEST_CODE_TAKE_PHOTO = 1;
    public static final int REQUEST_CODE_CHOOSE_PICTURE = 2;

    public static final String BLUETOOTH_ADDRESS_BUNDLE_KEY = "bluetooth_address_key";
    private static final String LOG_TAG = "TrackREditActivity";
    private AlertDialog mImageSourceDialog;
    private String mBluetoothDeviceAddress;
    private EditText mTrackRName;
    private PrefsManager mPrefs;
    private TrackR mTrack;
    private Resources mResource;

    public static final int     REQUEST_CODE_PICK_PICTURE = 1;
    public static final int     REQUEST_CODE_TAKE_PICTURE = 2;
    private static final int GET_ICON_FROM_ALBUM = 0x00;
    private static final int GET_ICON_FROM_CROP = 0x01;
    private static final int GET_ICON_FROM_TAKE = 0x02;
    private static final int SCAN_UUID_REQUEST = 0x03;
    private File                mDeviceIconTempFile = null;
    private String              mLastUpdatedIconFileName  = null;

    public static int[] TypeIds = {
            R.id.key,
            R.id.wallet,
            R.id.bag,
            R.id.computer,
            R.id.pet,
            R.id.car,
            R.id.child,
            R.id.other
    };

    public static int[] DrawableIds = {
            R.drawable.key,
            R.drawable.wallet,
            R.drawable.bag,
            R.drawable.computer,
            R.drawable.pet,
            R.drawable.car,
            R.drawable.child,
            R.drawable.other
    };

    private View.OnClickListener mTypesIconClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = positionOfView(v);
            if(position != -1) {
                mTrack.type  = position;
            }
            int drawableId = DrawableIds[mTrack.type];
            mImageView.setImageResource(drawableId);
            mTrackRName.setText(mTypeNames[mTrack.type]);

            new File(CsstSHImageData.TRACKR_IMAGE_FOLDER, mBluetoothDeviceAddress).delete();
        }

        private int positionOfView(View v) {
            int id = v.getId();

            for(int i = 0; i < TypeIds.length; i++) {
                if(TypeIds[i] == id) {
                    return i;
                }
            }
            return -1;
        }
    };
    private ImageView mImageView;
    private String[] mTypeNames;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_redit);
        mBluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);
        if(TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            finish();
            return;
        }
        findViewById(R.id.changeImage).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnOK).setOnClickListener(this);

        mTrackRName = (EditText) findViewById(R.id.track_r_type_name);
        mPrefs = PrefsManager.singleInstance(this);
        mTrack = mPrefs.getTrack(mBluetoothDeviceAddress);
        if(mTrack == null) {
            mTrack = new TrackR();
            mTrack.address = mBluetoothDeviceAddress;
        }
        mResource = getResources();
        mImageView = (ImageView) findViewById(R.id.centerLargeImage);
        mImageView.setImageResource(DrawableIds[mTrack.type]);
        mDeviceIconTempFile = CsstSHImageData.deviceIconTempFile();

        Uri customIconUri = CsstSHImageData.getIconImageUri(mBluetoothDeviceAddress);

        if(customIconUri != null) {
            mImageView.setImageURI(customIconUri);
        }

        mTypeNames = getResources().getStringArray(R.array.default_type_names);
        if(!TextUtils.isEmpty(mTrack.name)) {
            mTrackRName.setText(mTrack.name);
        } else {
            mTrackRName.setText(mTypeNames[mTrack.type]);
        }
        for(int id: TypeIds) {
            findViewById(id).setOnClickListener(mTypesIconClickListener);
        }

        startService(new Intent(this, BluetoothLeService.class));
    }



    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.changeImage:
                showImageSourceDialog();
                break;
            case R.id.takePhoto:
                Toast.makeText(this, R.string.take_photo, Toast.LENGTH_LONG).show();
                CsstSHImageData.tackPhoto(TrackREditActivity.this, mDeviceIconTempFile, GET_ICON_FROM_TAKE);
                dismissImageSourceDialog();
                break;
            case R.id.choosePicture:
                Toast.makeText(this, R.string.choose_picture, Toast.LENGTH_LONG).show();
                CsstSHImageData.pickAlbum(TrackREditActivity.this, GET_ICON_FROM_ALBUM);
                dismissImageSourceDialog();
                break;
            case R.id.btnCancel:
                finish();
                break;
            case R.id.btnOK:
                saveTrackRSetting();
                break;
        }
    }

    private void dismissImageSourceDialog() {
        if(mImageSourceDialog != null) {
            mImageSourceDialog.dismiss();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_ICON_FROM_TAKE:
                if (RESULT_OK == resultCode){
                    CsstSHImageData.cropDeviceIconPhoto(this, Uri.fromFile(mDeviceIconTempFile), GET_ICON_FROM_CROP);
                }
                break;

            case GET_ICON_FROM_CROP:
                if (null != data){
                    try{
                        Bundle extras = data.getExtras();
                        Bitmap source = extras.getParcelable("data");
                        String path = CsstSHImageData.TRACKR_IMAGE_FOLDER + File.separator + "temp_trackr_image.tmp";
                        source = CsstSHImageData.zoomBitmap(source, path);
                        File pngFile = new File(path);

                        File folder = ensureIconFolder();
                        if(!pngFile.renameTo(new File(folder, mBluetoothDeviceAddress))) {
                            Log.e(LOG_TAG, "Rename to address failed");
                        };
                        mImageView.setImageBitmap(source);
                    }catch(Exception ex ){
                        System.out.println("the error is "+ex.toString());
                    }

                }
                break;

            case GET_ICON_FROM_ALBUM:
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    CsstSHImageData.cropDeviceIconPhoto(this, uri, GET_ICON_FROM_CROP);
                }
                break;
        }
    }

    private File ensureIconFolder() {
        File folder = new File(CsstSHImageData.TRACKR_IMAGE_FOLDER);
        if(folder.exists() && folder.isFile()) {
            folder.delete();
            folder.mkdir();
        } else if(!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }


    private void saveTrackRSetting() {
        final String name = mTrackRName.getText().toString();
        if(!TextUtils.isEmpty(name)) {
            mTrack.name = name;
        }



        Thread t = new Thread() {
            @Override
            public void run() {
                final BindCommand bindcommand = new BindCommand(String.valueOf(mPrefs.getUid()), name, mBluetoothDeviceAddress, String.valueOf(mTrack.type));

                bindcommand.execTask();
                boolean bindOk = bindcommand.success();
                boolean uploadPhotoOk = false;
                if(bindOk) {
                    Log.i(LOG_TAG, "Bind track ok.");
                    mPrefs.addTrackId(mBluetoothDeviceAddress);
                    mPrefs.saveTrackToFile(mBluetoothDeviceAddress, mTrack);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toast(getString(R.string.binding_success));
                            finish();
                        }
                    });

                    startService(new Intent(TrackREditActivity.this, BluetoothLeService.class));
                    UploadImageCommand command = new UploadImageCommand(mPrefs.getUid(), mBluetoothDeviceAddress);
                    command.execTask();
                    if(command.success()) {
                        uploadPhotoOk = true;
                        Log.v(LOG_TAG, "upload track photo to server success.");
                    } else {
                        Log.e(LOG_TAG, "upload track photo to server failed.");
                    }
                } else {
                    Log.e(LOG_TAG, "Bind RrackR Error.");
                }
            }
        };
        t.start();

    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


//    public void testIconFileDownload() {
//        Thread t = new Thread() {
//            @Override
//            public void run() {
//                final DownloadImageCommand command = new DownloadImageCommand(mPrefs.getUid(), mBluetoothDeviceAddress);
//                command.execTask();
//                byte[] rawImageData = command.getRawImageData();
//                if(rawImageData != null) {
//                    Log.v(LOG_TAG, "get rawImageData length is " + rawImageData.length);
//                    saveDataToFile(rawImageData);
//                } else {
//                    Log.e(LOG_TAG, "no rawImageData return.");
//                }
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        finish();
//                    }
//                });
//
//            }
//        };
//        t.start();
//    }

    private void saveDataToFile(byte[] rawImageData) {
        File folder = ensureIconFolder();
        File iconFile = new File(folder, mBluetoothDeviceAddress);
        try {
            FileOutputStream out = new FileOutputStream(iconFile);
            out.write(rawImageData);
            out.close();

            Log.i(LOG_TAG, "saveDataToFile finish successfull .");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void showImageSourceDialog() {
        if(mImageSourceDialog != null) {
            mImageSourceDialog.show();
            return;
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View customView = getLayoutInflater().inflate(R.layout.custom_photo_source_dialog, null);
        b.setView(customView);
        b.setTitle(getString(R.string.change_image));
        customView.findViewById(R.id.takePhoto).setOnClickListener(this);
        customView.findViewById(R.id.choosePicture).setOnClickListener(this);
        b.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mImageSourceDialog.dismiss();
            }
        });
        b.setPositiveButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(!new File(CsstSHImageData.TRACKR_IMAGE_FOLDER, mBluetoothDeviceAddress).delete()) {
                    Log.w(LOG_TAG, "Reset trackr custom image failed.");
                    int drawableId = DrawableIds[mTrack.type];
                    mImageView.setImageResource(drawableId);
                };
            }
        });

        mImageSourceDialog = b.create();
        mImageSourceDialog.show();
    }
}
