package ru.spbau.bluecharm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {
    public static final String TAG = "BOOTUP_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Bootup receiver hits onReceive.");
        Intent i = new Intent(context, BlueCharmService.class);
        context.startService(i);
    }
}
