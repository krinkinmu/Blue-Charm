package ru.spbau.bluecharm;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallsNotifier extends BlueCharmNotifier {
	public static final String TYPE = "IN_CALL";

	@Override
	protected String buildMessage(Context context, Intent intent) {
		String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		return number + getDelimiter() + getContactName(context, number);
	}

	@Override
	protected boolean isTargetIntent(Context context, Intent intent) {
		return intent.hasExtra(TelephonyManager.EXTRA_STATE)
				&& intent.getStringExtra(TelephonyManager.EXTRA_STATE)
								.equals(TelephonyManager.EXTRA_STATE_RINGING);
	}

	@Override
	protected String getType(Context context, Intent intent) {
		return TYPE;
	}
	
	protected String getContactName(Context context, String number) {
		Log.d(TAG, "Searching contact with number: " + number);
		
		// define the columns I want the query to return
		String[] projection = new String[] {
		        ContactsContract.PhoneLookup.DISPLAY_NAME};
		
		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

		// query time
		Log.d(TAG, "Sending query");
		Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

		String name = "Unknown";
		if (cursor.moveToFirst()) {
		    // Get values from contacts database:
		    name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			Log.d(TAG, "Contact found for number: " + number + ". The name is: " + name);
		} else {
		    Log.d(TAG, "Contact not found for number: " + number);
		}
		
		return name;
	}
}
