package org.geopublishing.geopublisher.swing;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.SHTMLEditorKitActions;
import com.lightdev.app.shtm.SHTMLPanelImpl;
import com.lightdev.app.shtm.SHTMLPanelMultipleDocImpl;

import de.schmitzm.lang.LangUtil;

/**
 * A html editor based on SimplyHTML. 
 */
public class HTMLEditPaneSimplyHTML extends SHTMLPanelMultipleDocImpl implements HTMLEditPaneInterface {

  private final Logger LOGGER = LangUtil.createLogger(this);

  /**
   * Creates a new editor based on SimplyHTML.
   */
  public HTMLEditPaneSimplyHTML() {
    super();
  }
  
  /**
   * Returns {@code this}.
   */
  @Override
  public JComponent getComponent() {
    return this;
  }

  /**
   * Returns {@code true}, because {@link SHTMLPanelMultipleDocImpl} already provides
   * scrolling.
   */
  @Override
  public boolean hasScrollPane() {
    return true;
  }

  /**
   * Adds a tab pane to edit a HTML document.
   * @param title  tab title
   * @param url    URL of the document to be edit
   * @param idx    index number for the document title if a new
   *               file is created
   */
  @Override
  public void addEditorTab(String title, URL url, int idx) {
    // Create new Document
    DocumentPane oneLanguage = new DocumentPane(url, idx);

    // Set Title for the Tabbed Document
    oneLanguage.setName(title);

    getTabbedPaneForDocuments().add(oneLanguage);
  }

  /**
   * Removes all tabs.
   */
  @Override
  public void removeAllTabs() {
    getTabbedPaneForDocuments().removeAll();
  }
  
  /**
   * Called when surrounded window/dialog/application is closed.
   * Should perform editor specific actions (e.g. save operation).
   * @param source object which initiates the closing
   * @return <code>false</code> ever, because dialog will be closed
   *         by SimplyHTML action/event 
   */
  @Override
  public boolean performClosing(Object source) {
    new SHTMLEditorKitActions.SHTMLFileExitAction(this)
      .actionPerformed(new ActionEvent(source, 999,
            SHTMLPanelImpl.exitAction));
    // dialog will be closed by the event
    return false;
  }
  
}
