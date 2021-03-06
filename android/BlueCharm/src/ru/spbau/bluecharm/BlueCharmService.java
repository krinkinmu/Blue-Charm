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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

public class BlueCharmService extends Service {
    private static final String TAG = "BLUE_CHARM_SERVICE";

    /**
     * Notify message type
     */
    public static final int MSG_NOTIFY_LISTENERS = 1;

    /**
     * Save devices message type
     */
    public static final int MSG_SET_LISTENERS = 2;

    private static final int SERVER_CHANNEL = 10;

    /**
     * Handles incoming Intents (Messages)
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NOTIFY_LISTENERS:
                    Log.d(TAG, "MSG_NOTIFY_LISTENERS received");
                    String line = msg.getData().getString(null);
                    notifyDevices(line);
                    break;
                case MSG_SET_LISTENERS:
                    Log.d(TAG, "MSG_SET_LISTENERS received");
                    ArrayList<String> list = msg.getData().getStringArrayList(null);
                    Log.d(TAG, "Number of listeners: " + list.size());
                    saveDevices(list);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Called when application binds to Service
     *
     * @param intent Intent
     * @return Binder to send messages to service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service binded");
        return mMessenger.getBinder();
    }

    /**
     * Save list of Bluetooth devices to local data base
     *
     * @param list List of devices(string representation)
     */
    private void saveDevices(ArrayList<String> list) {
        SharedPreferences devicesStorage = getSharedPreferences(BluetoothDeviceList.DEVICES_STORAGE_NAME, 0);
        SharedPreferences.Editor editor = devicesStorage.edit();
        editor.clear();
        editor.commit();
        for (String device : list) {
            BluetoothDeviceWrapper wrapper = new BluetoothDeviceWrapper(device);
            editor.putString(wrapper.getAddress(), wrapper.getName());
            Log.d(TAG, wrapper.toDataString());
        }
        editor.commit();
        Log.d(TAG, "Changes committed");
    }

    /**
     * Notifies devices saved in the local base
     *
     * @param msg Message to send.
     */
    @SuppressWarnings("unchecked")
    private void notifyDevices(String msg) {
        SharedPreferences devicesStorage = getSharedPreferences(BluetoothDeviceList.DEVICES_STORAGE_NAME, 0);
        Map<String, String> devices = (Map<String, String>) devicesStorage.getAll();

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            Log.e(TAG, "Bluetooth adapter isn't accessiable");
            return;
        }

        for (Map.Entry<String, String> device : devices.entrySet()) {
            notifyByMac(device.getKey(), adapter, msg);
        }
    }

    /**
     * Sends message to device specified by mac address
     *
     * @param mac     MAC address of bluetooth device
     * @param adapter Bluetooth adapter
     * @param msg     Message
     */
    private void notifyByMac(String mac, BluetoothAdapter adapter, String msg) {
        BluetoothDevice device = adapter.getRemoteDevice(mac);
        BluetoothSocket socket = null;
        Log.d(TAG, "Notify: " + mac);

        try {
            /**
             * Bluetooth bug with HTC devices
             * @see: https://github.com/krinkinmu/Blue-Charm/issues/17
             */
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            socket = (BluetoothSocket) m.invoke(device, SERVER_CHANNEL);
            Log.d(TAG, "Socket created");
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        if (socket != null) {
            try {
                if (!adapter.cancelDiscovery()) {
                    Log.d(TAG, "Bluetooth device discovery wasn't canceled");
                }
                socket.connect();
                Log.d(TAG, "Socket connected");
                try {
                    OutputStream out = socket.getOutputStream();
                    out.write(msg.getBytes());
                    Log.d(TAG, "Message sent: " + msg);
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to connect to " + mac);
                Log.e(TAG, e.getLocalizedMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close socket");
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }
    }
}
