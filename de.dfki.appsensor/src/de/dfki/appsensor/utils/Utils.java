package de.dfki.appsensor.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.Log;

/**
 * This class provides some technical utilities, e.g. for logging, as well as
 * constants, e.g. for setting defaults.
 * 
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class Utils {

	/** tag for logging output */
	public static final String TAG = "appsensor";

	/** names of the settings **/
	public static final String PREFS_NAME = "user_conditions";
	public static final String DISCLAIMER_ACK = "disclaimer_acknowledge";
	public static final String SETTINGS_INSTALLATIONID_NAME = "settings_id";
	public static final String SETTINGS_DEVICEID_NAME = "settings_deviceid";
	public static final String SETTINGS_settings_sensorsactive = "settings_sensorsactive";
	public static final String SETTINGS_settings_sensorsamplingrate = "settings_sensorsamplingrate";
	public static final String SETTINGS_settings_serverip = "settings_serverip";
	public static final String SETTINGS_settings_serverport = "settings_serverport";
	public static final String SETTINGS_settings_syncwifionly = "settings_syncwifionly";
	public static final String SETTINGS_settings_syncrate = "settings_syncrate";
	public static final int SETTINGS_settings_syncrate_default = 360;
	public static final int SETTINGS_settings_sensorsamplingrate_default = 500;
	public static final boolean SETTINGS_settings_syncwifionly_default = false;
	public static final boolean SETTINGS_settings_sensorsactive_default = true;

	/**
	 * debugging output, e.g. to enable on-screen debug notifications and
	 * logging output
	 */
	public static final boolean D = false;

	/** charset for communication with the server */
	public static final String CHARSET = "UTF-8";

	/**
	 * this is the default value of the old style ID if we cannot get it from
	 * the API
	 */
	public static final String DEFAULT_DEVICE_ID = "unknownDeviceID";

	/**
	 * returns timestamp in UTC milliseconds
	 * 
	 * @return
	 */
	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}

	private static int offsetUTC = 0;
	private static boolean offsetUTCset = false;

	/**
	 * get offset to UTC in seconds
	 * 
	 * @return
	 */
	public synchronized static int utcOFF() {
		if (offsetUTCset) {
			return offsetUTC;
		} else {
			offsetUTC = Calendar.getInstance().getTimeZone().getRawOffset()
					/ (1000 * 3600);
			offsetUTCset = true;
			return offsetUTC;
		}
	}

	public static void d(Object o, String msg) {
		if (Utils.D) {
			if (o != null) {
				Log.d(o.getClass().getSimpleName(), msg);
			} else {
				Log.d(TAG, "NULL : " + msg);
			}
		}
	}

	public static void e(Object o, String msg) {
		if (Utils.D)
			Log.d(TAG, o.getClass().getSimpleName() + ": " + msg);
	}

	/**
	 * @param o
	 * @param msg
	 */
	public static void d2(Object o, String msg) {
		if (Utils.D) {
			Log.d(TAG, o.getClass().getSimpleName() + ": " + msg);
		}
	}

	/**
	 * prints debug output to log as well as toasting it
	 * 
	 * @param o
	 * @param msg
	 */
	public static void dToast(Context o, String msg) {
		if (Utils.D) {
			UIUtils.shortToast(o, msg);
			Log.e(TAG, "___TOAST___" + o.getClass().getSimpleName() + ": "
					+ msg);
		}
	}

	public static void dToast(String msg) {
		if (Utils.D) {
			dToast(App.getAppContext(), msg);
		}
	}

	public static Object getLastOfList(List<?> l) {
		final int s = l.size();
		if (s == 0)
			return null;
		return l.get(s - 1);
	}

	public static Object getNLastOfList(List<?> l, int n) {
		final int s = l.size();
		if (s == 0)
			return null;
		return l.get(s - n - 1);
	}

	/**
	 * Generates a md5 Hash of the string that's given
	 * 
	 * @param s
	 *            string to hash
	 * @return md5 Hash or if error null
	 */
	public static String md5(String s) {
		if (s == null) {
			e(null, "s == null");
			return null;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e(null, "md5 NoSuchAlgorithmException: " + e.getMessage());
		} catch (NullPointerException e) {
			e(null, " md5 NullPointerException: " + e.getMessage());
		}
		e(null, " md5 sthg wrong...");

		return null;
	}

	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		Utils.d(Utils.class, "copying stream from is to os");
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
			Utils.e(Utils.class, ex.getLocalizedMessage());
		}
	}
}
