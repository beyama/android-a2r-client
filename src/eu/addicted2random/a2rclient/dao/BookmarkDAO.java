package eu.addicted2random.a2rclient.dao;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import eu.addicted2random.a2rclient.models.Bookmark;

public class BookmarkDAO extends SQLiteOpenHelper {
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "Addicted2Random.db";

  private static final String SELECT_ALL = "SELECT * FROM " + Bookmark.TABLE_NAME;

  private static final String SELECT_BY_ID = Bookmark._ID + "=?";

  private static final String SQL_CREATE_TABLE = "CREATE TABLE " + Bookmark.TABLE_NAME + " (" + Bookmark._ID
      + " INTEGER PRIMARY KEY," + Bookmark.COLUMN_NAME_TITLE + " TEXT NOT NULL," + Bookmark.COLUMN_NAME_DESCRIPTION
      + " TEXT," + Bookmark.COLUMN_NAME_IMAGE + " TEXT," + Bookmark.COLUMN_NAME_URI + " TEXT UNIQUE NOT NULL" + " )";

  public BookmarkDAO(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_TABLE);

    Bookmark bookmark = new Bookmark();

    bookmark.setTitle("Radiofabrik Salzburg");
    bookmark.setDescription("Addicted²Random at radiofabrik.at");

    try {
      bookmark.setUri(new URI("ws://master.radiofabrik.at:8080"));
    } catch (URISyntaxException e) {
    }

    ContentValues values = Bookmark.toContentValues(bookmark);
    db.insert(Bookmark.TABLE_NAME, null, values);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }

  /**
   * Add a new {@link Bookmark} to the table.
   * 
   * @param bookmark
   * @return
   */
  public boolean add(Bookmark bookmark) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = Bookmark.toContentValues(bookmark);
    long id = db.insert(Bookmark.TABLE_NAME, null, values);
    db.close();

    if (id == -1)
      return false;

    bookmark.setId(id);
    return true;
  }

  public void update(Bookmark bookmark) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = Bookmark.toContentValues(bookmark);
    String[] args = new String[] { String.valueOf(bookmark.getId()) };

    db.update(Bookmark.TABLE_NAME, values, SELECT_BY_ID, args);
    db.close();
  }

  /**
   * Get all connections from the database.
   * 
   * @return
   */
  public List<Bookmark> getAll() {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(SELECT_ALL, null);

    int count = cursor.getCount();
    List<Bookmark> result = new ArrayList<Bookmark>(count);

    if (cursor.moveToFirst()) {
      do {
        result.add(Bookmark.fromCursor(cursor));
      } while (cursor.moveToNext());
    }
    cursor.close();
    db.close();
    return result;
  }

  public Bookmark get(long id) {
    SQLiteDatabase db = getReadableDatabase();

    String[] args = new String[] { String.valueOf(id) };

    Cursor cursor = db.query(Bookmark.TABLE_NAME, Bookmark.ALL_COLUMNS, SELECT_BY_ID, args, null, null, null);

    if (cursor != null) {
      cursor.moveToFirst();
      return Bookmark.fromCursor(cursor);
    }
    return null;
  }

  public boolean delete(Bookmark bookmark) {
    if (bookmark.getId() == null)
      return false;

    SQLiteDatabase db = getWritableDatabase();

    String[] args = new String[] { String.valueOf(bookmark.getId()) };
    int count = db.delete(Bookmark.TABLE_NAME, SELECT_BY_ID, args);

    return count == 1;
  }

}
