package de.dfki.appsensor.logging;

import de.dfki.appsensor.utils.Utils;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateUtils;

/**
 * <p>
 * This component observes the users location and always provides the actual
 * location of the user for logging. It provided values like longitude,
 * latitude, altitude, speed, etc.
 * </p>
 * 
 * <p>
 * As an observer, this component just traces the information and makes it
 * available through static variables. The component is running in its own
 * thread and makes all information available through static variables.
 * </p>
 * 
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class LocationObserver extends Thread implements LocationListener {

	// ---------------------------------------------
	// constants
	// ---------------------------------------------

	private static final int DO_PAUSE = 0;
	private static final int DO_START = 1;

	private static final long TIME_IMMEDIATELY = 0;
	private static final long TIME_LOCATIONCACHELOOKUPS = DateUtils.SECOND_IN_MILLIS * 5; // 30
																							// sec

	private static final float GPS_MIN_TIME_SERVICE = DateUtils.SECOND_IN_MILLIS * 5;// 30
																						// sec
	private static final long GPS_MIN_MOVEMENT_SERVICE = 50; // meters

	/** value of longitude if not available */
	public final static double LONGITUDE_UNKNOWN = 0.0;

	/** value of latitude if not available */
	public final static double LATITUDE_UNKNOWN = 0.0;

	/** value of altitude if not available */
	public final static double ALTITUDE_UNKNOWN = -9.9;

	/** value of speed if no accuracy is available */
	public static final double ACCURACY_UNKNOWN = -1;

	/** value of speed if not available */
	public final static double SPEED_UNKNOWN = -1;
	
	/* code if country code is unknown */
	private static final String COUNTRYCODE_UNKNOWN = "--";

	// ---------------------------------------------
	// observed public values of this observer
	// ---------------------------------------------

	/** the current longitude of the user */
	public static double longitude = LONGITUDE_UNKNOWN;

	/** the current latitude of the user */
	public static double latitude = LATITUDE_UNKNOWN;

	/** the current altitude of the user */
	public static double altitude = ALTITUDE_UNKNOWN;

	/** the current speed of the user */
	public static double speed = SPEED_UNKNOWN;

	/** the accuracy of the current location */
	public static double accuracy = ACCURACY_UNKNOWN;
	
	/** the country code of the last location */
	public static String countryCode = COUNTRYCODE_UNKNOWN;

	/**
	 * publish the given location as public static values of the component
	 * 
	 * @param l
	 */
	private static void updateLocation(Location l) {
		if (l != null) {
			longitude = l.getLongitude();
			latitude = l.getLatitude();
			
			// TODO implement country code here, otherwise the server has to resolve it

			if (l.hasAltitude()) {
				altitude = l.getAltitude();
			} else {
				altitude = ALTITUDE_UNKNOWN;
			}

			if (l.hasSpeed()) {
				speed = l.getSpeed();
			} else {
				speed = SPEED_UNKNOWN;
			}

			if (l.hasAccuracy()) {
				accuracy = l.getAccuracy();
			} else {
				accuracy = ACCURACY_UNKNOWN;
			}
		}
	}

	// ---------------------------------------------
	// attributes
	// ---------------------------------------------

	/** access to the application context */
	private Context context;

	/** manager that gives access to the location api */
	private LocationManager locationManager;

	/** handler for thread messages */
	private Handler mHandler;

	// ---------------------------------------------
	// livecycle
	// ---------------------------------------------

	public LocationObserver(Context context) {
		super();
		Utils.d(this, "constructed");
		this.context = context;

		// create a location manager to access location api
		locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * pause this logger, i.e. stop the thread and unregister all listeners
	 */
	public void pauseLogging() {
		Utils.dToast("pause location logging");

		// unregister location listener
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_MIN_MOVEMENT_SERVICE, GPS_MIN_TIME_SERVICE, this);
		locationManager.removeUpdates(this);

		// send message to the worker thread
		mHandler.sendEmptyMessageDelayed(DO_PAUSE, TIME_IMMEDIATELY);
	}

	/**
	 * start this logger, i.e. start the thread and register listeners
	 */
	public void startLogging() {
		Utils.dToast("start location logging");

		// register location listener
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_MIN_MOVEMENT_SERVICE, GPS_MIN_TIME_SERVICE, this);

		// send message to the worker thread
		mHandler.sendEmptyMessageDelayed(DO_START, TIME_IMMEDIATELY);
	}

	// ---------------------------------------------
	// core thread functionality
	// ---------------------------------------------

	@Override
	public void run() {
		Utils.d(this, "running the thread");
		Looper.prepare();

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case DO_START:
					Location l = getLocationFromCache();
					updateLocation(l);
					mHandler.sendEmptyMessageDelayed(DO_START, TIME_LOCATIONCACHELOOKUPS);
					break;
				}
			}

		};

		// send the first message so that the handler starts running
		mHandler.sendEmptyMessageDelayed(DO_START, TIME_IMMEDIATELY);

		Looper.loop();

		// // this is the main loop of the location logger
		// while (!service.isInterrupted() && locationManager != null) {
		//
		// Location l = getLocationFromCache();
		// if (l != null) {
		// LocationEntry tmpEntry = new LocationEntry(Utils.getCurrentTime(),
		// l.getLongitude(), l.getLatitude(), l.getAltitude(), l.getSpeed(),
		// l.getAccuracy(), "fix");
		// if (lastFix != null) {
		// // we already have a location fix
		// if (l.distanceTo(lastFix) > Utils.GPS_MIN_MOVEMENT_SERVICE) {
		// if (Utils.DEBUG_SERVICE) {
		// Utils.debug(Utils.DEBUG_SERVICE_MARK + getClass().getSimpleName() +
		// ": got a last fix and the distance > " +
		// Utils.GPS_MIN_MOVEMENT_SERVICE,
		// Utils.DEBUG_LEVELS.MINOR, Utils.DEBUG_TYPE.DEBUG);
		// }
		// /*
		// * inform everyone that's interested about a new
		// * location
		// */
		// Informer.getInstance().inform(tmpEntry);
		// updatePrefs(l.getLatitude(), l.getLongitude());
		// }
		// } else {
		// // we do not have a location fix yet
		// lastFix = l;
		// updatePrefs(l.getLatitude(), l.getLongitude());
		// }
		// if (!locationList.contains(tmpEntry)) {
		// if (Utils.DEBUG_SERVICE) {
		// Utils.debug(Utils.DEBUG_SERVICE_MARK + getClass().getSimpleName() +
		// ": new Location added to list (thread)", Utils.DEBUG_LEVELS.MINOR,
		// Utils.DEBUG_TYPE.DEBUG);
		// }
		// locationList.add(tmpEntry);
		// }
		//
		// /* clean up, that is write memory to local mobile database */
		// if (locationList.size() > Utils.LOCATION_INMEMORY_MAXCOUNT) {
		// // if list contains more than 10 elements...
		// // then write to db
		// writeLocationsToDatabase();
		// } else if (locationList.size() > 0) {
		// // if we have something in the list
		// if ((Utils.getCurrentTime() - locationList.get(0).getTime()) >
		// (Utils.LOCATION_INMEMORY_MAXAGE)) {
		// // ... and the content is already oder than two hours
		// // ...
		// // then write to db
		// writeLocationsToDatabase();
		// }
		// }
		//
		// }
		//
		//
		// }

	}

	// ---------------------------------------------
	// methods
	// ---------------------------------------------

	/**
	 * Try to retrieve a location from the cache, either from gps or network
	 * based. Whenever another app queries the location provider, there is a
	 * chance that the location providers have something in their caches.
	 * 
	 * @return
	 */
	public Location getLocationFromCache() {
		try {

			// check the gps-based localization cache
			Utils.d(this, "looking up location from gps cache");
			Location gpsCacheLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			// check the net-based localization cache
			Utils.d(this, "looking up location from network cache");
			Location netCacheLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			// if we have both locations, return the latest one
			Location bestLocation = null;
			if (gpsCacheLocation != null && netCacheLocation != null) {
				if (gpsCacheLocation.getTime() > netCacheLocation.getTime()) {
					bestLocation = gpsCacheLocation;
					// Utils.d(this, "gps location cache is more up to date");
				} else {
					bestLocation = netCacheLocation;
					// Utils.d(this, "net location cache is more up to date");
				}
			} else if (gpsCacheLocation != null) {
				bestLocation = gpsCacheLocation;
			} else if (netCacheLocation != null) {
				bestLocation = netCacheLocation;
			} else {
				bestLocation = null;
			}

			// set reminder to location
			if (bestLocation != null) {
				bestLocation.setProvider(bestLocation.getProvider() + "c");
			}

			Utils.d(this, "in cache:" + bestLocation);
			return bestLocation;

		} catch (SecurityException e) {
			Utils.e(this, e.getLocalizedMessage());
			return null;
		} catch (IllegalArgumentException e) {
			Utils.e(this, e.getLocalizedMessage());
			return null;
		}
	}

	/**
	 * This method is part of the location listener and is called -- of course
	 * ;-) -- when the location changes.
	 */
	public void onLocationChanged(Location location) {
		Utils.d(this, "onLocationChanged | " + location.toString());
		updateLocation(location);

	}

	public void onProviderDisabled(String provider) {
		Utils.d(this, "onProviderDisabled");
	}

	public void onProviderEnabled(String provider) {
		Utils.d(this, "onProviderEnabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Utils.d(this, "onStatusChanged");
	}

	// /**
	// * makes all entries in the in-memory list persistent in the local
	// database
	// * of the moile device
	// */
	// public synchronized void writeLocationsToDatabase() {
	// if (context == null) {
	// if (Utils.DEBUG_SERVICE) {
	// Utils.debug(Utils.DEBUG_SERVICE_MARK + this.getClass().getSimpleName() +
	// ": context null (writeLocationsToDatabase)", Utils.DEBUG_LEVELS.MAJOR,
	// Utils.DEBUG_TYPE.DEBUG);
	// }
	// return;
	// }
	// SQLiteDatabase db = null;
	// try {
	// db = context.openOrCreateDatabase(Utils.DB_NAME, Context.MODE_PRIVATE,
	// null);
	// while (db.isDbLockedByOtherThreads() || db.isDbLockedByCurrentThread()) {
	// try {
	// Thread.sleep(500);
	// } catch (InterruptedException e) {
	// if (Utils.DEBUG_SERVICE) {
	// if (Utils.DEBUG_SERVICE) {
	// Utils.debug(Utils.DEBUG_SERVICE_MARK + this.getClass().getSimpleName() +
	// " Could not sleep?? ", Utils.DEBUG_LEVELS.ALL, Utils.DEBUG_TYPE.DEBUG);
	// }
	// }
	// }
	// }
	// db.setLockingEnabled(true);
	//
	// for (LocationEntry location : locationList) {
	// /* create the values for the database and put them in */
	// ContentValues values = new ContentValues();
	// values.put(Utils.DB_COLUMN_TIME, location.getTime());
	// values.put(Utils.DB_COLUMN_LONGITUDE, location.getLongitude());
	// values.put(Utils.DB_COLUMN_LATITUDE, location.getLatitude());
	// values.put(Utils.DB_COLUMN_ALTITUDE, location.getAltitude());
	// values.put(Utils.DB_COLUMN_SPEED, location.getSpeed());
	// values.put(Utils.DB_COLUMN_ACCURACY, location.getAccuracy());
	// values.put(Utils.DB_COLUMN_TYPE, location.getType());
	//
	// db.insert(Utils.SQL_TABLE_NAME_SENSORS, null, values);
	//
	// if (Utils.DEBUG_SERVICE) {
	// Utils.debug(Utils.DEBUG_SERVICE_MARK +
	// "writeLocationsToDb: Location saved to DB: Latitude=" +
	// location.getLatitude() + ", Longitude=" + location.getLongitude() +
	// ", Altitude="
	// + location.getAltitude() + ", Speed=" + location.getSpeed() +
	// ", Accuracy=" + location.getAccuracy(), Utils.DEBUG_LEVELS.ALL,
	// Utils.DEBUG_TYPE.DEBUG);
	// }
	// }
	// /* just to be sure */
	// locationList.clear();
	// } catch (SQLException e) {
	// if (Utils.DEBUG_SERVICE) {
	// Utils.debug("Error opening database: " + e.getMessage(),
	// Utils.DEBUG_LEVELS.MAJOR, Utils.DEBUG_TYPE.ERROR);
	// }
	// if (Utils.DEBUG_SERVICE) {
	// Utils.debug("" + e.getMessage(), Utils.DEBUG_LEVELS.MAJOR,
	// Utils.DEBUG_TYPE.ERROR);
	// }
	// /*
	// * if this doesn't work out, there's something really wrong and we
	// * really don't want to continue!
	// */
	// } finally {
	// if (db != null) {
	// db.close();
	// db.setLockingEnabled(false);
	// }
	// }
	// }
}
