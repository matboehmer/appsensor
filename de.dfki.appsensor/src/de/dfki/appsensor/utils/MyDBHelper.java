package de.dfki.appsensor.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.dfki.appsensor.data.db.AppUsageEventDAO;
import de.dfki.appsensor.data.db.GeneralDAO;

/**
 * This is the basic component to create and manage the database. Whenever the
 * data schema is changed, we need to increment the version of the database.
 * This class should only be used in the {@link GeneralDAO}.
 * 
 * @author Matthias Boehmer, matthias.boehmer@dfki.de
 */
public class MyDBHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "appsensor";

	public MyDBHelper(Context context) {
		this(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public MyDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		Log.d(Utils.TAG, "database created");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(AppUsageEventDAO.TABLE_CREATE);
		Utils.d(this, "table " + AppUsageEventDAO.TABLE_NAME + " was created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		Utils.d(this, "===========DROPPING DBs====== old: "+oldVersion+"====new: "+newVersion+"=========");

		// clear old schema and data
		db.execSQL("DROP TABLE IF EXISTS " + AppUsageEventDAO.TABLE_NAME);

		// create new schema
		onCreate(db);

	}

}
