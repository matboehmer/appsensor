package de.dfki.appsensor.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service for synchronizing data to the server.
 * 
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class AppUsageSyncService extends Service {
	
    private static final Object sSyncAdapterLock = new Object();

    private static AppUsageSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
    	//Utils.dToast("AppUsageSyncService:onCreate");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new AppUsageSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
