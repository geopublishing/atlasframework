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
package org.geopublishing.geopublisher;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;

import schmitzm.geotools.io.GeoExportUtil;
import schmitzm.io.IOUtil;
import schmitzm.lang.ResourceProvider;
import schmitzm.swing.ExceptionDialog;
import skrueger.swing.formatter.MbDecimalFormatter;

public class GpUtil {

	private static final Logger LOGGER = Logger.getLogger(GpUtil.class);

	/**
	 * // Setting up the logger from a XML configuration file. We do that gain
	 * // in GPPros, as it outputs log messages first. Does not change the
	 * configuration if there are already appenders defined.
	 */
	public static void initGpLogging() throws FactoryConfigurationError {
		if (Logger.getRootLogger().getAllAppenders().hasMoreElements())
			return;
		DOMConfigurator.configure(GPProps.class.getResource("/gp_log4j.xml"));
	}

	/**
	 * This {@link IOFileFilter} returns <code>false</code> for files that
	 * should be omitted during export and when calculating the size of a
	 * {@link DpEntry} folder.
	 */
	public static final IOFileFilter BlacklistesFilesFilter = new IOFileFilter() {

		@Override
		public boolean accept(File file) {
			if (file.getName().equals("Thumbs.db"))
				return false;
			if (file.getName().endsWith("~"))
				return false;
			if (file.getName().toLowerCase().endsWith("bak"))
				return false;
			return true;
		}

		@Override
		public boolean accept(File dir, String name) {
			return accept(new File(dir, name));
		}
	};

	/**
	 * This filter omits SVN and CSV sub-folders.<br/>
	 * This {@link IOFileFilter} returns <code>false</code> for folder that
	 * should be omitted during export and when calculating the size of a
	 * {@link DpEntry} folder or chen copying folders in general.
	 */
	public static final IOFileFilter BlacklistedFoldersFilter = FileFilterUtils
			.makeCVSAware(FileFilterUtils.makeSVNAware(null));

	/**
	 * This {@link IOFileFilter} only return .ttf files
	 */
	public static final IOFileFilter FontsFilesFilter = new IOFileFilter() {

		@Override
		public boolean accept(File file) {
			if (file.getName().toLowerCase().endsWith(".ttf"))
				return true;
			return false;
		}

		@Override
		public boolean accept(File dir, String name) {
			return accept(new File(dir, name));
		}

	};

	public static final MbDecimalFormatter MbDecimalFormatter = new MbDecimalFormatter();

	/**
	 * {@link ResourceProvider}, der die Lokalisation fuer GUI-Komponenten des
	 * Package {@code skrueger.swing} zur Verfuegung stellt. Diese sind in
	 * properties-Datein unter {@code skrueger.swing.resource.locales}
	 * hinterlegt.
	 */
	protected final static ResourceProvider RESOURCE = ResourceProvider
			.newInstance("locales.GeopublisherTranslation", Locale.ENGLISH);

	/**
	 * Return all langauges that we have at least some .properties translations
	 * for.
	 */
	public static Set<Locale> getAvailableLocales() {
		return RESOURCE.getAvailableLocales(true);
	}

	/**
	 * Convenience method to access the {@link Geopublisher}s translation
	 * resources.
	 * 
	 * @param key
	 *            the key for the *Translation.properties file
	 * @param values
	 *            optional values
	 */
	public static String R(String key, Object... values) {
		return RESOURCE.getString(key, values);
	}

	/**
	 * Convenience method to access the {@link Geopublisher}s translation
	 * resources.
	 * 
	 * @param key
	 *            the key for the *Translation.properties file
	 * @param reqLanguage
	 *            requested Language/Locale
	 * @param values
	 *            optional values
	 */
	public static String R(final String key, Locale reqLanguage,
			final Object... values) {
		return RESOURCE.getString(key, reqLanguage, values);
	}

	/**
	 * Generates a random ID number with fix number of digits (11) and a leading
	 * prefix {@link String}.
	 * 
	 * @param prefix
	 *            A string with at least one char. Without a leading letter,
	 *            it's not usable as a valid XML identifier.
	 */
	public final static String getRandomID(String prefix) {
		DecimalFormat decimalFormat = new DecimalFormat(prefix + "_00000000000");
		return String.valueOf(decimalFormat.format(Math.abs(AVUtil.RANDOM
				.nextInt(Integer.MAX_VALUE))));
	}

	/**
	 * Stores the Charset defined in {@link DpEntry#getChart} as a .cpg file
	 * containing the name of charset. If the file already contains the same
	 * content, it is not written (SVN friendly)
	 */
	public static void writeCpg(DpLayerVectorFeatureSource dpe) {
		final AtlasConfigEditable ace = (AtlasConfigEditable) dpe
				.getAtlasConfig();

		final File cpgFile = IOUtil.changeFileExt(ace.getFileFor(dpe), "cpg");

		try {
			GeoExportUtil.writeCharset(cpgFile, dpe.getCharset());
		} catch (Exception e) {
			final String errMessage = "Unable to store the codepage settings for "
					+ dpe + "\nThe export will be continued."; // i8n
			LOGGER.warn(errMessage, e);
			// NULL TODO Null owner not nice
			ExceptionDialog.show(null, e, null, errMessage);
		}

	}

}
