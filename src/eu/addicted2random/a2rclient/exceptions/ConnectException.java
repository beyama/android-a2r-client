package eu.addicted2random.a2rclient.exceptions;

import java.net.URI;

import eu.addicted2random.a2rclient.R;

import android.content.Context;

public class ConnectException extends A2RException {
  private static final long serialVersionUID = -4964231415237193761L;
  
  private static final String DEFAULT_MESSAGE = "Failed to connect to '%s'.";

  private final URI uri;

  public ConnectException(URI uri, Throwable throwable) {
    super(String.format(DEFAULT_MESSAGE, uri.toString()), throwable);
    this.uri = uri;
  }

  public ConnectException(URI uri) {
    this(uri, null);
  }

  @Override
  public String getLocalizedMessage(Context context) {
    return context.getString(R.string.connect_exception, uri.toString());
  }

}
