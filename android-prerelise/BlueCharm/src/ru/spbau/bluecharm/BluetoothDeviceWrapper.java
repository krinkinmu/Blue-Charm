package ru.spbau.bluecharm;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
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
		return mName + "\n" + mAddress;    		
	}	
}