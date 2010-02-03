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
package skrueger.atlas;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import rachel.ResourceLoader;
import rachel.loader.ClassResourceLoader;
import rachel.loader.ResourceLoaderManager;
import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.Group;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;

/**
 * The whole Configuration for one Atlas project
 * 
 * @author Stefan Alfons Krüger
 */
public class AtlasConfig {
	private static final Logger LOGGER = Logger.getLogger(AtlasConfig.class);

	{
		// http://forum.java.sun.com/thread.jspa?threadID=217692&messageID=1946056
		UIManager.getDefaults().put(
				"Button.focusInputMap",
				new UIDefaults.LazyInputMap(new Object[] { "ENTER", "pressed",
						"released ENTER", "released", "SPACE", "pressed",
						"released SPACE", "released" }));
	}

	// /**
	// * If true, then the AtlasViewer classes will try to not cache files that
	// * could be changed during runtime, like SLDs
	// */
	// boolean noCachingMode = false;

	/**
	 * Resource name of the map emblem that will be shown hovering above the map
	 * in the lower right corner *
	 */
	public static final String MAPICON_RESOURCE_NAME = "ad/map_logo.png";

	/**
	 * Resource name of the icon that will be used for the About-Dialog and
	 * JavaWebStart (Attention: autostart.inf uses .ico only)
	 */
	public static final String JWSICON_RESOURCE_NAME = "ad/icon.gif";

	/**
	 * Resource name of the icon that will be used for JavaWebStart if the
	 * user-defined one can't be found.
	 */
	public static final String JWSICON_RESOURCE_NAME_FALLBACK = "skrueger/creator/resource/icon.gif";

	public static final String JSMOOTH_PROJEKT_RESOURCE = "skrueger/creator/export/jsmooth/atlas.jsmooth";
	public static final String JSMOOTH_SKEL_AD_RESOURCE1 = "skrueger/creator/export/jsmooth/autodownload-wrapper/autodownload.exe";
	public static final String JSMOOTH_SKEL_AD_RESOURCE2 = "skrueger/creator/export/jsmooth/autodownload-wrapper/autodownload.skel";
	public static final String JSMOOTH_SKEL_AD_RESOURCE3 = "skrueger/creator/export/jsmooth/autodownload-wrapper/customdownload.skel";

	/**
	 * Resource name of the splashscreen image that will be used for
	 * JavaWebStart and start.bat
	 */
	public static final String SPLASHSCREEN_RESOURCE_NAME = "ad/splashscreen.png";

	/**
	 * Resource name of the splashscreen image that will be used for
	 * JavaWebStart and start.bat IF if user defined one can't be found.
	 */
	public static final String SPLASHSCREEN_RESOURCE_NAME_FALLBACK = "skrueger/creator/resource/splashscreen.png";

	public static final String LICENSEHTML_RESOURCE_NAME = "skrueger/atlas/resource/license.html";

	/** The name of the directory containing the "About"- HTML pages **/
	public static final String ABOUT_DIRNAME = "about";

	/** The name of the directory containing the images used in HTML pages **/
	public static final String IMAGES_DIRNAME = "images";

	/**
	 * Name of the directory relative to {@value #ATLASDATA_DIRNAME} that
	 * contains HTML-folders for every map
	 **/
	public static final String HTML_DIRNAME = "html";

	/**
	 * Name of the directory relative to {@value #ATLASDATA_DIRNAME} that
	 * contains data-folders for every {@link DpEntry}
	 **/
	public static final String DATA_DIRNAME = "data";

	/**
	 * The name of the directory relative to the .gpa file that conatins atlas
	 * data. Usually 'ad'
	 **/
	public static final String ATLASDATA_DIRNAME = "ad";

	/** This is THE instance of the {@link ResourceLoaderManager} */
	protected static ResourceLoaderManager resLoMan = new ResourceLoaderManager();

