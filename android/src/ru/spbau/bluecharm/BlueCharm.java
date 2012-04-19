package ru.spbau.bluecharm;

import java.io.IOException;
import java.io.OutputStream;
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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

@SuppressWarnings("unused")
public class BlueCharm extends Activity {
    public static final int REQUEST_ENABLE_BT = 1;
    private final ArrayList<String> data = new ArrayList<String>();
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mArrayAdapter;
    private BroadcastReceiver mReceiver;
    private final String DEVICES_STORAGE_NAME = "testName";
    
	@Override
    public void onCreate(Bundle savedInstanceState) {	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, data);
        ((ListView) findViewById(R.id.blueDevices)).setAdapter(mArrayAdapter);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			int responseEnableBluetooth;
		    if (!mBluetoothAdapter.isEnabled()) {
		        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    }
		    registerAdapter();
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
            	String mac = "123";
            	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
            	UUID uuid = UUID.randomUUID();
            	BluetoothSocket socket;
            	try {
            		socket = device.createRfcommSocketToServiceRecord(uuid);
            	} catch (IOException e) {
            		Log.d("BLUETOOTH", "failed to create socket");
            		Log.d("BLUETOOTH", e.getMessage());
            		return; // TODO: deal with it
            	}
            	
            	try {
            		socket.connect();
            		try {
            			OutputStream out = socket.getOutputStream();
            			out.write("SOCKET_TEST".getBytes());
            		} catch (IOException e) {
            			Log.d("BLUETOOTH", "socket IO exception");
            			Log.d("BLUETOOTH", e.getMessage());
					}
            	} catch (IOException e) {
            		Log.d("BLUETOOTH", "failed to connect to " + device.getName());
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