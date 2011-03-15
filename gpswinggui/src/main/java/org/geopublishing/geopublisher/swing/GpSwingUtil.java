package org.geopublishing.geopublisher.swing;

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

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
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
import org.geopublishing.geopublisher.AMLExporter;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.geopublishing.geopublisher.gui.map.DesignHTMLInfoPane;

import de.schmitzm.i18n.I18NUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class GpSwingUtil extends GpUtil {

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
	 * @param askUserToVerify
	 *            If <code>true</code>, the user will be asked for confirmation.
	 *            The confirmation will list all references. If
	 *            <code>false</code>, the DPE and all references are
	 *            automatically removed.
	 * 
	 * @return <code>null</code> if the deletion failed or was aborted by the
	 *         user. Otherwise the removed {@link DpEntry}.
	 */
	public static DpEntry<?> deleteDpEntry(Component owner,
			AtlasConfigEditable ace, DpEntry<?> dpe, boolean askUserToVerify) {
		LinkedList<AtlasRefInterface<?>> references = new LinkedList<AtlasRefInterface<?>>();

		// ****************************************************************************
		// Go through all mapPoolEntries and groups and count the references to
		// this DatapoolEntry
		// ****************************************************************************
		Set<Map> mapsWithReferences = new HashSet<Map>();
		for (Map map : ace.getMapPool().values()) {

			for (DpRef<DpLayer<?, ? extends ChartStyle>> ref : map.getLayers()) {
				if (ref.getTargetId().equals(dpe.getId())) {
					references.add(ref);
					mapsWithReferences.add(map);
				}
			}
			for (DpRef<DpMedia<? extends ChartStyle>> ref : map.getMedia()) {
				if (ref.getTargetId().equals(dpe.getId())) {
					references.add(ref);
					mapsWithReferences.add(map);
				}
			}
			map.getAdditionalStyles().remove(dpe.getId());
			map.getSelectedStyleIDs().remove(dpe.getId());
		}

		int countRefsInMappool = references.size();

		// ****************************************************************************
		// Go through all group tree count the references to this DatapoolEntry
		// ****************************************************************************
		Group group = ace.getRootGroup();
		Group.findReferencesTo(group, dpe, references, false);

		if (askUserToVerify) {
			// Ask the user if she still wants to delete the DPE, even though
			// references exist.
			int res = JOptionPane.showConfirmDialog(owner, GpUtil.R(
					"DeleteDpEntry.QuestionDeleteDpeAndReferences", dpe
							.getFilename(), countRefsInMappool, LangUtil
							.stringConcatWithSep(", ", mapsWithReferences),
					references.size() - countRefsInMappool, dpe.getTitle()
							.toString()), GpUtil
					.R("DataPoolWindow_Action_DeleteDPE_label" + " "
							+ dpe.getTitle()), JOptionPane.YES_NO_OPTION);
			if (res != JOptionPane.YES_OPTION)
				return null;
		}

		// Close all dialogs that use this layer
		if (!GPDialogManager.closeAllMapComposerDialogsUsing(dpe))
			return null;

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
		String cleanName = IOUtil.cleanFilename(nameCandidate);

		if (!cleanName.equals(nameCandidate)) {
			/**
			 * The candidate was not clean. Ask the user to accept the new name
			 * or cancel.
			 */

			if (!AVSwingUtil.askOKCancel(owner,
					R("Cleanfile.Question", nameCandidate, cleanName))) {
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
	 * Also checks that all directories in <code>ad/data</code> folder are
	 * actually referenced. If now, the used is asked to delete the folder.</br>
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

			final String question = GpUtil
					.R("AtlasLoader.Validation.dpe.invalid.msg.exitOrRemoveQuestion");

			if (SwingUtil.askYesNo(owner, msg1 + "\n" + msg2 + "\n" + question)) {
				deleteDpEntry(owner, ace, dpe, false);
			}
		}

		cleanFolder(ace, owner);
	}

	/**
	 * Checks the data dir folder of the {@link AtlasConfigEditable} and asks to
	 * delete any unexpected folders.
	 * 
	 * @param owner
	 *            if <code>null</code> all files will be deleted automatically
	 */
	public static void cleanFolder(AtlasConfigEditable ace,
			final Component owner) {

		// ****************************************************************************
		// now list all directories in ad/html and check whether they are
		// actually used
		// ****************************************************************************
		for (File dir : ace.getHtmlDir().listFiles()) {
			if (!dir.isDirectory())
				continue;
			if (dir.getName().startsWith("."))
				continue;
			// if (dir.getName().equals(AtlasConfigEditable.IMAGES_DIRNAME))
			// continue;
			if (dir.getName().equals(AtlasConfigEditable.ABOUT_DIRNAME))
				continue;

			boolean isReferenced = false;
			for (Map map : ace.getMapPool().values()) {
				if (ace.getHtmlDirFor(map).getName().equals(dir.getName())) {
					isReferenced = true;
					break;
				}
			}

			if (isReferenced)
				continue;

			LOGGER.info("The map directory " + IOUtil.escapePath(dir)
					+ " is not referenced in the atlas.");

			askToDeleteUnreferencedFolder(ace.getHtmlDir(), owner, dir);

		}

		// ****************************************************************************
		// now list all directories in ad/data and check whether they are
		// actually used
		// ****************************************************************************
		for (File dir : ace.getDataDir().listFiles()) {
			if (!dir.isDirectory())
				continue;
			if (dir.getName().startsWith(".")) {
				continue;
			}

			boolean isReferenced = false;
			for (DpEntry<?> dpe : ace.getDataPool().values()) {
				if (dpe.getDataDirname().equals(dir.getName())) {
					isReferenced = true;
					break;
				}
			}

			if (isReferenced)
				continue;

			LOGGER.info("The directory " + IOUtil.escapePath(dir)
					+ " is not referenced in the atlas.");

			askToDeleteUnreferencedFolder(ace.getDataDir(), owner, dir);

		}
	}

	/**
	 * Asks to delete a file or folder and returns <code>true</code> if the file
	 * has been deleted.
	 */
	private static boolean askToDeleteUnreferencedFolder(File dir,
			final Component owner, File d) {

		boolean askDelete = true;
		if (owner != null)
			askDelete = AVSwingUtil
					.askOKCancel(
							owner,
							GpSwingUtil
									.R("UnreferencedDirectoryFoundInAtlasDataDir_AskIfItShouldBeDeleted",
											IOUtil.escapePath(dir), d.getName()));
		if (askDelete) {
			if (owner != null)
				LOGGER.info("User allowed to delete folder "
						+ IOUtil.escapePath(d) + ".");
			else
				LOGGER.info("Automatically delete folder "
						+ IOUtil.escapePath(d) + ".");

			if ((d.isDirectory() && new File(d, ".svn").exists())) {
				LOGGER.info("Please use:\nsvn del \"" + IOUtil.escapePath(d)
						+ "\" && svn commit \"" + IOUtil.escapePath(d)
						+ "\" -m \"deleted an unused directory\"");

				if (owner != null)
					AVSwingUtil
							.showMessageDialog(
									owner,
									GpSwingUtil
											.R("UnreferencedDirectoryFoundInAtlasDataDir_WillNotBeDeletedDueToSvnButOfferTheCommand",
													d.getName(),
													IOUtil.escapePath(d)));
			} else {

				// Just delete the directory!

				return FileUtils.deleteQuietly(d);
			}
		}
		return false;
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
	public static boolean save(final AtlasConfigEditable ace,
			final Component parentGUI, boolean confirm) {

		SwingUtil.checkOnEDT();

		AtlasSwingWorker<Boolean> swingWorker = new AtlasSwingWorker<Boolean>(
				parentGUI) {

			@Override
			protected Boolean doInBackground() throws Exception {
				AMLExporter amlExporter = new AMLExporter(ace);

				if (amlExporter.saveAtlasConfigEditable(statusDialog)) {
					ace.getProperties().save(
							new File(ace.getAtlasDir(),
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
				JOptionPane.showMessageDialog(parentGUI,
						GeopublisherGUI.R("SaveAtlas.Success.Message"));
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
	static public List<File> getHTMLFilesFor(
			DpLayer<?, ? extends ChartStyle> dpl) {

		List<File> htmlFiles = new ArrayList<File>();

		AtlasConfigEditable ac = (AtlasConfigEditable) dpl.getAtlasConfig();

		File dir = new File(ac.getDataDir(), dpl.getDataDirname());
		for (String lang : ac.getLanguages()) {
			try {
				File htmlFile = new File(
						(FilenameUtils.removeExtension(new File(dir, dpl
								.getFilename()).getCanonicalPath())
								+ "_"
								+ lang + ".html"));

				if (!htmlFile.exists()) {

					LOGGER.info("Creating a default info HTML file for dpe "
							+ dpl.getTitle() + "\n at "
							+ htmlFile.getAbsolutePath());

					/**
					 * Create a default HTML About window
					 */

					FileWriter fw = new FileWriter(htmlFile);
					fw.write(GpUtil.R("DPLayer.HTMLInfo.DefaultHTMLFile",
							I18NUtil.getFirstLocaleForLang(lang)
									.getDisplayLanguage(), dpl.getTitle()));

					fw.flush();
					fw.close();
				}
				htmlFiles.add(htmlFile);

			} catch (IOException e) {
				LOGGER.error(e);
				ExceptionDialog.show(GeopublisherGUI.getInstance().getJFrame(),
						e);
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

		AtlasConfigEditable ace = (AtlasConfigEditable) map.getAc();

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
					fw.write(GpUtil.R("Map.HTMLInfo.DefaultHTMLFile", I18NUtil
							.getFirstLocaleForLang(lang).getDisplayLanguage(),
							map.getTitle()));

					fw.flush();
					fw.close();
				}
				htmlFiles.add(htmlFile);

			} catch (IOException e) {
				LOGGER.error(e);
				ExceptionDialog.show(GeopublisherGUI.getInstance().getJFrame(),
						e);
			}
		}
		return htmlFiles;
	}

    /**
     * Factory method to create an design html viewport.
     * @param map a Map
     */
    public static DesignHTMLInfoPane createDesignHTMLInfoPane(AtlasConfigEditable ace, Map map) {
      // Note: although we now have 2 versions to display html...
      //  a) JEditorPane -> HTMLInfoJPane
      //  b) JWebBrowser -> HTMLInfoView
      // ... we only have ONE version of DesignHTMLInfoPane, which
      // uses (a) or (b) according to the factory method 
      // AVUtil.createHTMLInfoPane(.)
      return new DesignHTMLInfoPane(ace,map);
    }

}
