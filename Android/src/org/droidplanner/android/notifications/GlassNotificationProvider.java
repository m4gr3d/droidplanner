package org.droidplanner.android.notifications;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import org.droidplanner.android.lib.utils.GlassUtils;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Relays drone data to the connected glass devices via bluetooth.
 */
public class GlassNotificationProvider implements NotificationHandler.NotificationProvider{

    private static final String TAG = GlassNotificationProvider.class.getSimpleName();

    private final BluetoothAdapter mAdapter;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private volatile int mState;

    // Constants that indicate the current connection state
    private static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_LISTEN = 1;     // now listening for incoming connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    private static final int STATE_CONNECTED = 3;  // now connected to a remote device

    GlassNotificationProvider(){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    /**
     * Set the current state of the connection
     * @param state An integer defining the current connection state.
     */
    private synchronized void setState(int state){
        mState = state;
    }

    /**
     * Start the connectivity service.
     * Specifically, start AcceptThread to begin a session in listening (server) mode.
     */
    private void start(){
        //Cancel any thread attempting to make a connection
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        //Cancel any thread currently running a connection
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        //Start the thread to listen on a bluetooth server socker.
        if(mSecureAcceptThread == null){
            mSecureAcceptThread = new AcceptThread();
            mSecureAcceptThread.start();
        }
    }

    @Override
    public void quickNotify(String feedback) {
        //TODO: complete
    }

    @Override
    public void onTerminate() {
        //TODO: close the bluetooth link
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        //TODO: complete
        switch(event){

        }
    }

    /**
     * Start the ConnectedThread to begin managing a bluetooth connection
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
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

        //Cancel the accept thread because we only want to connect to one devicee
        if(mSecureAcceptThread != null){
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        //Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * Stops all threads
     */
    private synchronized void stop(){
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if(mSecureAcceptThread != null){
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     */
    private void write(byte[] out){
        //Create temporary object
        ConnectedThread r;

        //Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if(mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }

        //Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed.
     */
    private void connectionFailed(){
        //Start the service over to restart listening mode
        GlassNotificationProvider.this.start();
    }

    /**
     * Indicate that the connection was lost
     */
    private void connectionLost(){
        //Start the service over to restart listening mode
        GlassNotificationProvider.this.start();
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

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            //Create a new listening server socket
            try{
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(GlassUtils
                        .GLASS_BT_NAME_SECURE, GlassUtils.GLASS_BT_UUID_SECURE);
            } catch(IOException e){
                Log.e(TAG, "BT server listen() failed.", e);
            }
            mServerSocket = tmp;
        }

        @Override
        public void run(){
            setName(TAG + " BT listen thread");

            BluetoothSocket socket = null;

            //Listen to the server socket if we're not connected
            while(mState != STATE_CONNECTED){
                try{
                    //This is a blocking call, and will only return on a successful connection or
                    // an exception
                    socket = mServerSocket.accept();
                }catch(IOException e){
                    Log.e(TAG, "BT server accept() failed.", e);
                    break;
                }

                //If a connection was accepted
                if(socket != null){
                    synchronized(GlassNotificationProvider.this){
                        switch(mState){
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;

                            case STATE_NONE:
                            case STATE_CONNECTED:
                                //Either not ready or already connected. Terminate new socket.
                                try{
                                    socket.close();
                                } catch(IOException e){
                                    Log.e(TAG, "Could not close unwanted socket.", e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel(){
            try{
                mServerSocket.close();
            }catch(IOException e){
                Log.e(TAG, "BT server accept thread close() failed.", e);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a device. It runs
     * straight through; the connection either succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device){
            mDevice = device;
            BluetoothSocket tmp = null;

            //Get a BluetoothSocket for a connection with the given BluetoothDevice
            try{
                tmp = device.createRfcommSocketToServiceRecord(GlassUtils.GLASS_BT_UUID_SECURE);
            } catch(IOException e){
                Log.e(TAG, "BT server create() failed.", e);
            }
            mSocket = tmp;
        }

        @Override
        public void run(){
            Log.i(TAG, "Beginning connect thread.");
            setName(TAG + " connect thread.");

            //Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            //Make a connection to the BluetoothSocket
            try{
                //This is a blocking call and will only return on a successful connection or an
                // exception
                mSocket.connect();
            }catch(IOException e){
                //Close the socket
                try{
                    mSocket.close();
                }catch(IOException f){
                    Log.e(TAG, "Unable to close() during connection failure.", f);
                }

                connectionFailed();
                return;
            }

            //Reset the ConnectThread because we're done.
            synchronized(GlassNotificationProvider.this){
                mConnectThread = null;
            }

            //Start the connected thread
            connected(mSocket, mDevice);
        }

        public void cancel(){
            try{
                mSocket.close();
            } catch(IOException e){
                Log.e(TAG, "BT server connect thread close() failed.",e );
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "BT server connected thread created.");
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Get the BluetoothSocket input and output streams
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch(IOException e){
                Log.e(TAG, "temp sockets not created.", e);
            }

            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        @Override
        public void run(){
            Log.i(TAG, "Beginning connected thread.");
            setName(TAG + " connected thread.");

            byte[] buffer = new byte[1024];
            int bytes;

            //Keep listening to the input stream while connected.
            while(true){
                try{
                    //Read from the input stream
                    bytes = mInStream.read(buffer);

                    //TODO: parse the obtained bytes, and relay the decoded message.
                }catch(IOException e){
                    Log.e(TAG, "BT server connected thread disconnected.", e);
                    connectionLost();

                    //Start the service over to restart listening mode
                    GlassNotificationProvider.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected output stream.
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer){
            try{
                mOutStream.write(buffer);
            }catch(IOException e){
                Log.e(TAG, "Exception during write.", e);
            }
        }

        public void cancel(){
            try{
                mSocket.close();
            }catch(IOException e){
                Log.e(TAG, "BT server connected thread close() failed.", e);
            }
        }
    }
}
