package org.droidplanner.android.notifications;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import org.droidplanner.android.lib.utils.ParcelableUtils;
import org.droidplanner.android.lib.utils.glass.BluetoothBase;
import org.droidplanner.android.lib.utils.glass.GlassBtMessage;
import org.droidplanner.android.lib.utils.glass.GlassUtils;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.AbstractDrone;

import java.io.IOException;

/**
 * Relays drone data to the connected glass devices via bluetooth.
 */
public class GlassNotificationProvider extends BluetoothBase implements NotificationHandler
        .NotificationProvider {

    private static final String TAG = GlassNotificationProvider.class.getSimpleName();

    private AcceptThread mSecureAcceptThread;
    private BluetoothBase.ConnectedThread mConnectedThread;
    private volatile int mState;

    GlassNotificationProvider() {
        mState = STATE_NONE;
        start();
    }

    /**
     * Set the current state of the connection
     *
     * @param state An integer defining the current connection state.
     */
    private synchronized void setState(int state) {
        mState = state;
    }

    /**
     * Start the connectivity service.
     * Specifically, start AcceptThread to begin a session in listening (server) mode.
     */
    @Override
    protected void start() {
        //Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        //Start the thread to listen on a bluetooth server socker.
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread();
            mSecureAcceptThread.start();
        }
    }

    @Override
    public void quickNotify(String feedback) {
        final GlassBtMessage btMsg = new GlassBtMessage(GlassUtils.BT_TOAST_MSG, Toast.LENGTH_LONG,
                feedback, null);
        write(ParcelableUtils.marshall(btMsg));
    }

    @Override
    public void onTerminate() {
        stop();
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, AbstractDrone drone) {
        switch (event) {
            case CONNECTED:
                break;

            case DISCONNECTED:
                break;

            case ORIENTATION:
                break;

            case SPEED:
                break;

            case STATE:
                break;
        }
    }

    /**
     * Start the ConnectedThread to begin managing a bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Override
    protected synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        //Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //Cancel the accept thread because we only want to connect to one devicee
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        //Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(this, socket, mHandler);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * Stops all threads
     */
    @Override
    protected synchronized void stop() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     */
    private void write(byte[] out) {
        //Create temporary object
        ConnectedThread r;

        //Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }

        //Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed.
     */
    @Override
    protected void connectionFailed() {
        //Start the service over to restart listening mode
        GlassNotificationProvider.this.start();
    }

    /**
     * Indicate that the connection was lost
     */
    @Override
    protected void connectionLost() {
        //Start the service over to restart listening mode
        GlassNotificationProvider.this.start();
    }

    @Override
    protected void onBtMsgReceived(GlassBtMessage btMsg) {
        //TODO: handle the received message.
    }

    /**
     * This thread runs while listening for incoming connections. It behaves like a server-side
     * client. It runs until a connection is accepted (or until cancelled).
     */
    private class AcceptThread extends Thread {
        /**
         * The local server socket
         */
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            //Create a new listening server socket
            try {
                tmp = getBtAdapter().listenUsingRfcommWithServiceRecord(GlassUtils
                        .GLASS_BT_NAME_SECURE, GlassUtils.GLASS_BT_UUID_SECURE);
            } catch (IOException e) {
                Log.e(TAG, "BT server listen() failed.", e);
            }
            mServerSocket = tmp;
        }

        @Override
        public void run() {
            setName(TAG + " BT listen thread");

            BluetoothSocket socket = null;

            //Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    //This is a blocking call, and will only return on a successful connection or
                    // an exception
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "BT server accept() failed.", e);
                    break;
                }

                //If a connection was accepted
                if (socket != null) {
                    synchronized (GlassNotificationProvider.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;

                            case STATE_NONE:
                            case STATE_CONNECTED:
                                //Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket.", e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "BT server accept thread close() failed.", e);
            }
        }
    }


}
