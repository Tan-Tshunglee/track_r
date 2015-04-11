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
    private BluetoothGatt mBluetoothGatt;
    private TextView mTitleTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothDeviceAddress = getIntent().getStringExtra(BLUETOOTH_ADDRESS_BUNDLE_KEY);
        mEditNewTrack = getIntent().getBooleanExtra(EXTRA_EDIT_NEW_TRACK, false);

        if(mEditNewTrack) {
            if(ScanTrackActivity.sBluetoothConnected != null) {
                mBluetoothGatt = ScanTrackActivity.sBluetoothConnected;
                ScanTrackActivity.sBluetoothConnected = null;
            }
        }

        if(mBluetoothGatt == null) {
            Toast.makeText(this, "Try to edit an null connected gatt", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        mDeviceIconTempFile = CsstSHImageData.deviceIconTempFile();

        Uri customIconUri = CsstSHImageData.getIconImageUri(mBluetoothDeviceAddress);

        if(customIconUri != null) {
//            mImageView.setImageURI(customIconUri);
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
        startService(new Intent(this, BluetoothLeService.class));

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
                            return;
                        };
//                        mImageView.setImageBitmap(source);
                        mImageView.setImageBitmap(CsstSHImageData.toRoundCorner(source));
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

        mPrefs.addTrackId(mBluetoothDeviceAddress);
        mPrefs.saveTrackToFile(mBluetoothDeviceAddress, mTrack);

        if(mBluetoothLeService != null) {
            BluetoothDevice device = mBluetoothGatt.getDevice();
            if(device == null) {
                Log.e(LOG_TAG, "mBluetoothGatt device get device is null.");
                return;
            }
            mBluetoothLeService.addNewTrack(mBluetoothGatt);
            mBluetoothGatt = null;
        }

        Thread t = new Thread() {
            @Override
            public void run() {
                final BindCommand bindcommand = new BindCommand(String.valueOf(mPrefs.getUid()),
                        name,
                        mBluetoothDeviceAddress,
                        String.valueOf(mTrack.type));

                bindcommand.execTask();
                boolean bindOk = bindcommand.success();
                if(bindOk) {
                    Log.i(LOG_TAG, "Bind track ok.");
                    startService(new Intent(TrackREditActivity.this, BluetoothLeService.class));
                    UpdateTrackImageCommand command = new UpdateTrackImageCommand(mPrefs.getUid(), mBluetoothDeviceAddress);
                    command.execTask();
                    if(command.success()) {
                        Log.v(LOG_TAG, "upload track photo to server success.");
                    } else {
                        Log.e(LOG_TAG, "upload track photo to server failed.");
                    }
                } else {
                    Log.e(LOG_TAG, "Bind RrackR on Server Error.");
                }
            }
        };
        t.start();

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
