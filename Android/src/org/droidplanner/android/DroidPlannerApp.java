package org.droidplanner.android;

import org.droidplanner.android.communication.MAVLinkClient;
import org.droidplanner.android.receivers.NetworkStateReceiver;
import org.droidplanner.android.gcs.follow.Follow;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;

import android.content.Context;
import android.os.SystemClock;

import com.MAVLink.Messages.MAVLinkMessage;

public class DroidPlannerApp extends ErrorReportApp implements
		MAVLinkStreams.MavlinkInputStream, DroneInterfaces.OnDroneListener {

	private Drone drone;
	public Follow followMe;
	public MissionProxy missionProxy;
	private MavLinkMsgHandler mavLinkMsgHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		final Context context = getApplicationContext();

		MAVLinkClient MAVClient = new MAVLinkClient(this, this);
		Clock clock = new Clock() {
			@Override
			public long elapsedRealtime() {
				return SystemClock.elapsedRealtime();
			}
		};
		Handler handler = new Handler() {
			android.os.Handler handler = new android.os.Handler();

			@Override
			public void removeCallbacks(Runnable thread) {
				handler.removeCallbacks(thread);
			}

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				handler.postDelayed(thread, timeout);
			}
		};

		DroidPlannerPrefs pref = new DroidPlannerPrefs(context);
		drone = new Drone(MAVClient, clock, handler, pref);
		drone.events.addDroneListener(this);

		missionProxy = new MissionProxy(getDrone().mission);
		mavLinkMsgHandler = new org.droidplanner.core.MAVLink.MavLinkMsgHandler(
				getDrone());

		followMe = new Follow(this, getDrone());
		NetworkStateReceiver.register(context);

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);
	}

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyConnected() {
		getDrone().events.notifyDroneEvent(DroneEventsType.CONNECTED);
	}

	@Override
	public void notifyDisconnected() {
		getDrone().events.notifyDroneEvent(DroneEventsType.DISCONNECTED);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MISSION_RECEIVED:
			// Refresh the mission render state
			missionProxy.refresh();
			break;
		}
	}

	public Drone getDrone() {
		return drone;
	}
}
