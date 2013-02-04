package eu.addicted2random.a2rclient.grid.models;

public class InvalidLayoutException extends Exception {
  private static final long serialVersionUID = -2551267911289854695L;

  public InvalidLayoutException() {
    super();
  }

  public InvalidLayoutException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }

  public InvalidLayoutException(String detailMessage) {
    super(detailMessage);
  }

  public InvalidLayoutException(Throwable throwable) {
    super(throwable);
  }

}
