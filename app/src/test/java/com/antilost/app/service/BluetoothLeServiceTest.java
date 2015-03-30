package com.antilost.app.service;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class BluetoothLeServiceTest extends ServiceTestCase<BluetoothLeService> {

    private String mAddress;
    private BluetoothLeService mService;

    public BluetoothLeServiceTest() {
        super(BluetoothLeService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mService = getService();
    }

    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    @SmallTest
    public void testPreconditions() {
        mAddress = "00:01:02:03:04:05";
    }

    /**
     * Test basic startup/shutdown of Service
     */
    @SmallTest
    public void testStartable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), BluetoothLeService.class);
        startService(startIntent);
    }

    /**
     * Test binding to service
     */
    @MediumTest
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), BluetoothLeService.class);
        IBinder service = bindService(startIntent);
    }

}
