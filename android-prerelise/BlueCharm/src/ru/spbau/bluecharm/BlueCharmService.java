package ru.spbau.bluecharm;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class BlueCharmService extends Service {
	public static final String DPREFIX = "BLUE_CHARM_SERVICE";
	public static final int MSG_NOTIFY_LISTENERS = 1;
	public static final int MSG_SET_LISTENERS = 2;
	public static final int MSG_GET_LISTENERS = 3;
	
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NOTIFY_LISTENERS:
				Log.d(DPREFIX, "MSG_NOTIFY_LISTENERS recieved");
				break;
			case MSG_SET_LISTENERS:
				Log.d(DPREFIX, "MSG_SET_LISTENERS recieved");
				break;
			case MSG_GET_LISTENERS:
				Log.d(DPREFIX, "MSG_GET_LISTENERS recieved");
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(DPREFIX, "Service binded");
		return mMessenger.getBinder();
	}
}
