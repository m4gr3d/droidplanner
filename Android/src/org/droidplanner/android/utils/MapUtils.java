package org.droidplanner.android.utils;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.lib.utils.BaseMapUtils;
import org.droidplanner.core.helpers.coordinates.Coord2D;

/**
 * Adds functions requiring the google play services library.
 */
public class MapUtils extends BaseMapUtils {

    //Not instantiable
    private MapUtils(){}

    static public LatLng CoordToLatLang(Coord2D coord) {
        return new LatLng(coord.getLat(), coord.getLng());
    }

    public static Coord2D LatLngToCoord(LatLng point) {
        return new Coord2D(point.latitude, point.longitude);
    }
}
