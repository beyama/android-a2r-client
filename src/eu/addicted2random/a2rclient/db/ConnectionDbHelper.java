package eu.addicted2random.a2rclient.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import eu.addicted2random.a2rclient.models.Connection;

public class ConnectionDbHelper extends SQLiteOpenHelper {
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "Addicted2Random.db";
  
  private static final String SQL_CREATE_TABLE =
      "CREATE TABLE " + Connection.TABLE_NAME + " (" +
      Connection._ID + " INTEGER PRIMARY KEY," +
      Connection.COLUMN_NAME_TYPE + "INTEGER NOT NULL," +
      Connection.COLUMN_NAME_TITLE + " TEXT NOT NULL,"  +
      Connection.COLUMN_NAME_DESCRIPTION + " TEXT," +
      Connection.COLUMN_NAME_IMAGE + " TEXT," +
      Connection.COLUMN_NAME_URI + " TEXT UNIQUE NOT NULL" +
      " )";
  
  public ConnectionDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
  

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }

}
