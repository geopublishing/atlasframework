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
package org.geopublishing.atlasViewer.swing.internal;

import java.awt.Component;
import java.awt.Cursor;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;

import skrueger.swing.swingworker.AtlasStatusDialog;


/**
 * super.done should be called when overwriting done !
 * 
 * The {@link #isCancelled()} should be checking during
 * {@link #doInBackground()}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 * @param <K>
 *            The result type for this task. Returned by {@link #get()}
 */
public abstract class AtlasTask<K> extends SwingWorker<K, String> {
	static private final Logger LOGGER = Logger.getLogger(AtlasTask.class);

	protected final Component owner;

	/** A prefix to add to all published Strings * */
	private String prefix = "";

	protected Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

	protected AtlasStatusDialog progressWindow;

	/**
	 * Creating the object already puts the 
	 * @param owner
	 * @param startText
	 */
	public AtlasTask(final Component owner, final String startText) {

		this.owner = owner;

		progressWindow = new AtlasStatusDialog(owner, AtlasViewerGUI
				.R("dialog.title.wait"), startText);

		progressWindow.started();

//		 progressWindow.setTitle(AtlasViewer.R("dialog.title.wait"));

		// SwingUtil.setRelativeFramePosition(progressWindow, owner, 0.5,0.5);

	}

	/**
	 * Updates the JDialog which shows the user something
	 */
	@Override
	protected void process(final List<String> chunks) {
		final String msg = chunks.get(chunks.size() - 1);
		if ((msg == null) || (msg.equals(""))) {

			progressWindow.setDescription("");
		} else {
			final String string = "<html><h3>" + prefix + msg + "</h3></html>";
			progressWindow.setDescription(string);

		}

	}

	@Override
	protected void done() {

		try {
			get();
		} catch (Exception exception) {
			progressWindow.exceptionOccurred(exception);
			// ExceptionDialog.show(owner, exception);
		}

		progressWindow.complete();
		// progressWindow.dispose();
	}

	/**
	 * The prefix that will be added to all published {@link String}s
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * A prefix that will be added to all published {@link String}s
	 * 
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
		if (!this.prefix.endsWith(" "))
			this.prefix += this.prefix + " ";
	}

}
