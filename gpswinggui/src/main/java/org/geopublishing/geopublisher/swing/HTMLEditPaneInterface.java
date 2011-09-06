package org.geopublishing.geopublisher.swing;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Stack;

import javax.swing.JComponent;

import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geotools.util.WeakHashSet;

import com.enterprisedt.util.debug.Logger;

import de.schmitzm.swing.JPanel;

/**
 * Interface for all methods needed for a html editor. This interface helps to
 * switch between several implementations of an html viewer/editor.
 */
public abstract class HTMLEditPaneInterface extends JPanel /* extends HTMLInfoPaneInterface */{

	final static Logger  LOGGER = Logger .getLogger(HTMLEditPaneInterface.class);
	/**
	 * GUI component of the html view. This method usually should return
	 * {@code 'this'}. If the implementation does not extends a swing component
	 * this method has to perform a warp!
	 */
	public abstract JComponent getComponent();

	/**
	 * Indicates whether the html view already has its own scroll pane. This
	 * helps the application to decide whether or not it is necessary to create
	 * one.
	 */
	public abstract boolean hasScrollPane();

	/**
	 * Returns the preferred dialog size for the editor. Because of the menu
	 * structure some editors might need a bigger windows than others. If this
	 * method returns <code>null</code> a default size is used.
	 */
	public abstract Dimension getPreferredSize();

	/**
	 * Adds a tab pane to edit a HTML document.
	 * 
	 * @param title
	 *            tab title
	 * @param url
	 *            URL of the document to be edit
	 * @param idx
	 *            index number for the document title if a new file is created
	 */
	public abstract void addEditorTab(String title, URL url, int idx);

	/**
	 * Removes all tabs.
	 */
	public abstract void removeAllTabs();

	/**
	 * Called when surrounded window/dialog/application is closed. Should
	 * perform editor specific actions (e.g. save operation).
	 * 
	 * @param source
	 *            object which initiates the closing
	 * @return <code>true</code> if the dialog should be closed by the
	 *         surrounding application
	 */
	public abstract boolean performClosing(Object source);

	private final WeakHashSet<PropertyChangeListener> listeners = new WeakHashSet<PropertyChangeListener>(
			PropertyChangeListener.class);

	/**
	 * {@link PropertyChangeListener} can be registered to be informed when the
	 * {@link MapPool} changes.
	 * 
	 * @param propertyChangeListener
	 */
	public void addChangeListener(PropertyChangeListener propertyChangeListener) {
		listeners.add(propertyChangeListener);
	}

	/**
	 * {@link PropertyChangeListener} can be registered to be informed when the
	 * {@link MapPool} changes.
	 * 
	 * @param propertyChangeListener
	 */
	public void removeChangeListener(
			PropertyChangeListener propertyChangeListener) {
		listeners.remove(propertyChangeListener);
	}
	

	/**
	 * If {@link #quite} == <code>true</code> no {@link RuleChangedEvent} will
	 * be fired.
	 */
	private boolean quite = false;

	PropertyChangeEvent lastOpressedEvent = null;
	Stack<Boolean> stackQuites = new Stack<Boolean>();

	/**
	 * Add a QUITE-State to the event firing state stack
	 */
	public void pushQuite() {
		stackQuites.push(quite);
		setQuite(true);
	}

	/**
	 * Remove a QUITE-State from the event firing state stack
	 */
	public void popQuite() {
		setQuite(stackQuites.pop());
		if (quite == false) {
			if (lastOpressedEvent != null)
				fireChangeEvents(lastOpressedEvent);
		} else {
			LOGGER.debug("not firing event because there are "
					+ stackQuites.size() + " 'quites' still on the stack");
		}

	}

	public void popQuite(PropertyChangeEvent fireThis) {
		setQuite(stackQuites.pop());
		if (quite == false && fireThis != null)
			fireChangeEvents(fireThis);
		else {
			lastOpressedEvent = fireThis;
			LOGGER.debug("not firing event " + fireThis + " because there are "
					+ stackQuites.size() + " 'quites' still on the stack");
		}
	}

	/**
	 * If quite, the RuleList will not fire {@link RuleChangedEvent}s.
	 */
	private void setQuite(boolean b) {
		quite = b;
	}

	/**
	 * If quite, the RuleList will not fire {@link RuleChangedEvent}s
	 */
	public boolean isQuite() {
		return quite;
	}

	/**
	 * Informs all registered {@link PropertyChangeListener}s about a change in
	 * the {@link MapPool}.
	 */
	public void fireChangeEvents() {

		PropertyChangeEvent pce = new PropertyChangeEvent(this,
				"HTML-Editor changed HTML", null,null);

		fireChangeEvents(pce);
	}

	private void fireChangeEvents(PropertyChangeEvent pce) {
		if (quite) {
			lastOpressedEvent = pce;
			return;
		} else {
			lastOpressedEvent = null;
		}

		for (PropertyChangeListener pcl : listeners) {
			if (pcl != null)
				pcl.propertyChange(pce);
		}
	}

	public void dispose() {
		listeners.clear();
	}


}
