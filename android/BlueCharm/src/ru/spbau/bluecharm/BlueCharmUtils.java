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