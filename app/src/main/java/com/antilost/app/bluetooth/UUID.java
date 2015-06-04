package com.antilost.app.bluetooth;

/**
 * Created by Administrator on 2015/1/25.
 */
public class UUID {
    public static final String IMMEDIATE_ALERT_SERVICE_UUID_STRING = "00001802-0000-1000-8000-00805f9b34fb";
    public static final String ALARM_CHARACTERISTIC_UUID_PREFIX = "00002a06";

    public static final String SIMPLE_KEY_SERVICE_UUID_STRING = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String KEY_PRESS_CHARACTERISTIC_UUID_PREFIX = "0000ffe1";

    public static final String BATTERY_SERVICE_UUID_STRING = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String BATTERY_LEVEL_CHARACTERISTIC_UUID_PREFIX = "00002a19";

    public static final String ALERT_LEVEL_CHARACTERISTIC_UUID_PREFIX = "00002a06";

    public static final String CUSTOM_VERIFIED_SERVICE_UUID_PREFIX = "0000fff0";

    public static final java.util.UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID CHARACTERISTIC_KEY_PRESS_UUID = java.util.UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID CHARACTERISTIC_ALERT_LEVEL_UUID = java.util.UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID CHARACTERISTIC_BATTERY_LEVEL_UUID = java.util.UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public static final java.util.UUID LINK_LOSS_SERVICE_UUID = java.util.UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID BATTERY_SERVICE_UUID = java.util.UUID.fromString(BATTERY_SERVICE_UUID_STRING);
    public static final java.util.UUID IMMEDIATE_ALERT_SERVICE_UUID = java.util.UUID.fromString(IMMEDIATE_ALERT_SERVICE_UUID_STRING);

    public static final java.util.UUID CUSTOM_SERVICE = java.util.UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID CHARACTERISTIC_CUSTOM_SYSTEMID = java.util.UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID CHARACTERISTIC_CUSTOM_VERIFIED = java.util.UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID CHARACTERISTIC_PRESSED_FOR_ADD = java.util.UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID CHARACTERISTIC_HARDWARE_VERSION = java.util.UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID CHARACTERISTIC_ANDROID_SYSTEM_FLAG = java.util.UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");



}
