package ru.spbau.bluecharm;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Helpful utils
 */
public class BlueCharmUtils {
    private static final String TAG = "UTILS";

    /**
     * Finding contact name by telephone number.
     *
     * @param context Context
     * @param number  Telephone number
     * @return Contact display name
     */
    public static String getContactName(Context context, String number) {
        Log.d(TAG, "Searching contact with number: " + number);

        /* define the columns I want the query to return */
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        /* encode the phone number and build the filter URI */
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        /* query time */
        Log.d(TAG, "Sending query");
        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

        String name = null;
        if (cursor.moveToFirst()) {
            /* Get values from contacts database: */
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            Log.d(TAG, "Contact found for number: " + number + ". The name is: " + name);
        } else {
            Log.d(TAG, "Contact not found for number: " + number);
        }

        return name;
    }
}