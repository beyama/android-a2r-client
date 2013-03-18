package eu.addicted2random.a2rclient;

import java.util.List;

import android.content.Context;
import android.os.Build;
import eu.addicted2random.a2rclient.dao.BookmarkDAO;
import eu.addicted2random.a2rclient.models.Bookmark;

public class A2R {

  static public final String NAME = "A2R Client";
  
  static public final String VERSION = "0.0.1";
  
  static public final String USER_AGENT = String.format("%s %s (%s - %s)", NAME, VERSION, Build.MODEL, Build.VERSION.RELEASE);
  
  static private A2R instance;
  
  static public synchronized A2R getInstance(Context context) {
    if(instance != null) {
      return instance;
    }
    instance = new A2R(context);
    return instance;
  }
  
  static public A2R getInstance() {
    return instance;
  }
  
  private Context mContext;
  
  private BookmarkDAO mBookmarkDAO;

  public A2R(Context context) {
    super();
    mContext = context.getApplicationContext();
    mBookmarkDAO = new BookmarkDAO(mContext);
  }
  
  public boolean addBookmark(Bookmark bookmark) {
    return mBookmarkDAO.add(bookmark);
  }

  public List<Bookmark> getAllBookmarks() {
    return mBookmarkDAO.getAll();
  }

  public Bookmark getBookmark(long id) {
    return mBookmarkDAO.get(id);
  }

  public boolean deleteBookmark(Bookmark bookmark) {
    return mBookmarkDAO.delete(bookmark);
  }

  public void updateBookmark(Bookmark bookmark) {
    mBookmarkDAO.update(bookmark);
  }
  
}
