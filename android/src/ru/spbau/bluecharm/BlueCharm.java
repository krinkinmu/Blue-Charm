package ru.spbau.bluecharm;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


public class BlueCharm extends Activity {
    public static final int REQUEST_ENABLE_BT = 1;
    private final ArrayList<String> data = new ArrayList<String>();
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mArrayAdapter;
    private BroadcastReceiver mReceiver;
    private Messenger mService;
    
    private ServiceConnection mConnection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
		}

		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
    };
    private final String DEVICES_STORAGE_NAME = "testName";
    
	@Override
    public void onCreate(Bundle savedInstanceState) {	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, data);
        ((ListView) findViewById(R.id.blueDevices)).setAdapter(mArrayAdapter);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
		    if (!mBluetoothAdapter.isEnabled()) {
		        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    }
		    registerAdapter();
		    startService(new Intent(this, BlueCharmService.class));
		    bindService(new Intent(this, BlueCharmService.class), mConnection, Context.BIND_AUTO_CREATE);
		    try {
		    	Message msg = Message.obtain(null, BlueCharmService.NOTIFY);
		    	msg.obj = "Hello";
				mService.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		SharedPreferences devicesStorage = getSharedPreferences(DEVICES_STORAGE_NAME, 0);
//		SharedPreferences.Editor editor = devicesStorage.edit();
//		editor.putString("name", "value");
//		
//		editor.commit();
	
		Map<String, String> devices = (Map<String, String>) devicesStorage.getAll();
		for (Map.Entry<String, String> device : devices.entrySet()) {
			mArrayAdapter.add(device.getKey() + device.getValue());
		}
		
		final Button testButton = (Button) findViewById(R.id.Test);
		testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	String mac = "B4:82:FE:3A:09:81"; // Doredox-pc
            	//String mac = "50:63:13:F0:BC:0A"; // Sanya 
            	//String mac = "BC:77:37:A3:E3:36";
            	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
            	
//            	mArrayAdapter.add(device.getAddress());

            	BluetoothSocket socket = null;
            	final int serverPort = 10;
            	try {
            		Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            		socket = (BluetoothSocket) m.invoke(device, serverPort);
            		Log.d("BLUETOOTH", "socket created");
            	} catch (Exception e) {
            		Log.d("BLUETOOTH", "Exception: " + e.getMessage());
            		return;
            	}
            	
            	try {
            		if(!mBluetoothAdapter.cancelDiscovery()) {
            			Log.d("BLUETOOTH", "Cannot cancel discovery.");
            		}
            		socket.connect();
            		Log.d("BLUETOOTH", "socket connected");
            		try {
            			OutputStream out = socket.getOutputStream();
            			Log.d("BLUETOOTH", "output created");
            			out.write("SOCKET_TEST".getBytes());
            			Log.d("BLUETOOTH", "writed");
            			out.close();
            		} catch (IOException e) {
            			Log.d("BLUETOOTH", "socket IO exception");
            			Log.d("BLUETOOTH", e.getMessage());
					}
            	} catch (IOException e) {
            		Log.d("BLUETOOTH", "failed to connect to " + device.getAddress());
            		Log.d("BLUETOOTH", e.getMessage());
            		return;
            	} finally {
                	try {
                		socket.close();
                	} catch (IOException e) {
                		Log.d("BLUETOOTH", "failed to close socket");
                		Log.d("BLUETOOTH", e.getMessage());
                		return;
    				}
            	}
            }
        });
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterAdapter();
		unbindService(mConnection);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode != RESULT_OK) {
				Log.d("BLUETOOTH", "Bluetooth didn't turn on: " + resultCode);
				finish();
			}
			break;			
		default:
			Log.d("APPLICATION", "Unhandled request code: " + requestCode);		
			break;
		}
	}
	
	private void registerAdapter() {
        // Create a BroadcastReceiver for ACTION_FOUND
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);   
		
		mBluetoothAdapter.startDiscovery();
	}
	
	private void unregisterAdapter() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
	}
}
