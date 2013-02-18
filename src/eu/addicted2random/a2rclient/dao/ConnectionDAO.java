package eu.addicted2random.a2rclient.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.addicted2random.a2rclient.models.Connection;

public class ConnectionDAO extends SQLiteOpenHelper {
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "Addicted2Random.db";
  
  private static final String SELECT_ALL = "SELECT * FROM " + Connection.TABLE_NAME;
  
  private static final String SELECT_BY_ID = Connection._ID + "=?";
  
  private static final String SQL_CREATE_TABLE =
      "CREATE TABLE " + Connection.TABLE_NAME + " (" +
      Connection._ID + " INTEGER PRIMARY KEY," +
      Connection.COLUMN_NAME_TYPE + " INTEGER NOT NULL," +
      Connection.COLUMN_NAME_TITLE + " TEXT NOT NULL,"  +
      Connection.COLUMN_NAME_DESCRIPTION + " TEXT," +
      Connection.COLUMN_NAME_IMAGE + " TEXT," +
      Connection.COLUMN_NAME_URI + " TEXT UNIQUE NOT NULL" +
      " )";
  
  public ConnectionDAO(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
  
  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.v("ConnectionDAO", "onCreate " + SQL_CREATE_TABLE);
    db.execSQL(SQL_CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }
  
  /**
   * Add a new {@link Connection} to the table.
   * @param connection
   * @return
   */
  public boolean add(Connection connection) {
    SQLiteDatabase db = getWritableDatabase();
    
    ContentValues values = Connection.toContentValues(connection);
    long id = db.insert(Connection.TABLE_NAME, null, values);
    db.close();
    
    if(id == -1) return false;
    
    connection.setId(id);
    return true;
  }
  
  public void update(Connection connection) {
    SQLiteDatabase db = getWritableDatabase();
    
    
    ContentValues values = Connection.toContentValues(connection);
    String[] args = new String[] { String.valueOf(connection.getId()) };
    
    db.update(Connection.TABLE_NAME, values, SELECT_BY_ID, args);
    db.close();
  }
  
  /**
   * Get all connections from the database.
   * 
   * @return
   */
  public List<Connection> getAll() {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(SELECT_ALL, null);
    
    int count = cursor.getCount();
    List<Connection> result = new ArrayList<Connection>(count);
    
    if(cursor.moveToFirst()) {
      do {
        result.add(Connection.fromCursor(cursor));
      } while(cursor.moveToNext());
    }
    cursor.close();
    db.close();
    return result;
  }
  
  public Connection get(long id) {
    SQLiteDatabase db = getReadableDatabase();
    
    String[] args = new String[] { String.valueOf(id) };
    
    Cursor cursor = db.query(Connection.TABLE_NAME, Connection.ALL_COLUMNS, SELECT_BY_ID, args, null, null, null);
    
    if(cursor != null) {
      cursor.moveToFirst();
      return Connection.fromCursor(cursor);
    }
    return null;
    
  }

}
