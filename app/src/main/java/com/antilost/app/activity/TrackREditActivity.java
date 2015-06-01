package com.antilost.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.model.TrackR;
import com.antilost.app.network.BindCommand;
import com.antilost.app.network.UpdateTrackImageCommand;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.util.CsstSHImageData;
import com.antilost.app.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TrackREditActivity extends Activity implements View.OnClickListener {

    public static final int REQUEST_CODE_TAKE_PHOTO = 1;
    public static final int REQUEST_CODE_CHOOSE_PICTURE = 2;

    public static final String BLUETOOTH_ADDRESS_BUNDLE_KEY = "bluetooth_address_key";
    public static final String EXTRA_EDIT_NEW_TRACK = "bluetooth_edit_new";

    private static final String LOG_TAG = "TrackREditActivity";
    private static final String TEMP_ICON_FOR_CROPPED_FILE = CsstSHImageData.TRACKR_IMAGE_FOLDER + File.separator + "temp_trackr_image.tmp";;
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
    private File mTempFileForPhotoTaken = null;

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

            CsstSHImageData.removePhoto(mBluetoothDeviceAddress);
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

    private BluetoothLeService mBluetoothLeService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothDeviceAddress = null;
        }
    };

    private ImageView mImageView;
    private String[] mTypeNames;
    private boolean mEditNewTrack;
    private TextView mTitleTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);
        mEditNewTrack = getIntent().getBooleanExtra(EXTRA_EDIT_NEW_TRACK, false);


        if(TextUtils.isEmpty(mBluetoothDeviceAddress)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_track_redit);
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
        mTempFileForPhotoTaken = CsstSHImageData.deviceIconTempFile();

        Uri customIconUri = CsstSHImageData.getIconImageUri(mBluetoothDeviceAddress);

        if(customIconUri != null) {
            mImageView.setImageBitmap(CsstSHImageData.toRoundCorner(CsstSHImageData.getIconImageString(mBluetoothDeviceAddress)));
        }


        mTitleTextView = (TextView) findViewById(R.id.titleTextView);

        if(mEditNewTrack) {
            mTitleTextView.setText(R.string.add_track_r);
        } else {
            mTitleTextView.setText(getString(R.string.edit_itrack));
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

        bindService(new Intent(this, BluetoothLeService.class), mServiceConnection, BIND_AUTO_CREATE);
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothLeService != null) {
            unbindService(mServiceConnection);
        }
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.changeImage:
                showImageSourceDialog();
                break;
            case R.id.takePhoto:
                Toast.makeText(this, R.string.take_photo, Toast.LENGTH_LONG).show();
                CsstSHImageData.takePhoto(TrackREditActivity.this, mTempFileForPhotoTaken, GET_ICON_FROM_TAKE);
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
                //after user take photo we let user to crop it
                if (resultCode == RESULT_OK) {
                    //temp file name;
                    Uri savedCroppedFile = Uri.fromFile(new File(TEMP_ICON_FOR_CROPPED_FILE));
                    CsstSHImageData.cropDeviceIconPhoto(this, Uri.fromFile(mTempFileForPhotoTaken), savedCroppedFile, GET_ICON_FROM_CROP);
                } else {
                    Log.e(LOG_TAG, "take photo  result not ok");
                }
                break;

            case GET_ICON_FROM_CROP:
                int orientation = -1;
                if(mTempFileForPhotoTaken.exists()) {
                    orientation = Utils.neededRotation(mTempFileForPhotoTaken.getAbsoluteFile());
                   mTempFileForPhotoTaken.delete();
                }
                if (resultCode == RESULT_OK) {
                    try {
                        Log.v(LOG_TAG, "user choose crop photo");
                        File croppedTargetFile = new File(TEMP_ICON_FOR_CROPPED_FILE);


                        //check weather we should rotate  the cropped image
                        if ( orientation > 0){
                            Matrix m = new Matrix();
                            m.postRotate(orientation);
                            Bitmap origin = BitmapFactory.decodeFile(TEMP_ICON_FOR_CROPPED_FILE);
                            Bitmap rotated = Bitmap.createBitmap(origin,
                                    0,
                                    0,
                                    origin.getWidth(),
                                    origin.getHeight(),
                                    m,
                                    true);
                            rotated.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(croppedTargetFile));
                        }

                        mImageView.setImageURI(Uri.fromFile(croppedTargetFile));
                    } catch (Exception ex) {
                        System.out.println("The error is " + ex.toString());
                    }

                }
                break;

            case GET_ICON_FROM_ALBUM:
                if (resultCode == RESULT_OK) {
                    Uri sourceImageUri = data.getData();
                    CsstSHImageData.cropDeviceIconPhoto(this, sourceImageUri, Uri.fromFile(new File(TEMP_ICON_FOR_CROPPED_FILE)), GET_ICON_FROM_CROP);
                } else {
                    Log.e(LOG_TAG, "choose photo from album result not ok");
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

        mPrefs.addTrackId(mBluetoothDeviceAddress);
        mPrefs.saveTrackToFile(mBluetoothDeviceAddress, mTrack);
        startService(new Intent(TrackREditActivity.this, BluetoothLeService.class));
        File folder = ensureIconFolder();
        File trackIconFile = new File(folder, mBluetoothDeviceAddress);
        if (trackIconFile.exists()) {
            Log.v(LOG_TAG, "mTrack icon file exist delete it.");
            trackIconFile.delete();
        }


        if (new File(TEMP_ICON_FOR_CROPPED_FILE).renameTo(trackIconFile)) {
            Log.e(LOG_TAG, "Rename to address failed");
        }



        Thread t = new Thread() {
            @Override
            public void run() {
                final BindCommand bindcommand = new BindCommand(String.valueOf(mPrefs.getUid()),
                        name,
                        mBluetoothDeviceAddress,
                        String.valueOf(mTrack.type));
                bindcommand.setPassword(mPrefs.getPassword());
                bindcommand.execTask();
                boolean bindOk = bindcommand.success();
                if(bindOk) {
                    Log.i(LOG_TAG, "Bind mTrack ok.");
                    UpdateTrackImageCommand uploadImageCommand = new UpdateTrackImageCommand(mPrefs.getUid(), mBluetoothDeviceAddress);
                    uploadImageCommand.setPassword(mPrefs.getPassword());
                    uploadImageCommand.execTask();
                    if(uploadImageCommand.success()) {
                        Log.v(LOG_TAG, "upload mTrack photo to server success.");
                    } else {
                        Log.e(LOG_TAG, "upload mTrack photo to server failed.");
                    }
                } else {
                    mPrefs.addNewlyTrackId(mBluetoothDeviceAddress);
                    Log.e(LOG_TAG, "Bind iRrack on Server Error.");
                }
            }
        };
        t.start();
        mBluetoothLeService.clearAfterAddSuccess();
        finish();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


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
                CsstSHImageData.removePhoto(mBluetoothDeviceAddress);
                int drawableId = DrawableIds[mTrack.type];
                mImageView.setImageResource(drawableId);
            }
        });

        mImageSourceDialog = b.create();
        mImageSourceDialog.show();
    }
}
