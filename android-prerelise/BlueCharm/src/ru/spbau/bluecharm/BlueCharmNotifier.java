package ru.spbau.bluecharm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;

public abstract class BlueCharmNotifier extends BroadcastReceiver {
    private static final String TAG = "CHARM_NOTIFIER";

    public static final String MAGIC = "BLUECHARM";

    abstract protected String buildMessage(Context context, Intent intent);

    abstract protected String getType(Context context, Intent intent);

    abstract protected boolean isTargetIntent(Context context, Intent intent);

    public static char getDelimiter() {
        return 3;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Intent received: " + intent.getAction());
        Intent service = new Intent(context, BlueCharmService.class);
        IBinder binder = peekService(context, service);
        if (binder != null) {
            if (isTargetIntent(context, intent)) {
                sendMessage(
                        MAGIC + getDelimiter() + getType(context, intent) + getDelimiter() + buildMessage(context, intent),
                        binder);
            }
        } else {
            Log.e(TAG, "BlueCharmService isn't running");
        }
    }
    
    private static void sendMessage(String msg, IBinder binder) {
        Messenger messenger = new Messenger(binder);
        Message message = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString(null, msg);
        message.setData(bundle);
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }
}
