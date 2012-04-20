package ru.spbau.bluecharm;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallsNotifier extends BroadcastReceiver {
	public static final String TAG = "CALLS_NOTIFIER";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, BlueCharmService.class);
		IBinder binder = peekService(context, service);
		if (binder != null) {
			sendMessage(intent, binder);
		} else {
			Log.d(TAG, "Cannot to bind, start service");
			ComponentName name = context.startService(service);
			Log.d(TAG, "Service starting status: " + name);
			binder = peekService(context, service);
			if (binder != null) {
				sendMessage(intent, binder);
			} else {
				Log.d(TAG, "BlueCharmService isn't running");
			}
			context.stopService(service);
		}
	}
	
	private void sendMessage(Intent intent, IBinder binder) {
		Log.d(TAG, "Intent received: " + intent.getStringExtra(TelephonyManager.EXTRA_STATE));
		if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			Messenger messenger = new Messenger(binder);
			Message msg = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
			Bundle bundle = new Bundle();
			bundle.putString(null, "Call: " + intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
			msg.setData(bundle);
			try {
				messenger.send(msg);
			} catch (RemoteException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
		}
	}
}
