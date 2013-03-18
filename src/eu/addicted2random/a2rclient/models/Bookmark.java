package eu.addicted2random.a2rclient.models;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Represents a bookmark.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class Bookmark implements Serializable, BaseColumns {
  
  public static final String TABLE_NAME = "bookmarks";
  public static final String COLUMN_NAME_TITLE = "title";
  public static final String COLUMN_NAME_DESCRIPTION = "description";
  public static final String COLUMN_NAME_IMAGE = "image";
  public static final String COLUMN_NAME_URI = "uri";

  private static Integer COLUMN_INDEX_ID = null;
  private static Integer COLUMN_INDEX_TITLE = null;
  private static Integer COLUMN_INDEX_DESCRIPTION = null;
  private static Integer COLUMN_INDEX_IMAGE = null;
  private static Integer COLUMN_INDEX_URI = null;

  public static String[] ALL_COLUMNS = new String[] { _ID, COLUMN_NAME_TITLE,
      COLUMN_NAME_DESCRIPTION, COLUMN_NAME_IMAGE, COLUMN_NAME_URI };

  private static synchronized void columnIndexFromCursor(Cursor c) {
    if (COLUMN_INDEX_ID != null)
      return;

    COLUMN_INDEX_ID = c.getColumnIndexOrThrow(_ID);
    COLUMN_INDEX_TITLE = c.getColumnIndexOrThrow(COLUMN_NAME_TITLE);
    COLUMN_INDEX_DESCRIPTION = c.getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION);
    COLUMN_INDEX_IMAGE = c.getColumnIndexOrThrow(COLUMN_NAME_IMAGE);
    COLUMN_INDEX_URI = c.getColumnIndexOrThrow(COLUMN_NAME_URI);
  }

  /**
   * Create a {@link ContentValues} object from {@link Bookmark}.
   * 
   * @param c
   * @return
   */
  public static ContentValues toContentValues(Bookmark c) {
    ContentValues v = new ContentValues();
    v.put(COLUMN_NAME_TITLE, c.getTitle());
    v.put(COLUMN_NAME_DESCRIPTION, c.getDescription());
    v.put(COLUMN_NAME_IMAGE, c.getImage());
    if (c.getUri() != null)
      v.put(COLUMN_NAME_URI, c.getUri().toString());
    else
      v.put(COLUMN_NAME_URI, (String) null);
    return v;
  }

  /**
   * Create a {@link Bookmark} from a cursor.
   * 
   * @param cur
   *          The cursor to read from.
   * @return
   */
  public static Bookmark fromCursor(Cursor cur) {
    columnIndexFromCursor(cur);

    Log.v("Connection", "from cursor");

    Bookmark c = new Bookmark();

    c.id = cur.getLong(COLUMN_INDEX_ID);
    c.title = cur.getString(COLUMN_INDEX_TITLE);
    c.description = cur.getString(COLUMN_INDEX_DESCRIPTION);
    c.image = cur.getString(COLUMN_INDEX_IMAGE);

    String uri = cur.getString(COLUMN_INDEX_URI);

    if (uri != null) {
      try {
        c.uri = new URI(uri);
      } catch (URISyntaxException e) {
        // should never happen here...
      }
    }

    return c;
  }

  private static final long serialVersionUID = -1416934697302061497L;

  private Long id;
  private String title;
  private String description;
  private String image;
  private URI uri;

  /**
   * Create a new empty connection.
   */
  public Bookmark() {
    super();
  }

  /**
   * Create a new instance of {@link Bookmark}.
   * 
   * @param title
   *          Title of this connection
   * @param description
   *          Description of this connection
   * @param image
   *          Image of this connection
   * @param uri
   *          URI of this connection
   */
  public Bookmark(Long id, String title, String description, String image, URI uri) {
    super();
    this.id = id;
    this.title = title;
    this.description = description;
    this.image = image;
    this.uri = uri;
  }

  /**
   * Create a new instance of {@link Bookmark}.
   * 
   * @param title
   *          Title of this connection
   * @param description
   *          Description of this connection
   * @param image
   *          Image of this connection
   * @param uri
   *          URI of this connection
   */
  public Bookmark(String title, String description, String image, URI uri) {
    this(null, title, description, image, uri);
  }

  /**
   * Get id.
   * 
   * @return
   */
  public Long getId() {
    return id;
  }

  /**
   * Set id.
   * 
   * @param id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get title.
   * 
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set title.
   * 
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get description.
   * 
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set description.
   * 
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get image address.
   * 
   * @return
   */
  public String getImage() {
    return image;
  }

  /**
   * Set image address.
   * 
   * @param image
   */
  public void setImage(String image) {
    this.image = image;
  }

  /**
   * Get URI.
   * 
   * @return
   */
  public URI getUri() {
    return uri;
  }

  /**
   * Set uri.
   * 
   * @param uri
   */
  public void setUri(URI uri) {
    this.uri = uri;
  }

  public boolean isValid() {
    if (title == null || uri == null)
      return false;
    if (title.length() == 0)
      return false;

    if (uri.getHost() == null)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((uri == null) ? 0 : uri.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Bookmark other = (Bookmark) obj;
    if (uri == null) {
      if (other.uri != null)
        return false;
    } else if (!uri.equals(other.uri))
      return false;
    return true;
  }
}
