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
	public static final String IN_SMS = "IN_SMS";
	public static final char DELIMETER = 3;
	public static final String CHARM = "BLUECHARM";
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) {
			Log.d(TAG, "Message received");
			Bundle pudsBundle = intent.getExtras();
			Object[] pdus = (Object[]) pudsBundle.get("pdus");
			SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0]);
			String body = message.getDisplayMessageBody();
			String orign = message.getOriginatingAddress();
			Log.d(TAG, "Message: " + body);
			Log.d(TAG, "From: " + orign);
			Intent service = new Intent(context, BlueCharmService.class);
			IBinder binder = peekService(context, service);
			if (binder != null) {
				StringBuilder str = new StringBuilder();
				str.append(CHARM);
				str.append(DELIMETER);
				str.append(IN_SMS);
				str.append(DELIMETER);
				str.append(orign);
				str.append(DELIMETER);
				str.append(body);
				sendMessage(str.toString(), binder);
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
