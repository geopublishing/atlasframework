/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer;

import java.awt.event.ActionListener;

import org.opengis.util.InternationalString;

public interface AtlasStatusDialogInterface {

	/**
	 * Returns the window title. The default title is "Progress" localized in
	 * current locale.
	 * 
	 * @return the window title
	 */
	public String getTitle() ;

	/**
	 * Set the window title. A {@code null} value reset the default title.
	 * 
	 * @param title
	 *            the window title
	 */
	public void setTitle(String title) ;

	public String getDescription() ;

	public void setDescription(final String description) ;

	/**
	 * Notifies that the operation begins. This method display the windows if it
	 * was not already visible.
	 */
	public void started();

	/**
	 * {@inheritDoc}
	 */
	public void progress(final float percent);
	public float getProgress() ;
	/**
	 * Notifies that the operation has finished. The window will disaspears,
	 * except if it contains warning or exception stack traces.
	 */
	public void complete() ;

	/**
	 * Releases any resource holds by this window. Invoking this method destroy
	 * the window.
	 */
	public void dispose() ;

	/**
	 * {@inheritDoc}
	 */
	public boolean isCanceled() ;

	/**
	 * {@inheritDoc}
	 * 
	 * @param stop
	 *            true to stop; false otherwise
	 */
	public void setCanceled(final boolean stop);
	
	public void addCancelListener(ActionListener l) ;

	/**
	 * Display a warning message under the progress bar. The text area for
	 * warning messages appears only the first time this method is invoked.
	 * 
	 * @param source
	 *            DOCUMENT ME
	 * @param margin
	 *            DOCUMENT ME
	 * @param warning
	 *            DOCUMENT ME
	 */
	public void warningOccurred(final String source,
			String margin, String warning) ;
	/**
	 * Display an exception stack trace.
	 * 
	 * @param exception
	 *            the exception to display
	 */
	public void exceptionOccurred(final Throwable exception) ;

	public void setTask(InternationalString task);

	public InternationalString getTask() ;

	public void startModal() ;

	public boolean isWarningOccured() ;


}
