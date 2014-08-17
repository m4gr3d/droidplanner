package org.droidplanner.android.lib.utils;

import java.util.UUID;

/**
 * Contains functions and constants used by the glass related modules.
 */
public class GlassUtils {

    //Not instantiable
    private GlassUtils(){}

    /**
     * Name for the SDP record when creating server socket.
     */
    public static final String GLASS_BT_NAME_SECURE = "GlassBluetoothSecure";

    /**
     * Unique UUID for this application.
     * //TODO: not quite unique, generate a new one once you get the change.
     */
    public static final UUID GLASS_BT_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");



}
