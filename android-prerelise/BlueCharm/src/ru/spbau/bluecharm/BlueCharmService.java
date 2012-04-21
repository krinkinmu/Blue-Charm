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
    public static final String TAG = "BLUE_CHARM_SERVICE";

    public static final int MSG_NOTIFY_LISTENERS = 1;

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
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service binded");
        return mMessenger.getBinder();
    }

    /**
     * Save list of Bluetooth devices to local base
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
     * Notifies device specified by mac value
     */
    private void notifyByMac(String mac, BluetoothAdapter adapter, String msg) {
        BluetoothDevice device = adapter.getRemoteDevice(mac);
        BluetoothSocket socket = null;
        Log.d(TAG, "Notify: " + mac);

        try {
            /**
             * Bluetooth bug with HTC devices
             * @see: https://github.com/krinkinmu/Blue-Charm/wiki/Socket-opening-on-HTC-devices
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
