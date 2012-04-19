package ru.spbau.bluecharm;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BlueCharm extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ArrayList<String> data = new ArrayList<String>();
        ArrayAdapter<String> tst = new ArrayAdapter(this,
        		android.R.layout.simple_list_item_checked, data);
        ((ListView) findViewById(R.id.blueDevices)).setAdapter(tst);
        tst.add("tst1");
        tst.add("tst2");
    }
}