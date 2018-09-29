/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package site.visit.wmi.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

	private static final String TAG = SQLiteHandler.class.getSimpleName();

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "wmi_sitevisit";

	// Login table name
	private static final String TABLE_USER = "user";
	private static final String TABLE_SETTINGS = "settings";

	// Login Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_USERNAME = "username";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_LEVEL = "level";
	private static final String KEY_IMEI_DEVICE= "imei_device";
	private static final String KEY_UID = "uid";
	private static final String KEY_CREATED_AT = "created_at";

	private static final String KEY_UPDATE = "update_per";
	private static final String KEY_UPDATE_LINK = "update_link";
	private static final String KEY_REGISTER = "register_per";

	private static final String CREATE_TABLE_SETTINGS = "CREATE TABLE " + TABLE_SETTINGS + "("
			+ KEY_ID + " INTEGER PRIMARY KEY,"
			+ KEY_UPDATE + " TEXT,"
			+ KEY_UPDATE_LINK + " TEXT,"
			+ KEY_REGISTER + " TEXT" +")";

	private static final String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
			+ KEY_ID + " INTEGER PRIMARY KEY,"
			+ KEY_NAME + " TEXT,"
			+ KEY_USERNAME + " TEXT,"
			+ KEY_EMAIL + " TEXT,"
			+ KEY_LEVEL + " TEXT,"
			+ KEY_IMEI_DEVICE + " TEXT,"
			+ KEY_UID + " TEXT,"
			+ KEY_CREATED_AT + " TEXT" +")";

	public SQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_LOGIN_TABLE);
		db.execSQL(CREATE_TABLE_SETTINGS);
		Log.d(TAG, "Database tables created");
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
		// Create tables again
		onCreate(db);
	}

	/**
	 * Storing user details in database
	 * */
	public void addUser(String name, String username, String email, String level, String imei_device, String uid, String created_at) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name); // Name
		values.put(KEY_USERNAME, username); //Username
		values.put(KEY_EMAIL, email); // Email
		values.put(KEY_LEVEL, level); // imei
		values.put(KEY_IMEI_DEVICE, imei_device); // imei
		values.put(KEY_UID, uid); // uid
		values.put(KEY_CREATED_AT, created_at); // Created At


		// Inserting Row
		long id = db.insert(TABLE_USER, null, values);
		db.close(); // Closing database connection

		Log.d(TAG, "New user inserted into sqlite: " + id);
	}

	public int addSettings (String updatestr, String updatelinkstr, String registerstr){
		int courseId;
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_UPDATE, updatestr);
		values.put(KEY_UPDATE_LINK, updatelinkstr);
		values.put(KEY_REGISTER, registerstr);

		courseId = (int) db.insert(TABLE_SETTINGS, null, values);
		db.close();

		Log.d(TAG, "Settings: " + courseId);
		return courseId;
	}

	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_USER;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("name", cursor.getString(1));
			user.put("username", cursor.getString(2));
			user.put("email", cursor.getString(3));
			user.put("level", cursor.getString(4));
			user.put("imei_device", cursor.getString(5));
			user.put("uid", cursor.getString(6));
			user.put("created_at", cursor.getString(7));
		}
		cursor.close();
		db.close();
		// return user
		Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

		return user;
	}

	public HashMap<String, String> getSettings() {
		HashMap<String, String> setting = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			setting.put("update", cursor.getString(2));
			setting.put("update_link", cursor.getString(3));
			setting.put("register", cursor.getString(1));
		}
		cursor.close();
		db.close();
		// return user
		Log.d(TAG, "Fetching Settings from Sqlite: " + setting.toString());

		return setting;
	}


	/**
	 * Re crate database Delete all tables and create them again
	 * */
	public void deleteUsers() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_USER, null, null);
		db.close();

		Log.d(TAG, "Deleted all user info from sqlite");
	}

	public void deleteSettings() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_SETTINGS, null, null);
		db.close();

		Log.d(TAG, "Deleted all Settings info from sqlite");
	}

}
