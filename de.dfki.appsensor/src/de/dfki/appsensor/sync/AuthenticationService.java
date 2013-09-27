package de.dfki.appsensor.sync;

import de.dfki.appsensor.utils.Utils;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
* @author Matthias Boehmer, matthias.boehmer@dfki.de
*/
public class AuthenticationService extends Service {
	
    private static final String TAG = "AuthenticationService";
    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
    	Utils.dToast("AuthenticationService::onCreate");
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "SampleSyncAdapter Authentication Service started.");
        }
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
    	Utils.dToast("AuthenticationService::onDestroy");
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "SampleSyncAdapter Authentication Service stopped.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
    	Utils.dToast("AuthenticationService::onBind");
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG,
                "getBinder()...  returning the AccountAuthenticator binder for intent " + intent);
        }
        return mAuthenticator.getIBinder();
    }
}
