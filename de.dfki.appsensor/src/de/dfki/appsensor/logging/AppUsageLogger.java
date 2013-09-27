package de.dfki.appsensor.logging;

import java.util.ArrayList;
import java.util.List;


import de.dfki.appsensor.data.db.AppUsageEventDAO;
import de.dfki.appsensor.data.entities.AppUsageEvent;
import de.dfki.appsensor.utils.App;
import de.dfki.appsensor.utils.Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;

/**
 * This runnable observes a users device interaction and makes it persistent to
 * the local database. It tracks the application usage and saves events in
 * memory (in a list). At the end of a session (i.e. when the device goes into
 * standby) the data is saved to a local database on the mobile device.
 * database.
 * 
 * This component is a thread that can be controlled through the start and pause
 * methods.
 * 
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class AppUsageLogger extends Thread {

	/** number of recent tasks that will be tracked form the API */
	private static final int MAX_TASKS = 1;

	/** period of running app usage checks (in milliseconds) */
	public static long TIME_APPCHECKINTERVAL = 500;
	private static final long TIME_IMMEDIATELY = 5;

	/** states of the thread */
	private static final int DO_OBSERVE = 0;
	private static final int DO_PAUSE = 1;
	private static final int DO_START = 2;

	/** task id for the app that was open before going into standby */
	private static final int LAST_BEFORE_STANDBY = -1;

	/** package name of the phone app */
	private static final Object PHONEPACKAGENAME = "com.android.phone";

	// CHANGEONRELEASE
	/** number of items in table that we cache on the mobile before sending data to the server */
	public static final int MIN_INTERACTIONS_TO_SEND = 1;

	/** history of apps that have been used in current session */
	public static ArrayList<AppUsageEvent> appHistory;

	public static boolean ACTIVE = true;

	
	
	/** general history of other app events */
	private ArrayList<AppUsageEvent> generalInteractionHistory;
	
	/** reference to the activity manager for retrieving info on current apps */
	private ActivityManager activityManager;

	/** the previous app */
	private AppUsageEvent lastApp;

	/** list of recently running apps */
	private List<RunningTaskInfo> apps = null;
	
	/** handler for thread messages */
	private Handler mHandler;

	/** number of apps that are already in our list */
	private int lengthHistory;

	/** context of the app */
	private Context context;

	/** to check if keyboard is locked */
	private KeyguardManager keyManager;

	
	
	private static final Object semaphore = new Object();

	private static AppUsageLogger instance = null;
	
	public static AppUsageLogger getAppUsageLogger(Context c) {
		synchronized (semaphore) {
			if (instance == null)
				instance = new AppUsageLogger(c);	
		}
		
		return instance;
	}
	
	
	/**
	 * creates a new app logger that needs to be started afterwards
	 * @param context
	 */
	private AppUsageLogger(Context context) {
		super();
		Utils.d(this, "constructed");
		this.context = context;
		keyManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		appHistory = new ArrayList<AppUsageEvent>();
		generalInteractionHistory = new ArrayList<AppUsageEvent>();
		activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	}

	/**
	 * Analyzes the runtime of the currently used application and adds the
	 * information to the history (list of recent apps) in memory.
	 * 
	 * @param forceClosingHistory
	 */
	private void observeCurrentApp(boolean forceClosingHistory) {
		synchronized (appHistory) {

			apps = activityManager.getRunningTasks(MAX_TASKS);

			if (apps == null) {
				Utils.e(this, "ups, we cannot look which apps are running; API ERROR");
				return;
			}
			
			/** fill list with running apps */
			for (RunningTaskInfo app : apps) {
				// Utils.d2(this, "we are in the RunningTaskInfo loop");

				lengthHistory = appHistory.size();

				if (lengthHistory > 0) {

					lastApp = appHistory.get(appHistory.size() - 1);

					if (app.id == lastApp.taskID) {

						/** here the previous app is still running */
						lastApp.runtime = Utils.getCurrentTime() - lastApp.starttime;
						lastApp.longitude = LocationObserver.longitude;
						lastApp.latitude = LocationObserver.latitude;
						lastApp.accuracy = LocationObserver.accuracy;
						lastApp.speed = LocationObserver.speed;
						lastApp.altitude = LocationObserver.altitude;
						lastApp.powerstate = HardwareObserver.powerstate;
						
						/** update "running" attributes FIXME build sliding average*/
						lastApp.wifistate = HardwareObserver.wifistate;
						lastApp.bluetoothstate = HardwareObserver.bluetoothstate;
						lastApp.headphones = HardwareObserver.headphones;
						
						// HardwareObserver.orientation is either +1 or -1
						HardwareObserver.updateOrientation(context);
						lastApp.orientation += HardwareObserver.orientation;
						
						if (forceClosingHistory) {
							lastApp.taskID = LAST_BEFORE_STANDBY;
						}
					} else {
						/** here the user has opened a different app */
						if (lastApp.taskID != LAST_BEFORE_STANDBY) {
							// add runtime to the old app
							lastApp.runtime = (Utils.getCurrentTime() - lastApp.starttime);
						}

						Utils.d2(this, lastApp.packageName + "(taskID:" + lastApp.taskID + ") " + " was running " + lastApp.runtime);
						//Utils.dToast(context, lastApp.packageName + " ran " + Math.round(lastApp.runtime/1000) + " s");
						
						if (!forceClosingHistory) {
							
							// create a usage event and fill in all the context information
							AppUsageEvent aue = new AppUsageEvent();
							aue.taskID = app.id;
							aue.packageName = app.baseActivity.getPackageName();
							aue.eventtype = AppUsageEvent.EVENT_INUSE;
							aue.starttime = Utils.getCurrentTime();
							aue.runtime = 0; // since we just started
							aue.longitude = LocationObserver.longitude;
							aue.latitude = LocationObserver.latitude;
							aue.accuracy = LocationObserver.accuracy;
							aue.speed = LocationObserver.speed;
							aue.altitude = LocationObserver.altitude;
							aue.powerstate = HardwareObserver.powerstate;
							aue.wifistate = HardwareObserver.wifistate;
							aue.bluetoothstate = HardwareObserver.bluetoothstate;
							aue.headphones = HardwareObserver.headphones;
							aue.orientation = HardwareObserver.orientation;
							aue.timestampOfLastScreenOn = HardwareObserver.timestampOfLastScreenOn;
							aue.syncStatus = AppUsageEvent.STATUS_LOCAL_ONLY;
							
							appHistory.add(aue);
							Utils.d(this,"added the following app to history: " + aue.packageName);
						}
					}

				} else {

					/** here the user starts his first app */
					AppUsageEvent aue = new AppUsageEvent();
					aue.taskID = app.id;
					aue.runtime = 0; // since we just started
					aue.packageName = app.baseActivity.getPackageName();
					aue.eventtype = AppUsageEvent.EVENT_INUSE;
					aue.starttime = Utils.getCurrentTime();
					aue.longitude = LocationObserver.longitude;
					aue.latitude = LocationObserver.latitude;
					aue.accuracy = LocationObserver.accuracy;
					aue.powerlevel = HardwareObserver.powerlevel;
					aue.speed = LocationObserver.speed;
					aue.altitude = LocationObserver.altitude;
					aue.powerstate = HardwareObserver.powerstate;
					aue.wifistate = HardwareObserver.wifistate;
					aue.bluetoothstate = HardwareObserver.bluetoothstate;
					aue.headphones = HardwareObserver.headphones;
					aue.orientation = HardwareObserver.orientation;
					aue.timestampOfLastScreenOn = HardwareObserver.timestampOfLastScreenOn;
					aue.syncStatus = AppUsageEvent.STATUS_LOCAL_ONLY;
					
					appHistory.add(aue);
					Utils.d(this,"added the first app to history: " + aue.packageName);
				}
				
				
				// BOF
				if (Utils.D) {
					AppUsageEvent lastAUE = getLastInteraction();
					Utils.d(this, lastAUE.toStringLong());
				}
			}

		}
	}

	private AppUsageEvent getLastInteraction() {
		int s = appHistory.size();
		if (s > 0) {
			return appHistory.get(s-1); 
		} else {
			return null;
		}
	}

	/**
	 * Makes the history of app interaction, which is normally written to the
	 * list in memory, persistent to the local database. Usually, this is done
	 * when the device goes into standby mode.
	 */
	private void makeAppHistoryPersistent() {
		Utils.d(this, "making app history persistent from memory");
		AppUsageEventDAO d = new AppUsageEventDAO(context);
		d.openWrite();
		synchronized (appHistory) {
			d.insert(appHistory);
			appHistory.clear();
		}
		synchronized (generalInteractionHistory) {
			d.insert(generalInteractionHistory);
			generalInteractionHistory.clear();
		}
		d.close();
	}
	
	public void logAppInstalled(String packageName) {
		synchronized (generalInteractionHistory) {
			generalInteractionHistory.add(new AppUsageEvent(packageName, Utils.getCurrentTime(), 0, AppUsageEvent.EVENT_INSTALLED, AppUsageEvent.STATUS_LOCAL_ONLY));
		}
	}

	public void logAppRemoved(String packageName) {
		synchronized (generalInteractionHistory) {
			generalInteractionHistory.add(new AppUsageEvent(packageName, Utils.getCurrentTime(), 0, AppUsageEvent.EVENT_UNINSTALLED, AppUsageEvent.STATUS_LOCAL_ONLY));
		}
	}

	public void logAppUpdated(String packageName) {
		synchronized (generalInteractionHistory) {
			generalInteractionHistory.add(new AppUsageEvent(packageName, Utils.getCurrentTime(), 0, AppUsageEvent.EVENT_UPDATE, AppUsageEvent.STATUS_LOCAL_ONLY));
		}
	}

	
	
	
	@Override
	public void run() {
		Utils.d(this, "running the thread");
		Looper.prepare();

		mHandler = new Handler() {
			
			
			
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				
				case DO_OBSERVE:
					if (keyManager.inKeyguardRestrictedInputMode() || (HardwareObserver.screenState == HardwareObserver.SCREEN_OFF) && !getLastInteraction().packageName.equals(PHONEPACKAGENAME)) {
						// screen still to be locked
						mHandler.sendEmptyMessageDelayed(DO_START, TIME_APPCHECKINTERVAL);
					} else {
						observeCurrentApp(false);
						mHandler.sendEmptyMessageDelayed(DO_OBSERVE, TIME_APPCHECKINTERVAL);
					}
					break;
					
				case DO_START:
					if (keyManager.inKeyguardRestrictedInputMode()) {
						// screen still seems to be locked
						//Utils.d2(this, "device is in restricted mode");
						mHandler.sendEmptyMessageDelayed(DO_START, TIME_APPCHECKINTERVAL);
					} else {
						// screen was unlocked by user
						//Utils.d2(this, "device is not in restricted mode anymore");
						mHandler.sendEmptyMessageDelayed(DO_OBSERVE, TIME_IMMEDIATELY);
					}
					break;
					
				case DO_PAUSE:
					observeCurrentApp(true);
					mHandler.removeMessages(DO_START);
					mHandler.removeMessages(DO_OBSERVE);
					// tidy up everything and make history persistent
					makeAppHistoryPersistent();
					break;
				}
			}
		};
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
		boolean activate = prefs.getBoolean(Utils.SETTINGS_settings_sensorsactive, true);
		if (activate) {
			Utils.dToast("AppSensor activated");
			// send the first message so that the handler starts running
			mHandler.sendEmptyMessageDelayed(DO_OBSERVE, TIME_IMMEDIATELY);
		} else {
			Utils.dToast("AppSensor deactivated");
			AppUsageLogger.getAppUsageLogger(App.getAppContext()).pauseLogging();
		}
		

		Looper.loop();
	}

	public void pauseLogging() {
		// send message to the worker thread if initialized
		if (mHandler != null) {
			mHandler.sendEmptyMessageDelayed(DO_PAUSE, TIME_IMMEDIATELY);
		}
	}

	public void startLogging() {
		// send message to the worker thread if initialized
		if (mHandler != null) {
			mHandler.sendEmptyMessageDelayed(DO_START, TIME_IMMEDIATELY);
		}
	}

	/**
	 * Call this method if device screen turns off to check wheter user is
	 * currently on the phone or not (some phones turn the screen off when the
	 * user is coming close to the display to prevent unintended button
	 * pressing). If phone app is running, we will continue to log, otherwise we
	 * will pause the logging.
	 */
	public void checkStandByOnSceenOff() {
		boolean userIsOnThePhone = false;
		
		List<RunningTaskInfo> current = activityManager.getRunningTasks(1);
		if (current.size() > 0) {
			RunningTaskInfo rti = current.get(0);
			userIsOnThePhone = rti.baseActivity.getPackageName().equals(PHONEPACKAGENAME);
		}
		
		// only pause if user is not on the phone, otherwise track phone usage
		if (!userIsOnThePhone) {
			pauseLogging();
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		Utils.d2(this, "destroy()");
	};

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		Utils.d2(this, "finalize()");
	}

}
