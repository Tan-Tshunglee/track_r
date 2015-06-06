package com.antilost.app.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.antilost.app.R;
import com.antilost.app.prefs.PrefsManager;
import com.antilost.app.service.BluetoothLeService;
import com.antilost.app.view.DotsMarquee;

public class ScanTrackActivity extends Activity implements View.OnClickListener {

    public static final int MSG_SHOW_CONNECTING_PAGE = 1;
    public static final int MSG_SHOW_SCAN_FAILED_PAGE = 2;
    public static final int MSG_SHOW_SCANNING_PAGE = 3;

    public static final int MAX_TRACK_SUPPORT_COUNT = 5;

    private static final String LOG_TAG = "ScanTrackActivity";
    public static final int MIN_RSSI_ACCEPTABLE = -60;
    public static final int SCAN_TIMEOUT = 30 * 1000;


    private RelativeLayout mFirstPage;
    private RelativeLayout mConnectingPage;
    private RelativeLayout mFailedPage;
    private ImageButton mBackBtn;
    private Button mTryAgain;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private PrefsManager mPrefsManager;

    // Device scan callback.
    private ImageView mSearchingIcon;
    private Animation mSearchingAnimation;
    private Handler mHandler;
    private boolean mDeviceScanSuccess;

    private void log(String s) {
        Log.d(LOG_TAG, s);
    }

    private DotsMarquee mBindingDotsMarquee;
    private DotsMarquee mConnecingDotsMarquee;

    private BluetoothLeService mBluetoothLeService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            scanLeDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void startTrackEdit() {
        mDeviceScanSuccess = true;
        Intent i = new Intent(ScanTrackActivity.this, TrackREditActivity.class);
        i.putExtra(TrackREditActivity.EXTRA_EDIT_NEW_TRACK, true);
        i.putExtra(TrackREditActivity.BLUETOOTH_ADDRESS_BUNDLE_KEY, mBluetoothLeService.getAddingDeviceAddress());
        startActivity(i);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        mBluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.your_device_no_ble_support), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mPrefsManager = PrefsManager.singleInstance(this);

        if (mPrefsManager.getTrackIds().size() == MAX_TRACK_SUPPORT_COUNT) {
            Toast.makeText(this, getString(R.string.max_device_limit_reached), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_binding);
        mFirstPage = (RelativeLayout) findViewById(R.id.firstPage);
        mConnectingPage = (RelativeLayout) findViewById(R.id.connectingPage);
        mFailedPage = (RelativeLayout) findViewById(R.id.failedPage);
        mFirstPage.setVisibility(View.VISIBLE);
        mConnectingPage.setVisibility(View.GONE);
        mFailedPage.setVisibility(View.GONE);
        mBackBtn = (ImageButton) findViewById(R.id.backBtn);
        mBackBtn.setOnClickListener(this);

        mTryAgain = (Button) findViewById(R.id.tryAgain);
        mTryAgain.setOnClickListener(this);

        mSearchingIcon = (ImageView) findViewById(R.id.imageViewSearchIcon);
        mBindingDotsMarquee = (DotsMarquee) findViewById(R.id.bindingDotsMarquee);
        mConnecingDotsMarquee = (DotsMarquee) findViewById(R.id.connectingDotsMarquee);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.v(LOG_TAG, "handling Msg " + msg.toString());
                switch (msg.what) {
                    case MSG_SHOW_CONNECTING_PAGE:
                        mFirstPage.setVisibility(View.GONE);
                        mConnectingPage.setVisibility(View.VISIBLE);
                        mFailedPage.setVisibility(View.GONE);
                        break;
                    case MSG_SHOW_SCAN_FAILED_PAGE:

                        if(isFinishing()) {
                            return;
                        }
                        if(mBluetoothLeService != null) {
                            mBluetoothLeService.giveUpConnectNewTrack();
                        }
                        mFirstPage.setVisibility(View.GONE);
                        mConnectingPage.setVisibility(View.GONE);
                        mFailedPage.setVisibility(View.VISIBLE);
                        break;
                    case MSG_SHOW_SCANNING_PAGE:
                        mFirstPage.setVisibility(View.VISIBLE);
                        mConnectingPage.setVisibility(View.GONE);
                        mFailedPage.setVisibility(View.GONE);
                        break;
                }
            }
        };

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothLeService != null) {
            unbindService(mServiceConnection);
        }
    }



    private void scanLeDevice() {

        mBluetoothLeService.startBleScanForAdd(new BluetoothLeService.ScanResultListener() {
            @Override
            public void onFailure() {
                Log.e(LOG_TAG, "Scan or connection failed");
                mHandler.sendEmptyMessage(MSG_SHOW_SCAN_FAILED_PAGE);
            }

            @Override
            public void onConnectionStart() {
                mHandler.sendEmptyMessage(MSG_SHOW_CONNECTING_PAGE);
            }

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startTrackEdit();
                    }
                });
            }
        });
        mHandler.sendEmptyMessage(MSG_SHOW_SCANNING_PAGE);
        mHandler.sendEmptyMessageDelayed(MSG_SHOW_SCAN_FAILED_PAGE, SCAN_TIMEOUT);

    }

    private void startSearchingAnimation() {
        if(mSearchingAnimation == null) {
            mSearchingAnimation = AnimationUtils.loadAnimation(this, R.anim.searching);
        }
        mSearchingIcon.startAnimation(mSearchingAnimation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBindingDotsMarquee.startMarquee();
        mConnecingDotsMarquee.startMarquee();
        startSearchingAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBindingDotsMarquee.stopMarquee();
        mConnecingDotsMarquee.stopMarquee();

        if(!mDeviceScanSuccess) {
            if(mBluetoothLeService != null) {
                mBluetoothLeService.giveUpConnectNewTrack();
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.tryAgain:
                mBluetoothLeService.giveUpConnectNewTrack();
                scanLeDevice();
                break;
        }
    }
}
