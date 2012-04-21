package ru.spbau.bluecharm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class BluetoothAdapterUtils {

    /**
     * Request constant for enabling BT
     */
    public static final int REQUEST_ENABLE_BT = 1;


    public static void prepare(Activity activity, BluetoothAdapter adapter) {
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
}
