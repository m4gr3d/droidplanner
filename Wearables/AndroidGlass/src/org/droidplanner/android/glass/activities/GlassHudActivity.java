package org.droidplanner.android.glass.activities;

import android.content.ComponentName;
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
import org.droidplanner.android.glass.views.HUD;
import org.droidplanner.android.lib.maps.BaseDPMap;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneEvents;
import org.droidplanner.core.drone.DroneInterfaces;

import java.util.List;

public class GlassHudActivity extends FragmentActivity implements BaseDPMap.DroneProvider, DroneInterfaces.OnDroneListener {

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
        if (menu != null) {
            //Update the toggle connection menu title
            MenuItem connectMenuItem = menu.findItem(R.id.menu_connect);
            if (connectMenuItem != null) {
                connectMenuItem.setTitle(drone.MavClient.isConnected()
                        ? R.string.menu_disconnect
                        : R.string.menu_connect);
            }

            //Fill the flight modes menu with all the implemented flight modes
            MenuItem flightModes = menu.findItem(R.id.menu_flight_modes);
            SubMenu flightModesMenu = flightModes.getSubMenu();
            flightModesMenu.clear();

            //Get the list of apm modes for this drone
            List<ApmModes> apmModesList = ApmModes.getModeList(drone.type.getType());

            //Add them to the flight modes menu
            for (ApmModes apmMode : apmModesList) {
                flightModesMenu.add(apmMode.getName());
            }

            final boolean isDroneConnected = drone.MavClient.isConnected();

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
                final ApmModes selectedMode = ApmModes.getMode(itemTitle, drone.type.getType());
                if (ApmModes.isValid(selectedMode)) {
                    drone.state.changeFlightMode(selectedMode);
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

        drone.events.addDroneListener(this);

        //Check if we're connected to the drone
//        hudWidget.setEnabled(drone.MavClient.isConnected());
    }

    @Override
    public void onStop() {
        super.onStop();
        final DroneEvents droneEvents = drone.events;
        droneEvents.removeDroneListener(this);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case ARMING:
                invalidateOptionsMenu();
                updateMenu(mMenu);

            case MODE:
                hudWidget.updateDroneState(drone.state);
                break;

            case BATTERY:
                hudWidget.updateBatteryInfo(drone.battery);
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
                hudWidget.updateGpsInfo(drone.GPS);
                break;

            case ORIENTATION:
                //Update yaw, pitch, and roll
                hudWidget.updateOrientation(drone.orientation);
                break;

            case SPEED:
                hudWidget.updateAltitudeAndSpeed(drone.altitude, drone.speed);
                break;

            case TYPE:
                updateMenu(mMenu);
                hudWidget.setDroneType(drone.type.getType());
                break;
        }
    }

    @Override
    public Drone getDrone() {
        return null;
    }
}
