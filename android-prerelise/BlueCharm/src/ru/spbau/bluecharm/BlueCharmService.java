package ru.spbau.bluecharm;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

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

public class BlueCharmService extends Service {
	public static final String TAG = "BLUE_CHARM_SERVICE";
	public static final int MSG_NOTIFY_LISTENERS = 1;
	public static final int MSG_SET_LISTENERS = 2;
	public static final int MSG_GET_LISTENERS = 3;
	public static final int SERVER_PORT = 10;
	public static final String DEVICES_STORAGE_NAME = "blueCharmDevices";
	
	/* Handles incoming Intents (Messages) */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NOTIFY_LISTENERS:
				Log.d(TAG, "MSG_NOTIFY_LISTENERS recieved");
				String line = msg.getData().getString(null);
				notifyDevices(line);
				break;
			case MSG_SET_LISTENERS:
				Log.d(TAG, "MSG_SET_LISTENERS recieved");
				ArrayList<String> list = msg.getData().getStringArrayList(null);
				Log.d(TAG, "Number of listeners: " + list.size());
				saveDevices(list);
				break;
			case MSG_GET_LISTENERS:
				Log.d(TAG, "MSG_GET_LISTENERS recieved");
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Service binded");
		return mMessenger.getBinder();
	}
	
	private void saveDevices(ArrayList<String> list) {
		SharedPreferences devicesStorage = getSharedPreferences(DEVICES_STORAGE_NAME, 0);
		SharedPreferences.Editor editor = devicesStorage.edit();
		editor.clear();
		editor.commit();
		for (String device : list) {
			BluetoothDeviceWrapper wrapper = new BluetoothDeviceWrapper(device);
			editor.putString(wrapper.getAddress(), wrapper.getName());
			Log.d(TAG, wrapper.toDataString());          			
		}					
		editor.commit();
		Log.d(TAG, "Changes commited");
	}
	
	@SuppressWarnings("unchecked")
	private void notifyDevices(String msg) {
		SharedPreferences devicesStorage = getSharedPreferences(DEVICES_STORAGE_NAME, 0);
		Map<String, String> devices = (Map<String, String>) devicesStorage.getAll();
		
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    	if(!adapter.isEnabled()) {
    		Log.d(TAG, "Bluetooth adapter isn't accessiable");
    		return;
    	}
    	
		for (Map.Entry<String, String> device : devices.entrySet()) {
			notifyByMac(device.getKey(), adapter, msg);
		}
	}
	
	/* Notifies device specified by mac value */
	private void notifyByMac(String mac, BluetoothAdapter adapter, String msg) {		
    	BluetoothDevice device = adapter.getRemoteDevice(mac);	
    	BluetoothSocket socket = null;
    	Log.d(TAG, "Notify: " + mac);

    	try {    		
    		Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
    		socket = (BluetoothSocket) m.invoke(device, SERVER_PORT);
    		Log.d(TAG, "Socket created");
    	} catch (Exception e) {
    		Log.e(TAG, e.getLocalizedMessage());
    	}  
    	
    	if (socket != null) {
	    	try {
	    		if(!adapter.cancelDiscovery()) {
	    			Log.d(TAG, "Bluetooth device discovery wasn't canceled");
	    		}
	    		socket.connect();
	    		Log.d(TAG, "Socket connected");
	    		try {
	    			OutputStream out = socket.getOutputStream();
	    			Log.d(TAG, "Output stream created");
	    			out.write(msg.getBytes());
	    			Log.d(TAG, "Message sent: " + msg);
	    			out.close();
	    			Log.d(TAG, "Output stream closed");
	    		} catch (IOException e) {
	    			Log.e(TAG, e.getLocalizedMessage());
				}    		
	    	} catch (IOException e) {
	    		Log.d(TAG, "Failed to connect to " + mac);
	    		Log.e(TAG, e.getLocalizedMessage());
	    	} finally {
	        	try {
	        		socket.close();
	        	} catch (IOException e) {
	        		Log.d(TAG, "Failed to close socket");
	        		Log.e(TAG, e.getLocalizedMessage());
				}
	    	}
    	}
	}
}
