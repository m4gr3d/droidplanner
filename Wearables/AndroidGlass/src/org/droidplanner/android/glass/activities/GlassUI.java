package org.droidplanner.android.glass.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;

import org.droidplanner.core.drone.DroneInterfaces;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the main activity for the glass interface.
 */
public abstract class GlassUI extends FragmentActivity {

    private static final String TAG = GlassUI.class.getSimpleName();

    /**
     * Glass gesture detector.
     * Detects glass specific swipes, and taps, and uses it for navigation.
     */
    protected GestureDetector mGestureDetector;

    protected ScheduledExecutorService mThreadMgr;

    /**
     * Screen record tracker fields
     */
    private Process mScreenRecord;
    private final AtomicBoolean mIsRecordingScreen = new AtomicBoolean(false);
    private final Runnable mScreenRecordMonitor = new Runnable() {
        @Override
        public void run() {
            if(mScreenRecord != null && mIsRecordingScreen.get()){
                try {
                    final int exitValue = mScreenRecord.waitFor();
                    mIsRecordingScreen.compareAndSet(true, false);
                }
                catch (InterruptedException e) {
                    Log.e(TAG, "Screen record monitoring thread was interrupted",e);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();
        mGestureDetector = new GestureDetector(context);
    }

    @Override
    public void onStart(){
        super.onStart();
        mThreadMgr = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mThreadMgr != null){
            mThreadMgr.shutdownNow();
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null && event.getSource() == InputDevice.SOURCE_TOUCHPAD) {
            return mGestureDetector.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    /**
     * Used to detect glass specific gestures.
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }

        if(keyCode == KeyEvent.KEYCODE_CAMERA){
            //For debug purposes, trigger recording of the glass screen using 'screenrecord',
            // as well as recording through the glass camera.
            recordScreen();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use screenrecord to record the screen
     */
    private void recordScreen(){
        try {
            if(mScreenRecord == null || !mIsRecordingScreen.get()) {
                //TODO: retrieve storage location using the external storage api.
                mScreenRecord = Runtime.getRuntime().exec("screenrecord /sdcard/Download/dp_screen_record");
                mIsRecordingScreen.compareAndSet(false, true);
                mThreadMgr.execute(mScreenRecordMonitor);
            }
            else{
                mScreenRecord.destroy();
            }
        }
        catch (IOException e) {
            Log.e(TAG, "Unable to start screen record process.", e);
        }
    }

    /**
     * Use the camera to record the user view.
     */
    private void recordViewPoint(){

    }
}
