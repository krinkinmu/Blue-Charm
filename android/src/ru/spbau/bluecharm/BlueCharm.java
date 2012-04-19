package ru.spbau.bluecharm;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

@SuppressWarnings("unused")
public class BlueCharm extends Activity {
    public static final int REQUEST_ENABLE_BT = 1;
    private final ArrayList<String> data = new ArrayList<String>();
    private ArrayAdapter<String> mArrayAdapter;
    private BroadcastReceiver mReceiver;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, data);
        ((ListView) findViewById(R.id.blueDevices)).setAdapter(mArrayAdapter);
    }
	
	@Override
	protected void onStart() {
		super.onStart();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
        	int responseEnableBluetooth;
	        if (!bluetoothAdapter.isEnabled()) {
	            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	        }
	        registerAdapter(bluetoothAdapter);
        } else {
        	// TODO: notify user he is stupid 
        }
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode != RESULT_OK) {
				Log.d("BLUETOOTH", "Bluetooth didn't turn on: " + resultCode);		
				// TODO: Die if bluetooth didn't turn on
			}
			break;			
		default:
			Log.d("APPLICATION", "Unhandled request code: " + requestCode);		
			break;
		}
	}
	
	private void registerAdapter(BluetoothAdapter mBluetoothAdapter) {
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
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy      
		
		mBluetoothAdapter.startDiscovery();
	}
	
	public void onDestroy(Bundle savedInstanceState) {
		super.onDestroy();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
	}
}