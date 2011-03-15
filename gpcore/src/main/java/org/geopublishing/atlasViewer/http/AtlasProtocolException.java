package org.geopublishing.atlasViewer.http;

/**
 * Runtime exception to handle errors with
 * {@link AtlasProtocol}.
 * Can also be used in deep deep methods where current
 * atlas configuration can not be accessed. In this case
 * the exception can be caught somewhere where the protocol
 * can be interpreted. 
 */
public class AtlasProtocolException extends RuntimeException {
  
  protected String url = null;
  
  /**
   * Creates a new exception.
   * @param url   URL which causes the exception
   */
  public AtlasProtocolException(String url) {
    super();
    this.url = url;
  }

  /**
   * Creates a new exception.
   * @param url   URL which causes the exception
   * @param err   source error
   */
  public AtlasProtocolException(String url, Throwable err) {
    super(err);
    this.url = url;
  }

  /**
   * Creates a new exception.
   * @param url   URL which causes the exception
   * @param mess  exception information
   */
  public AtlasProtocolException(String url, String mess) {
    super(mess);
    this.url = url;
  }

  /**
   * Creates a new exception.
   * @param url   URL which causes the exception
   * @param mess  exception information
   * @param err   source error
   */
  public AtlasProtocolException(String url, String mess, Throwable err) {
    super(mess,err);
    this.url = url;
  }

  /**
   * Returns the URL this exception is caused by.
   */
  public String getURL() {
    return url;
  }
}
