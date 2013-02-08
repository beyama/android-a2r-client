package eu.addicted2random.a2rclient.models;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Represents a connection.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class Connection implements Serializable, BaseColumns {
  /**
   * An intern connection.
   */
  public final static int INTERN = 0;
  
  /**
   * A connection to a server to
   * query for available connections.
   */
  public final static int INDEX = 1;
  
  /**
   * A connection loaded from an external
   * server.
   */
  public final static int EXTERN = 2;
  
  public static final String TABLE_NAME = "connections";
  public static final String COLUMN_NAME_TYPE = "type";
  public static final String COLUMN_NAME_TITLE = "title";
  public static final String COLUMN_NAME_DESCRIPTION = "description";
  public static final String COLUMN_NAME_IMAGE = "image";
  public static final String COLUMN_NAME_URI = "uri";
  
  private static Integer COLUMN_INDEX_ID = null;
  private static Integer COLUMN_INDEX_TYPE = null;
  private static Integer COLUMN_INDEX_TITLE = null;
  private static Integer COLUMN_INDEX_DESCRIPTION = null;
  private static Integer COLUMN_INDEX_IMAGE = null;
  private static Integer COLUMN_INDEX_URI = null;
  
  private static synchronized void columnIndexFromCursor(Cursor c) {
    if(COLUMN_INDEX_ID != null) return;
    
    COLUMN_INDEX_ID = c.getColumnIndexOrThrow(_ID);
    COLUMN_INDEX_TYPE = c.getColumnIndexOrThrow(COLUMN_NAME_TYPE);
    COLUMN_INDEX_TITLE = c.getColumnIndexOrThrow(COLUMN_NAME_TITLE);
    COLUMN_INDEX_DESCRIPTION = c.getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION);
    COLUMN_INDEX_IMAGE = c.getColumnIndexOrThrow(COLUMN_NAME_IMAGE);
    COLUMN_INDEX_URI = c.getColumnIndexOrThrow(COLUMN_NAME_URI);
  }
  
  /**
   * Create a {@link ContentValues} object from {@link Connection}.
   * 
   * @param c
   * @return
   */
  public static ContentValues toContentValues(Connection c) {
    ContentValues v = new ContentValues();
    v.put(COLUMN_NAME_TYPE, c.getType());
    v.put(COLUMN_NAME_TITLE, c.getTitle());
    v.put(COLUMN_NAME_DESCRIPTION, c.getDescription());
    v.put(COLUMN_NAME_IMAGE, c.getImage());
    if(c.getUri() != null)
      v.put(COLUMN_NAME_URI, c.getUri().toString());
    else
      v.put(COLUMN_NAME_URI, (String)null);
    return v;
  }
  
  /**
   * Create a connection from a cursor.
   * 
   * @param cur The cursor to read from.
   * @return
   */
  public static Connection fromCursor(Cursor cur) {
    columnIndexFromCursor(cur);
    
    Connection c = new Connection();
    
    c.id = cur.getInt(COLUMN_INDEX_ID);
    c.type = cur.getInt(COLUMN_INDEX_TYPE);
    c.title = cur.getString(COLUMN_INDEX_TITLE);
    c.description = cur.getString(COLUMN_INDEX_DESCRIPTION);
    c.image = cur.getString(COLUMN_INDEX_IMAGE);
    
    String uri = cur.getString(COLUMN_INDEX_URI);
    
    if(uri != null) {
      try {
        c.uri = new URI(uri);
      } catch (URISyntaxException e) {
        // should never happen here...
      }
    }
    
    return c;
  }
  
  public static Connection fromJSONObject(JSONObject o) throws URISyntaxException {
    Connection c = new Connection();
    
    c.type = EXTERN;
    c.title = o.optString("title", null);
    c.description = o.optString("description", null);
    
    String uri = o.optString("uri", null);
    
    if(uri != null) {
      c.uri = new URI(uri);
    }
    c.image = o.optString("image", null);
    
    return c;
  }
  
  
  private static final long serialVersionUID = -1416934697302061497L;
  
  private Integer id;
  private Integer type = EXTERN;
  private String title;
  private String description;
  private String image;
  private URI uri;
  
  /**
   * Create a new empty connection.
   */
  public Connection() {
    super();
  }
  
  /**
   * Create a new connection.
   * 
   * @param title Title of this connection
   * @param description Description of this connection
   * @param image Image of this connection
   * @param uri URI of this connection
   */
  public Connection(String title, String description, String image, URI uri) {
    super();
    this.title = title;
    this.description = description;
    this.image = image;
    this.uri = uri;
  }
  
  /**
   * Create a new connection.
   * 
   * @param title Title of this connection
   * @param description Description of this connection
   * @param image Image of this connection
   * @param uri URI of this connection
   */
  public Connection(Integer id, String title, String description, String image, URI uri) {
    this(title, description, image, uri);
    this.id = id;
  }

  /**
   * Get id.
   * @return
   */
  public Integer getId() {
    return id;
  }

  /**
   * Set id.
   * @param id
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Get type of this connection.
   * 
   * It's either {@link Connection#INTERN} or {@link Connection#EXTERN}.
   * @return
   */
  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  /**
   * Get title.
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set title.
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get description.
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set description.
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get image address.
   * @return
   */
  public String getImage() {
    return image;
  }

  /**
   * Set image address.
   * @param image
   */
  public void setImage(String image) {
    this.image = image;
  }

  /**
   * Get URI.
   * @return
   */
  public URI getUri() {
    return uri;
  }

  /**
   * Set uri.
   * @param uri
   */
  public void setUri(URI uri) {
    this.uri = uri;
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
    Connection other = (Connection) obj;
    if (uri == null) {
      if (other.uri != null)
        return false;
    } else if (!uri.equals(other.uri))
      return false;
    return true;
  }
}
