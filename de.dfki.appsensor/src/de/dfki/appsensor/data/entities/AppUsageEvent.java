package de.dfki.appsensor.data.entities;

import de.dfki.appsensor.logging.HardwareObserver;
import de.dfki.appsensor.logging.LocationObserver;
import de.dfki.appsensor.utils.Utils;

/**
 * This is a value object for the information required to observe app
 * interaction, i.e. usage and installations. Additionally it also keeps track
 * of the context, that this event took place in (mainly local time, location).
 * 
 * 
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class AppUsageEvent {

	// ---------------------------------------------
	// constants
	// ---------------------------------------------

	public static final short EVENT_UNDEFINED = 0;
	public static final short EVENT_INUSE = 1;
	public static final short EVENT_INSTALLED = 2;
	public static final short EVENT_UNINSTALLED = 3;
	public static final short EVENT_UPDATE = 4;
	// for the applications which were already 
	// installed before the AppSensor was installed
	public static final short EVENT_ALREADYTHERE = 5;
	public static final short EVENT_OPEN = 6;
	
	public static final short STATUS_LOCAL_ONLY = 1;
	public static final short STATUS_SYNCING = 2;
	public static final short STATUS_SERVER_PERSISTED = 3;

	// ---------------------------------------------
	// public properties
	// ---------------------------------------------

	/** the app */
	public String packageName;

	// information on the interaction itself
	public int eventtype = EVENT_UNDEFINED;
	public long starttime = Utils.getCurrentTime();
	
	/**
	 * This is the timespan in milliseconds that the app was used. 
	 */
	public long runtime;
	
	public int taskID;
	
	/**
	 * This is the UTC timestamp of the last screen on event, i.e. the start of
	 * the session where the current app interaction is embedded in. It might be
	 * zero, e.g. if the background logger starts within a session (e.g. when
	 * AppSensor is just being installed).
	 */
	public long timestampOfLastScreenOn = HardwareObserver.timestampOfLastScreenOn;

	/* location information */
	public double longitude = LocationObserver.longitude;
	public double latitude = LocationObserver.latitude;
	public double accuracy = LocationObserver.accuracy;
	public double speed = LocationObserver.speed;
	public double altitude = LocationObserver.altitude;

	/** battery level */
	public short powerstate = HardwareObserver.powerstate;
	public short powerlevel = HardwareObserver.powerlevel;

	/** wheather wifi is on, off or connected */
	public short wifistate = HardwareObserver.wifistate;

	/** wheather bluetooth is on, off or connected */
	public short bluetoothstate = HardwareObserver.bluetoothstate;

	/** the most often measured orientation of the device during this event (portrait or landscape) */
	public short orientation = HardwareObserver.orientation;

	/** state of the headphones of the device, i.e. connected or not */
	public short headphones = HardwareObserver.headphones;
	
	/**
	 * Shows the sync status of the record
	 * 
	 * <li>STATUS_LOCAL_ONLY: interest exists only locally
	 * <li>STATUS_SYNCING: interest is in process of syncing to server
	 * <li>STATUS_SERVER_PERSISTED: interest is successfully synced
	 * 
	 */
	public int syncStatus;

	// ---------------------------------------------
	// constructors
	// ---------------------------------------------

	public AppUsageEvent() {
	}

	public AppUsageEvent(int taskID, String packagename, long starttime, int runtime, int eventtype, int syncStatus) {
		this.taskID = taskID;
		this.packageName = packagename;
		this.starttime = starttime;
		this.runtime = runtime;
		this.eventtype = eventtype;
		this.syncStatus = syncStatus;
	}

	public AppUsageEvent(String packagename, long starttime, int runtime, int eventtype, int syncStatus) {
		this.taskID = 0;
		this.packageName = packagename;
		this.starttime = starttime;
		this.runtime = runtime;
		this.eventtype = eventtype;
		this.syncStatus = syncStatus;
	}

	// ---------------------------------------------
	// technical methods
	// ---------------------------------------------

	@Override
	public String toString() {
		return "AppUsageEvent [packageName=" + packageName + ", taskID=" + taskID + ", eventtype=" + eventtype + ", runtime=" + runtime + ", starttime=" + starttime + ", syncStatus=" + syncStatus + "]";
	}

	/**
	 * Long output of device interaction data for debugging purposes.
	 * 
	 * @return
	 */
	public String toStringLong() {
		return "aue[" +
				"p=" + packageName + ", " +
				//"e=" + eventtype + ", " +
				"s=" + starttime + ", " +
				"t=" + runtime + ", " +
				"or=" + orientation + ", " +
//				"taskID=" + taskID + ", " +
				//"al=" + altitude + ", " +
				"ps=" + powerstate + ", " +
				"pl=" + powerlevel + ", " +
				"wf=" + wifistate + ", " +
				"bt=" + bluetoothstate + ", " +
				"ph=" + headphones + ", " +
				"l=" + longitude + "/" + latitude + //"(~" + accuracy + "," + speed + "), " +
				"tso=" + timestampOfLastScreenOn + ", " +
				//"syn=" + syncStatus +
				"]";
		
	}
	
	public short getOrientationSignum() {
		if (orientation < 0 ) return -1;
		if (orientation > 0 ) return +1;
		return 0;
	}

}