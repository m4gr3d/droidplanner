package org.droidplanner.android.glass.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.MAVLink.Messages.ApmModes;

import org.droidplanner.core.drone.DroneInterfaces;

import java.util.List;

/**
 * Glass' droidplanner background service.
 * This service establishes a bluetooth bridge with the main app, through which it's pushed data
 * update for the connected drone.
 */
public class DroidPlannerGlassService extends Service {

    private final DroidPlannerApi mDpApi = new DroidPlannerApi();

    @Override
    public IBinder onBind(Intent intent) {
        return mDpApi;
    }

    /**
     * Provide access to droidplanner set of apis.
     */
    public class DroidPlannerApi extends Binder {

        public void addDroneListener(DroneInterfaces.OnDroneListener listener){
            //TODO: complete
        }

        public void removeDroneListener(DroneInterfaces.OnDroneListener listener){
            //TODO: complete
        }

        public boolean isDroneConnected(){
            //TODO: complete
            return false;
        }

        public List<ApmModes> getApmModes(){
            //TODO: complete
            return null;
        }

        public void changeFlightMode(ApmModes mode){
            //TODO: complete
        }
    }
}