	/** Name of a file in the ad-folder that stores the atlas default CRS **/
	public static String DEFAULTCRS_FILENAME = "defaultcrs.prj";

	private Translation title = new Translation();

	private Translation desc = new Translation();

	private Translation creator = new Translation();

	private Translation copyright = new Translation();

	/**
	 * TODO Think about the version field. At the moment it is used nowhere, but
	 * increased by 0.1 very time we save.
	 */
	private Float atlasversion = 0f;

	/**
	 * A HashMap with all the defined {@link Map}s, identified by their IDs.
	 */
	private MapPool mapPool = new MapPool();

	/**
	 * A HashMap with all the defined {@link DpEntry}, identified by their IDs.
	 */
	private DataPool datapool = new DataPool();

	/**
	 * List of LanguageCodes that this Atlas is supposed to "speak". Consistency
	 * with all description-fields will be checked during export.
	 */
	private List<String> languages = new LinkedList<String>();

	/**
	 * Root-group of the "Topics-tree", defined by < aml:group > tags The
	 * subgroup called "menubar" will be parsed as {@link JMenuBar} in the
	 * AtlasViewer.
	 */
	private Group firstGroup = new Group(this);

	private String resourceBasename = ATLASDATA_DIRNAME + "/" + DATA_DIRNAME
			+ "/";

	/**
	 * An readily instantiated DatapoolEntry is added...
	 * 
	 * @param entry
	 *            {@link DpEntry}-subclass. Must not be <code>null</code>
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void add(DpEntry<? extends ChartStyle> entry) {
		datapool.add(entry);
	}

	/**
	 * Returns the translated name of the Atlas
	 */
	public final Translation getTitle() {
		return title;
	}

	/**
	 * Set the {@link Translation} of the name of the atlas
	 */
	public void setTitle(Translation title) {
		this.title = title;
	}

	public Float getAtlasversion() {
		return atlasversion;
	}

	public void setAtlasversion(Float atlasversion) {
		this.atlasversion = atlasversion;
	}

	public void setMapPool(MapPool mapPool) {
		this.mapPool = mapPool;
	}

	public void setDatapool(DataPool datapool) {
		this.datapool = datapool;
	}

	/**
	 * Returns a {@link DataPool} {@link HashMap} (ID-> {@link DpEntry})
	 */
	public DataPool getDataPool() {
		return datapool;
	}

	/**
	 * Reset the {@link List} of supported Languages to the passed
	 * {@link String}
	 * 
	 * @param langs
	 *            new {@link List} of lang codes
	 */
	public void setLanguages(String... langs) {
		if (langs.length > 0)
			languages.clear();

		for (String code : langs) {
			code = code.trim();
			if (I8NUtil.isValidISOLangCode(code)) {
				LOGGER.debug("Atlas " + getTitle() + " set up to support "
						+ code);
				languages.add(code);
			} else
				throw new IllegalArgumentException("The ISO Language code '"
						+ code + "' is not known/valid." + "\nIgnoring it.");
		}

	}

	public final List<String> getLanguages() {
		return languages;
	}

	/**
	 * Returns a {@link MapPool} {@link HashMap} (ID-> {@link Map})
	 */

	public final MapPool getMapPool() {
		return mapPool;
	}

	/**
	 * Returns the root/first group of the "groups-tree" which contains
	 * references to {@link DpEntry}s or sub groups *
	 */
	public Group getFirstGroup() {
		return firstGroup;
	}

	/**
	 * Sets the root/first group of the "groups-tree" which can contain
	 * references to {@link DpEntry}s or sub groups *
	 */
	public void setFirstGroup(Group firstGroup) {
		this.firstGroup = firstGroup;
	}

	/**
	 * @return the {@link ResourceLoaderManager} that unites the
	 *         {@link ResourceLoader}s for this application
	 */
	public static ResourceLoaderManager getResLoMan() {
		return resLoMan;
	}

