package ru.spbau.bluecharm;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class BlueCharmService extends Service {
	public static final int NOTIFY = 1;
	public final Messenger mMessenger = new Messenger(new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NOTIFY:
				notifyClients((String) msg.obj);
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	});
	
	@Override
    public void onCreate() {
		super.onCreate();
		Log.d("SERVICE", "STARTED");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mMessenger.getBinder();
	}
	
	private void notifyClients(String message) {
		Toast.makeText(this, R.string.bidn_message,
				Toast.LENGTH_SHORT).show();
	}
}
