package org.droidplanner.android.glass.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import org.droidplanner.android.lib.utils.glass.GlassBtMessage;
import org.droidplanner.android.lib.utils.glass.GlassUtils;

/**
 * Glass's DroidPlanner background service.
 * This service establishes a bluetooth bridge with the main app, through which it receives data
 * update for the connected drone.
 */
public class DroidPlannerGlassService extends Service {

    private static final String CLAZZ_NAME = DroidPlannerGlassService.class.getName();

    public static final String ACTION_START_BT_CONNECTION = CLAZZ_NAME +
            ".ACTION_START_BT_CONNECTION";

    /**
     * Handle to the DroidPlanner api implementation.
     */
    private final GlassDrone mGlassDrone = new GlassDrone(this);

    /**
     * Bluetooth link between the glass app, and the mobile app.
     */
    private final BluetoothClient mBtClient = new BluetoothClient(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mGlassDrone;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mBtClient.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_START_BT_CONNECTION.equals(action)){
                mBtClient.start();
            }
        }

        return START_REDELIVER_INTENT;
    }

    public void onBtMsgReceived(GlassBtMessage btMsg) {
        final Context context = getApplicationContext();

        if(GlassUtils.BT_TOAST_MSG.equals(btMsg.getMessageAction())){
            //noinspection ResourceType
            Toast.makeText(context, btMsg.getMessage(), btMsg.getArg()).show();
        }
    }

}
