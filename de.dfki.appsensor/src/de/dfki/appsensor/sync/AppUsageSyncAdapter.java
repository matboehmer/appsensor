package de.dfki.appsensor.sync;

import java.util.List;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.format.DateUtils;
import de.dfki.appsensor.data.db.AppUsageEventDAO;
import de.dfki.appsensor.data.entities.AppUsageEvent;
import de.dfki.appsensor.logging.AppUsageLogger;
import de.dfki.appsensor.logging.DeviceObserver;
import de.dfki.appsensor.logging.LocationObserver;
import de.dfki.appsensor.utils.CSVCompressor;
import de.dfki.appsensor.utils.NetUtils;
import de.dfki.appsensor.utils.Utils;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 * 
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class AppUsageSyncAdapter extends AbstractThreadedSyncAdapter {
   
	/**
	*  This is a limit for the maximum number of simultaneously synced app usage
	*  records. It is necessary, because a lot of records to be synced results in
	*  a too much data transfer, which results in http client timeout and the sync
	*  fails forever.
	*/
	private static int MAX_SYNCED_APP_USAGES = 50;
	
    private final Context mContext;
    
    public AppUsageSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }
    
    
    
    private boolean sendAppHistoryToServer() {
		final NetUtils netUtils = new NetUtils(mContext);

		// get everything from the database
		final AppUsageEventDAO d = new AppUsageEventDAO(mContext);
		d.openWrite();
		
		boolean result=false;
		//this is loop is terminated when there are no more portions of records to be synced 
		// or the sync failed at some point
		// There is a just in case limit of 100 iterations, so that it does not loop infinitely
		// in some exceptional case
		int loopLimit = 1;
		while(loopLimit < 100) {
			loopLimit++;
			final List<AppUsageEvent> notSynced = d.getAndUpdateNonSyncedInterestsAsEntities(MAX_SYNCED_APP_USAGES);
			Utils.d(this, "Non synced # of app usage events:" + notSynced.size());
			
			// only send something if we have something in the database
			if (notSynced.size() > AppUsageLogger.MIN_INTERACTIONS_TO_SEND) {
				
				CSVCompressor compressor = new CSVCompressor();
				
				// add header
				compressor.add(new String[]{
						"did", // IMEI based device id
						"iid", // installation id
						"res", // resolution
						"mod", // model name
						"api", // api level
						"apv", // appsensor version
						"eve", // event type
						"pac", // package name
						"sta", // timestamp of starttime in milliseconds UTC
						"off", // offset to UTC in hours
						"run", // runtime of app in milliseconds
						"lon", // longitude of interaction
						"lat", // latitude of interaction
						"alt", // altitude
						"spe", // speed
						"ccd", // country code of this location
						"acc", // accuracy of location information
						"pos", // power state
						"pol", // power level
						"blu", // bluetooth state
						"wif", // wifi state 
						"hea", // headphones state
						"ori", // orientation of the device
						"lso"  // timestamp of last screen on				
				});
				
				// add rows from interaction history
				for (AppUsageEvent aue : notSynced) {
					Utils.d(this, "added " + aue.toStringLong());
					compressor.add(new String[]{
						DeviceObserver.hashedIMEI(mContext),
						DeviceObserver.id(mContext),
						DeviceObserver.resolution(mContext),
						DeviceObserver.modelName(),
						"" + DeviceObserver.apiLevel(),
						"" + DeviceObserver.appsensorVersion(mContext),
						"" + aue.eventtype,
						aue.packageName,
						"" + aue.starttime, // starttime in UTC milliseconds
						"" + Utils.utcOFF(), // offset to UTC in hours
						"" + aue.runtime,
						"" + aue.longitude,
						"" + aue.latitude,
						"" + aue.altitude,
						"" + aue.speed,
						"" + LocationObserver.countryCode,
						"" + aue.accuracy,
						"" + aue.powerstate,
						"" + aue.powerlevel,
						"" + aue.bluetoothstate,
						"" + aue.wifistate,
						"" + aue.headphones,
						"" + aue.orientation,
						"" + aue.timestampOfLastScreenOn
					});
				}
				
				String sentApps = compressor.getCSV();
				// send to the server and remove from local db if worked
				result = netUtils.postToURL(NetUtils.getScriptURL(getContext()), sentApps);
				
				Utils.d(this,sentApps);
				
				// update sync status to server persisted status 
				// only if the send was successful
				if (result) {
					int number = d.updateSyncingToServerPersisted();
					Utils.d(this, "Persisted # of app usages:" + number);
				}
				else break; // if the sync failed, stop the whole sync operation of all portions of records
			}
			else {
				Utils.d(this, "only " + notSynced.size() + " entries, but min is " +  AppUsageLogger.MIN_INTERACTIONS_TO_SEND);
				result = true;
				break; // no more portions of records to be sent, so stop the syncing of portions of records
			}
		}
		
		// clear too old server persisted records
		int rowsAffected = d.deleteOldSyncedUsageEvents(Utils.getCurrentTime() - (DateUtils.HOUR_IN_MILLIS * 6));
		Utils.d(this, "Deleted # of old persisted app usage:" + rowsAffected);
		d.close();
		return result;
	}
    
    
    
