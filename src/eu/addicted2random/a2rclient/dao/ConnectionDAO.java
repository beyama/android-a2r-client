package eu.addicted2random.a2rclient.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import eu.addicted2random.a2rclient.db.ConnectionDbHelper;

public class ConnectionDAO {

  private ConnectionDbHelper helper;
  private SQLiteDatabase db;
  
  public ConnectionDAO(Context context) {
    helper = new ConnectionDbHelper(context);
    db = helper.getWritableDatabase();
  }
  
}
