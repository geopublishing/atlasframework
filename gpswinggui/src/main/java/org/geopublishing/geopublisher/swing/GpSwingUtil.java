package org.geopublishing.geopublisher.swing;
/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/


import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasRefInterface;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.media.DpMedia;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.AtlasSwingWorker;
import org.geopublishing.geopublisher.AMLExporter;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.ExceptionDialog;
import skrueger.i8n.I8NUtil;

public class GpSwingUtil extends GpUtil{

	private static final Logger LOGGER = Logger.getLogger(GpSwingUtil.class);
	

	/**
	 * Deletes a {@link DpEntry}. This deletes the Entry from the Atlas'
	 * datapool, as well as all references to it, as well as the folder on disk.
	 * 
	 * @param ace
	 *            {@link AtlasConfigEditable} where the {@link DpEntry} is part
	 *            of.
	 * @param dpe
	 *            {@link DpEntry} to be deleted.
	 * @param askAgainIfLinked
	 *            If <code>true</code> and the {@link DpEntry} is linked, the
	 *            user will be asked again if he really want to delete the
	 *            {@link DpEntry}. If <code>false</code>, the references are
	 *            automatically removed.
	 * 
	 * @return <code>null</code> if the deletion failed or was aborted by the
	 *         user. Otherwise the removed {@link DpEntry}.
	 */
	public static DpEntry<?> deleteDpEntry(Component owner,
			AtlasConfigEditable ace, DpEntry<?> dpe, boolean askAgainIfLinked) {
		LinkedList<AtlasRefInterface<?>> references = new LinkedList<AtlasRefInterface<?>>();

		if (!GPDialogManager.dm_MapComposer.closeAllInstances())
			return null;

		// ****************************************************************************
		// Go through all mapPoolEntries and groups and count the references to
		// this DatapoolEntry
		// ****************************************************************************
		for (Map map : ace.getMapPool().values()) {

			for (DpRef<DpLayer<?, ? extends ChartStyle>> ref : map.getLayers()) {
				if (ref.getTargetId().equals(dpe.getId()))
					references.add(ref);
			}
			for (DpRef<DpMedia<? extends ChartStyle>> ref : map.getMedia()) {
				if (ref.getTargetId().equals(dpe.getId()))
					references.add(ref);
			}
			map.getAdditionalStyles().remove(dpe.getId());
			map.getSelectedStyleIDs().remove(dpe.getId());
		}

		int countRefsInMappool = references.size();

		// ****************************************************************************
		// Go through all group tree count the references to this DatapoolEntry
		// ****************************************************************************
		Group group = ace.getFirstGroup();
		Group.findReferencesTo(group, dpe, references, false);

		if (references.size() > 0 && askAgainIfLinked) {
			// Ask the user if she still wants to delete the DPE, even though
			// references exist.
			int res = JOptionPane
					.showConfirmDialog(
							owner,
							GpUtil
									.R(
											"AtlasConfig.DeleteDpEntry.DeleteReferences.Question",
											countRefsInMappool, references
													.size()
													- countRefsInMappool),
													GpUtil
									.R("DataPoolWindow_Action_DeleteDPE_label"
											+ " " + dpe.getTitle()),
							JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.NO_OPTION)
				return null;
		}

		// ****************************************************************************
		// Delete the references first. Kill DesignMapViewJDialogs if affected.
		// Abort everything if the user doesn't want to close the
		// DesignMapViewJDialog.
		// ****************************************************************************
		for (Map map : ace.getMapPool().values()) {
			boolean affected = false;

			// Check all the layers
			final LinkedList<DpRef<DpLayer<?, ? extends ChartStyle>>> layersNew = new LinkedList<DpRef<DpLayer<?, ? extends ChartStyle>>>();
			for (DpRef<DpLayer<?, ? extends ChartStyle>> ref : map.getLayers()) {
				if (!ref.getTargetId().equals(dpe.getId()))
					layersNew.add(ref);
				else {
					affected = true;
				}
			}

			// Check all the media
			final List<DpRef<DpMedia<? extends ChartStyle>>> mediaNew = new LinkedList<DpRef<DpMedia<? extends ChartStyle>>>();
			for (DpRef<DpMedia<? extends ChartStyle>> ref : map.getMedia()) {
				if (!ref.getTargetId().equals(dpe.getId()))
					mediaNew.add(ref);
				else {
					affected = true;
				}
			}

			// Close any open DesignMapViewJDialogs or abort if the user doesn't
			// want to close.
			if (affected) {
				if (!GPDialogManager.dm_MapComposer.close(map))
					return null;
			}

			// Now we change this map
			map.setLayers(layersNew);
			map.setMedia(mediaNew);
		}

		Group.findReferencesTo(group, dpe, references, true);

		final File dir = new File(ace.getDataDir(), dpe.getDataDirname());
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			ExceptionDialog.show(owner, e);
		}

