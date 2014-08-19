package org.droidplanner.android.lib.utils.glass;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.droidplanner.android.lib.utils.ParcelableUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * Interface implemented by the Glass bluetooth client and server.
 */
public abstract class BluetoothBase {

    //Type to be used by a handler to signal received messages.
    public static final int MESSAGE_READ = 1;

    // Constants that indicate the current connection state
    protected static final int STATE_NONE = 0;       // we're doing nothing
    protected static final int STATE_LISTEN = 1;     // now listening for incoming connections
    protected static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    protected static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Handle to the bluetooth adapter.
     */
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

    protected final Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MESSAGE_READ:
                    //All received messages should be of type GlassBtMessage
                    final int bytes = msg.arg1;
                    final byte[] buffer = (byte[])msg.obj;
                    if(bytes > 0 && buffer != null){
                        final GlassBtMessage btMsg = ParcelableUtils.unmarshall(buffer, bytes,
                                GlassBtMessage.CREATOR);
                        if(btMsg != null){
                            onBtMsgReceived(btMsg);
                        }
                    }

                    break;
            }
        }
    };

    protected abstract void connected(BluetoothSocket socket, BluetoothDevice device);

    protected abstract void connectionFailed();

    protected abstract void connectionLost();

    protected BluetoothAdapter getBtAdapter() {
        return mAdapter;
    }

    protected abstract void onBtMsgReceived(GlassBtMessage btMsg);

    protected abstract void start();

    protected abstract void stop();

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    protected static class ConnectThread extends Thread {
        private final static String TAG = ConnectThread.class.getSimpleName();

        private final WeakReference<BluetoothBase> mBtImplRef;
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread(BluetoothBase btBase, BluetoothDevice device) {
            mBtImplRef = new WeakReference<BluetoothBase>(btBase);
            mDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(GlassUtils.GLASS_BT_UUID_SECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket creation failed", e);
            }
            mSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "Beginning connect thread");
            setName("Bt ConnectThread");

            final BluetoothBase btBase = mBtImplRef.get();
            if (btBase == null) {
                return;
            }

            // Always cancel discovery because it will slow down a connection
            btBase.getBtAdapter().cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() bt socket during connection failure", e2);
                }
                btBase.connectionFailed();
                return;
            }

            // Start the connected thread
            btBase.connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    protected static class ConnectedThread extends Thread {
        private static final String TAG = ConnectedThread.class.getSimpleName();

        private final WeakReference<BluetoothBase> mBtImplRef;
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;
        private final Handler mHandler;

        public ConnectedThread(BluetoothBase btBase, BluetoothSocket socket, Handler handler) {
            Log.d(TAG, "BT server connected thread created.");
            mBtImplRef = new WeakReference<BluetoothBase>(btBase);
            mSocket = socket;
            mHandler = handler;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created.", e);
            }

            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        @Override
        public void run() {
            Log.i(TAG, "Beginning connected thread.");
            setName(TAG + " connected thread.");

            byte[] buffer = new byte[1024];
            int bytes;

            //Keep listening to the input stream while connected.
            while (true) {
                try {
                    //Read from the input stream
                    bytes = mInStream.read(buffer);

                    if(mHandler != null){
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "BT server connected thread disconnected.", e);
                    final BluetoothBase btBase = mBtImplRef.get();
                    if (btBase != null) {
                        btBase.connectionLost();

                        //Start the service over to restart listening mode
                        btBase.start();
                    }
                    break;
                }
            }
        }

        /**
         * Write to the connected output stream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write.", e);
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "BT server connected thread close() failed.", e);
            }
        }
    }

}
