package org.droidplanner.android.lib.utils;

import android.content.res.Resources;
import android.location.Location;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class BaseMapUtils {

    //Not instantiable
    protected BaseMapUtils(){}

    public static GeoPoint CoordToGeoPoint(Coord2D coord) {
        return new GeoPoint(coord.getLat(), coord.getLng());
    }

    public static Coord2D GeoPointToCoord(IGeoPoint point) {
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
