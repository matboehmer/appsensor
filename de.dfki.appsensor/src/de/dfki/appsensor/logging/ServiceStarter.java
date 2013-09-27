package de.dfki.appsensor.logging;

import de.dfki.appsensor.utils.App;
import de.dfki.appsensor.utils.Utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class ServiceStarter extends BroadcastReceiver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent action) {
		Utils.dToast(App.getAppContext(), "onReceive");

		Utils.d(this, "received boot intent -- starting component BackgroundService");
		Intent starter = new Intent(context, BackgroundService.class);
		starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(starter);
	}

}
