package de.dfki.appsensor.utils;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * A wrapper around the application itself, to keep a static reference to the
 * application context and other components.
 * 
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class App extends Application {

	private static Context context;

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		App.context = getApplicationContext();
	}

	
	public static Context getAppContext() {
		return context;
	}
	
	public static WifiManager getWifiManager() {
		return (WifiManager) context.getSystemService(WIFI_SERVICE);
	}
}
