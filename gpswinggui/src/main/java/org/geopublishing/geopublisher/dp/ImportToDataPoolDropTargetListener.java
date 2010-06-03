/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.dp;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.exceptions.AtlasImportCancelledException;
import org.geopublishing.geopublisher.gui.internal.AtlasDropTargetListener;

import schmitzm.swing.ExceptionDialog;

/**
 * A {@link DropTargetListener} that listens for anything that can be imported
 * into the {@link DataPool}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class ImportToDataPoolDropTargetListener extends AtlasDropTargetListener {
	static final private Logger LOGGER = Logger
			.getLogger(ImportToDataPoolDropTargetListener.class);

	private final AtlasConfigEditable ace;

	/**
	 * Creates a {@link DropTargetListener} that listens for anything that can
	 * be imported into the {@link DataPool}
	 */
	public ImportToDataPoolDropTargetListener(AtlasConfigEditable ace_) {
		this.ace = ace_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent dropTargetDropEvent) {
		LOGGER.debug("drop");

		Component owner = dropTargetDropEvent.getDropTargetContext()
				.getComponent();

		Transferable tf = dropTargetDropEvent.getTransferable();
		DataFlavor[] transferDataFlavors = tf.getTransferDataFlavors();

		if (tf.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				LOGGER.debug("Trying flavor: " + DataFlavor.javaFileListFlavor);
				dropTargetDropEvent
						.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				Object transferData = tf
						.getTransferData(DataFlavor.javaFileListFlavor);
				for (File f : (Iterable<File>) transferData) {

					URLConnection uc = f.toURI().toURL().openConnection();

					LOGGER.debug("    " + f + ": " + uc.getContentType());
					importFile(ace, f, owner);
				}
				LOGGER.info("Calling AtlasConfig to import the list..");
				dropTargetDropEvent.getDropTargetContext().dropComplete(true);
				return;
			} catch (Exception e) {
				LOGGER.warn(
						"javaFileListFlavor nicht erfolgreich, mit Exception",
						e);
			}
		}

		if (tf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				LOGGER.debug("Trying flavor: " + DataFlavor.stringFlavor);
				dropTargetDropEvent
						.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				String fileList = (String) tf
						.getTransferData(DataFlavor.stringFlavor);
				LOGGER.debug("String fileList from TransferData = " + fileList);
				String[] files = fileList.split("\n");

				java.util.List<Object> dropped = new LinkedList<Object>();
				for (String s : files) {
					dropped.add(s);
				}
				LOGGER.debug("Calling add(List) to import the list..");
				// add(ace, dropped);
				dropTargetDropEvent.getDropTargetContext().dropComplete(
						importMany(ace, dropped, owner));
				return;
			} catch (Exception e) {
				LOGGER
						.error(
								" DataFlavor.stringFlavor not successful, with Exception",
								e);
			}
		}

	}

	/**
	 * Add a arbitrary {@link List} of Objects to the atlas. Used by
	 * Drag'n'Drop.
	 * 
	 * @param dropped
	 *            {@link List} of {@link Object} to add to the atlas. Type will
	 *            be determined with DpEntryFactory.create in importFile()
	 * @return true if at least one Object was imported
	 */
	public boolean importMany(AtlasConfigEditable ace, List<Object> dropped,
			Component owner) {
		LOGGER.debug("Adding list of objects to AtlasConfig...");
		boolean result = false;
		for (Object o : dropped) {
			if (o instanceof String) {
				String droppedString = (String) o;
				LOGGER.debug("One string to import: '" + droppedString + "'");
				File f;

				// TODO machen wie im HTML import? mit try new url catch new

				// Next: Try to make a File from the String
				if (droppedString.startsWith("file:")) {
					LOGGER
							.debug("Filename with 'file:'. Interpreting it as URL...");
					String urlpath;
					try {
						urlpath = new URL(droppedString).toURI().getPath();
						LOGGER.debug("URL-path = " + urlpath
								+ ". Creating a file...");
						f = new File(urlpath);
						if (!f.exists())
							throw new AtlasImportException(
									"Problems Converting URL to File with '"
											+ droppedString + "'"); // i8n
					} catch (Exception e) {
						LOGGER.info("ERROR while DnD:", e);
						ExceptionDialog.show(owner, e);
						return false;
					}
				} else {
					f = new File(droppedString);
				}
				result &= importFile(ace, f, owner);
			} else if (o instanceof File) {
				File doppedFile = (File) o;
				// Try to add the single file
				result &= importFile(ace, doppedFile, owner);
			}
		}
		return result;
	}

	/**
	 * Try to add an arbitrary file to the {@link DataPool} of an
	 * {@link AtlasConfig}
	 * 
	 * @param file
	 *            Must not be <code>null</code>. {@link File} to add to the
	 *            Datapool. The filetype and whether it can be imported will be
	 *            determined by {@link AtlasConfigEditable}.add().
	 * @return <code>true</code> if the import was successful.
	 */
	private boolean importFile(AtlasConfigEditable ace, File file,
			Component owner) {
		try {

			if (!file.exists()) {

				// Testing if the file might come from a network drive and is
				// therefore unavailable.
				// TODO How does Windows deal with that?
				if ((file.toString().startsWith("smb"))
						| (file.toString().startsWith("fish"))
						| (file.toString().startsWith("samba"))
						| (file.toString().startsWith("ssh"))) {
					JOptionPane
							.showConfirmDialog(
									owner,
									"Importing files from a network drive is not yet supported. Please copy the files to a local drive first.",
									"Import media not supported",
									JOptionPane.OK_OPTION); // i8nAC
				}

				LOGGER.warn("Can't add file " + file
						+ " because it doesn't exist.");
				return false;
			}
			LOGGER.debug("Calling DatapoolEntryFactory on " + file + " ...");

			// ****************************************************************************
			// Here we try to determine and import the file..
			// ****************************************************************************
			DpEntry newEntry = DpEntryFactory.create(ace, file, owner);

			if (newEntry == null) {
				String message = "<html><h3>Can't import "
						+ file
						+ "!</h3><br>"
						+ "Please always Drag'n'Drop the \"main\" file.<ul>"
						+ "<li>for PDF, drop .pdf"
						+ "<li>for ESRI Shape files, drop .shp"
						+ "<li>for raster images with pending .prj or .sld, please drop the image file"
						+ "<li>for ImagePyramidLayers, drop the .properties file."; // i8nAC
				LOGGER.info(message);
				AVSwingUtil.showMessageDialog(owner, message);
				return false;
			}

			LOGGER.debug("Adding a new DatapoolEntry to the Datapool...");
			ace.add(newEntry);
//
//			/*
//			 * It's better to save here
//			 * 
//			 * TODO We need a better logic here
//			 */
//			ace.save(AtlasCreator.getInstance().getJFrame(), false);

			return true;

		} catch (AtlasImportCancelledException e) {
			return false;
		} catch (AtlasImportException e) {
			ExceptionDialog.show(owner, e);
			return false;
		}
	}

}