//	/**
//	 * Sends the history to the server. 
//	 */
//	private boolean sendAppHistoryToServer() {
//		Utils.d(this, "trying sendAppHistoryToServer");
//		
//		final NetUtils netUtils = new NetUtils(mContext);
//
//		// get everything from the database
//		final AppUsageEventDAO d = new AppUsageEventDAO(mContext);
//		d.openWrite();
//		
//		final List<AppUsageEvent> notSynced = d.getAndUpdateNonSyncedInterestsAsEntities();
//		Utils.d(this, "number of not-synced app usage events: " + notSynced.size());
//		
//		boolean result;
//		// only send something if we have something in the database
//		if (notSynced.size() > AppUsageLogger.MIN_INTERACTIONS_TO_SEND) {
//			
//			CSVCompressor compressor = new CSVCompressor();
//			
//			// add header
//			compressor.add(new String[]{
//					"did", // IMEI based device id
//					"iid", // installation id
//					"res", // resolution
//					"mod", // model name
//					"api", // api level
//					"apv", // appsensor version
//					"eve", // event type
//					"pac", // package name
//					"sta", // timestamp of starttime in milliseconds UTC
//					"off", // offset to UTC in hours
//					"run", // runtime of app in milliseconds
//					"lon", // longitude of interaction
//					"lat", // latitude of interaction
//					"alt", // altitude
//					"spe", // speed
//					"ccd", // country code of this location
//					"acc", // accuracy of location information
//					"pos", // power state
//					"pol", // power level
//					"blu", // bluetooth state
//					"wif", // wifi state 
//					"hea", // headphones state
//					"ori", // orientation of the device
//					"lso"  // timestamp of last screen on
//			});
//			
//			// add rows from interaction history
//			for (AppUsageEvent aue : notSynced) {
//				compressor.add(new String[]{
//					DeviceObserver.hashedIMEI(mContext),
//					DeviceObserver.id(mContext),
//					DeviceObserver.resolution(mContext),
//					DeviceObserver.modelName(),
//					"" + DeviceObserver.apiLevel(),
//					"" + DeviceObserver.appsensorVersion(mContext),
//					"" + aue.eventtype,
//					aue.packageName,
//					"" + aue.starttime, // starttime in UTC milliseconds
//					"" + Utils.utcOFF(), // offset to UTC in hours
//					"" + aue.runtime,
//					"" + aue.longitude,
//					"" + aue.latitude,
//					"" + aue.altitude,
//					"" + aue.speed,
//					"" + LocationObserver.countryCode,
//					"" + aue.accuracy,
//					"" + aue.powerstate,
//					"" + aue.powerlevel,
//					"" + aue.bluetoothstate,
//					"" + aue.wifistate,
//					"" + aue.headphones,
//					"" + aue.orientation,
//					"" + aue.timestampOfLastScreenOn
//				});
//			}
//			
//			
//			// send to the server and remove from local db if worked
//			result = netUtils.postToURL(NetUtils.getScriptURL(getContext()), compressor.getCSV());
//			
//			// update sync status to server persisted status 
//			// only if the send was successful
//			if (result) {
//				int number = d.updateSyncingToServerPersisted();
//				Utils.d(this, "Persisted # of app usages:" + number);
//			}
//		}
//		else {
//			result = true;
//		}
//		// clear too old server persisted records
//		int rowsAffected = d.deleteOldSyncedUsageEvents(Utils.getCurrentTime() - (DateUtils.HOUR_IN_MILLIS * 6));
//		Utils.d(this, "Deleted # of old persisted app usage:" + rowsAffected);
//		d.close();
//		return result;
//	}

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
    	
    	Utils.d(this, "performing sync of app usage");
    	
    	if(!sendAppHistoryToServer()) {
    		Utils.d(this, "Sync of app usage failed");
    		syncResult.stats.numIoExceptions++;
    	}
    }
}
