package ru.spbau.bluecharm;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsNotifier extends BlueCharmNotifier { 
	public static final String IN_SMS = "IN_SMS";
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	protected String buildMessage(Context context, Intent intent) {
		Bundle pudsBundle = intent.getExtras();
		Object[] pdus = (Object[]) pudsBundle.get("pdus");
		SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0]);
		return message.getOriginatingAddress() + getDelimiter() + message.getDisplayMessageBody();
	}

	@Override
	protected String getType(Context context, Intent intent) {
		return IN_SMS;
	}

	@Override
	protected boolean isTargetIntent(Context context, Intent intent) {
		return intent.getAction().equals(ACTION);
	}
}
