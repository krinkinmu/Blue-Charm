package ru.spbau.bluecharm;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Custom ListView adapter does not contain duplicates
 */
public class SetListAdapter<T> extends ArrayAdapter<T> {
    public SetListAdapter(Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public void add(T t) {
        if (!contains(t)) {
            super.add(t);
        }
    }

    private boolean contains(T t) {
        for (int i = 0; i < this.getCount(); ++i) {
            if (t.equals(this.getItem(i))) {
                return true;
            }
        }
        return false;
    }
}