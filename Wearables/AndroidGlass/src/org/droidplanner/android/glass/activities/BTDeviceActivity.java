package org.droidplanner.android.glass.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.droidplanner.R;

/**
 * Displays a set of bluetooth devices for the user to select from.
 */
public class BTDeviceActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_device);
    }
}
