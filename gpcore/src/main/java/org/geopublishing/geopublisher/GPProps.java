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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.geopublishing.geopublisher.GPProps.Keys;

import schmitzm.swing.ExceptionDialog;

/**
 * Singleton and delegation pattern to the {@link Geopublisher}'s
 * {@link Properties}
 * 
 * Only the methods working on the enumeration {@link Keys} are public
 * Interanally the {@link Keys} are saved as {@link String}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public abstract class GPProps {

	private static final String DEFAULTS_POSTFIX = ".defaults";

	private static final Logger LOGGER = Logger.getLogger(GPProps.class);

	/**
	 * List of all legal keys in the
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 */
	public enum Keys {
		antialiasingMaps, bugReportEmail, compressJARs, gpWindowHeight, gpWindowLeftDividerLocation, gpWindowRightDividerLocation, gpWindowWidth, JarCmd, JarSignerCmd, jnlpURL, JWSStartScript,
		/** Remember the last file we imported **/
		LAST_IMPORTED_FILE,
		/** Remember the GP Atlas folder we imported stuff from the last time **/
		LAST_IMPORTED_GPA, LastExportDisk,

		LastExportFolder, LastExportJWS, LastOpenAtlasFolder,
		/**
		 * Height of the {@link DesignMapViewJDialog}
		 **/
		mapComposerHeight,
		/** Width of the {@link DesignMapViewJDialog} **/
		mapComposerWidth, MinimumJavaVersion, NativeLibs, signingAlias, signingkeystorePassword, startJVMWithXmx,
		/** GP starts maximized **/
		windowMaximized
	}

	/** E.G. ".AtlasStyler" or ".Geopublisher" **/
	private static String appDirname;

	private static FileOutputStream FOS = null;

	private static boolean haveToCloseFOS = false;

	/** This stores the properties */
	private static final Properties properties = new Properties();

	public static final String PROPERTIES_FILENAME = "geopublisher.properties";

	public static final String PROPERTIES_FOLDER = ".Geopublisher";

	private static File propertiesFile = null;

	/**
	 * Name of the file that is stored on the local mashine in an applicatin
	 * preferences directory
	 */
	private static String propertiesFilename;

	/**
	 * Statically initializes this Properties Helper Class with a application-
	 * and filename
	 */
	static {
		init(PROPERTIES_FILENAME, PROPERTIES_FOLDER);
	}

	/**
	 * Returns the value as an {@link Integer}. If conversion fails, the default
	 * value is returned
	 */
	public static String get(final Keys key) {
		return get(key.toString());

	}

	public static String get(final Keys key, final String def) {
		return get(key.toString(), def);
	}

	/** ****************** GET ****************************** */
	/**
	 * Get a value from the underlying {@link Properties}
	 */
	private static final String get(final String key) {
		return properties.getProperty(key);
	}

	private static final String get(final String key, final String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	// TODO Add the possibility to provide default boolean values
	public static Boolean getBoolean(final Keys key) {
		return Boolean.valueOf(get(key));
	}

	public static Integer getInt(final Keys key, final Integer def) {
		return getInt(key.toString(), def);
	}

	static final Integer getInt(final String key, final Integer defaultValue) {
		try {
			final String string = get(key);
			if (string == null)
				return defaultValue;
			return Integer.valueOf(string.trim());
		} catch (final Exception e) {
			LOGGER
					.warn(
							"The property value saved for "
									+ key
									+ " can't be converted to Integer. Returning default value " // i8nlog
									+ defaultValue, e);
		}
		return defaultValue;
	}

	protected static File getPropertiesFile() {

		if (propertiesFile == null) {
			final File applicationPropertiesDirectory = new File(new File(
					System.getProperty("user.home")), appDirname);
			if (!applicationPropertiesDirectory.exists()) {
				applicationPropertiesDirectory.mkdirs();
			}

			propertiesFile = new File(applicationPropertiesDirectory,
					propertiesFilename);

			if (!propertiesFile.exists()) {
				resetProperties(null);
			} else {
				try {
					final FileInputStream inStream = new FileInputStream(
							propertiesFile);
					try {

						properties.load(inStream);
					} finally {
						inStream.close();
					}
				} catch (final Exception e) {
					LOGGER.error("Loading the properties - the second try", e);
					ExceptionDialog.show(null, e);
				}
			}
		}

		return propertiesFile;
	}

	/**
	 * Initialize this helpercLass for a some application
	 * 
	 * @param propertiesFilename
	 *            name or the Properties file, e.g. "ac.properties"
	 * 
	 * @param appDirname
	 *            Dirname in the User Home directory, e.g. ".ssh" or
	 *            ".AtlasSTyler"
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	protected static void init(final String propertiesFilename,
			final String appDirname) {

		// Setting up the logger from a XML configuration file. We do that gain
		// in GPPros, as it outputs log messages first.
		DOMConfigurator.configure(GPProps.class.getResource("/gp_log4j.xml"));

		GPProps.propertiesFilename = propertiesFilename;
		GPProps.appDirname = appDirname;

		try {
			final FileInputStream inStream = new FileInputStream(
					getPropertiesFile());
			try {
				properties.load(inStream);
			} finally {
				inStream.close();
			}
		} catch (final Exception e) {
			ExceptionDialog.show(null, e);
		}

		upgrade(); // TODO Schema version comparison... naja.. eigentlich egal

	}

	/**
	 * Deletes the .properties in the ApplicationPreferences directory creates a
	 * default geopublisher.properties from the one saved in the jar. The new
	 * .properties are automatically loaded.
	 * 
	 * @param guiOwner
	 *            If not <code>null</code> a JDialog message will inform the
	 *            user.
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static void resetProperties(final Component guiOwner) {
		final String msg = GpUtil
				.R("GpProps.could_not_find_default_properties_in_file");

		// Delete the old one
		getPropertiesFile().delete();

		// Create the new one
		// e.printStackTrace();
		LOGGER.info("Resetting " + getPropertiesFile().getAbsolutePath());
		// If we don't have a .properties file, we copy the one from the jar
		URL inJar = null;
		try {
			inJar = GpUtil.class.getResource("/" + propertiesFilename
					+ DEFAULTS_POSTFIX);
			// LOGGER.debug("inJar = " + inJar);

			if (inJar == null)
				throw new RuntimeException(msg);

			org.apache.commons.io.FileUtils.copyURLToFile(inJar,
					getPropertiesFile());

			/**
			 * After creating the new default file, we may not forget to read it
			 * ;-)
			 */
			properties.load(new FileInputStream(getPropertiesFile()));

		} catch (final IOException e1) {
			if (guiOwner != null) {
				ExceptionDialog.show(guiOwner, e1);
			} else {
				LOGGER.debug(msg, e1);
			}
		}

	}

	/**
	 * Set the value in the underlying {@link Properties} and store it
	 */
	public static final void set(final Keys key, final Boolean value) {
		set(key, value.toString());
		store();
	}

	/**
	 * Set the value as an integer and store the {@link Properties}
	 */
	static public final void set(final Keys key, final Integer value) {
		set(key, value.toString());
	}

	/**
	 * Set the value and store the {@link Properties}
	 */
	public static final void set(final Keys key, final String value) {
		set(key.toString(), value);
		store();
	}

	/**
	 * Set the value in the underlying {@link Properties} and store it
	 */
	private static final void set(final String key, final String value) {
		properties.setProperty(key, value);
		store();
	}

	/**
	 * Save the changes to the .properties file
	 */
	public static void store() {
		try {
			FOS = new FileOutputStream(getPropertiesFile());
			haveToCloseFOS = true;

			properties.store(FOS,
					"This is the properties file for the Geopublisher");
		} catch (final IOException e) {
			LOGGER.error("Can't write to " + getPropertiesFile().toString(), e); // i8nlog
			LOGGER.error(e);
			ExceptionDialog.show(null, e);
		} finally {
			if (haveToCloseFOS) {
				try {
					FOS.close();
					haveToCloseFOS = false;
				} catch (final IOException e) {
					LOGGER.error(e);
					ExceptionDialog.show(null, e);
				}
			}
		}
	}

	/**
	 * Copies more or less application dependent properties from the JAR to the
	 * .Geopublisher/geopublisher.properties...<br/>
	 * TODO should probably be seperated into a user and a system properties
	 * file...
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static void upgrade() {
		LOGGER.debug("upgrade from "+PROPERTIES_FILENAME);

		final URL inJar = GpUtil.class.getResource("/" + propertiesFilename
				+ DEFAULTS_POSTFIX);
		final Properties virginProps = new Properties();
		try {
			virginProps.load(inJar.openStream());
		} catch (final IOException e) {
			LOGGER.error(e);
		}

		set(Keys.NativeLibs, virginProps
				.getProperty(Keys.NativeLibs.toString()));
		set(Keys.signingAlias, virginProps.getProperty(Keys.signingAlias
				.toString()));
		set(Keys.signingkeystorePassword, virginProps
				.getProperty(Keys.signingkeystorePassword.toString()));
		set(Keys.MinimumJavaVersion, virginProps
				.getProperty(Keys.MinimumJavaVersion.toString()));
		set(Keys.JWSStartScript, virginProps.getProperty(Keys.JWSStartScript
				.toString()));
		set(Keys.bugReportEmail, virginProps.getProperty(Keys.bugReportEmail
				.toString()));
	}

	@Override
	protected void finalize() throws Throwable {
		synchronized (FOS) {
			if (haveToCloseFOS) {
				FOS.close();
				haveToCloseFOS = false;
			}
		}
	}

}
