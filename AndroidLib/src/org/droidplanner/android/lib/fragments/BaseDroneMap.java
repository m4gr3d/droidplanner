package org.droidplanner.android.lib.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.android.lib.R;
import org.droidplanner.android.lib.maps.BaseDPMap;
import org.droidplanner.android.lib.maps.graphics.GraphicDrone;
import org.droidplanner.android.lib.maps.graphics.GraphicGuided;
import org.droidplanner.android.lib.maps.graphics.GraphicHome;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.AbstractDrone;

/**
 * Base implementation for fragments that need access to a DPMap instance.
 */
public abstract class BaseDroneMap extends Fragment implements DroneInterfaces.OnDroneListener{

    protected BaseDPMap mMapFragment;

    protected GraphicHome home;
    protected GraphicDrone graphicDrone;
    protected GraphicGuided guided;

    protected AbstractDrone drone;

    protected Context context;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(!(activity instanceof BaseDPMap.DroneProvider)){
            throw new IllegalStateException("Parent activity must implement " + BaseDPMap.DroneProvider
                    .class.getName());
        }

        context = activity.getApplicationContext();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        drone = ((BaseDPMap.DroneProvider)getActivity()).getDrone();

        home = new GraphicHome(drone.getHome());
        graphicDrone = new GraphicDrone(drone.getGps(), drone.getOrientation());
        guided = new GraphicGuided(drone.getGuidedPoint(), drone.getGps());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_drone_map, viewGroup, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        updateMapFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        drone.removeDroneListener(this);
        mMapFragment.saveCameraPosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        drone.addDroneListener(this);
        mMapFragment.loadCameraPosition();
        update();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMapFragment();
    }

    protected abstract void updateMapFragment();

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, AbstractDrone drone) {
        switch(event) {
            case GPS:
                mMapFragment.updateMarker(graphicDrone);
                mMapFragment.updateDroneLeashPath(guided);
                mMapFragment.addFlightPathPoint(drone.getGps().getPosition());
                break;

            case GUIDEDPOINT:
                mMapFragment.updateMarker(guided);
                mMapFragment.updateDroneLeashPath(guided);
                break;
        }
    }

    public void update(){
        mMapFragment.cleanMarkers();

        if (home.isValid()) {
            mMapFragment.updateMarker(home);
        }
    }
}
