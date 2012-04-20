package ru.spbau.bluecharm;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BlueCharmActivity extends Activity {
	public static final String TAG = "BLUE_CHARM_ACTIVITY";
	public static final int REQUEST_ENABLE_BT = 1;
	
	private final ArrayList<BluetoothDeviceWrapper> mData = new ArrayList<BluetoothDeviceWrapper>();
	private ArrayAdapter<BluetoothDeviceWrapper> mArrayAdapter;
	private ListView mListView;
	
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mReceiver;
    
    private boolean mBound;
	private Messenger mService;
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
        
        final Intent service = new Intent(this, BlueCharmService.class); 
        
    	startService(service);
        
        /* Bind View with Model */
        mArrayAdapter = new SetListAdapter<BluetoothDeviceWrapper>(this, android.R.layout.simple_list_item_checked, mData);
        mListView = (ListView) findViewById(R.id.blueDevices);
        mListView.setAdapter(mArrayAdapter);        
        
		if (prepareAdapter(BluetoothAdapter.getDefaultAdapter())) {
			registerListForFoundedDevices();
			mBluetoothAdapter.startDiscovery();
		}		
        
		/* Set UI event listeners */
        findViewById(R.id.refresh_button).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.d(TAG, "onClick (refresh button)");
				refreshListView();
			}
        });
        
        findViewById(R.id.exit_button).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.d(TAG, "onClick (exit button)");
				stopService(service);
				finish();
			}
        });
        
        findViewById(R.id.test_button).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.d(TAG, "onClick (test button)");
				notifyDevices();
			}
        });
        
        mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				setDevices();
			}
        }); 
    }
    
    private void refreshListView() {
		if (prepareAdapter(BluetoothAdapter.getDefaultAdapter())) {
			mBluetoothAdapter.cancelDiscovery();
			mArrayAdapter.clear();
			renewChoices();
			mBluetoothAdapter.startDiscovery();
		}
	}

    private void renewChoices() {
		mListView.clearChoices();
    }
    
	@Override
    protected void onStart() {
    	super.onStart();
    	/* Bind to BlueCharService */
    	bindService(new Intent(this, BlueCharmService.class), mConnection,
    			Context.BIND_AUTO_CREATE);
    	mBound = true;
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	if (mBound) {
    		/* Unbind from BlueCharmService */
    		unbindService(mConnection);
    		mBound = false;
    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	/* Free broadcast receiver */
    	if (mReceiver != null) {
    		unregisterReceiver(mReceiver);
    	}
    }
  
    /* Test method initiates Bluetooth notification */
    private void notifyDevices() {
    	if (!mBound) return;
    	
    	Message msg = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
    	Bundle bundle = new Bundle();
    	bundle.putString(null, mBluetoothAdapter.getName());
    	msg.setData(bundle);
    	try {
    		mService.send(msg);
    	} catch (RemoteException e) {
    		e.printStackTrace();
    	}
    }
    
    /* Save user choice */
    private void setDevices() {
    	if (!mBound) return;
    	
    	Message msg = Message.obtain(null, BlueCharmService.MSG_SET_LISTENERS, 0, 0);
    	ArrayList<String> devices = new ArrayList<String>();
    	SparseBooleanArray checked = mListView.getCheckedItemPositions();
    	for (int i = 0; i < mData.size(); ++i) {
    		if (checked.get(i)) {
    			devices.add(mData.get(i).toString());
    		}
    	}
 
    	Bundle bundle = new Bundle();
    	bundle.putStringArrayList(null, devices);
    	msg.setData(bundle);
    	try {
    		mService.send(msg);
    	} catch (RemoteException e) {
    		e.printStackTrace();
    	}
    }
    
    private boolean prepareAdapter(BluetoothAdapter adapter) {
        /* Prepare Bluetooth device */        
	    mBluetoothAdapter = adapter;
		if (mBluetoothAdapter != null) {
		    if (!mBluetoothAdapter.isEnabled()) {
		        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    }
		    return true;	    
		}
		return false;
    }
    
	private void registerListForFoundedDevices() {
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
	}
	
	private class SetListAdapter<T> extends ArrayAdapter<T> {
		public SetListAdapter(Context context, int textViewResourceId, List<T> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public void add(T t) {
			if (!contains(t)) {
				super.add(t);
			}
		}
		
		private boolean contains(T t) {
			for (int i = 0; i < this.getCount(); ++i) {
				if (t.equals(this.getItem(i))) {
					return true;
				}
			}
			return false;
		}
	}
}