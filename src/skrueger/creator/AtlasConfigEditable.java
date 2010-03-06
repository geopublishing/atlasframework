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
package skrueger.creator;

import java.awt.Component;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.styling.Style;

import rachel.ResourceManager;
import rachel.loader.FileResourceLoader;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.AVProps;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.AtlasRefInterface;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.dp.Group;
import skrueger.atlas.dp.layer.DpLayer;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.dp.media.DpMedia;
import skrueger.atlas.exceptions.AtlasException;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.atlas.map.MapPool.EventTypes;
import skrueger.atlas.swing.AtlasSwingWorker;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;

/**
 * An extension of the AtlasConfig for use within AtlasCreator. One purpose is,
 * to keep {@link File} related references out of {@link AtlasViewer}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class AtlasConfigEditable extends AtlasConfig {

	/** The name of the marker file for a AtlasWorkingCopy folder **/
	public static final String ATLAS_GPA_FILENAME = "atlas.gpa";

	final static private Logger LOGGER = Logger
			.getLogger(AtlasConfigEditable.class);

	private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

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
	public boolean save(final Component parentGUI, boolean confirm) {

		AVUtil.checkThatWeAreOnEDT();

		AtlasSwingWorker<Boolean> swingWorker = new AtlasSwingWorker<Boolean>(
				parentGUI) {

			@Override
			protected Boolean doInBackground() throws Exception {
				AMLExporter amlExporter = new AMLExporter(
						AtlasConfigEditable.this);

				if (amlExporter.saveAtlasConfigEditable(statusDialog)) {
					getProperties().save(new File(AtlasConfigEditable.this
							.getAtlasDir(),
							AVProps.PROPERTIESFILE_RESOURCE_NAME));

					new File(AtlasConfigEditable.this.getAtlasDir(),
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
				JOptionPane.showMessageDialog(parentGUI, AtlasCreator
						.R("SaveAtlas.Success.Message"));
			}
			return saved;
		} catch (Exception e) {
			ExceptionDialog.show(parentGUI, e);
			return false;
		}
	}

	/**
	 * Returns the root/first group of the "groups-tree" which contains
	 * references to {@link DpEntry}s or sub groups *
	 */
	@Override
	public final Group getFirstGroup() {
		Group fg = super.getFirstGroup();
		/**
		 * Fill the description of the first group and tell the user, that it is
		 * never visible in the atlas.
		 */
		if (fg != null && I8NUtil.isEmpty(fg.getTitle())) {
			List<String> activeLang = new ArrayList<String>();
			activeLang.add(Translation.getActiveLang());
			fg.setTitle(new Translation(activeLang, AtlasCreator
					.R("FirstGroup.DefaultTitle")));
			fg.setDesc(new Translation(activeLang, AtlasCreator
					.R("FirstGroup.DefaultDesc")));
		}

		return fg;

	}

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
	 * Informs all registered {@link PropertyChangeListener}s about a change in
	 * the {@link AtlasConfigEditable}. Also informs all listeners of MapPool
	 * and DataPool.
	 */
	public void fireChangeEvents() {
		PropertyChangeEvent pce = new PropertyChangeEvent(this, "change",
				false, true);

		for (PropertyChangeListener pcl : listeners) {
			if (pcl != null)
				pcl.propertyChange(pce);
		}

		getMapPool().fireChangeEvents(this, EventTypes.unknown, null);
		getDataPool().fireChangeEvents(
				skrueger.atlas.dp.DataPool.EventTypes.unknown);
	}

	/**
	 * Directory where the Atlas was created in, or loaded from, or saved to.
	 * AtlasDir has <code>ad</code> sub-directory!
	 */
	private File atlasDir;

	/** A cache to remember the sizes of the {@link DpEntry} folders **/
	private final java.util.Map<String, Long> rememberFolderSizes = new HashMap<String, Long>();

	/**
	 * Convenience method for file system operations in the AC Returns a File to
	 * the 'ad' directory under the atlasDir
	 */
	public File getAd() {
		File adDir = new File(atlasDir, ATLASDATA_DIRNAME);
		if (!adDir.exists())
			adDir.mkdirs();
		return adDir;
	}

	/**
	 * Convenience method for file system operations in the AC
	 */
	public File getDataDir() {

		File dataDir = new File(getAd(), DATA_DIRNAME);
		if (!dataDir.exists())
			dataDir.mkdirs();
		return dataDir;
	}

	/**
	 * Convenience method for file system operations in the AC
	 */
	public File getHtmlDir() {
		File htmlDir = new File(getAd(), HTML_DIRNAME);
		if (!htmlDir.exists())
			htmlDir.mkdirs();
		return htmlDir;
	}

	/**
	 * Convenience method for file system operations in the AC
	 */
	public File getImagesDir() {
		File imagesDir = new File(getAd(), IMAGES_DIRNAME);
		if (!imagesDir.exists()) {
			imagesDir.mkdirs();
		}
		return imagesDir;
	}

	/**
	 * Convenience method for file system operations in the AC. The about dir
	 * contains the popup and the about HTML info pages.
	 */
	public File getAboutDir() {
		File aboutDir = new File(getHtmlDir(), ABOUT_DIRNAME);
		if (!aboutDir.exists())
			aboutDir.mkdirs();
		return aboutDir;
	}

	/**
	 * Set the atlasDir as {@link File}. Also registers the folder with the
	 * {@link ResourceManager} as a {@link FileResourceLoader}.
	 * AtlasConfigEditable.resetResLoMan() should be called before calling this
	 * method.
	 * 
	 * @param atlasDir_
	 *            directory where the Atlas was loaded, or where it was created.
	 */
	public void setAtlasDir(File atlasDir_) {
		this.atlasDir = atlasDir_;

		LOGGER.debug("AtlasDir of AtlasConfigEditable set to "
				+ atlasDir.getAbsolutePath());

		// Initialize ResMan to see the working directory
		LOGGER.debug("Registering " + atlasDir_
				+ " directory as FileResourceLoader with ResMan");
		getResLoMan().addResourceLoader(new FileResourceLoader(atlasDir));
	}

	/**
	 * @return workDir directory where the atlas was loaded from or created in.
	 *         This method also creates all sub-folders automatically .
	 */
	public File getAtlasDir() {
		getAd();
		getHtmlDir();
		getImagesDir();
		getDataDir();
		getAboutDir();
		return this.atlasDir;
	}

	/**
	 * Validates, that all directory references actually exist. If some
	 * directory is missing, asks the user if he want's to delete the entry and
	 * all references to it.<br/>
	 * This method also initializes the size-cache for every {@link DpEntry}.
	 */
	protected void validate(final Component owner) {
		LOGGER.debug("starting validation of datatpool");
		LinkedList<DpEntry<?>> errorEntries = new LinkedList<DpEntry<?>>();

		// ****************************************************************************
		// First collect all erroneous DpEntries...
		// ****************************************************************************
		for (DpEntry<?> dpe : getDataPool().values()) {

			File dir = new File(getDataDir(), dpe.getDataDirname());

			// Checking for possible errors...
			if (!dir.exists() || !dir.isDirectory()) {
				errorEntries.add(dpe);
			} else {
				// Calculate the size of the folder now and cache it for
				// later...

				getFolderSize(dpe);
			}
		}

		// ****************************************************************************
		// ... now delete them. (We should not modify a datapool that we are
		// iterating through.
		// ****************************************************************************
		for (final DpEntry<?> dpe : errorEntries) {
			// progressListener.info(AtlasCreator.R(
			// "AtlasLoader.processinfo.validating_datapool_entries", dpe
			// .getTitle()));

			final String msg1 = AtlasCreator.R(
					"AtlasLoader.Validation.dpe.invalid.msg", dpe.getTitle(),
					dpe.getId());
			final String msg2 = AtlasCreator.R(
					"AtlasLoader.Validation.dpe.invalid.msg.folderDoesnExist",
					new File(getDataDir(), dpe.getDataDirname())
							.getAbsolutePath());

			final String question = AtlasCreator
					.R("AtlasLoader.Validation.dpe.invalid.msg.exitOrRemoveQuestion");

			// try {
			// SwingUtilities.invokeAndWait(new Runnable() {

			// @Override
			// public void run() {
			if (AVUtil.askYesNo(owner, msg1 + "\n" + msg2 + "\n" + question)) {
				deleteDpEntry(owner, AtlasConfigEditable.this, dpe, false);
			}
			// }

			// });
			// } catch (InterruptedException e) {
			// LOGGER.error(e);
			// ExceptionDialog.show(owner, e);
			// } catch (InvocationTargetException e) {
			// LOGGER.error(e);
			// ExceptionDialog.show(owner, e);
			// }

		}
	}

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
							AtlasCreator
									.R(
											"AtlasConfig.DeleteDpEntry.DeleteReferences.Question",
											countRefsInMappool, references
													.size()
													- countRefsInMappool),
							AtlasCreator
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
	 * @return A {@link List} of human-readable strings representing
	 *         {@link DpEntry}s and/or {@link Map}s which are not referenced
	 *         from the grouptree.
	 */
	public List<String> listNotReferencedInGroupTree() {
		List<String> unrefed = new LinkedList<String>();

		for (Map map : getMapPool().values()) {
			LinkedList<AtlasRefInterface<?>> refs = new LinkedList<AtlasRefInterface<?>>();
			Group.findReferencesTo(this, map, refs, false);
			if (refs.size() == 0)
				unrefed.add("Map: " + map.getTitle()); // i8n
		}

		for (DpEntry<? extends ChartStyle> dpe : getDataPool().values()) {
			LinkedList<AtlasRefInterface<?>> refs = new LinkedList<AtlasRefInterface<?>>();
			Group.findReferencesTo(this, dpe, refs, false);
			if (refs.size() == 0)
				unrefed.add("DatapoolEntry: " + dpe.getTitle() + ", ("
						+ dpe.getFilename() + ")"); // i8n
		}

		return unrefed;
	}

	/**
	 * @return A {@link List} of {@link DpEntry}s which are not used in the
	 *         atlas. If no {@link Map} is linked from the {@link Group}-Tree
	 *         and no default map is selected, the first map (if any exists) of
	 *         the atlas is use as the startup map.
	 */
	public List<DpEntry<? extends ChartStyle>> listNotReferencedInGroupTreeNorInAnyMap() {
		List<DpEntry<? extends ChartStyle>> unrefed = new LinkedList<DpEntry<? extends ChartStyle>>();

		for (DpEntry<? extends ChartStyle> dpe : getDataPool().values()) {
			LinkedList<AtlasRefInterface<?>> refs = new LinkedList<AtlasRefInterface<?>>();
			Group.findReferencesTo(this, dpe, refs, false);

			if (refs.size() > 0) {
				// We have a direct reference, so no chance to add this DPE to
				// the list.
				continue;
			}

			/**
			 * Now look at all the maps that are actually part of the atlas.
			 */

			// LOGGER.debug("Checking " + dpe.getId() + " / " + dpe.getTitle());
			// if (dpe.getId().equals("pyr_satbild01940922212")) {
			// LOGGER.debug("\n\n" + dpe.getId() + " / " + dpe.getTitle());
			// }
			//			
			boolean referencedFromAMap = false;

			for (Map m : getMapPool().values()) {
				boolean thisMapIsInteresting = false;

				// Check, if this map is directly reachable from the group tree.
				LinkedList<AtlasRefInterface<?>> refs2map = new LinkedList<AtlasRefInterface<?>>();

				Group.findReferencesTo(this, m, refs2map, false);

				if (refs2map.size() == 0) {
					// This map is not interesting, EXCEPT it is the default map

					if (getMapPool().getStartMapID() != null
							&& getMapPool().getStartMapID().equals(m.getId())) {
						// Startupmap has been defined and this is it.
						thisMapIsInteresting = true;
					}

					if ((getMapPool().getStartMapID() == null && getMapPool()
							.get(0).equals(m))) {
						// NO startup map has been defined, but this is map
						// number zero.
						thisMapIsInteresting = true;
					}
				} else {
					thisMapIsInteresting = true;
				}

				if (!thisMapIsInteresting)
					continue;

				//				
				// if (dpe.getId().equals("pyr_satbild01940922212")) {
				// LOGGER.debug(" Map : "+m.getId() + " / " +
				// m.getTitle()+" is referenced from the Grouptree");
				// }
				//

				/**
				 * OK... If we find a reference to our DPE in this map, the
				 * DpEntry can be reached from the atlas
				 */
				// for (DpRef dpr : m.getLayers()) {
				// referencedFromAMap = true;
				// break;
				// if (dpr.getTarget().equals(dpe)) {
				// }
				// }
				if (m.containsDpe(dpe.getId())) {
					referencedFromAMap = true;
					break;
				}
			}

			/**
			 * If there are no direct references from the group tree, AND there
			 * are no references from maps that are in the group tree (or the
			 * startup map), then add it to the list
			 */
			if (!referencedFromAMap) {
				// LOGGER.info(dpe.getId() + "/" + dpe.getTitle()
				// + " are unreachable!");
				unrefed.add(dpe);
			}
		}
		return unrefed;
	}

	/**
	 * @return {@link List} of {@link File}s to the HTML About Documents for all
	 *         languages. The order of the {@link File}s equals the order of the
	 *         languages in getLanguages(). If the files do not exist they will
	 *         be created with defaults.
	 * @throws IOException
	 *             If the default files can not be created.
	 */
	public List<File> getAboutHtMLFiles(Component owner) {
		ArrayList<File> urls = new ArrayList<File>();

		for (String lang : getLanguages()) {
			File aboutHTMLfile = new File(getAboutDir(), "about_" + lang
					+ ".html");
			try {

				if (!aboutHTMLfile.exists()) {
					/**
					 * Create a default HTML About window
					 */

					FileWriter fw = new FileWriter(aboutHTMLfile);
					fw
							.write("<html> <body> <p> "
									+ AtlasCreator.RESOURCE
											.getString(
													"EditAboutWindow.TabName",
													(getTitle().get(lang) != null && getTitle()
															.get(lang).equals(
																	"")) ? "..."
															: getTitle().get(
																	lang),
													new Locale(lang)
															.getDisplayLanguage(new Locale(
																	Translation
																			.getActiveLang())))
									+ " </p> </body> </html>");
					fw.flush();
					fw.close();
				}
				urls.add(aboutHTMLfile);
			} catch (Exception e) {
				ExceptionDialog.show(owner, new AtlasException(
						"Couldn't create the default HTML about file.", e));
			}

		}
		return urls;
	}

	/**
	 * @return {@link List} of {@link File}s to the HTML Popup documents for all
	 *         languages. The order of the {@link File}s equals the order of the
	 *         languages in getLanguages(). If the files do not exist they will
	 *         be created with defaults.
	 * @throws IOException
	 *             If the default files can not be created.
	 */
	public List<File> getPopupHtMLFiles(Window owner) {
		ArrayList<File> urls = new ArrayList<File>();

		for (String lang : getLanguages()) {
			File popupHTMLfile = new File(getAboutDir(), "popup_" + lang
					+ ".html");
			try {

				if (!popupHTMLfile.exists()) {
					/**
					 * Create a default HTML About window
					 */

					FileWriter fw = new FileWriter(popupHTMLfile);
					fw
							.write("<html> <body> <p> "
									+ AtlasCreator.RESOURCE
											.getString(
													"EditPopupWindow.TabName",
													(getTitle().get(lang) != null && getTitle()
															.get(lang).equals(
																	"")) ? "..."
															: getTitle().get(
																	lang),
													new Locale(lang)
															.getDisplayLanguage(new Locale(
																	Translation
																			.getActiveLang())))
									+ " </p> </body> </html>");
					fw.flush();
					fw.close();
				}
				urls.add(popupHTMLfile);
			} catch (Exception e) {
				ExceptionDialog.show(owner, new AtlasException(
						"Couldn't create the default HTML popup file.", e));
			}

		}
		return urls;
	}

	/**
	 * A helper method to determine the on disk size of a {@link DpEntry}. Size
	 * is the recursive folder size in bytes... so this method might may take
	 * some while to finish. Once determined, the size is cached.<br/>
	 * The calculation omits all files, that are blacklisted for the exports,
	 * that means, that Thumbs.db, .svn etc are not counted.
	 * 
	 * @param dpe
	 *            The {@link DpEntry} to have a look at.
	 * 
	 * @see #uncacheFolderSize(DpEntry)
	 * 
	 * @return Number of Bytes in the corresponding folder.
	 */
	public Long getFolderSize(DpEntry<? extends ChartStyle> dpe) {
		final String id = dpe.getId();
		if (!(rememberFolderSizes.containsKey(id))) {

			File directory = new File(getDataDir(), dpe.getDataDirname());
			// LOGGER.debug("\nDetermining the size of " + directory);
			try {
				rememberFolderSizes.put(id,
						sizeOfDirectoryWithBlacklist(directory));
			} catch (Exception e) {
				LOGGER.warn("Failed to calculate the size of folder "
						+ directory, e);
				return -1l;
			}
		}
		return rememberFolderSizes.get(id);
	}

	/**
	 * Counts the size of a directory recursively (sum of the length of all
	 * files). It omits the blacklisted filed via
	 * {@link GpUtil#BlacklistedFoldersFilter} and
	 * {@link GpUtil#BlacklistesFilesFilter}.
	 * 
	 * @param directory
	 *            directory to inspect, must not be <code>null</code>
	 * @return size of directory in bytes, 0 if directory is security restricted
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 */
	private Long sizeOfDirectoryWithBlacklist(File directory) {

		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		long size = 0;

		Collection<File> files = FileUtils.listFiles(directory,
				GpUtil.BlacklistesFilesFilter, GpUtil.BlacklistedFoldersFilter);
		if (files == null) { // null if security restricted
			return 0L;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				size += sizeOfDirectoryWithBlacklist(file);
			} else {
				size += file.length();
			}
		}

		return size;
	};

	/**
	 * Removes the cached size of {@link DpEntry}'s folder. Next time the folder
	 * size is queried, it will have to be recalculated.
	 * 
	 * @see #getFolderSize(DpEntry)
	 */
	public void uncacheFolderSize(DpEntry<? extends ChartStyle> dpe) {
		if (rememberFolderSizes.containsKey(dpe.getId())) {
			rememberFolderSizes.remove(dpe.getId());
		}
	}

//	/**
//	 * Resets the {@link ResourceLoader} to contain. This reset is needed
//	 * whenever the Geopublisher loads a new atlas in the same JVM instance..
//	 */
//	public void resetResLoMan() {
//
//		LOGGER.info("Resetting the ResLoMan to only contain defaults");
//
//		resLoMan = new ResourceLoaderManager();
//
////		setupResLoMan();
//
//	}

	/**
	 * Define the list of languages supported by this atlas. This may trigger a
	 * Translation.LocaleChangeEvent even though the locale didn't really
	 * change.
	 */
	public final void setLanguages(List<String> languages) {
		setLanguages(languages.toArray(new String[] {}));
	}

	/**
	 * Define the list of languages supported by this atlas. This may trigger a
	 * Translation.LocaleChangeEvent even though the locale didn't really
	 * change.
	 */
	@Override
	public void setLanguages(String... langs) {
		//
		// /*
		// * Check if there is any real change.
		// */
		// if (Arrays.equals(langs, getLanguages().toArray(new String[] {})))
		// return;

		super.setLanguages(langs);
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
	public List<File> getHTMLFilesFor(DpLayer<?, ? extends ChartStyle> dpl) {

		List<File> htmlFiles = new ArrayList<File>();

		File dir = new File(getDataDir(), dpl.getDataDirname());
		for (String lang : getLanguages()) {
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
					fw.write(AtlasCreator.R("DPLayer.HTMLInfo.DefaultHTMLFile",
							I8NUtil.getLocaleFor(lang).getDisplayLanguage(),
							dpl.getTitle()));

					fw.flush();
					fw.close();
				}
				htmlFiles.add(htmlFile);

			} catch (IOException e) {
				LOGGER.error(e);
				ExceptionDialog.show(AtlasCreator.getInstance().getJFrame(), e);
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
	public List<File> getHTMLFilesFor(Map map) {

		List<File> htmlFiles = new ArrayList<File>();

		File dir = new File(getHtmlDir(), map.getId());
		dir.mkdirs();

		for (String lang : getLanguages()) {
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
					fw.write(AtlasCreator.R("Map.HTMLInfo.DefaultHTMLFile",
							I8NUtil.getLocaleFor(lang).getDisplayLanguage(),
							map.getTitle()));

					fw.flush();
					fw.close();
				}
				htmlFiles.add(htmlFile);

			} catch (IOException e) {
				LOGGER.error(e);
				ExceptionDialog.show(AtlasCreator.getInstance().getJFrame(), e);
			}
		}
		return htmlFiles;
	}

	/**
	 * Uncaches all cached information and will result in the GP to continue
	 * with some IO stuff.
	 */
	public void uncacheAndReread() {

		rememberFolderSizes.clear();

		/**
		 * First uncache all Styles
		 */
		for (DpEntry<? extends ChartStyle> dpe : getDataPool().values()) {
			uncacheAndReread(dpe);
		}

		for (Map map : getMapPool().values()) {
			map.uncache(null);
			map.getMissingHTMLLanguages();
			map.getCrs();
		}

	}

	/**
	 * Uncaches all cached information for a DPE Entry
	 */
	public void uncacheAndReread(DpEntry<? extends ChartStyle> dpe) {

		rememberFolderSizes.remove(dpe.getId());
		dpe.uncache();
		dpe.getQuality();
		if (dpe instanceof DpLayer) {
			DpLayer<?, ? extends ChartStyle> dpl = (DpLayer<?, ? extends ChartStyle>) dpe;
			dpl.getMissingHTMLLanguages();
			dpl.getGeoObject();
			dpl.getCrs();
			

			// Correcting any wrongly upper/lowercased attribute names
			if (dpe instanceof DpLayerVectorFeatureSource) {
				Style style = ((DpLayerVectorFeatureSource) dpe).getStyle();
				
				style  = StylingUtil.correctPropertyNames(style,
						((DpLayerVectorFeatureSource) dpe).getSchema());
				
				((DpLayer) dpe).setStyle(style);
			}
		}
		getFolderSize(dpe);
		
		// Even though we didn't throw away the Style, we have to check the attributes again
		
	}

	/**
	 * @return a {@link File} object pointing to the main file of the
	 *         {@link DpEntry}
	 */
	public File getFileFor(DpEntry<? extends ChartStyle> dpe) {
		return new File(new File(getDataDir(), dpe.getDataDirname()), dpe
				.getFilename());
	}

	/**
	 * Uncaches all cached information..
	 */
	@Override
	public void uncache() {
		rememberFolderSizes.clear();
		
		super.uncache();
	}

}
