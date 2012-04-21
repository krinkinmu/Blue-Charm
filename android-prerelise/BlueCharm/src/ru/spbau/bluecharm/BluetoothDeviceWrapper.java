package ru.spbau.bluecharm;

import android.bluetooth.BluetoothDevice;

/**
 * Immortal class... hmmm... it's magic... BlueCharm!
 */
public class BluetoothDeviceWrapper {
    private final String mName;

    private final String mAddress;

    /**
     * Constructs wrapper from bluetooth device
     *
     * @param device Bluetooth device
     */
    public BluetoothDeviceWrapper(BluetoothDevice device) {
        mName = device.getName();
        mAddress = device.getAddress();
    }

    /**
     * Constructs wrapper from data string
     *
     * @param string Data string
     */
    public BluetoothDeviceWrapper(String string) {
        String[] contents = string.split("\n");
        mName = contents[0];
        mAddress = contents[1];
    }

    /**
     * Constructs wrapper from name and address of device
     *
     * @param name    Name of device
     * @param address MAC address of device
     */
    public BluetoothDeviceWrapper(String name, String address) {
        mName = name;
        mAddress = address;
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public String toString() {
        return mName;
    }

    /**
     * String representation of device
     *
     * @return String representation of device
     */
    public String toDataString() {
        return mName + "\n" + mAddress;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        boolean result = false;

        if (other instanceof BluetoothDeviceWrapper) {
            BluetoothDeviceWrapper that = (BluetoothDeviceWrapper) other;
            result = that.canEqual(this) && (getAddress().equals(that.getAddress()));
        }

        return result;

    }

    /**
     * This is code snippet for equals method. For explanations read
     * article http://www.artima.com/lejava/articles/equality.html
     *
     * @param other Object to compare
     * @return Can be equal or not
     */
    boolean canEqual(Object other) {
        return (other instanceof BluetoothDeviceWrapper);
    }
}
