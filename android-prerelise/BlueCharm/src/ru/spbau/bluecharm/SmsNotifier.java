package ru.spbau.bluecharm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsNotifier extends BroadcastReceiver { 
	public static final String TAG = "SMS_NOTIFIER";
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) {
			Log.d(TAG, "Message received");
			Bundle pudsBundle = intent.getExtras();
			Object[] pdus = (Object[]) pudsBundle.get("pdus");
			SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0]);
			String body = "Mesage: " + message.getDisplayMessageBody();
			String orign = "From: " + message.getOriginatingAddress();
			Log.d(TAG, body);
			Intent service = new Intent(context, BlueCharmService.class);
			IBinder binder = peekService(context, service);
			if (binder != null) {
				sendMessage(body + "\n" + orign , binder);
			} else {
				Log.d(TAG, "BlueCharmService isn't running");
			}
		}
	}
	
	private void sendMessage(String sms, IBinder binder) {
		Messenger messenger = new Messenger(binder);
		Message msg = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
		Bundle bundle = new Bundle();
		bundle.putString(null, sms);
		msg.setData(bundle);
		try {
			messenger.send(msg);
		} catch (RemoteException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
}
