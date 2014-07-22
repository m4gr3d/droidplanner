package org.droidplanner.android.proxy.mission.item.markers;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.lib.maps.BaseMarkerInfo;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.survey.Survey;

/**
 *
 */
public class SurveyMarkerInfoProvider {

	private final Survey mSurvey;
	private final List<BaseMarkerInfo> mPolygonMarkers = new ArrayList<BaseMarkerInfo>();

	protected SurveyMarkerInfoProvider(MissionItemProxy origin) {
		mSurvey = (Survey) origin.getMissionItem();
		updateMarkerInfoList();
	}

	private void updateMarkerInfoList() {
		for (Coord2D point : mSurvey.polygon.getPoints()) {
			mPolygonMarkers.add(new PolygonMarkerInfo(point));
		}
	}

	public List<BaseMarkerInfo> getMarkersInfos() {
		return mPolygonMarkers;
	}
}
