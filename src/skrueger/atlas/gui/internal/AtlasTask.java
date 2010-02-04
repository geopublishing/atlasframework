/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.gui.internal;

import java.awt.Component;
import java.awt.Cursor;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import skrueger.atlas.AtlasViewer;

/**
 * super.done should be called when overwriting done !
 * 
 * The {@link #isCancelled()} should be checking during
 * {@link #doInBackground()}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
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

		progressWindow = new AtlasStatusDialog(owner, AtlasViewer
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
