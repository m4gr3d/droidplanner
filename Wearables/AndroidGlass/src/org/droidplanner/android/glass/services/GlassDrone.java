package org.droidplanner.android.glass.services;

import android.os.Binder;

import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;

import org.droidplanner.android.lib.parcelables.ParcelableAltitude;
import org.droidplanner.android.lib.parcelables.ParcelableBattery;
import org.droidplanner.android.lib.parcelables.ParcelableGPS;
import org.droidplanner.android.lib.parcelables.ParcelableOrientation;
import org.droidplanner.android.lib.parcelables.ParcelableRadio;
import org.droidplanner.android.lib.parcelables.ParcelableSpeed;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.WaypointManager;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.Preferences;
import org.droidplanner.core.drone.profiles.Parameters;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Altitude;
import org.droidplanner.core.drone.variables.Battery;
import org.droidplanner.core.drone.variables.Calibration;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.drone.variables.Home;
import org.droidplanner.core.drone.variables.MissionStats;
import org.droidplanner.core.drone.variables.Navigation;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.drone.variables.RC;
import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.drone.variables.Speed;
import org.droidplanner.core.drone.variables.State;
import org.droidplanner.core.drone.variables.StreamRates;
import org.droidplanner.core.firmware.FirmwareType;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.model.Drone;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide access to droidplanner set of apis.
 */
public class GlassDrone extends Binder implements Drone {

    private final List<DroneInterfaces.OnDroneListener> mDroneListeners = new ArrayList<DroneInterfaces.OnDroneListener>();
    private final WeakReference<DroidPlannerGlassService> mDPServiceRef;

    /**
     * Drone type
     */
    private int mType;

    private final Speed mSpeed = new Speed(this);
    private final Battery mBattery = new Battery(this);
    private final Orientation mOrientation = new Orientation(this);
    private final GPS mGps = new GPS(this);
    private final Radio mRadio = new Radio(this);
    private final Altitude mAltitude = new Altitude(this);

    GlassDrone(DroidPlannerGlassService dpService){
        mDPServiceRef = new WeakReference<DroidPlannerGlassService>(dpService);
    }

    void setSpeed(ParcelableSpeed speedParcel){
        speedParcel.updateSpeed(mSpeed);
    }

    void setBattery(ParcelableBattery batteryParcel){
        batteryParcel.updateBattery(mBattery);
    }

    void setOrientation(ParcelableOrientation orientationParcel){
        orientationParcel.updateOrientation(mOrientation);
    }

    void setGps(ParcelableGPS gpsParcel){
        gpsParcel.updateGps(mGps);
    }

    void setRadio(ParcelableRadio radioParcel){
        radioParcel.updateRadio(mRadio);
    }

    void setAltitude(ParcelableAltitude altitudeParcel){
        altitudeParcel.updateAltitude(mAltitude);
    }

    public void addDroneListener(DroneInterfaces.OnDroneListener listener) {
        mDroneListeners.add(listener);
    }

    public void removeDroneListener(DroneInterfaces.OnDroneListener listener) {
        mDroneListeners.remove(listener);
    }

    @Override
    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event) {
        if(mDroneListeners.isEmpty()){
            return;
        }

        for(DroneInterfaces.OnDroneListener listener: mDroneListeners){
            listener.onDroneEvent(event, this);
        }
    }

    @Override
    public GPS getGps() {
        return mGps;
    }

    @Override
    public int getMavlinkVersion() {
        return 0;
    }

    @Override
    public void onHeartbeat(msg_heartbeat msg) {}

    @Override
    public State getState() {
        //TODO: implement
        return null;
    }

    @Override
    public Parameters getParameters() {
        return null;
    }

    @Override
    public void setType(int type) {
        mType = type;
        notifyDroneEvent(DroneInterfaces.DroneEventsType.TYPE);
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public FirmwareType getFirmwareType() {
        return null;
    }

    @Override
    public void loadVehicleProfile() {

    }

    @Override
    public VehicleProfile getVehicleProfile() {
        return null;
    }

    @Override
    public MAVLinkStreams.MAVLinkOutputStream getMavClient() {
        //TODO: implement
        return null;
    }

    @Override
    public Preferences getPreferences() {
        return null;
    }

    @Override
    public WaypointManager getWaypointManager() {
        return null;
    }

    @Override
    public Speed getSpeed() {
        return mSpeed;
    }

    @Override
    public Battery getBattery() {
        return mBattery;
    }

    @Override
    public Radio getRadio() {
        return mRadio;
    }

    @Override
    public Home getHome() {
        return null;
    }

    @Override
    public Altitude getAltitude() {
        return mAltitude;
    }

    @Override
    public Orientation getOrientation() {
        return mOrientation;
    }

    @Override
    public Navigation getNavigation() {
        return null;
    }

    @Override
    public Mission getMission() {
        return null;
    }

    @Override
    public StreamRates getStreamRates() {
        return null;
    }

    @Override
    public MissionStats getMissionStats() {
        return null;
    }

    @Override
    public GuidedPoint getGuidedPoint() {
        return null;
    }

    @Override
    public Calibration getCalibrationSetup() {
        return null;
    }

    @Override
    public RC getRC() {
        return null;
    }

    @Override
    public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed, double climb) {

    }

    @Override
    public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error) {

    }
}
