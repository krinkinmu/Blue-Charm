package ru.spbau.bluecharm;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallsNotifier extends BlueCharmNotifier
{
    public static final String TYPE = "IN_CALL";

    @Override
    protected String buildMessage(Context context, Intent intent)
    {
        String origin = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        String name = BlueCharmUtils.getContactName(context, origin);
        if (name != null) {
            origin = name;
        }
        return origin;
    }

    @Override
    protected boolean isTargetIntent(Context context, Intent intent)
    {
        return intent.hasExtra(TelephonyManager.EXTRA_STATE)
            && intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING);
    }

    @Override
    protected String getType(Context context, Intent intent)
    {
        return TYPE;
    }
}