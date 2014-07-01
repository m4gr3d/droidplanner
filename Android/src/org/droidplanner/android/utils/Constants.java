package org.droidplanner.android.utils;

import org.droidplanner.android.utils.Utils.ConnectionType;
import static org.droidplanner.android.glass.utils.GlassUtils.isGlassDevice;

/**
 * Contains application related constants.
 */
public class Constants {

    /**
     * Class package name.
     */
    public static final String PACKAGE_NAME = Constants.class.getPackage().getName();

    /*
     * Preferences, and default values.
     */
    public static final String PREF_BLUETOOTH_DEVICE_ADDRESS = "pref_bluetooth_device_address";

    /**
     * Sets whether or not the default language for the ui should be english.
     */
    public static final String PREF_UI_LANGUAGE = "pref_ui_language_english";

    /**
     * By default, the system language should be used for the ui.
     */
    public static final boolean DEFAULT_PREF_UI_LANGUAGE = false;
    /**
     * This is the default mavlink connection type
     *
     * @since 1.2.0
     */
    public static final String DEFAULT_CONNECTION_TYPE = isGlassDevice()
            ? ConnectionType.BLUETOOTH.name()
            : ConnectionType.USB.name();

    /**
     * Preference key for the drone settings' category.
     */
    public static final String PREF_DRONE_SETTINGS = "pref_drone_settings";
    /**
     * This preference controls the activation of the mavlink bluetooth relay server.
     * @since 1.2.0
     */
    public static final String PREF_BLUETOOTH_RELAY_SERVER_TOGGLE =
            "pref_bluetooth_relay_server_toggle";

    /**
     * By default, the mavlink bluetooth relay server is turned off.
     * @since 1.2.0
     */
    public static final boolean DEFAULT_BLUETOOTH_RELAY_SERVER_TOGGLE = false;

	/**
	 * Preference screen grouping the ui related preferences.
	 */
	public static final String PREF_UI_SCREEN = "pref_ui";

    /*
    Intent actions
     */
    private static final String PREFIX_ACTION = PACKAGE_NAME + ".action.";
    public static final String ACTION_BLUETOOTH_RELAY_SERVER = PREFIX_ACTION + "RELAY_SERVER";


    /*
    Bundle extras
     */
    private static final String PREFIX_EXTRA = PACKAGE_NAME + ".extra.";
    public static final String EXTRA_BLUETOOTH_RELAY_SERVER_ENABLED = PREFIX_EXTRA +
            "BLUETOOTH_RELAY_SERVER_ENABLED";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Constants() {}
}