	public Translation getDesc() {
		return desc;
	}

	public void setDesc(Translation desc) {
		this.desc = desc;
	}

	public Translation getCreator() {
		return creator;
	}

	public void setCreator(Translation creator) {
		this.creator = creator;
	}

	public Translation getCopyright() {
		return copyright;
	}

	public void setCopyright(Translation copyright) {
		this.copyright = copyright;
	}

	/**
	 * Copies bytes from the URL <code>source</code> to a file
	 * <code>destination</code>. The directories up to <code>destination</code>
	 * will be created if they don't already exist. <code>destination</code>
	 * will be overwritten if it already exists.
	 * 
	 * If an IO error occurs during copying it is not thrown!
	 */
	public static void exportURLtoFileNoEx(URL source, File dest) {
		try {
			FileUtils.copyURLToFile(source, dest);
		} catch (IOException e) {
		}
	}

	//
	// /**
	// * If true, then the AtlasViewer classes will try not to cache files that
	// * could be changed during runtime, like SLDs, .prj, ...
	// * @Deprecated Use ACE.uncache()
	// *
	// */
	// public boolean isNoCachingMode() {
	// return noCachingMode;
	// }
	//
	// /**
	// * If true, then the AtlasViewer classes will try not to cache files that
	// * could be changed during runtime, like SLDs
	// * @Deprecated Use ACE.uncache()
	// */
	// public void setNoCachingMode(boolean noCachingMode) {
	// this.noCachingMode = noCachingMode;
	// }

	/**
	 * @return Default "ad/data/"
	 */
	public String getResouceBasename() {
		return resourceBasename;
	};

	/**
	 * Change the basename of any datapool resource in the Atlas. Main reason
	 * for this method is the JUnit test scenario. Is this method is not called,
	 * <code>ad/data/</code> will be the default.
	 * 
	 * @param baseName
	 */
	public void setResouceBasename(String baseName) {
		resourceBasename = baseName;
	};

	/**
	 * @return {@link URL} to the HTML About Document for the active language
	 */
	public URL getAboutHTMLURL() {

		// TODO dangerouse redundant paths here.. see ACE.getAboutDir()

		String location = "ad/html/" + ABOUT_DIRNAME + "/about_"
				+ Translation.getActiveLang() + ".html";
		URL url = AtlasConfig.getResLoMan().getResourceAsUrl(location);
		LOGGER.debug("AboutHTML URL = " + url + " for location = " + location);
		return url;
	}

	/**
	 * @return {@link URL} to the HTML Popup Document for the active language
	 */
	public URL getPopupHTMLURL() {

		// TODO dangerous redundant paths here.. see ACE.getAboutDir()

		String location = "ad/html/about/popup_" + Translation.getActiveLang()
				+ ".html";
		URL url = AtlasConfig.getResLoMan().getResourceAsUrl(location);
		LOGGER.debug("PopupHTML URL = " + url + " for location = " + location);
		return url;
	}

	/**
	 * @return <code>null</code> if no icon found. Otherwise an {@link URL} to a
	 *         user-defined- or default icon.
	 */
	public URL getIconURL() {
		URL iconURL = getResLoMan().getResourceAsUrl(JWSICON_RESOURCE_NAME);
		if (iconURL == null)
			iconURL = getResLoMan().getResourceAsUrl(
					JWSICON_RESOURCE_NAME_FALLBACK);
		return iconURL;
	}

	/**
	 * Adds a {@link ClassResourceLoader} of AtlasViewer class to the
	 * {@link ResourceLoader}. Neede at startup of {@link AtlasViewer} and
	 * Geopublisher
	 */
	public static void setupResLoMan() {
		// Adding the default ClassResourceLoader
		System.out
				.println("Adding new ClassResourceLoader( AtlasViewer.class ) to ResLoMan");
		getResLoMan().addResourceLoader(
				new ClassResourceLoader(AtlasViewer.class));

	}

}