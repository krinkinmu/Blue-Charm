package ru.spbau.bluecharm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BlueCharmActivity extends Activity {
    /**
     * Debugging tag symbol
     */
    private static final String TAG = "BLUE_CHARM_ACTIVITY";

    /**
     * Request constant for enabling BT
     */
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBluetoothAdapter;

    private BroadcastReceiver mReceiver;

    private boolean mBound;

    private Messenger mService;

    private BroadcastReceiver mDeviceDiscoveryReceiver;

    private BluetoothDeviceList mDeviceList;

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
        findViewById(R.id.progress).setVisibility(View.INVISIBLE);

        /* Starting service */
        final Intent service = new Intent(this, BlueCharmService.class);
        startService(service);

        mDeviceList = new BluetoothDeviceList(this, (ListView) findViewById(R.id.blueDevices));

        registerProgressBar();

        /**
         * Preparing device and filling choice list
         */
        if (prepareAdapter(BluetoothAdapter.getDefaultAdapter())) {
            registerListForFoundedDevices();
            mDeviceList.renewChoices();
            mBluetoothAdapter.startDiscovery();
        }

        /* Set UI event listeners */
        findViewById(R.id.refresh_button).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Log.d(TAG, "onClick (refresh button)");
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.cancelDiscovery();
                    mDeviceList.refreshListView();
                    mBluetoothAdapter.startDiscovery();
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
        mDeviceList.getListView().setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                saveDevices();
            }
        });
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

    /**
     * Called when Activity closed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothAdapter.cancelDiscovery();

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
        if (!mBound) {
            return;
        }

        Message msg = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
        Bundle bundle = new Bundle();
        char sep = BlueCharmNotifier.getDelimiter();
        bundle.putString(null, BlueCharmNotifier.MAGIC + sep + SmsNotifier.TYPE + sep
            + mBluetoothAdapter.getName() + sep + getResources().getString(R.string.test_message));
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

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(null, mDeviceList.getDevices());
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
                if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                    findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    findViewById(R.id.refresh_button).setEnabled(false);
                } else {
                    findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                    findViewById(R.id.refresh_button).setEnabled(true);
                }
            }
        };

        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mDeviceDiscoveryReceiver, filterStart);
        registerReceiver(mDeviceDiscoveryReceiver, filterFinish);
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
                    mDeviceList.add(new BluetoothDeviceWrapper(device));
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    /**
     * Utility method prepares Bluetooth adapter
     */
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
}
