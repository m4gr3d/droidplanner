package org.droidplanner.android.proxy.mission.item.markers;

import org.droidplanner.android.lib.maps.BaseMarkerInfo;
import org.droidplanner.core.helpers.coordinates.Coord2D;

/**
 */
public class PolygonMarkerInfo extends BaseMarkerInfo.SimpleMarkerInfo {

	private final Coord2D mPoint;

	public PolygonMarkerInfo(Coord2D point) {
		mPoint = point;
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
		return mPoint;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}
}
