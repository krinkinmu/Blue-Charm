package ru.spbau.bluecharm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

public class BlueCharmActivity extends Activity {
    /**
     * Debugging tag symbol
     */
    public static final String TAG = "BLUE_CHARM_ACTIVITY";

    /**
     * Bluetooth devices storage name
     */
    public static final String DEVICES_STORAGE_NAME = "blueCharmDevices";

    private final ArrayList<BluetoothDeviceWrapper> mData = new ArrayList<BluetoothDeviceWrapper>();

    private ArrayAdapter<BluetoothDeviceWrapper> mArrayAdapter;

    private ListView mListView;

    private BluetoothAdapterWrapper bluetoothAdapterWrapper;

    private BroadcastReceiver mReceiver;

    private boolean mBound;

    private Messenger mService;

    private BroadcastReceiver mDeviceDiscoveryReceiver;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };

    /**
     * Method called by Android at creation time. It starts BlueCharmService, register Bluetooth interface and start
     * discovering. Then connect to UI events.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /* Starting service */
        final Intent service = new Intent(this, BlueCharmService.class);
        startService(service);

        /* Bind View with Model */
        mArrayAdapter =
                new SetListAdapter<BluetoothDeviceWrapper>(this, android.R.layout.simple_list_item_checked, mData);
        mListView = (ListView) findViewById(R.id.blueDevices);
        mListView.setAdapter(mArrayAdapter);

        /**
         * Preparing device and filling choice list
         */
        bluetoothAdapterWrapper = new BluetoothAdapterWrapper();
        bluetoothAdapterWrapper.prepare(this);

        findViewById(R.id.progress).setVisibility(View.INVISIBLE);

        if (bluetoothAdapterWrapper.isPrepared()) {
            registerListForFoundedDevices();
            renewChoices();
            bluetoothAdapterWrapper.startDiscovery();
            registerProgressBar();
        } else {
            // TODO: if bluetooth enabling declined
        }

        /* Set UI event listeners */
        findViewById(R.id.refresh_button).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Log.d(TAG, "onClick (refresh button)");
                if (bluetoothAdapterWrapper.isPrepared()) {
                    bluetoothAdapterWrapper.cancelDiscovery();
                    refreshListView();
                    bluetoothAdapterWrapper.startDiscovery();
                }
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
                notifyTest();
            }
        });

        /* Save device user chosen */
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                saveDevices();
            }
        });
    }

    /**
     * Initiate Bluetooth device discovering
     */
    private void refreshListView() {
        mArrayAdapter.clear();
        renewChoices();
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    /**
     * Update user chosen devices in ListView
     */
    private void renewChoices() {
        mListView.clearChoices();
        SharedPreferences devicesStorage = getSharedPreferences(DEVICES_STORAGE_NAME, 0);
        @SuppressWarnings("unchecked")
        Map<String, String> devices = (Map<String, String>) devicesStorage.getAll();
        int i = 0;
        for (Map.Entry<String, String> device : devices.entrySet()) {
            mArrayAdapter.add(new BluetoothDeviceWrapper(device.getValue(), device.getKey()));
            mListView.setItemChecked(i++, true);
        }
    }

    /**
     * Called every time, when Activity takes screen. Binds with BlueCharmService
     */
    @Override
    protected void onStart() {
        super.onStart();
        /* Bind to BlueCharService */
        bindService(new Intent(this, BlueCharmService.class), mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    /**
     * Called every time, when Activity goes background. Unbinds from BlueCHarmService
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            /* Unbind from BlueCharmService */
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * Called when Activity closed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* Free broadcast receiver */
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        if (mDeviceDiscoveryReceiver != null) {
            unregisterReceiver(mDeviceDiscoveryReceiver);
        }
    }

    /**
     * Test method initiates Bluetooth notification
     */
    private void notifyTest() {
        if (!mBound)
            return;

        Message msg = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString(null, SmsNotifier.MAGIC + SmsNotifier.getDelimiter()
                + SmsNotifier.TYPE + SmsNotifier.getDelimiter()
                + bluetoothAdapterWrapper.getName() + SmsNotifier.getDelimiter()
                + getResources().getString(R.string.test_message));
        msg.setData(bundle);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save user choice in local database
     */
    private void saveDevices() {
        if (!mBound) {
            return;
        }

        Message msg = Message.obtain(null, BlueCharmService.MSG_SET_LISTENERS, 0, 0);
        ArrayList<String> devices = new ArrayList<String>();
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        for (int i = 0; i < mData.size(); ++i) {
            if (checked.get(i)) {
                devices.add(mData.get(i).toDataString());
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

    /**
     * Bind ProgressBar with Bluetooth device discovering
     */
    private void registerProgressBar() {
        mDeviceDiscoveryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                findViewById(R.id.progress).setVisibility(View.INVISIBLE);
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mDeviceDiscoveryReceiver, filter);
    }

    /**
     * Bind ListView with Bluetooth device discovering
     */
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
}
