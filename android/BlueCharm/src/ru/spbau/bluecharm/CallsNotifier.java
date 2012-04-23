package ru.spbau.bluecharm;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * Incoming calls notifier
 */
public class CallsNotifier extends BlueCharmNotifier {
    private static final String TYPE = "IN_CALL";

    /**
     * Message that will be sent to server when incoming call received
     *
     * @param context Context
     * @param intent  Intent
     * @return Message
     */
    @Override
    protected String buildMessage(Context context, Intent intent) {
        String origin = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        String name = BlueCharmUtils.getContactName(context, origin);
        if (name != null) {
            origin = name;
        }
        return origin;
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
        return intent.hasExtra(TelephonyManager.EXTRA_STATE)
                && intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING);
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
}
