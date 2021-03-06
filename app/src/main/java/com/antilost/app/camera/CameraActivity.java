package com.antilost.app.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.service.BluetoothLeService;

import java.util.List;

public class CameraActivity extends FragmentActivity implements Camera.ErrorCallback {


    private static final String LOG_TAG = "CameraActivity";
    private BroadcastReceiver mReceiver = new  BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(BluetoothLeService.ACTION_DEVICE_CLICKED.equals(intent.getAction())) {
                tryTakePicture();
            };
        }
    };
    private Camera mCamera;
    private CameraPreview mPreview;

    private   Camera.PictureCallback mJpegPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] bytes, android.hardware.Camera camera) {
            Log.i(LOG_TAG, "bytes size is " + bytes.length);
            mTakingPhoto = false;
            PhotoSaver.save(bytes, CameraActivity.this);
        };
    };

    private Camera.AutoFocusCallback mAutofocusListener  = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean b, Camera camera) {

                Log.v(LOG_TAG, "onAutoFocus callback, focus result " + b);
                camera.setParameters(mParameters);
                doSnap();
        }
    };

    private boolean mTakingPhoto;
    private Camera.Parameters mParameters;


    private void tryTakePicture() {
        if(mTakingPhoto) {
            Log.w(LOG_TAG, "taking photo already triggered,");
            return;
        }
        mTakingPhoto = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(supportAutoFocus()) {
                        Log.v(LOG_TAG, "camera support auto focus");
                        mCamera.autoFocus(mAutofocusListener);
                    } else {
                        doSnap();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean supportAutoFocus() {
        List<String> focusModes = mParameters.getSupportedFocusModes();
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            return true;
        }
        return false;
    }

    private void doSnap() {
        List<Camera.Size> sizes = mParameters.getSupportedPictureSizes();
        Camera.Size maxsize = sizes.get(0);

        for(Camera.Size size : sizes) {
            if(size.width > maxsize.width) {
                maxsize = size;
            }
        }
        mParameters.setPictureSize(maxsize.width, maxsize.height);
        mCamera.setParameters(mParameters);
        mCamera.takePicture(null, null, mJpegPictureCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }

    private void setupCamera() {
        // Create an instance of Camera
        mCamera = getCameraInstance();

        if(mCamera == null) {
            Toast.makeText(this, "Open Camera failed.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mCamera.setErrorCallback(this);
        mParameters = mCamera.getParameters();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();
        preview.addView(mPreview, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(BluetoothLeService.ACTION_DEVICE_CLICKED));
        setupCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        closeCamera();
    }

    private void closeCamera() {
        if(mCamera != null) {
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     *  Check if this device has a camera
     *
     *  */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }

    }


    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    @Override
    public void onError(int i, Camera camera) {

    }
}
