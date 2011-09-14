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

import java.awt.Component;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.geopublisher.GPProps.Keys;

import de.schmitzm.geotools.io.GeoExportUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.ResourceProvider;
import de.schmitzm.net.mail.Mailer;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.FileExtensionFilter;
import de.schmitzm.swing.formatter.MbDecimalFormatter;
import de.schmitzm.versionnumber.ReleaseUtil;

public class GpUtil {
	
	static {
		// https://issues.apache.org/bugzilla/show_bug.cgi?id=27970
		System.setProperty("org.apache.batik.warn_destination", "false");
	}

	private static final Logger LOGGER = Logger.getLogger(GpUtil.class);

	/**
	 * {@link FileFilter} for image files (accepts .png, .jpg, .jpeg, .tif,
	 * .tiff, .gif).
	 */
	public static final FileExtensionFilter IMAGE_FILE_FILTER = new FileExtensionFilter(
			"Images", true, ".png", ".jpg", ".jpeg", ".tif", ".tiff", ".gif");

	/**
	 * {@link FileFilter} for image files (accepts .png, .jpg, .jpeg, .tif,
	 * .tiff, .gif).
	 */
	public static final FileExtensionFilter GIS_FILE_FILTER = new FileExtensionFilter(
			"GIS-Data", true, ".shp", ".tif", ".tiff", ".a00", ".asc", ".gml");

	/**
	 * This Mailer can sends E-Mails to <code>bugreport@wikisquare.de</code> and
	 * can not be used to send mails to any other address.
	 */
	public static final Mailer bugReportMailer = new Mailer("bugreport", "",
			"", "mail.wikisquare.de", "bugreport@wikisquare.de");

	/**
	 * Setting up the logger from a global XML configuration file. This method
	 * might be called multiple times.<br/>
	 * This method does not do anything if there are already appenders defined.
	 */
	public static void initGpLogging() throws FactoryConfigurationError {
		if (Logger.getRootLogger().getAllAppenders().hasMoreElements())
			return;

		DOMConfigurator.configure(GPProps.class
				.getResource("/geopublishing_log4j.xml"));

		Logger.getRootLogger().addAppender(
				Logger.getLogger("dummy").getAppender("gpFileLogger"));

		// Apply the LOG level configured in the user-specific application
		// .properties file
		String logLevelStr = GPProps.get(Keys.logLevel);
		if (logLevelStr != null) {
			Logger.getRootLogger().setLevel(Level.toLevel(logLevelStr));
		}

		initBugReporting();
	}

	public static void initBugReporting() {
		ExceptionDialog.setMailDestinationAddress("tzeggai@wikisquare.de");
		ExceptionDialog.setSmtpMailer(bugReportMailer);

		// Add application version number to Exception mails
		ExceptionDialog.addAdditionalAppInfo(ReleaseUtil
				.getVersionInfo(GpUtil.class));
	}

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
		return String.valueOf(decimalFormat.format(Math.abs(GpCoreUtil.RANDOM
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
	

	/**
	 * Performs a file OPEN choose as a fallback
	 * 
	 * @param parent
	 *            component for the dialog (can be {@code null})
	 * @param startFolder
	 *            start folder for the chooser (if {@code null} "/" is used)
	 * @param filter
	 *            defines which files can be selected. Only the last filter in the list will be offered due to
	 *            limitations
	 * @return {@code null} if the dialog was not approved
	 */
	public static File chooseFileOpenFallback(Component parent, File startFolder, String title,
			FileExtensionFilter... filters) {
		if (startFolder == null)
			startFolder = new File("/");
		
		if (startFolder.isFile())
			startFolder = startFolder.getParentFile();

		JFileChooser chooser = new JFileChooser(startFolder);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);

		if (filters != null) {
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(filters[0].toJFileChooserFilter());
		}
		if (title != null)
			chooser.setDialogTitle(title);

		int ret = chooser.showOpenDialog(parent);
		if (ret == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		return null;
	}


}
