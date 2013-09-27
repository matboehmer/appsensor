package de.dfki.appsensor.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateUtils;
import de.dfki.appsensor.data.AppUsageProvider;
import de.dfki.appsensor.utils.NetUtils;
import de.dfki.appsensor.utils.Utils;

/**
 * This thread is responsible for requesting the sync of data with the server
 * 
 * @author Lyubomir Ganev, Matthias Boehmer, matthias.boehmer@dfki.de
 * 
 */
public class SyncThread extends Thread {
	
	private static final Object semaphore = new Object();
	
	/** handler for thread messages */
	private Handler mHandler;
	
	/** context of the app */
	protected static Context context;

	
	private static final int DO_REQUEST_SYNC = 0;
	
	/** period of running app usage checks (in milliseconds) */
	private static long TIME_SYNC_INTERVAL = DateUtils.HOUR_IN_MILLIS * 6;
	private static final long TIME_IMMEDIATELY = 0;
	
	/** singleton */
	private static SyncThread instance;

	

	/**
	 * singleton for this component
	 * @param context
	 * @return
	 */
	public static SyncThread getSyncThread(Context context) {
		synchronized (semaphore) {
			if (instance == null) {
				instance = new SyncThread(context);
			}
		}
		return instance;
	}
	
	
	/**
	 * creates a new app logger that needs to be started afterwards
	 * @param context
	 */
	private SyncThread(Context c) {
		super();
		Utils.d(this, "created new SyncThread");
		context = c;
	}

	
	@Override
	public void run() {
		Utils.d(this, "running the thread");
		Looper.prepare();

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if(msg.what == DO_REQUEST_SYNC) {
					
					Utils.d(SyncThread.this, "event DO_REQUEST_SYNC");
					
					boolean SYNC_WIFI_ONLY = NetUtils.doSyncWifiOnly(SyncThread.context);

					if (!SYNC_WIFI_ONLY) { 
						Utils.d(SyncThread.this, "try to sync via any network");
						doSync();
					} else if (SYNC_WIFI_ONLY) {

						WifiManager wfm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
						WifiInfo info = wfm.getConnectionInfo();
						
						if ((info != null) && (info.getNetworkId() != -1)) {
							Utils.d(SyncThread.this, "try to sync via wifi");
							doSync();
						} else {
							Utils.d(SyncThread.this, "we want wifi by do not have wifi connected");
						}
					}
				}
				
				// re-schedule the request
				mHandler.sendEmptyMessageDelayed(DO_REQUEST_SYNC, TIME_SYNC_INTERVAL);
			}

			/**
			 * do schedule a sync
			 */
			private void doSync() {
				ContentResolver cr = context.getContentResolver();
				cr.notifyChange(AppUsageProvider.CONTENT_URI, null);
				Account[] accounts = AccountManager.get(context).getAccountsByType(Authenticator.ACCOUNT_TYPE);
				for(Account account : accounts) {
					Bundle bundle = new Bundle();
					bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
					bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
					bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
					ContentResolver.requestSync(account, AppUsageProvider.AUTHORITY, bundle);
				}
			}
		};

		// send the first message so that the handler starts running after
		// TIME_SYNC_INTERVAL milliseconds
		mHandler.sendEmptyMessageDelayed(DO_REQUEST_SYNC, TIME_SYNC_INTERVAL);

		Looper.loop();
	}
	
	/**
	 * manually request an immediate sync of the data
	 */
	public void requestSync() {
		Utils.d(this, "sync was requested");
		mHandler.sendEmptyMessageDelayed(DO_REQUEST_SYNC, TIME_IMMEDIATELY);
	}
	
	
	public void setTimeSyncInterval(int minutes) {
		TIME_SYNC_INTERVAL = minutes * DateUtils.MINUTE_IN_MILLIS;
		
		if (mHandler != null) {
			// might have not be created yet
			mHandler.sendEmptyMessageDelayed(DO_REQUEST_SYNC, TIME_SYNC_INTERVAL);
		}
	}
	
}
