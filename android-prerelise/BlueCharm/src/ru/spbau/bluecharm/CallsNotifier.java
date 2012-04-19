package ru.spbau.bluecharm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class CallsNotifier extends BroadcastReceiver {
	public static final String TAG = "CALLS_NOTIFIER";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		IBinder binder = peekService(context, new Intent(context, BlueCharmService.class));
		if (binder != null) {
			Messenger messenger = new Messenger(binder);
			Message msg = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
			try {
				messenger.send(msg);
			} catch (RemoteException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
		} else {
			Log.d(TAG, "BlueCharmService isn't running");
		}
	}
}
