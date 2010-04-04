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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.GpUtil;

import schmitzm.swing.ExceptionDialog;

/**
 * Singleton and delegation pattern to the {@link AtlasCreator}'s
 * {@link Properties}
 * 
 * Only the methods working on the enumeration {@link Keys} are public
 * Interanally the {@link Keys} are saved as {@link String}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public abstract class GPProps {
	private static final Logger LOGGER = Logger.getLogger(GPProps.class);

	/**
	 * List of all legal keys in the
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 */
	public enum Keys {
		LastOpenAtlasFolder, ClassPathLibs, // space separated list of all libs
		// needed to run AV
		NativeLibs, // space separated list of all native .so and .dll files
		// neede to run AV
		JarSignerCmd, // path to jarsigner executable
		JarCmd, // path to jar executable
		// unsignedLibDir, // folder with all the unsigned libs
		// signingKeystore, // location of the key store to sign jars with
		signingAlias, // Which alias to sign the jars with
		signingkeystorePassword, // Which password the key store is crypted
		// with
		LastExportDisk, // Did the last export use DISK?
		LastExportJWS, // Did the last export use DISK?
		LastExportFolder, // Last dir where the AC exported to
		startJVMWithXmx, // Parameter for java -Xmx???m (Amount of max HEAP
		// to allocate to the mashine for the AVs released
		compressJARs, // 0 for no compression, everything else for comressed
		// JARs, default = compress
		jnlpURL, // e.g. "http://172.16.115.1/atlas" The location of the
		// jnlpJar once it is deployed
		/** GP starts maximized **/
		windowMaximized, antialiasingMaps,

		gpWindowWidth, gpWindowHeight, gpWindowLeftDividerLocation, gpWindowRightDividerLocation, bugReportEmail, MinimumJavaVersion, JWSStartScript,
		/** Width of the {@link DesignMapViewJDialog} **/
		mapComposerWidth,
		/** Height of the {@link DesignMapViewJDialog} **/
		mapComposerHeight, 
		/** Remember the GP Atlas folder we imported stuff from the last time **/
		LAST_IMPORTED_GPA,
		/** Remember the last file we imported **/
		LAST_IMPORTED_FILE
	}

	/**
	 * This stores the properties
	 */
	private static final Properties properties = new Properties();

	/**
	 * Name of the file that is stored on the local mashine in an applicatin
	 * preferences directory
	 */
	private static String propertiesFilename;

	private static File propertiesFile = null;

	private static FileOutputStream FOS = null;

	private static boolean haveToCloseFOS = false;

	private static String appDirname;

	@Override
	protected void finalize() throws Throwable {
		synchronized (FOS) {
			if (haveToCloseFOS) {
				FOS.close();
				haveToCloseFOS = false;
			}
		}
	}

	/**
	 * Initialize this helpercLass for a some application
	 * 
	 * @param propertiesFilename
	 *            name or the Properties file, e.g. "ac.properties"
	 * 
	 * @param appDirname
	 *            Dirname in the User Home directory, e.g. ".shh"
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	protected static void init(String propertiesFilename, String appDirname) {
		LOGGER.info("Initialising the Properties");

		GPProps.propertiesFilename = propertiesFilename;
		GPProps.appDirname = appDirname;

		try {
			properties.load(new FileInputStream(getPropertiesFile()));
		} catch (Exception e) {
			ExceptionDialog.show(null, e);
		}

		upgrade(); // TODO Schema version comparison... naja.. eigentlich egal

	}

	protected static File getPropertiesFile() {

		if (propertiesFile == null) {
			File applicationPropertiesDirectory = new File(new File(System
					.getProperty("user.home")), appDirname);
			if (!applicationPropertiesDirectory.exists())
				applicationPropertiesDirectory.mkdirs();

			propertiesFile = new File(applicationPropertiesDirectory,
					propertiesFilename);

			if (!propertiesFile.exists()) {
				resetProperties(null);
			} else {
				try {
					properties.load(new FileInputStream(propertiesFile));
				} catch (Exception e) {
					LOGGER.error("Loading the properties - the second try", e);
					ExceptionDialog.show(null, e);
				}
			}
		}

		return propertiesFile;
	}

	/**
	 * Set the value in the underlying {@link Properties} and store it
	 */
	private static final void set(String key, String value) {
		properties.setProperty(key, value);
		store();
	}

	/**
	 * Set the value in the underlying {@link Properties} and store it
	 */
	public static final void set(Keys key, Boolean value) {
		set(key, value.toString());
		store();
	}

	/**
	 * Set the value as an integer and store the {@link Properties}
	 */
	static public final void set(Keys key, Integer value) {
		set(key, value.toString());
	}

	/**
	 * Set the value and store the {@link Properties}
	 */
	public static final void set(Keys key, String value) {
		set(key.toString(), value);
		store();
	}

	/** ****************** GET ****************************** */
	/** ****************** GET ****************************** */
	/** ****************** GET ****************************** */
	/**
	 * Get a value from the underlying {@link Properties}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	private static final String get(String key) {
		return properties.getProperty(key);
	}

	private static final String get(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	/**
	 * Returns the value as an {@link Integer}. If conversion fails, the default
	 * value is returned
	 */
	public static String get(Keys key) {
		return get(key.toString());

	}

	public static String get(Keys key, String def) {
		return get(key.toString(), def);
	}

	public static Integer getInt(Keys key, Integer def) {
		return getInt(key.toString(), def);
	}

	static final Integer getInt(String key, Integer defaultValue) {
		try {
			final String string = get(key);
			if (string == null)
				return defaultValue;
			return Integer.valueOf(string.trim());
		} catch (Exception e) {
			LOGGER
					.warn(
							"The property value saved for "
									+ key
									+ " can't be converted to Integer. Returning default value " // i8nlog
									+ defaultValue, e);
		}
		return defaultValue;
	}

	/**
	 * Save the changes to the .properties file
	 */
	public static void store() {
		// LOGGER.debug("STORE PROPS");
		try {
			FOS = new FileOutputStream(getPropertiesFile());
			haveToCloseFOS = true;

			properties.store(FOS,
					"This is the properties file for the AtlasCreator");
		} catch (IOException e) {
			LOGGER.error("Can't write to " + getPropertiesFile().toString(), e); // i8nlog
			LOGGER.error(e);
			ExceptionDialog.show(null, e);
		} finally {
			if (haveToCloseFOS)
				try {
					FOS.close();
					haveToCloseFOS = false;
				} catch (IOException e) {
					LOGGER.error(e);
					ExceptionDialog.show(null, e);
				}
		}
	}

	/**
	 * Statically initializes this Properties Helper Class with a application-
	 * and filename
	 */
	static {
		init("geopublisher.properties", ".Geopublisher");
	}

	/**
	 * Copies more or less application dependent properties from the JAR to the
	 * .AtlasCreator/geopublisher.properties...<br/>
	 * TODO should probably be seperated into a user and a system properties
	 * file...
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public static void upgrade() {
		LOGGER.debug("upgrade from geopublisher.properties");

		URL inJar = GpUtil.class.getResource("/"+propertiesFilename);
		Properties virginProps = new Properties();
		try {
			virginProps.load(inJar.openStream());
		} catch (IOException e) {
			LOGGER.error(e);
		}

		set(Keys.ClassPathLibs, virginProps.getProperty(Keys.ClassPathLibs
				.toString()));
		LOGGER.debug(" setting " + Keys.ClassPathLibs + " to "
				+ virginProps.getProperty(Keys.ClassPathLibs.toString()));
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

	/**
	 * Deletes the .properties in the ApplicationPreferences directory creates a
	 * default geopublisher.properties from the one saved in the jar. The new
	 * .properties are automatically loaded.
	 * 
	 * @param guiOwner
	 *            If not <code>null</code> a JDialog message will inform the
	 *            user.
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
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
			inJar = GpUtil.class.getResource(propertiesFilename);
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

			if (guiOwner != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(guiOwner, "A default "
								+ propertiesFilename
								+ " file has been created in " // i8nAC
								+ getPropertiesFile().getAbsolutePath());
					}
				});
			}

		} catch (IOException e1) {
			if (guiOwner != null) {
				ExceptionDialog.show(guiOwner, e1);
			} else {
				LOGGER.debug(msg, e1);
			}
		}

	}

	public static Boolean getBoolean(Keys key) {
		return Boolean.valueOf(get(key));

	}

}
