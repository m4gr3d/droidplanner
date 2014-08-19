package org.droidplanner.core.drone;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

/**
 * Defines the set of methods that a drone implementation should have.
 */
public interface AbstractDrone {

    public void addDroneListener(OnDroneListener listener);

    public void removeDroneListener(OnDroneListener listener);

    public void notifyDroneEvent(DroneEventsType event);
}
