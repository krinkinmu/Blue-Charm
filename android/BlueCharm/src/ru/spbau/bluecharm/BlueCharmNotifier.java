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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;

/**
 * Abstract notifier which contains notifying logic.
 */
public abstract class BlueCharmNotifier extends BroadcastReceiver {
    private static final String TAG = "CHARM_NOTIFIER";

    /**
     * Magic word. Prefix for each bluetooth message to server.
     */
    public static final String MAGIC = "BLUECHARM";

    /**
     * Message that will be sent to server
     *
     * @param context Context
     * @param intent  Intent
     * @return Message
     */
    abstract protected String buildMessage(Context context, Intent intent);

    /**
     * Type of notification. Second parameter of packet.
     *
     * @param context Context
     * @param intent  Intent
     * @return Type
     */
    abstract protected String getType(Context context, Intent intent);

    /**
     * Condition for defining target intent.
     *
     * @param context Context
     * @param intent  Intent
     * @return True if we want to send message.
     */
    abstract protected boolean isTargetIntent(Context context, Intent intent);

    /**
     * Char used to glue parameters of packet.
     *
     * @return Delimiter char code.
     */
    public static char getDelimiter() {
        return 3;
    }

    /**
     * Actions performed when some notification needed.
     *
     * @param context Context
     * @param intent  Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Intent received: " + intent.getAction());
        Intent service = new Intent(context, BlueCharmService.class);
        IBinder binder = peekService(context, service);
        if (binder != null) {
            if (isTargetIntent(context, intent)) {
                sendMessage(MAGIC + getDelimiter() + getType(context, intent) + getDelimiter()
                        + buildMessage(context, intent), binder);
            }
        } else {
            Log.e(TAG, "BlueCharmService isn't running");
        }
    }

    private static void sendMessage(String msg, IBinder binder) {
        Messenger messenger = new Messenger(binder);
        Message message = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString(null, msg);
        message.setData(bundle);
        try {
            messenger.send(message);
        } catch (RemoteException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }
}
