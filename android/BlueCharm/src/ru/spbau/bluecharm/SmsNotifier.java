/*****************************************************************************************
 * Copyright (c) 2012 A. Korovin, K. Krasheninnikova, M. Krinkin, S. Lazarev, A. Opeykin *
 *                                                                                       *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this  *
 * software and associated documentation files (the "Software"), to deal in the Software *
 * without restriction, including without limitation the rights to use, copy, modify,    *
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to    *
 * permit persons to whom the Software is furnished to do so, subject to the following   *
 * conditions:                                                                           *
 *                                                                                       *
 * The above copyright notice and this permission notice shall be included in all copies *
 * or substantial portions of the Software.                                              *
 *                                                                                       *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,   *
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A         *
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT    *
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF  *
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE  *
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                                         *
 *****************************************************************************************/

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
