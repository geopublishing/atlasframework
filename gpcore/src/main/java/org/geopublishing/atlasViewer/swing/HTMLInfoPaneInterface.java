package org.geopublishing.atlasViewer.swing;

import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 * Interface for all methods needed for a html viewer.
 * This interface helps to switch between several
 * implementations of an html viewer/editor. 
 */
public interface HTMLInfoPaneInterface {
  
  /**
   * Loads a document in the html view. 
   * @param url source url
   */
  public void showDocument(URL url);
  
  /**
   * Shows a document in the html view. 
   * @param content content to show
   */
  public void showDocument(String content);

  /**
   * GUI component of the html view. This method usually should
   * return {@code 'this'}.
   * If the implementation does not extends a swing component
   * this method has to perform a warp!
   */
  public JComponent getComponent();
  
  /**
   * Indicates whether the html view already has its own scroll
   * pane. This helps the application to decide whether or not it is
   * necessary to create one.
   */
  public boolean hasScrollPane();
  
  /**
   * Connects a popup menu to the html view. This method has to disable
   * a browser internal popup menu.
   */
  public void connectPopupMenu(JPopupMenu menu);
  
}
