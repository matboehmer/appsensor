package de.dfki.appsensor.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import de.dfki.appsensor.ui.HomeActivity;
import de.dfki.appsensor.ui.SettingsActivity;

/**
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class UIUtils {

	public static void openHomePage(Context c) {
		Intent intent = new Intent(c, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Log.d(Utils.TAG, "UIUtils: HomeActivity");
		c.startActivity(intent);
	}

	public static void openSettingsPage(Context c) {
		Intent intent = new Intent(c, SettingsActivity.class);
		c.startActivity(intent);
	}
	
	public static void openURIView(Context c, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		c.startActivity(intent);
	}

	public static void openURIView(Context c, Uri uri, String headline) {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra("headline", headline); // TODO externalize
		c.startActivity(intent);
	}

	public static void shortToast(Context context, String text) {
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	public static void shortToastNotYetImplemented(Context context) {
		int duration = Toast.LENGTH_SHORT;
		CharSequence text = "Not yet implemented ;-)";
		Toast toast = Toast.makeText(context, text , duration);
		toast.show();
	}

}
