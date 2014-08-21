package org.droidplanner.android.glass.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.droidplanner.android.glass.activities.BTDeviceActivity;
import org.droidplanner.android.glass.fragments.BTDeviceCardsFragment;
import org.droidplanner.android.lib.utils.glass.BluetoothBase;
import org.droidplanner.android.lib.utils.glass.GlassBtMessage;
import org.droidplanner.android.lib.utils.glass.GlassUtils;

import java.lang.ref.WeakReference;

/**
* Created by fhuya on 8/21/14.
*/
class BluetoothClient extends BluetoothBase {
    private final WeakReference<DroidPlannerGlassService> mDPServiceRef;
    private final String TAG = BluetoothClient.class.getSimpleName();

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private volatile int mState;

    BluetoothClient(DroidPlannerGlassService droidPlannerGlassService) {
        mDPServiceRef = new WeakReference(droidPlannerGlassService);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device the BluetoothDevice to connect.
     */
    private synchronized void connect(BluetoothDevice device) {
        //Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING && mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        //Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //Start the thread to connect with the given device
        mConnectThread = new ConnectThread(this, device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * {@inheritDoc}
     *
     * Start the ConnectedThread to begin managing a bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Override
    protected synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        //Cancel the thread that completed the connection
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        //Cancel any thread currently running a connection
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(this, socket, mHandler);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    @Override
    protected void connectionFailed() {
        //TODO: figure out what to do. Most likely broadcast the failed connection,
        // and let the user decide what to do.
    }

    @Override
    protected void connectionLost() {
        //TODO: figure out what to do. Most likely broadcast the loss of connection,
        // and let the user decide what to do.
    }

    @Override
    protected void onBtMsgReceived(GlassBtMessage btMsg) {
        final DroidPlannerGlassService dpService = mDPServiceRef.get();
        if(dpService == null){
            return;
        }

        dpService.onBtMsgReceived(btMsg);
    }

    /**
     * Set the current state of the connection
     *
     * @param state An integer defining the current connection state.
     */
    private synchronized void setState(int state) {
        mState = state;
    }

    @Override
    protected void start() {
        final DroidPlannerGlassService dpService = mDPServiceRef.get();
        if(dpService == null){
            return;
        }

        final Context context = dpService.getApplicationContext();

        //Check if a bluetooth device address is stored in the preferences.
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                (context);

        final String pairedBtAddress = preferences.getString(BTDeviceCardsFragment.PREF_PAIRED_BT_ADDRESS, null);
        if (BluetoothAdapter.checkBluetoothAddress(pairedBtAddress)) {
            //Connect to it if it's a valid address.
            final BluetoothDevice device = getBtAdapter().getRemoteDevice(pairedBtAddress);
            if (device != null) {
                connect(device);
                return;
            }
        }

        //pop up a dialog asking the user to select a bluetooth device among a refreshed set.
        dpService.startActivity(new Intent(context, BTDeviceActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the connected thread in an unsynchronized manner.
     * @param out The bytes to write
     */
    private void write(byte[] out){
        //Create temporary object
        ConnectedThread r;

        //Synchronize a copy of the ConnectedThread
        synchronized(this){
            if(mState != STATE_CONNECTED) {return;}
            r = mConnectedThread;
        }

        //Perform the write unsynchronized
        r.write(out);
    }
}
