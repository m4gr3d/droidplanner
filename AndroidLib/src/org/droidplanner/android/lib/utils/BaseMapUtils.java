package org.droidplanner.android.lib.utils;

import android.content.res.Resources;
import android.location.Location;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.droidplanner.core.helpers.coordinates.Coord2D;

public class BaseMapUtils {

    //Not instantiable
    protected BaseMapUtils(){}

    public static LatLng CoordToLatLng(Coord2D coord){
        return new LatLng(coord.getLat(), coord.getLng());
    }

    public static Coord2D ILatLngToCoord(ILatLng point){
        return new Coord2D(point.getLatitude(), point.getLongitude());
    }

    public static Coord2D LocationToCoord(Location location) {
        return new Coord2D(location.getLatitude(), location.getLongitude());
    }

    public static int scaleDpToPixels(double value, Resources res) {
        final float scale = res.getDisplayMetrics().density;
        return (int) Math.round(value * scale);
    }
}