		return ace.getDataPool().remove(dpe.getId());

	}



	/**
	 * Checks if a filename is OK for the AV. Asks the use to accespt the
	 * changed name
	 * 
	 * @param owner
	 *            GUI owner
	 * @param nameCandidate
	 *            Filename to check, e.g. bahn.jpg
	 * @return <code>null</code> if the user didn't accept the new filename.
	 * 
	 * @throws AtlasImportException
	 *             if the user doesn't like the change of the filename.
	 */
	public static String cleanFilenameWithUI(Component owner,
			String nameCandidate) throws AtlasImportException {
		String cleanName = AVUtil.cleanFilename(nameCandidate);

		if (!cleanName.equals(nameCandidate)) {
			/**
			 * The candidate was not clean. Ask the user to accept the new name
			 * or cancel.
			 */

			if (!AVSwingUtil.askOKCancel(owner, R("Cleanfile.Question",
					nameCandidate, cleanName))) {
				throw new AtlasImportException(R(
						"Cleanfile.Denied.ImportCancelled", nameCandidate));
			}
		}

		return cleanName;
	}
	

	/**
	 * Validates, that all directory references actually exist. If some
	 * directory is missing, asks the user if he want's to delete the entry and
	 * all references to it.<br/>
	 * This method also initializes the size-cache for every {@link DpEntry}.
	 */
	public static void validate(AtlasConfigEditable ace, final Component owner) {
		LOGGER.debug("starting validation of datatpool");
		LinkedList<DpEntry<?>> errorEntries = new LinkedList<DpEntry<?>>();

		// ****************************************************************************
		// First collect all erroneous DpEntries...
		// ****************************************************************************
		for (DpEntry<?> dpe : ace.getDataPool().values()) {

			File dir = new File(ace.getDataDir(), dpe.getDataDirname());

			// Checking for possible errors...
			if (!dir.exists() || !dir.isDirectory()) {
				errorEntries.add(dpe);
			} else {
				// Calculate the size of the folder now and cache it for
				// later...

				ace.getFolderSize(dpe);
			}
		}

		// ****************************************************************************
		// ... now delete them. (We should not modify a datapool that we are
		// iterating through.
		// ****************************************************************************
		for (final DpEntry<?> dpe : errorEntries) {

			final String msg1 = GpUtil.R(
					"AtlasLoader.Validation.dpe.invalid.msg", dpe.getTitle(),
					dpe.getId());
			final String msg2 = GpUtil.R(
					"AtlasLoader.Validation.dpe.invalid.msg.folderDoesnExist",
					new File(ace.getDataDir(), dpe.getDataDirname())
							.getAbsolutePath());

			final String question = GpUtil.R("AtlasLoader.Validation.dpe.invalid.msg.exitOrRemoveQuestion");

			if (AVSwingUtil.askYesNo(owner, msg1 + "\n" + msg2 + "\n" + question)) {
				deleteDpEntry(owner, ace, dpe, false);
			}
		}
	}


	/**
	 * Save the {@link AtlasConfig} to its project directory
	 * 
	 * @param parentGUI
	 *            If not <code>null</code>, the user get's feedback message
	 *            SaveAtlas.Success.Message
	 * 
	 * @return false Only if there happened an error while saving. If there is
	 *         nothing to save, returns true;
	 */
	public static boolean save(final AtlasConfigEditable ace,  final Component parentGUI, boolean confirm) {

		AVUtil.checkThatWeAreOnEDT();

		AtlasSwingWorker<Boolean> swingWorker = new AtlasSwingWorker<Boolean>(
				parentGUI) {

			@Override
			protected Boolean doInBackground() throws Exception {
				AMLExporter amlExporter = new AMLExporter(
						ace);

				if (amlExporter.saveAtlasConfigEditable(statusDialog)) {
					ace.getProperties().save(new File(ace
							.getAtlasDir(),
							AVProps.PROPERTIESFILE_RESOURCE_NAME));

					new File(ace.getAtlasDir(),
							AtlasConfigEditable.ATLAS_GPA_FILENAME)
							.createNewFile();

					return true;
				}
				return false;
			}

		};

		try {
			Boolean saved = swingWorker.executeModal();
			if (saved && confirm) {
				JOptionPane.showMessageDialog(parentGUI, GeopublisherGUI
						.R("SaveAtlas.Success.Message"));
			}
			return saved;
		} catch (Exception e) {
			ExceptionDialog.show(parentGUI, e);
			return false;
		}
	}


	/**
	 * Returns a {@link List} of {@link File}s that point to the HTML info files
	 * of a DpLayer. The order of the {@link File}s in the {@link List} is equal
	 * to the order of the languages.<br/>
	 * All HTML files returned to exist! If they don't exist they are being
	 * created with a default text.
	 * 
	 * @param dpl
	 *            {@link DpLayer} that the HTML files belong to.
	 */
	static public List<File> getHTMLFilesFor(DpLayer<?, ? extends ChartStyle> dpl) {

		List<File> htmlFiles = new ArrayList<File>();
		
		AtlasConfigEditable ac = (AtlasConfigEditable)dpl.getAtlasConfig();

		File dir = new File(ac.getDataDir(), dpl.getDataDirname());
		for (String lang : ac.getLanguages()) {
			try {
				File htmlFile = new File((FilenameUtils
						.removeExtension(new File(dir, dpl.getFilename())
								.getCanonicalPath())
						+ "_" + lang + ".html"));

				if (!htmlFile.exists()) {

					LOGGER.info("Creating a default info HTML file for dpe "
							+ dpl.getTitle() + "\n at "
							+ htmlFile.getAbsolutePath());

					/**
					 * Create a default HTML About window
					 */

					FileWriter fw = new FileWriter(htmlFile);
					fw.write(GpUtil.R("DPLayer.HTMLInfo.DefaultHTMLFile",
							I8NUtil.getLocaleFor(lang).getDisplayLanguage(),
							dpl.getTitle()));

					fw.flush();
					fw.close();
				}
				htmlFiles.add(htmlFile);

			} catch (IOException e) {
				LOGGER.error(e);
				ExceptionDialog.show(GeopublisherGUI.getInstance().getJFrame(), e);
			}
		}
		return htmlFiles;
	}


	/**
	 * Returns a {@link List} of {@link File}s that point to the HTML info
	 * filesfor a {@link Map}. The order of the {@link File}s in the
	 * {@link List} is equal to the order of the languages.<br/>
	 * All HTML files returned to exist! If they don't exist they are being
	 * created with a default text.
	 * 
	 * @param dpl
	 *            {@link DpLayer} that the HTML files belong to.
	 */
	public static List<File> getHTMLFilesFor(Map map) {

		List<File> htmlFiles = new ArrayList<File>();
		
		AtlasConfigEditable ace = (AtlasConfigEditable)map.getAc();

		File dir = new File(ace.getHtmlDir(), map.getId());
		dir.mkdirs();

		for (String lang : ace.getLanguages()) {
			try {
				File htmlFile = new File(new File(dir, "index" + "_" + lang
						+ ".html").getCanonicalPath());

				if (!htmlFile.exists()) {
					LOGGER.info("Creating a default info HTML file for map "
							+ map.getTitle() + "\n at "
							+ htmlFile.getAbsolutePath());

					/**
					 * Create a default HTML About window
					 */

					FileWriter fw = new FileWriter(htmlFile);
					fw.write(GpUtil.R("Map.HTMLInfo.DefaultHTMLFile",
							I8NUtil.getLocaleFor(lang).getDisplayLanguage(),
							map.getTitle()));

					fw.flush();
					fw.close();
				}
				htmlFiles.add(htmlFile);

			} catch (IOException e) {
				LOGGER.error(e);
				ExceptionDialog.show(GeopublisherGUI.getInstance().getJFrame(), e);
			}
		}
		return htmlFiles;
	}

}
