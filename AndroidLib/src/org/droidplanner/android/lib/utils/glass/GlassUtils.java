package org.droidplanner.android.lib.utils.glass;

import java.util.UUID;

/**
 * Contains functions and constants used by the glass related modules.
 */
public class GlassUtils {

    private static final String CLAZZ_NAME = GlassUtils.class.getName();

    public static final String BT_TOAST_MSG = CLAZZ_NAME + ".BT_TOAST_MSG";

    /**
     * Name for the SDP record when creating server socket.
     */
    public static final String GLASS_BT_NAME_SECURE = "GlassBluetoothSecure";

    /**
     * Unique UUID for this application.
     * //TODO: not quite unique, generate a new one once you get the change.
     */
    public static final UUID GLASS_BT_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    //Not instantiable
    private GlassUtils(){}
}
