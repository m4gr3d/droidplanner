package org.droidplanner.android.fragments;

import java.util.List;

import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.lib.maps.BaseDPMap;
import org.droidplanner.android.lib.maps.BaseMarkerInfo;
import org.droidplanner.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import org.droidplanner.android.proxy.mission.item.markers.SurveyMarkerInfoProvider;
import org.droidplanner.android.lib.prefs.AutoPanMode;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

@SuppressLint("UseSparseArrays")
public class EditorMapFragment extends DroneMap implements BaseDPMap.OnMapLongClickListener,
		BaseDPMap.OnMarkerDragListener, BaseDPMap.OnMapClickListener, BaseDPMap.OnMarkerClickListener {

	// public MapPath polygonPath;
	// public CameraGroundOverlays cameraOverlays;
	private OnEditorInteraction editorListener;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mMapFragment.setOnMarkerDragListener(this);
        mMapFragment.setOnMarkerClickListener(this);
        mMapFragment.setOnMapClickListener(this);
        mMapFragment.setOnMapLongClickListener(this);

        // TODO: figure out if it's still needed
        // polygonPath = new MapPath(mMap, Color.BLACK, getResources());
        // cameraOverlays = new CameraGroundOverlays(mMap);

    }

	@Override
	public void onMapLongClick(Coord2D point) {
		// mListener.onAddPoint(point);
	}

	@Override
	public void onMarkerDrag(BaseMarkerInfo markerInfo) {
		checkForWaypointMarkerMoving(markerInfo);
	}

	@Override
	public void onMarkerDragStart(BaseMarkerInfo markerInfo) {
		checkForWaypointMarkerMoving(markerInfo);
	}

	private void checkForWaypointMarkerMoving(BaseMarkerInfo markerInfo) {
		if (SpatialCoordItem.class.isInstance(markerInfo)) {
			Coord2D position = markerInfo.getPosition();

			// update marker source
			SpatialCoordItem waypoint = (SpatialCoordItem) markerInfo;
			waypoint.setPosition(position);

			/*
			 * // update info window if(dragging)
			 * waypoint.updateDistanceFromPrevPoint(); else
			 * waypoint.setPrevPoint(mission.getWaypoints());
			 * updateInfoWindow(waypoint, marker);
			 */

			// update flight path
			mMapFragment.updateMissionPath(missionProxy);
		}
	}

	@Override
	public void onMarkerDragEnd(BaseMarkerInfo markerInfo) {
		checkForWaypointMarker(markerInfo);
		checkForPolygonMarker();
	}

	private void checkForWaypointMarker(BaseMarkerInfo markerInfo) {
		if (!(markerInfo instanceof SurveyMarkerInfoProvider)
				&& (markerInfo instanceof MissionItemMarkerInfo)) {
			missionProxy.move(((MissionItemMarkerInfo) markerInfo).getMarkerOrigin(),
					markerInfo.getPosition());
		}
	}

	private void checkForPolygonMarker() {
		/*
		 * if (PolygonPoint.class.isInstance(info)) {
		 * Listener.onMovePolygonPoint((PolygonPoint)
		 * info,marker.getPosition()); }
		 */
	}

	@Override
	public void onMapClick(Coord2D point) {
		editorListener.onMapClick(point);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		editorListener = (OnEditorInteraction) activity;
	}

	@Override
	public boolean setAutoPanMode(AutoPanMode target) {
		if (target == AutoPanMode.DISABLED)
			return true;

		Toast.makeText(getActivity(), "Auto pan is not supported on this map.", Toast.LENGTH_LONG)
				.show();
		return false;
	}

	@Override
	public boolean onMarkerClick(BaseMarkerInfo info) {
		if (info instanceof MissionItemMarkerInfo) {
			editorListener.onItemClick(((MissionItemMarkerInfo) info).getMarkerOrigin());
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean isMissionDraggable() {
		return true;
	}

	public void zoomToFit() {
		// get visible mission coords
		final List<Coord2D> visibleCoords = missionProxy.getVisibleCoords();

		// add home coord if visible
		final Coord2D homeCoord = drone.home.getCoord();
		if (homeCoord != null && !homeCoord.isEmpty())
			visibleCoords.add(homeCoord);

		mMapFragment.zoomToFit(visibleCoords);
	}

}
