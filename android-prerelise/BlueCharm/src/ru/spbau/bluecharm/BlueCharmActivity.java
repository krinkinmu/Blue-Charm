package ru.spbau.bluecharm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class BlueCharmActivity extends Activity {
	public static final String DPREFIX = "BLUE_CHARM_SERVICE";
	private Messenger mService = null;
	private boolean mBound;
	private ServiceConnection mConnection = new	ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			mBound = true;
		}
		
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			mBound = false;
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findViewById(R.id.refresh_button).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.d(DPREFIX, "onClick (refresh button)");
				sayHello();
			}
        });
        
        findViewById(R.id.exit_label).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.d(DPREFIX, "onClick (exit button)");
				finish();
			}
        });
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	bindService(new Intent(this, BlueCharmService.class), mConnection,
    			Context.BIND_AUTO_CREATE);
    }
    
    protected void onStop() {
    	super.onStop();
    	if (mBound) {
    		unbindService(mConnection);
    		mBound = false;
    	}
    }
    
    public void sayHello() {
    	if (!mBound) return;
    	Message msg = Message.obtain(null, BlueCharmService.MSG_NOTIFY_LISTENERS, 0, 0);
    	try {
    		mService.send(msg);
    	} catch (RemoteException e) {
    		e.printStackTrace();
    	}
    }
}