package ru.spbau.bluecharm;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ArrayAdapter;
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