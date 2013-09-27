package de.dfki.appsensor.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import de.dfki.appsensor.R;
import de.dfki.appsensor.logging.BackgroundService;
import de.dfki.appsensor.sync.SyncThread;
import de.dfki.appsensor.utils.NetUtils;
import de.dfki.appsensor.utils.UIUtils;
import de.dfki.appsensor.utils.Utils;

/**
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class HomeActivity extends Activity {
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		BackgroundService.startByIntent(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	// -------------------------------------------------------
	// handling click events
	// -------------------------------------------------------
	

	public void onSettingsButtonClick(View v) {
		UIUtils.openSettingsPage(this);
	}
	
	public void onSubmitButtonClick(View v) {
		Utils.d(this, "onSubmitButtonClick");
		
		Utils.dToast(NetUtils.getScriptURL(this));
		
		SyncThread.getSyncThread(this).requestSync();
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}