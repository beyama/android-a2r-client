package eu.addicted2random.a2rclient.exceptions;

import java.net.URI;

import eu.addicted2random.a2rclient.R;

import android.content.Context;

public class ProtocolNotSupportedException extends A2RException {

  private static final String DEFAULT_MESSAGE = "Protocol '%s' not supported (%s)";
  
  private static final long serialVersionUID = -2698643934009130470L;

  private final URI uri;
  
  public ProtocolNotSupportedException(URI uri, Throwable throwable) {
    super(String.format(DEFAULT_MESSAGE, uri.getScheme(), uri.toString()), throwable);
    this.uri = uri;
  }

  public ProtocolNotSupportedException(URI uri) {
    this(uri, null);
  }

  @Override
  public String getLocalizedMessage(Context context) {
    return context.getString(R.string.protocol_not_supported_exception, uri.getScheme(), uri.toString());
  }
  
  public URI getURI() {
    return this.uri;
  }

}
