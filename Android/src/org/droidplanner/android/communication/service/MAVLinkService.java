package org.droidplanner.android.communication.service;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.communication.connection.MAVLinkConnection;
import org.droidplanner.android.communication.connection.MAVLinkConnection.MavLinkConnectionListener;

import org.droidplanner.android.communication.connection.bluetooth.BluetoothServer;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.notifications.StatusBarNotificationProvider;
import org.droidplanner.android.utils.DroidplannerPrefs;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.analytics.GAUtils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * http://developer.android.com/guide/components/bound-services.html#Messenger
 * 
 */

public class MAVLinkService extends Service implements	MavLinkConnectionListener {

    private static final String LOG_TAG = MAVLinkService.class.getSimpleName();

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SEND_DATA = 3;

    /**
     * Provides access to the app preferences.
     */
    private DroidplannerPrefs mAppPrefs;

	private MAVLinkConnection mavConnection;
	Messenger msgCenter = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private boolean couldNotOpenConnection = false;

    /**
     * Bluetooth server to relay the mavlink packet to listening connected clients.
     *
     * @since 1.2.0
     */
    private BluetoothServer mBtServer;

    /**
     * This is the bluetooth server relay listener. Handles the messages received by the
     * bluetooth relay server by the connected client devices, and push them through the mavlink
     * connection.
     * @since 1.2.0
     */
    private BluetoothServer.RelayListener mRelayListener = new BluetoothServer.RelayListener() {
        @Override
        public void onMessageToRelay(MAVLinkPacket[] relayedPackets) {
            if (relayedPackets != null) {
                for (MAVLinkPacket packet : relayedPackets){
                    if(mavConnection != null && packet != null)
                        mavConnection.sendMavPacket(packet);
                }

            }
        }
    };

    /**
     * Listens to broadcast events, and appropriately enable or disable the bluetooth relay server.
     * @since 1.2.0
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(Constants.ACTION_BLUETOOTH_RELAY_SERVER.equals(action)){
                boolean isEnabled = intent.getBooleanExtra(Constants
                        .EXTRA_BLUETOOTH_RELAY_SERVER_ENABLED,
                        Constants.DEFAULT_BLUETOOTH_RELAY_SERVER_TOGGLE);

                setupBtRelayServer(isEnabled);
            }
        }
    };

    /**
     * @return true if the user has enabled the bluetooth relay server.
     * @since 1.2.0
     */
    private boolean isBtRelayServerEnabled(){
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(
                Constants.PREF_BLUETOOTH_RELAY_SERVER_TOGGLE,
                Constants.DEFAULT_BLUETOOTH_RELAY_SERVER_TOGGLE);
    }

    /**
     * Setup the bluetooth relay server based on the passed argument.
     * @param isEnabled true to initialize, and start the bluetooth relay server; false to stop it.
     * @since 1.2.0
     */
    private void setupBtRelayServer(boolean isEnabled){
        if(isEnabled){
            if(mBtServer == null)
                mBtServer = new BluetoothServer();
            mBtServer.addRelayListener(mRelayListener);
            mBtServer.start();
        }else if(mBtServer != null){
            mBtServer.stop();
            mBtServer.removeRelayListener(mRelayListener);
            mBtServer = null;
        }
    }

	/**
	 * 
	 * Handler for Communication Errors Messages used in onComError() to display
	 * Toast msg.
	 * 
	 * */

	private String commErrMsgLocalStore;
	private Handler commErrHandler;

	/**
	 * Handler of incoming messages from clients.
	 */
	@SuppressLint("HandlerLeak")
	// TODO fix this error message
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				msgCenter = msg.replyTo;
				if (couldNotOpenConnection) {
					selfDestryService();
				}
				break;
			case MSG_UNREGISTER_CLIENT:
				msgCenter = null;
				break;
			case MSG_SEND_DATA:
				Bundle b = msg.getData();
				MAVLinkPacket packet = (MAVLinkPacket) b.getSerializable("msg");
				if (mavConnection != null) {
					mavConnection.sendMavPacket(packet);
				}
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	private void notifyNewMessage(MAVLinkMessage m) {
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null,
						MAVLinkClient.MSG_RECEIVED_DATA);
				Bundle data = new Bundle();
				data.putSerializable("msg", m);
				msg.setData(data);
				msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReceiveMessage(MAVLinkPacket msgPacket) {
		notifyNewMessage(msgPacket.unpack());

        //Additionally, relay the message to the relay server.
        if (mBtServer != null) {
            //Send the received packet to the connected clients
            mBtServer.relayMavPacket(msgPacket);
        }
	}

