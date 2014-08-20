package org.droidplanner.android.lib.maps.graphics;

import org.droidplanner.android.lib.R;
import org.droidplanner.android.lib.maps.BaseMarkerInfo;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GraphicDrone extends BaseMarkerInfo.SimpleMarkerInfo {

    private final GPS droneGps;
    private final Orientation droneOrientation;

	public GraphicDrone(GPS gps, Orientation orientation) {
        droneGps = gps;
        droneOrientation = orientation;
	}

	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Coord2D getPosition() {
		return droneGps.getPosition();
	}

	@Override
	public Bitmap getIcon(Resources res) {
		return BitmapFactory.decodeResource(res, R.drawable.quad);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}

	@Override
	public float getRotation() {
		return (float) droneOrientation.getYaw();
	}
}
