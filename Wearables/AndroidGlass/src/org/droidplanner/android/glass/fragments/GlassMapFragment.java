package org.droidplanner.android.glass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import org.droidplanner.R;
import org.droidplanner.android.lib.fragments.BaseDroneMap;
import org.droidplanner.android.lib.maps.BaseDPMap;
import org.droidplanner.android.lib.maps.providers.mapbox.MapBoxFragment;
import org.droidplanner.android.lib.prefs.AutoPanMode;

/**
 * Used on glass to display the flight map data. Provides support for map control via glass
 * gestures.
 */
public class GlassMapFragment extends BaseDroneMap {

    private static final String TAG = GlassMapFragment.class.getSimpleName();

    /**
     * How many different zooming level are supported.
     */
    private static final int MAP_ZOOM_PARTITIONS = 5;

    /**
     * Glass gesture detector used to provide map navigation via glass gestures.
     */
    protected GestureDetector mGestureDetector;

    /**
     * Zoom level limits for the underlying map.
     */
    protected float mMaxZoomLevel = -1;
    protected float mMinZoomLevel = -1;
    protected float mZoomLevelRange = -1;
    protected float mZoomStep = -1;

    /**
     * Used to track user head orientation. This is enabled when the user holds two fingers on
     * the touchpad, and is used to allow panning of the underlying map.
     */

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = getActivity().getApplicationContext();

        mGestureDetector = new GestureDetector(context);
        mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
                    @Override
                    public boolean onGesture(Gesture gesture) {
                        if (mMinZoomLevel == -1) {
                            mMinZoomLevel = mMapFragment.getMinZoomLevel();
                        }

                        if (mMaxZoomLevel == -1) {
                            mMaxZoomLevel = mMapFragment.getMaxZoomLevel();
                        }

                        if (mZoomLevelRange == -1) {
                            mZoomLevelRange = mMaxZoomLevel - mMinZoomLevel;
                            mZoomStep = mZoomLevelRange / MAP_ZOOM_PARTITIONS;
                        }

                        switch (gesture) {
                            case SWIPE_RIGHT: {
                                updateMapZoomLevel(mMapFragment.getMapZoomLevel() + mZoomStep);
                                return true;
                            }

                            case SWIPE_LEFT: {
                                updateMapZoomLevel(mMapFragment.getMapZoomLevel() - mZoomStep);
                                return true;
                            }
                        }
                        return false;
                    }
                });
    }

    @Override
    protected void updateMapFragment() {
        //Add the map fragment instance
        final FragmentManager fm = getChildFragmentManager();
        mMapFragment = (BaseDPMap) fm.findFragmentById(R.id.map_fragment_container);
        if(mMapFragment == null){
            mMapFragment = new MapBoxFragment();
            fm.beginTransaction()
                    .add(R.id.map_fragment_container, (Fragment) mMapFragment)
                    .commit();
        }

        mMapFragment.selectAutoPanMode(AutoPanMode.DRONE);
    }

    private float clampZoomLevel(float zoomLevel) {
        if (zoomLevel < mMinZoomLevel) {
            return mMinZoomLevel;
        }
        else if (zoomLevel > mMaxZoomLevel) {
            return mMaxZoomLevel;
        }
        return zoomLevel;
    }

    private void updateMapZoomLevel(float newZoomLevel) {
        final float clampedZoom = clampZoomLevel(newZoomLevel);
        mMapFragment.updateCamera(mMapFragment.getMapCenter(), clampedZoom);
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector != null && mGestureDetector.onMotionEvent(event);
    }
}