    /**
     * Successful mavlink connection, so start the relay server is the user has it enabled.
     */
    @Override
    public void onConnect(){
        //Register a broadcast event receiver in case the relay server is enabled/disabled
        // while the mavlink connection is running.
        registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_BLUETOOTH_RELAY_SERVER));

        final StatusBarNotificationProvider statusBarNotification = ((DroidPlannerApp) getApplication()).mNotificationHandler
                .getStatusBarNotificationProvider();

        statusBarNotification.showNotification();
        statusBarNotification.updateNotification(getResources().getString(
                R.string.connected));

        setupBtRelayServer(isBtRelayServerEnabled());
    }

	@Override
	public void onDisconnect() {
		couldNotOpenConnection = true;
		selfDestryService();

        try{
            //Stop listening for relay server activation broadcast events
            unregisterReceiver(mReceiver);
        }catch(IllegalArgumentException e){
            //Falls here is onDisconnect was called before we had a chance to register.
        }

        final StatusBarNotificationProvider statusBarNotification = ((DroidPlannerApp) getApplication()).mNotificationHandler
                .getStatusBarNotificationProvider();
        statusBarNotification.dismissNotification();

        //Stop the relay server
        setupBtRelayServer(false);
	}

	private void selfDestryService() {
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null,
						MAVLinkClient.MSG_SELF_DESTRY_SERVICE);
				msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate() {
		super.onCreate();

        final Context context = getApplicationContext();
        final DroidPlannerApp dpApp = (DroidPlannerApp) getApplication();

        //Initialise the app preferences handle.
        mAppPrefs = new DroidplannerPrefs(context);

		commErrMsgLocalStore = getString(R.string.MAVLinkError);

		commErrHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Toast.makeText(context, commErrMsgLocalStore, Toast.LENGTH_LONG).show();
			}
		};

		connectMAVConnection();

		final StatusBarNotificationProvider statusBarNotification = dpApp.mNotificationHandler
				.getStatusBarNotificationProvider();

		statusBarNotification.showNotification();
		statusBarNotification.updateNotification(getString(R.string.connected));
	}

	@Override
	public void onDestroy() {
		disconnectMAVConnection();

		final StatusBarNotificationProvider statusBarNotification = ((DroidPlannerApp) getApplication()).mNotificationHandler
				.getStatusBarNotificationProvider();
		statusBarNotification.dismissNotification();

		super.onDestroy();
	}

	/**
	 * Called after the exception raised in any of the MavLinkConnection classes
	 */
	public void onComError(String errMsg) {

		Message errMessageObj = new Message();
		Bundle resBundle = new Bundle();
		resBundle.putString("status", "SUCCESS");
		errMessageObj.obj = resBundle;

		commErrMsgLocalStore = commErrMsgLocalStore + " " + errMsg;
		commErrHandler.sendMessage(errMessageObj);

		Log.d(CONNECTIVITY_SERVICE, commErrMsgLocalStore);
	}

	/**
	 * Toggle the current state of the MAVlink connection. Starting and closing
	 * the as needed. May throw a onConnect or onDisconnect callback
	 */
    private void connectMAVConnection() {
        String connectionType = mAppPrefs.getMavLinkConnectionType();

        Utils.ConnectionType connType = Utils.ConnectionType.valueOf(connectionType);
        mavConnection = connType.getConnection(this);
        mavConnection.start();

        //Record which connection type is used.
        GAUtils.sendEvent(new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.MAVLINK_CONNECTION.toString())
                .setAction( "Mavlink connecting using " + connectionType));
    }

    private void disconnectMAVConnection() {
        Log.d(LOG_TAG, "Pre disconnect");
        if (mavConnection != null) {
            mavConnection.disconnect();
            mavConnection = null;
        }

        GAUtils.sendEvent(new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.MAVLINK_CONNECTION.toString())
                .setAction("Mavlink disconnecting"));
    }

}
