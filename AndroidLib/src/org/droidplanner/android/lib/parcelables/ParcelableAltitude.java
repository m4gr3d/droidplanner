package org.droidplanner.android.lib.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import org.droidplanner.core.drone.variables.Altitude;
import org.droidplanner.core.model.Drone;

/**
 * Parcelable wrapper for an 'Altitude' object.
 */
public class ParcelableAltitude implements Parcelable {

    private double altitude;
    private double targetAltitude;

    public ParcelableAltitude(Altitude altitude){
        this.altitude = altitude.getAltitude();
        this.targetAltitude = altitude.getTargetAltitude();
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getTargetAltitude() {
        return targetAltitude;
    }

    public void setTargetAltitude(double targetAltitude) {
        this.targetAltitude = targetAltitude;
    }

    public Altitude getAltitude(Drone drone){
        final Altitude altitude = new Altitude(drone);
        return updateAltitude(altitude);
    }

    public Altitude updateAltitude(Altitude altitude){
        altitude.setAltitude(this.altitude);
        altitude.setAltitudeError(this.targetAltitude - this.altitude);
        return altitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.altitude);
        dest.writeDouble(this.targetAltitude);
    }

    private ParcelableAltitude(Parcel in) {
        this.altitude = in.readDouble();
        this.targetAltitude = in.readDouble();
    }

    public static final Parcelable.Creator<ParcelableAltitude> CREATOR = new Parcelable.Creator<ParcelableAltitude>() {
        public ParcelableAltitude createFromParcel(Parcel source) {
            return new ParcelableAltitude(source);
        }

        public ParcelableAltitude[] newArray(int size) {
            return new ParcelableAltitude[size];
        }
    };
}
