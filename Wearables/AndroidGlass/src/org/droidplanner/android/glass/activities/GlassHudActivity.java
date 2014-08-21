package org.droidplanner.android.glass.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;

import com.MAVLink.Messages.ApmModes;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;

import org.droidplanner.R;
import org.droidplanner.android.glass.fragments.GlassMapFragment;
import org.droidplanner.android.glass.services.DroidPlannerGlassService;
import org.droidplanner.android.glass.services.GlassDrone;
import org.droidplanner.android.glass.views.HUD;
import org.droidplanner.android.lib.maps.BaseDPMap;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;

import java.util.List;

public class GlassHudActivity extends FragmentActivity implements BaseDPMap.DroneProvider, DroneInterfaces.OnDroneListener {

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDrone = (GlassDrone) service;

            //TODO: query the drone connection state
            mDrone.getMavClient().queryConnectionState();

            updateMenu(mMenu);

            mDrone.addDroneListener(GlassHudActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDrone = null;
        }
    };

    private Drone mDrone;
    private HUD hudWidget;

    /**
     * Glass gesture detector.
     * Detects glass specific swipes, and taps, and uses it for navigation.
     */
    protected GestureDetector mGestureDetector;

    /**
     * Reference to the menu so it can be updated when used with contextual voice commands.
     */
    protected Menu mMenu;

    private GlassMapFragment mMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass_hud);

        hudWidget = (HUD) findViewById(R.id.hudWidget);
        mMapFragment = (GlassMapFragment) getSupportFragmentManager().findFragmentById(R.id
                .glass_flight_map_fragment);

        mGestureDetector = new GestureDetector(getApplicationContext());
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.menu_glass_hud, menu);
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            mMenu = menu;
        }
            updateMenu(menu);
        return true;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mMapFragment != null && mMapFragment.onGenericMotionEvent(event)
                || mGestureDetector.onMotionEvent(event);
    }

    /**
     * Used to detect glass specific gestures.
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void updateMenu(Menu menu) {
        if (menu != null && mDrone != null) {
            //TODO: fix
            final boolean isDroneConnected = mDrone.getMavClient().isConnected();

            //Update the toggle connection menu title
            MenuItem connectionToggleItem = menu.findItem(R.id.menu_toggle_connection);
            if (connectionToggleItem != null) {
                connectionToggleItem.setTitle(isDroneConnected
                        ? R.string.menu_disconnect
                        : R.string.menu_connect);
            }

            //Fill the flight modes menu with all the implemented flight modes
            MenuItem flightModes = menu.findItem(R.id.menu_flight_modes);
            SubMenu flightModesMenu = flightModes.getSubMenu();
            flightModesMenu.clear();

            //Get the list of apm modes for this drone
            //TODO: fix
            List<ApmModes> apmModesList = ApmModes.getModeList(mDrone.getType());

            //Add them to the flight modes menu
            for (ApmModes apmMode : apmModesList) {
                flightModesMenu.add(apmMode.getName());
            }

            //Make the drone control menu visible if connected
            menu.setGroupVisible(R.id.menu_group_drone_connected, isDroneConnected);
            menu.setGroupEnabled(R.id.menu_group_drone_connected, isDroneConnected);

        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case Menu.NONE: {

                //Handle the flight modes
                final String itemTitle = item.getTitle().toString();
                final ApmModes selectedMode = ApmModes.getMode(itemTitle, mDrone.getType());
                if (ApmModes.isValid(selectedMode)) {
                    //TODO: fix
                    mDrone.getState().changeFlightMode(selectedMode);
                    return true;
                }

                return false;
            }

            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        bindService(new Intent(getApplicationContext(), DroidPlannerGlassService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mDrone != null){
            mDrone.removeDroneListener(this);
        }
        unbindService(mServiceConnection);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case ARMING:
                invalidateOptionsMenu();
                updateMenu(mMenu);

            case MODE:
                hudWidget.updateDroneState(drone.getState());
                break;

            case BATTERY:
                hudWidget.updateBatteryInfo(drone.getBattery());
                break;

            case CONNECTED:
                invalidateOptionsMenu();
                updateMenu(mMenu);

                //Enable the hud view
                hudWidget.setEnabled(true);
                break;

            case DISCONNECTED:
                invalidateOptionsMenu();
                updateMenu(mMenu);

                //Disable the hud view
                hudWidget.setEnabled(true);
                break;

            case GPS:
            case GPS_COUNT:
            case GPS_FIX:
                hudWidget.updateGpsInfo(drone.getGps());
                break;

            case ORIENTATION:
                //Update yaw, pitch, and roll
                hudWidget.updateOrientation(drone.getOrientation());
                break;

            case SPEED:
                hudWidget.updateAltitudeAndSpeed(drone.getAltitude(), drone.getSpeed());
                break;

            case TYPE:
                updateMenu(mMenu);
                hudWidget.setDroneType(drone.getType());
                break;
        }
    }

    @Override
    public Drone getDrone() {
        return mDrone;
    }
}
