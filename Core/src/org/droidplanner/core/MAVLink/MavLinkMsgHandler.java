package org.droidplanner.core.MAVLink;

import org.droidplanner.core.model.Drone;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_attitude;
import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;
import com.MAVLink.Messages.ardupilotmega.msg_gps_raw_int;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.ardupilotmega.msg_mission_current;
import com.MAVLink.Messages.ardupilotmega.msg_nav_controller_output;
import com.MAVLink.Messages.ardupilotmega.msg_radio;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_raw;
import com.MAVLink.Messages.ardupilotmega.msg_servo_output_raw;
import com.MAVLink.Messages.ardupilotmega.msg_statustext;
import com.MAVLink.Messages.ardupilotmega.msg_sys_status;
import com.MAVLink.Messages.ardupilotmega.msg_vfr_hud;
import com.MAVLink.Messages.enums.MAV_MODE_FLAG;
import com.MAVLink.Messages.enums.MAV_STATE;

public class MavLinkMsgHandler {

	private Drone drone;

	public MavLinkMsgHandler(Drone drone) {
		this.drone = drone;
	}

	public void receiveData(MAVLinkMessage msg) {
		if (drone.getParameters().processMessage(msg)) {
			return;
		}

		drone.getWaypointManager().processMessage(msg);
		drone.getCalibrationSetup().processMessage(msg);

		switch (msg.msgid) {
		case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
			msg_attitude m_att = (msg_attitude) msg;
			drone.getOrientation().setRollPitchYaw(m_att.roll * 180.0 / Math.PI, m_att.pitch * 180.0
                    / Math.PI, m_att.yaw * 180.0 / Math.PI);
			break;
		case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
			msg_vfr_hud m_hud = (msg_vfr_hud) msg;
			drone.setAltitudeGroundAndAirSpeeds(m_hud.alt, m_hud.groundspeed, m_hud.airspeed,
					m_hud.climb);
			checkIsFlying(m_hud);
			break;
		case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
			drone.getMissionStats().setWpno(((msg_mission_current) msg).seq);
			break;
		case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
			msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
			drone.setDisttowpAndSpeedAltErrors(m_nav.wp_dist, m_nav.alt_error, m_nav.aspd_error);
			drone.getNavigation().setNavPitchRollYaw(m_nav.nav_pitch, m_nav.nav_roll, m_nav.nav_bearing);
			break;

		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			msg_heartbeat msg_heart = (msg_heartbeat) msg;
			drone.setType(msg_heart.type);
			processState(msg_heart);
			ApmModes newMode = ApmModes.getMode(msg_heart.custom_mode, drone.getType());
			drone.getState().setMode(newMode);
			drone.onHeartbeat(msg_heart);
			break;

		case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
			drone.getGps().setPosition(new Coord2D(((msg_global_position_int) msg).lat / 1E7,
                    ((msg_global_position_int) msg).lon / 1E7));
			break;
		case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
			msg_sys_status m_sys = (msg_sys_status) msg;
			drone.getBattery().setBatteryState(m_sys.voltage_battery / 1000.0, m_sys.battery_remaining,
                    m_sys.current_battery / 100.0);
			break;
		case msg_radio.MAVLINK_MSG_ID_RADIO:
			msg_radio m_radio = (msg_radio) msg;
			drone.getRadio().setRadioState(m_radio.rxerrors, m_radio.fixed, m_radio.rssi,
                    m_radio.remrssi, m_radio.txbuf, m_radio.noise, m_radio.remnoise);
			break;
		case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
			drone.getGps().setGpsState(((msg_gps_raw_int) msg).fix_type,
                    ((msg_gps_raw_int) msg).satellites_visible, ((msg_gps_raw_int) msg).eph);
			break;
		case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
			drone.getRC().setRcInputValues((msg_rc_channels_raw) msg);
			break;
		case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
			drone.getRC().setRcOutputValues((msg_servo_output_raw) msg);
			break;
		case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
			msg_statustext msg_statustext = (msg_statustext)msg;
			String message = msg_statustext.getText();
			if(message.length()>7){
				if(message.substring(0,7).equals("PreArm:")||message.substring(0,4).equals("Arm:")){
					drone.getState().setWarning(message);
				}
			}
			break;
		}
	}

	public void processState(msg_heartbeat msg_heart) {
		checkArmState(msg_heart);
		checkFailsafe(msg_heart);
	}

	public void checkIsFlying(msg_vfr_hud m_hud) {
		drone.getState().setIsFlying(m_hud.throttle > 0);
	}

	private void checkFailsafe(msg_heartbeat msg_heart) {
		boolean failsafe2 = msg_heart.system_status == (byte) MAV_STATE.MAV_STATE_CRITICAL;
		if(failsafe2){
			drone.getState().setWarning("Failsafe");
		}
	}

	private void checkArmState(msg_heartbeat msg_heart) {
		drone.getState()
				.setArmed((msg_heart.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED);
	}
}
