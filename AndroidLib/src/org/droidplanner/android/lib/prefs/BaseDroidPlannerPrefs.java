package org.droidplanner.android.lib.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.droidplanner.android.lib.utils.file.IO.VehicleProfileReader;
import org.droidplanner.core.drone.Preferences;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.firmware.FirmwareType;

/**
 * Provides structured access to Droidplanner preferences.
 */
public class BaseDroidPlannerPrefs implements Preferences {

    // Public for legacy usage
    public SharedPreferences prefs;
    protected Context context;

    public BaseDroidPlannerPrefs(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public FirmwareType getVehicleType() {
        String str = prefs.getString("pref_vehicle_type", FirmwareType.ARDU_COPTER.toString());
        return FirmwareType.firmwareFromString(str);
    }

    @Override
    public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
        return VehicleProfileReader.load(context, firmwareType);
    }

    @Override
    public Rates getRates() {
        Rates rates = new Rates();

        rates.extendedStatus = Integer.parseInt(prefs.getString(
                "pref_mavlink_stream_rate_ext_stat", "0"));
        rates.extra1 = Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra1", "0"));
        rates.extra2 = Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra2", "0"));
        rates.extra3 = Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra3", "0"));
        rates.position = Integer
                .parseInt(prefs.getString("pref_mavlink_stream_rate_position", "0"));
        rates.rcChannels = Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_rc_channels",
                "0"));
        rates.rawSensors = Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_raw_sensors",
                "0"));
        rates.rawController = Integer.parseInt(prefs.getString(
                "pref_mavlink_stream_rate_raw_controller", "0"));
        return rates;
    }
}
