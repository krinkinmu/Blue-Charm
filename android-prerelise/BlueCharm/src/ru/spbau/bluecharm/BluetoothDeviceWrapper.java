package ru.spbau.bluecharm;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class BluetoothDeviceWrapper {
	private String mName;
	private String mAddress;

	public BluetoothDeviceWrapper(BluetoothDevice device) {
		mName = device.getName();
		mAddress = device.getAddress();
	}

	public BluetoothDeviceWrapper(String string) {
		String[] contents = string.split("\n");
		mName = contents[0];
		mAddress = contents[1];
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

    boolean canEqual(Object other) {
        return (other instanceof BluetoothDeviceWrapper);
    }
}