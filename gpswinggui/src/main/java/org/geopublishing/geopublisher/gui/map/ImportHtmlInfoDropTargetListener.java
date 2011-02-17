/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.map;

import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.HTMLInfoJPane;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.internal.AtlasDropTargetListener;

import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.ExceptionDialog;

/**
 * This {@link AtlasDropTargetListener} manages the import of new HTML pages
 * beeing dropped into a {@link HTMLInfoJPane}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 * @deprecated We now use the internal SimplyHTML editor...
 */
@Deprecated
public class ImportHtmlInfoDropTargetListener extends AtlasDropTargetListener {
	static private final Logger LOGGER = Logger
			.getLogger(ImportHtmlInfoDropTargetListener.class);

	private final Window owner;

	private final Map map;

	private final AtlasConfigEditable ace;

	private final HTMLInfoJPane infoPanel;

	public ImportHtmlInfoDropTargetListener(Window owner2, Map map_,
			AtlasConfigEditable ace_, HTMLInfoJPane infoPanel) {
		this.map = map_;
		this.owner = owner2;
		this.ace = ace_;
		this.infoPanel = infoPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent dropTargetDropEvent) {
		LOGGER.debug("drop");
		Transferable tf = dropTargetDropEvent.getTransferable();

		// DataFlavor[] transferDataFlavors = tf.getTransferDataFlavors();

		if (tf.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				LOGGER.debug("Trying flavor: " + DataFlavor.javaFileListFlavor);
				dropTargetDropEvent
						.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				Object transferData = tf
						.getTransferData(DataFlavor.javaFileListFlavor);
				for (Object o : (Iterable<File>) transferData) {
					File f = (File) o;
					String s = f.getAbsolutePath();
					importHtmlFolder(s);
				}
				dropTargetDropEvent.getDropTargetContext().dropComplete(true);
				return;
			} catch (Exception e) {
				LOGGER.warn(
						"javaFileListFlavor nicht erfolgreich, mit Exception",
						e);
			}
		}
		;

		if (tf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				LOGGER.debug("Trying flavor: " + DataFlavor.stringFlavor);
				dropTargetDropEvent
						.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				String fileList = (String) tf
						.getTransferData(DataFlavor.stringFlavor);
				LOGGER.debug("String fileList from TransferData = " + fileList);
				String[] files = fileList.split("\n");
				dropTargetDropEvent.getDropTargetContext().dropComplete(
						importHtmlFolder(files[0]));
				return;
			} catch (Exception e) {
				LOGGER.info(
						" DataFlavor.stringFlavor not successful, witj Exception",
						e);
			}
		}

	}

	/**
	 * 
	 * @param droppedString
	 * @return
	 */
	private boolean importHtmlFolder(String droppedString) {
		LOGGER.debug("dropped string = " + droppedString);

		File file;
		try {

			if (droppedString.startsWith("file:")) {
				LOGGER.debug("Filename with 'file:' . Interpreting as URL...");

				URL url = new URL(droppedString);

				try {
					file = new File(url.toURI());
				} catch (URISyntaxException e) {
					file = new File(url.getPath());
				}

				if (!file.exists()) {
					throw new RuntimeException(
							"Problems Converting URL to File with '"
									+ droppedString + "'");
				}
				LOGGER.debug("Got an existing file = " + file.getAbsolutePath());
				if (!file.isDirectory()) {
					AVSwingUtil.showMessageDialog(owner, "Not a dir"); // i8nAC
					return false;
				}
			} else {
				LOGGER.debug("Not starting with file:// make a File().. ...");
				file = new File(droppedString);
			}

		} catch (Exception e) {
			ExceptionDialog.show(null, e);
			return false;
		}

		// ****************************************************************************
		// Checking that an index_LANG.html is available for every configured
		// language
		// ****************************************************************************
		for (String lang : ace.getLanguages()) {
			final String htmlindexFilename = "index_" + lang + ".html";
			if (!new File(file, htmlindexFilename).exists()) {
				AVSwingUtil.showMessageDialog(owner,
						"This atlas is configured to support the language '"
								+ lang
								+ "'.\nHowever, the folder doesn't contain a "
								+ htmlindexFilename); // i8nAC
				return false;
			}
		}

		// ****************************************************************************
		// This copies the directory to the ad structure
		// ****************************************************************************
		try {
			File targetDir = new File(ace.getHtmlDir(), map.getId());

			ace.getHtmlDir().mkdir();

			boolean newDir = targetDir.mkdir();

			// ****************************************************************************
			// If the directory already existed, then delete all its content
			// ****************************************************************************
			if (!newDir) {
				FileUtils.deleteDirectory(targetDir);
				targetDir.mkdir();
			}

			IOUtil.copyFile(LOGGER, file, targetDir, false);

		} catch (IOException e) {
			ExceptionDialog.show(owner, e);
			return false;
		}

		infoPanel.showDocument(map.getInfoURL());
		infoPanel.repaint();

		return true;
	}

}
