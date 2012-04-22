package ru.spbau.bluecharm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Incoming sms notifier
 */
public class SmsNotifier extends BlueCharmNotifier {
    public static final String TYPE = "IN_SMS";

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    /**
     * Message that will be sent to server when incoming sms received
     *
     * @param context Context
     * @param intent  Intent
     * @return Message
     */
    @Override
    protected String buildMessage(Context context, Intent intent) {
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

    /**
     * Type of notification. Second parameter of packet.
     *
     * @param context Context
     * @param intent  Intent
     * @return Type
     */
    @Override
    protected String getType(Context context, Intent intent) {
        return TYPE;
    }

    /**
     * Condition for defining target intent.
     *
     * @param context Context
     * @param intent  Intent
     * @return True if we want to send message.
     */
    @Override
    protected boolean isTargetIntent(Context context, Intent intent) {
        return intent.getAction().equals(ACTION);
    }
}
