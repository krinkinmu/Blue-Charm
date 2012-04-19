package ru.spbau.bluecharm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BlueCharmActivity extends Activity {
	public static final String DPREFIX = "BLUE_CHARM_ACTIVITY";
    public static final int REQUEST_ENABLE_BT = 1;
	private Messenger mService = null;
	private boolean mBound;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<BluetoothDeviceWrapper> mArrayAdapter;
    private final ArrayList<BluetoothDeviceWrapper> data = new ArrayList<BluetoothDeviceWrapper>();
    private BroadcastReceiver mReceiver;  
    private ListView mListView;
    
  
	private ServiceConnection mConnection = new	ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			mBound = true;
		}
		
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			mBound = false;
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mArrayAdapter = new ArrayAdapter<BluetoothDeviceWrapper>(this, android.R.layout.simple_list_item_checked, data);
        mListView =  (ListView) findViewById(R.id.blueDevices);
        mListView.setAdapter(mArrayAdapter);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
		    if (!mBluetoothAdapter.isEnabled()) {
		        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    }
		    registerAdapter();	    
		}
        
        findViewById(R.id.refresh_button).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.d(DPREFIX, "onClick (refresh button)");

			}
        });
        
        findViewById(R.id.save_button).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.d(DPREFIX, "onClick (save button)");
				setDevices();
			}
        });
        
        findViewById(R.id.exit_button).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.d(DPREFIX, "onClick (exit button)");
				finish();
			}
        });
        
        findViewById(R.id.test_button).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.d(DPREFIX, "onClick (exit button)");
				notifyDevices();
			}
        });
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	bindService(new Intent(this, BlueCharmService.class), mConnection,
    			Context.BIND_AUTO_CREATE);
    }
    
    protected void onStop() {
    	super.onStop();
    	if (mBound) {
    		unbindService(mConnection);
    		mBound = false;
    	}
    }
    
    public void notifyDevices() {
    	if (!mBound) return;
    	Message msg = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
    	try {
    		mService.send(msg);
    	} catch (RemoteException e) {
    		e.printStackTrace();
    	}
    }
    
    public void setDevices() {
    	if (!mBound) return;
    	Message msg = Message.obtain(null, BlueCharmService.MSG_SET_LISTENERS, 0, 0);
    	Set<BluetoothDeviceWrapper> devices = new HashSet<BluetoothDeviceWrapper>();
    	SparseBooleanArray checked = mListView.getCheckedItemPositions();
    	for (int i = 0; i < data.size(); ++i) {
    		if (checked.get(i)) {
    			devices.add(data.get(i));
    		}
    	}
 
    	msg.obj = devices;
    	try {
    		mService.send(msg);
    	} catch (RemoteException e) {
    		e.printStackTrace();
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
                    mArrayAdapter.add(new BluetoothDeviceWrapper(device));
                }
            }
        };
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);   
		
		mBluetoothAdapter.startDiscovery();
	}
}