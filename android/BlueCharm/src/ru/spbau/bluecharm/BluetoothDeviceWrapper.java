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
