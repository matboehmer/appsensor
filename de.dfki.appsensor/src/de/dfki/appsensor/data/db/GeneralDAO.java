package de.dfki.appsensor.data.db;

import de.dfki.appsensor.utils.MyDBHelper;
import de.dfki.appsensor.utils.Utils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * This component is a general data access object handling the live cycle of the
 * connection. Other DAOs of the app should subclass this component.
 * 
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public abstract class GeneralDAO {

	private Context context;
	private MyDBHelper dbHelper;
	protected SQLiteDatabase db;

	public GeneralDAO(Context context) {
		this.context = context;
		dbHelper = new MyDBHelper(this.context);
	}

	public GeneralDAO open() {
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public GeneralDAO openWrite() {
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public GeneralDAO openRead() {
		db = dbHelper.getWritableDatabase();
		Log.d(Utils.TAG, "db opened4read :-)");
		return this;
	}

	public void close() {
		db.close();
		Log.d(Utils.TAG, "db closed :-(");
	}
	
}
