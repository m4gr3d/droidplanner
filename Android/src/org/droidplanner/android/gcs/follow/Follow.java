package org.droidplanner.android.gcs.follow;

import org.droidplanner.android.gcs.follow.FollowAlgorithm.FollowModes;
import org.droidplanner.android.gcs.location.FusedLocation;
import org.droidplanner.android.gcs.location.LocationFinder;
import org.droidplanner.android.gcs.location.LocationReceiver;
import org.droidplanner.android.gcs.roi.ROIEstimator;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.AbstractDrone;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;

public class Follow implements OnDroneListener, LocationReceiver {

	private Context context;
	private boolean followMeEnabled = false;
	private Drone drone;

	private ROIEstimator roiEstimator;
	private LocationFinder locationFinder;
	private FollowAlgorithm followAlgorithm;

	public Follow(Context context, Drone drone, Handler handler) {
		this.context = context;
		this.drone = drone;
		followAlgorithm = new FollowLeash(drone, new Length(5.0));
		locationFinder = new FusedLocation(context, this);
		roiEstimator = new ROIEstimator(handler,drone);
		drone.addDroneListener(this);
	}

	public void toggleFollowMeState() {
		if (isEnabled()) {
			disableFollowMe();
			drone.getState().changeFlightMode(ApmModes.ROTOR_LOITER);
		} else {
			if (drone.getMavClient().isConnected()) {
				if (drone.getState().isArmed()) {
					drone.getState().changeFlightMode(ApmModes.ROTOR_GUIDED);
					enableFollowMe();
				} else {
					Toast.makeText(context, "Drone Not Armed", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(context, "Drone Not Connected", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void enableFollowMe() {
		Log.d("follow", "enable");
		Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();
		
		locationFinder.enableLocationUpdates();

		followMeEnabled = true;
        drone.notifyDroneEvent(DroneEventsType.FOLLOW_START);
	}

	private void disableFollowMe() {
		if (followMeEnabled) {
			Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
			followMeEnabled = false;
			Log.d("follow", "disable");
		}
		locationFinder.disableLocationUpdates();
		roiEstimator.disableLocationUpdates();
        drone.notifyDroneEvent(DroneEventsType.FOLLOW_STOP);
	}

	public boolean isEnabled() {
		return followMeEnabled;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, AbstractDrone drone) {
		switch (event) {
		case MODE:
			if ((drone.getState().getMode() != ApmModes.ROTOR_GUIDED)) {
				disableFollowMe();
			}
			break;
		case DISCONNECTED:
			disableFollowMe();
			break;
		default:
		}
	}

	public Length getRadius() {
		return followAlgorithm.radius;
	}

	@Override
	public void onLocationChanged(Location location) {
		followAlgorithm.processNewLocation(location);
		roiEstimator.onLocationChanged(location);
	}

	public void setType(FollowModes item) {
		followAlgorithm = item.getAlgorithmType(drone);
		drone.notifyDroneEvent(DroneEventsType.FOLLOW_CHANGE_TYPE);
	}

	public void changeRadius(double increment) {
		followAlgorithm.changeRadius(increment);
	}

	public void cycleType() {
		setType(followAlgorithm.getType().next());
	}

	public FollowModes getType() {
		return followAlgorithm.getType();
	}
}
