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
package org.geopublishing.geopublisher;

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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasRefInterface;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.map.MapPool.EventTypes;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geotools.styling.Style;

import rachel.loader.FileResourceLoader;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.ExceptionDialog;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;

/**
 * An extension of the AtlasConfig for use within AtlasCreator. One purpose is,
 * to keep {@link File} related references out of {@link AtlasViewerGUI}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class AtlasConfigEditable extends AtlasConfig {

	public AtlasConfigEditable(File atlasDir) {
		if (!atlasDir.exists() || !atlasDir.isDirectory())
			throw new IllegalArgumentException(
					atlasDir
							+ " is not a directory. An editable atlas (ACE) can only be created/openend from a directory.");

		this.atlasDir = atlasDir;
		getResLoMan().addResourceLoader(new FileResourceLoader(atlasDir));
	}

	/** The name of the marker file for a AtlasWorkingCopy folder **/
	public static final String ATLAS_GPA_FILENAME = "atlas.gpa";

	final static private Logger LOGGER = Logger
			.getLogger(AtlasConfigEditable.class);

	private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

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
			fg.setTitle(new Translation(activeLang, GpUtil
					.R("FirstGroup.DefaultTitle")));
			fg.setDesc(new Translation(activeLang, GpUtil
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
				org.geopublishing.atlasViewer.dp.DataPool.EventTypes.unknown);
	}

	/**
	 * Directory where the Atlas was created in, or loaded from, or saved to.
	 * AtlasDir has <code>ad</code> sub-directory!
	 */
	private final File atlasDir;

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

	//
	// /**
	// * Set the atlasDir as {@link File}. Also registers the folder with the
	// * {@link ResourceManager} as a {@link FileResourceLoader}.
	// * AtlasConfigEditable.resetResLoMan() should be called before calling
	// this
	// * method.
	// *
	// * @param atlasDir_
	// * directory where the Atlas was loaded, or where it was created.
	// */
	// public void setAtlasDir(File atlasDir_) {
	// this.atlasDir = atlasDir_;
	//
	// LOGGER.debug("AtlasDir of AtlasConfigEditable set to "
	// + atlasDir.getAbsolutePath());
	//
	// // Initialize ResMan to see the working directory
	// LOGGER.debug("Registering " + atlasDir_
	// + " directory as FileResourceLoader with ResMan");
	// getResLoMan().addResourceLoader(new FileResourceLoader(atlasDir));
	// }

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
	 * @return a subset of {@link #values()}, containing only the {@link DpEntry}s that are actually referenced in the atlas
	 */
	public List<DpEntry<? extends ChartStyle>> getUsedDpes() {
		List<DpEntry<? extends ChartStyle>> notUsed = getUnusedDpes();
		List<DpEntry<? extends ChartStyle>> used = new ArrayList<DpEntry<? extends ChartStyle>>();
		
		// TODO faster or cache
		for (DpEntry dpe : getDataPool().values()) {
			if (notUsed.contains(dpe)) continue;
			used.add(dpe);
		}
		
		return used;
	}

	/**
	 * @return A {@link List} of {@link DpEntry}s which are not used in the
	 *         atlas. If no {@link Map} is linked from the {@link Group}-Tree
	 *         and no default map is selected, the first map (if any exists) of
	 *         the atlas is use as the startup map.
	 */
	public List<DpEntry<? extends ChartStyle>> getUnusedDpes() {

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
			boolean referencedFromAMap = false;

			for (Map m : getMapPool().values()) {
				boolean thisMapIsInteresting = false;

				// Check, if this map is directly reachable from the group tree.
				LinkedList<AtlasRefInterface<?>> refs2map = new LinkedList<AtlasRefInterface<?>>();
				Group.findReferencesTo(this, m, refs2map, false);

				if (refs2map.size() == 0) {
					// This map is not interesting, EXCEPT it is the default map

					if (m.getId().equals(getMapPool().getStartMapID())) {
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

				/**
				 * OK... If we find a reference to our DPE in this map, the
				 * DpEntry can be reached from the atlas
				 */
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
									+ GpUtil
											.R(
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
									+ GpUtil
											.R(
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

	// /**
	// * Resets the {@link ResourceLoader} to contain. This reset is needed
	// * whenever the Geopublisher loads a new atlas in the same JVM instance..
	// */
	// public void resetResLoMan() {
	//
	// LOGGER.info("Resetting the ResLoMan to only contain defaults");
	//
	// resLoMan = new ResourceLoaderManager();
	//
	// // setupResLoMan();
	//
	// }

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

				style = StylingUtil.correctPropertyNames(style,
						((DpLayerVectorFeatureSource) dpe).getSchema());

				((DpLayer) dpe).setStyle(style);
			}
		}
		getFolderSize(dpe);

		// Even though we didn't throw away the Style, we have to check the
		// attributes again

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
