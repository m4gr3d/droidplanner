package org.droidplanner.android.utils.prefs;

import java.util.UUID;

import org.droidplanner.R;
import org.droidplanner.android.lib.prefs.AutoPanMode;
import org.droidplanner.android.lib.prefs.BaseDroidPlannerPrefs;
import org.droidplanner.android.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * {@inheritDoc}
 * 
 * Over time it might be good to move the various places that are doing
 * prefs.getFoo(blah, default) here - to collect prefs in one place and avoid
 * duplicating string constants (which tend to become stale as code evolves).
 * This is called the DRY (don't repeat yourself) principle of software
 * development.
 * 
 * 
 */
public class DroidPlannerPrefs extends BaseDroidPlannerPrefs {

	/*
	 * Default preference value
	 */
	public static final boolean DEFAULT_USAGE_STATISTICS = true;
	public static final String DEFAULT_CONNECTION_TYPE = Utils.ConnectionType.USB.name();
	private static final boolean DEFAULT_KEEP_SCREEN_ON = false;
	private static final boolean DEFAULT_MAX_VOLUME_ON_START = false;
	private static final boolean DEFAULT_PERMANENT_NOTIFICATION = true;
	private static final boolean DEFAULT_OFFLINE_MAP_ENABLED = false;
	private static final String DEFAULT_MAP_TYPE = "";
	private static final AutoPanMode DEFAULT_AUTO_PAN_MODE = AutoPanMode.DISABLED;
	private static final boolean DEFAULT_GUIDED_MODE_ON_LONG_PRESS = true;
	public static final boolean DEFAULT_PREF_UI_LANGUAGE = false;

    public DroidPlannerPrefs(Context context) {
        super(context);
    }

    public boolean getLiveUploadEnabled() {
		return prefs.getBoolean("pref_live_upload_enabled", false);
	}

	public String getDroneshareLogin() {
		return prefs.getString("dshare_username", "").trim();
	}

	public void setDroneshareLogin(String b) {
		prefs.edit().putString("dshare_username", b.trim()).apply();
	}

	public String getDroneshareEmail() {
		return prefs.getString("dshare_email", "").trim();
	}

	public void setDroneshareEmail(String b) {
		prefs.edit().putString("dshare_email", b.trim()).apply();
	}

	public String getDronesharePassword() {
		return prefs.getString("dshare_password", "").trim();
	}

	public void setDronesharePassword(String b) {
		prefs.edit().putString("dshare_password", b.trim()).apply();
	}

	public boolean getDroneshareEnabled() {
		return prefs.getBoolean("dshare_enabled", true);
	}

	public void setDroneshareEnabled(boolean b) {
		prefs.edit().putBoolean("dshare_enabled", b).apply();
	}

	/**
	 * How many times has this application been started? (will increment for
	 * each call)
	 */
	public int getNumberOfRuns() {
		int r = prefs.getInt("num_runs", 0) + 1;

		prefs.edit().putInt("num_runs", r).apply();

		return r;
	}

	/**
	 * Return a unique ID for the vehicle controlled by this tablet. FIXME,
	 * someday let the users select multiple vehicles
	 */
	public String getVehicleId() {
		String r = prefs.getString("vehicle_id", "").trim();

		// No ID yet - pick one
		if (r.isEmpty()) {
			r = UUID.randomUUID().toString();

			prefs.edit().putString("vehicle_id", r).apply();
		}
		return r;
	}

	/**
	 * @return true if google analytics reporting is enabled.
	 */
	public boolean isUsageStatisticsEnabled() {
		return prefs.getBoolean(context.getString(R.string.pref_usage_statistics_key),
				DEFAULT_USAGE_STATISTICS);
	}

	/**
	 * @return the selected mavlink connection type.
	 */
	public String getMavLinkConnectionType() {
		return prefs.getString(context.getString(R.string.pref_connection_type_key),
				DEFAULT_CONNECTION_TYPE);
	}

	/**
	 * @return true if the device screen should stay on.
	 */
	public boolean keepScreenOn() {
		return prefs.getBoolean(context.getString(R.string.pref_keep_screen_bright_key),
				DEFAULT_KEEP_SCREEN_ON);
	}

	/**
	 * @return true if Volume should be set to 100% on app start
	 */
	public boolean maxVolumeOnStart() {
		return prefs.getBoolean(context.getString(R.string.pref_request_max_volume_key),
				DEFAULT_MAX_VOLUME_ON_START);
	}

	/**
	 * @return true if the status bar notification should be permanent when
	 *         connected.
	 */
	public boolean isNotificationPermanent() {
		return prefs.getBoolean(context.getString(R.string.pref_permanent_notification_key),
				DEFAULT_PERMANENT_NOTIFICATION);
	}

	/**
	 * @return true if offline map is enabled (if supported by the map
	 *         provider).
	 */
	public boolean isOfflineMapEnabled() {
		return prefs.getBoolean(context.getString(R.string.pref_advanced_use_offline_maps_key),
				DEFAULT_OFFLINE_MAP_ENABLED);
	}

	/**
	 * @return the selected map type (if supported by the map provider).
	 */
	public String getMapType() {
		return prefs.getString(context.getString(R.string.pref_map_type_key), DEFAULT_MAP_TYPE);
	}

	/**
	 * @return the target for the map auto panning.
	 */
	public AutoPanMode getAutoPanMode() {
		final String defaultAutoPanModeName = DEFAULT_AUTO_PAN_MODE.name();
		final String autoPanTypeString = prefs.getString(AutoPanMode.PREF_KEY,
				defaultAutoPanModeName);
		try {
			return AutoPanMode.valueOf(autoPanTypeString);
		} catch (IllegalArgumentException e) {
			return DEFAULT_AUTO_PAN_MODE;
		}
	}

	/**
	 * Updates the map auto panning target.
	 * 
	 * @param target
	 */
	public void setAutoPanMode(AutoPanMode target) {
		prefs.edit().putString(AutoPanMode.PREF_KEY, target.name()).apply();
	}

	public boolean isGuidedModeOnLongPressEnabled() {
		return prefs
				.getBoolean("pref_guided_mode_on_long_press", DEFAULT_GUIDED_MODE_ON_LONG_PRESS);
	}

	public String getBluetoothDeviceAddress() {
		return prefs.getString(context.getString(R.string.pref_bluetooth_device_address_key), null);
	}

	public void setBluetoothDeviceAddress(String newAddress) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(context.getString(R.string.pref_bluetooth_device_address_key), newAddress)
				.apply();
	}

	/**
	 * Use HDOP instead of satellite count on infobar
	 */
	public boolean shouldGpsHdopBeDisplayed() {
		return prefs.getBoolean(context.getString(R.string.pref_ui_gps_hdop_key), false);
	}

	public boolean isEnglishDefaultLanguage() {
		return prefs.getBoolean(context.getString(R.string.pref_ui_language_english_key),
				DEFAULT_PREF_UI_LANGUAGE);
	}

	public String getMapProviderName() {
		return prefs.getString(context.getString(R.string.pref_maps_providers_key), null);
	}
}
