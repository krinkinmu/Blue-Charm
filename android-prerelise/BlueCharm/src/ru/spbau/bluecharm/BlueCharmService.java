package ru.spbau.bluecharm;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BlueCharmService extends Service {
	public static final String DPREFIX = "BLUE_CHARM_SERVICE";
	public static final int MSG_NOTIFY_LISTENERS = 1;
	public static final int MSG_SET_LISTENERS = 2;
	public static final int MSG_GET_LISTENERS = 3;
	private BroadcastReceiver mReceiver;
	
	final static int SERVER_PORT = 10;
	
	private final String DEVICES_STORAGE_NAME = "blueCharmDevices";
	
	private class IncomingHandler extends Handler {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NOTIFY_LISTENERS:
				Log.d(DPREFIX, "MSG_NOTIFY_LISTENERS recieved");
				notifyDevices();
				break;
			case MSG_SET_LISTENERS:
				Log.d(DPREFIX, "MSG_SET_LISTENERS recieved");
				if (msg.obj instanceof Set) {
					saveDevices((Set<BluetoothDeviceWrapper>) msg.obj);					
				}
				break;
			case MSG_GET_LISTENERS:
				Log.d(DPREFIX, "MSG_GET_LISTENERS recieved");
				break;
			default:
				super.handleMessage(msg);
			}
		}

	}
	
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	@Override
	public void onCreate() {
		mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(DPREFIX, action);
                // When discovery finds a device
//                if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
//                    
//                }
            }
        };
		IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		registerReceiver(mReceiver, filter);   
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(DPREFIX, "Service binded");
		return mMessenger.getBinder();
	}
	
	private void saveDevices(Set<BluetoothDeviceWrapper> set) {
		SharedPreferences devicesStorage = getSharedPreferences(DEVICES_STORAGE_NAME, 0);
		SharedPreferences.Editor editor = devicesStorage.edit();
		editor.clear();
		editor.commit();
		for (BluetoothDeviceWrapper device : set) {
			editor.putString(device.getAddress(), device.getName());
			Log.d(DPREFIX, device.toString());					
		}					
		editor.commit();		
	}
	
	private void notifyDevices() {
		SharedPreferences devicesStorage = getSharedPreferences(DEVICES_STORAGE_NAME, 0);
		Map<String, String> devices = (Map<String, String>) devicesStorage.getAll();
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    	if(!adapter.isEnabled()) {
    		Log.d(DPREFIX, "Adapter is not enabled");
    		return;
    	}
    	
		for (Map.Entry<String, String> device : devices.entrySet()) {
			notifyByMac(device.getKey(), adapter);
		}
	}
	
	private void notifyByMac(String mac, BluetoothAdapter adapter) {
		
    	BluetoothDevice device = adapter.getRemoteDevice(mac);
    		
    	BluetoothSocket socket = null;
    	try {    		
    		Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
    		socket = (BluetoothSocket) m.invoke(device, SERVER_PORT);
    		Log.d(DPREFIX, "socket created");
    	} catch (Exception e) {
    		Log.d(DPREFIX, "Exception: " + e.getMessage());
    		return;
    	}  
    	
    	try {
    		if(!adapter.cancelDiscovery()) {
    			Log.d(DPREFIX, "Didn't cancel discovery.");
    		}
    		socket.connect();
    		Log.d(DPREFIX, "socket connected");
    		try {
    			OutputStream out = socket.getOutputStream();
    			Log.d(DPREFIX, "output created");
    			out.write("SOCKET_TEST".getBytes());
    			Log.d(DPREFIX, "writed");
    			out.close();
    		} catch (IOException e) {
    			Log.d(DPREFIX, "socket IO exception");
    			Log.d(DPREFIX, e.getMessage());
			}    		
    	} catch (IOException e) {
    		Log.d(DPREFIX, "failed to connect to " + device.getAddress());
    		Log.d(DPREFIX, e.getMessage());
    		return;
    	} finally {
        	try {
        		socket.close();
        	} catch (IOException e) {
        		Log.d(DPREFIX, "failed to close socket");
        		Log.d(DPREFIX, e.getMessage());
        		return;
			}
    	}
	}
}
