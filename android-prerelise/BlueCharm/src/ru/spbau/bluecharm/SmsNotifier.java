package ru.spbau.bluecharm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsNotifier extends BlueCharmNotifier
{
    public static final String TYPE = "IN_SMS";

    public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    protected String buildMessage(Context context, Intent intent)
    {
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0]);
        String origin = message.getOriginatingAddress();
        String name = BlueCharmUtils.getContactName(context, origin);
        if (name != null) {
            origin = name;
        }
        return origin + getDelimiter() + message.getDisplayMessageBody();
    }

    @Override
    protected String getType(Context context, Intent intent)
    {
        return TYPE;
    }

    @Override
    protected boolean isTargetIntent(Context context, Intent intent)
    {
        return intent.getAction().equals(ACTION);
    }
}
