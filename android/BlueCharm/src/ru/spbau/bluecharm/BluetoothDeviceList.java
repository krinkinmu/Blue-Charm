/*****************************************************************************************
 * Copyright (c) 2012 A. Korovin, K. Krasheninnikova, M. Krinkin, S. Lazarev, A. Opeykin *
 *                                                                                       *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this  *
 * software and associated documentation files (the "Software"), to deal in the Software *
 * without restriction, including without limitation the rights to use, copy, modify,    *
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to    *
 * permit persons to whom the Software is furnished to do so, subject to the following   *
 * conditions:                                                                           *
 *                                                                                       *
 * The above copyright notice and this permission notice shall be included in all copies *
 * or substantial portions of the Software.                                              *
 *                                                                                       *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,   *
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A         *
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT    *
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF  *
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE  *
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                                         *
 *****************************************************************************************/

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

    private final ArrayAdapter<BluetoothDeviceWrapper> mArrayAdapter;

    private final ListView mListView;

    private final Activity mActivity;

    /**
     * Constructs device list for activity and list view
     *
     * @param activity Activity
     * @param listView List View
     */
    public BluetoothDeviceList(Activity activity, ListView listView) {
        /* Bind View with Model */
        mArrayAdapter = new SetListAdapter<BluetoothDeviceWrapper>(activity,
                android.R.layout.simple_list_item_checked, mData);
        mListView = listView;
        mListView.setAdapter(mArrayAdapter);
        mActivity = activity;
    }

    /**
     * Clears list and add only saved devices.
     */
    public void refreshListView() {
        mArrayAdapter.clear();
        renewChoices();
    }

    /**
     * Update user chosen devices in list view
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

    /**
     * Returns List View object
     *
     * @return List View object
     */
    public ListView getListView() {
        return mListView;
    }

    /**
     * Returns list of string representations of devices.
     *
     * @return List of string representations of devices.
     */
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

    /**
     * Adds new bluetooth device to list
     *
     * @param object Bluetooth device
     */
    public void add(BluetoothDeviceWrapper object) {
        mArrayAdapter.add(object);
    }
}
