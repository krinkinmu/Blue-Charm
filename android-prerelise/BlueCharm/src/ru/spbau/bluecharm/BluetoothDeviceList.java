package ru.spbau.bluecharm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

public class BluetoothDeviceList {
    /**
     * Bluetooth devices storage name
     */
    public static final String DEVICES_STORAGE_NAME = "blueCharmDevices";

    private final ArrayList<BluetoothDeviceWrapper> mData = new ArrayList<BluetoothDeviceWrapper>();

    private ArrayAdapter<BluetoothDeviceWrapper> mArrayAdapter;

    private ListView mListView;

    private Activity mActivity;

    public BluetoothDeviceList(Activity activity, ListView listView) {
        /* Bind View with Model */
        mArrayAdapter = new SetListAdapter<BluetoothDeviceWrapper>(activity,
                android.R.layout.simple_list_item_checked, mData);
        mListView = listView;
        mListView.setAdapter(mArrayAdapter);
        mActivity = activity;
    }

    /**
     * Initiate Bluetooth device discovering
     */
    public void refreshListView() {
        mArrayAdapter.clear();
        renewChoices();
    }

    /**
     * Update user chosen devices in ListView
     */
    public void renewChoices() {
        mListView.clearChoices();
        SharedPreferences devicesStorage = mActivity.getSharedPreferences(DEVICES_STORAGE_NAME, 0);
        @SuppressWarnings("unchecked")
        Map<String, String> devices = (Map<String, String>) devicesStorage.getAll();
        int i = 0;
        for (Map.Entry<String, String> device : devices.entrySet()) {
            mArrayAdapter.add(new BluetoothDeviceWrapper(device.getValue(), device.getKey()));
            mListView.setItemChecked(i++, true);
        }
    }

    public ListView getListView() {
        return mListView;
    }

    public ArrayList<String> getDevices() {
        ArrayList<String> devices = new ArrayList<String>();
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        for (int i = 0; i < mData.size(); ++i) {
            if (checked.get(i)) {
                devices.add(mData.get(i).toDataString());
            }
        }
        return devices;
    }

    public void add(BluetoothDeviceWrapper object) {
        mArrayAdapter.add(object);
    }
}
