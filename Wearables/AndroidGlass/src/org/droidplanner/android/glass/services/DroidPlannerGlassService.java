package org.droidplanner.android.glass.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.enums.MAV_TYPE;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.Type;

import java.util.List;

/**
 * Glass's DroidPlanner background service.
 * This service establishes a bluetooth bridge with the main app, through which it receives data
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

        public Drone getDrone(){
            //TODO: complete
            return null;
        }

        public boolean isDroneConnected(){
            //TODO: complete
            return false;
        }

        public List<ApmModes> getApmModes(){
            //TODO: complete
            return null;
        }

        public int getDroneType(){
            //TODO: complete
            return MAV_TYPE.MAV_TYPE_GENERIC;
        }

        public void changeFlightMode(ApmModes mode){
            //TODO: complete
        }

        public void queryConnectionState(){
            //TODO: complete
        }
    }
}
