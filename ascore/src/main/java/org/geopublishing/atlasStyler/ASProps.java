/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.log4j.Logger;

import schmitzm.swing.ExceptionDialog;

/**
 * Singleton and delegation pattern to the {@link AtlasStyler}'s
 * {@link Properties}
 * 
 * Only the methods working on the enumeration {@link Keys} are public
 * Interanally the {@link Keys} are saved as {@link String}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public abstract class ASProps {
	private static final Logger LOGGER = Logger.getLogger(ASProps.class);

	/**
	 * This stores the properties
	 */
	private static final Properties properties = new Properties();

	/** TODO: Rethink where this "default" shall be used. */
	public static final String DEFAULT_CHARSET_NAME = "UTF-8";

	/**
	 * Name of the file that is stored on the local mashine in an applicatin
	 * preferences directory
	 */
	private static String propertiesFilename;

	private static File propertiesFile = null;

	private static FileOutputStream FOS = null;

	private static boolean haveToCloseFOS = false;

	private static String appDirname;

	private static Window owner;

	static {
		init("atlasStyler.properties", ".AtlasStyler");
	}

	/**
	 * List of all legal keys in the
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 */
	public enum Keys {
		/** Last directory used for open dialog */
		lastImportDirectory,
		/** Last directory used for save full SLD dialog */
		lastExportDirectory,
		/** AC JFrame starts maximized? */
		windowMaximized,
		/** Name of charset used for exporting SLD XML* */
		charsetName,
		/** Shall Warning1 popup again? 1 == yes* */
		showWarning1,
		/**
		 * java2d AntiAliasing, Quality and TextAntiALiasing Hints turned on or
		 * off*
		 */
		antialiasingMaps, automaticPreview, language
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
		LOGGER.info("Initialising the AS Properties");

		LOGGER.info("Native JVM Charset is " + Charset.defaultCharset().name());

		ASProps.propertiesFilename = propertiesFilename;
		ASProps.appDirname = appDirname;

		/**
		 * It is not a problem if the propertiesFile for the AtlasStyler can't
		 * be loaded
		 */
		try {
			properties.load(new FileInputStream(getPropertiesFile()));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			LOGGER.error(e);
		}
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
				resetProperties();
			} else {
				FileInputStream inStream = null;
				try {
					inStream = new FileInputStream(getPropertiesFile());
					properties.load(inStream);
				} catch (Exception e) {
					ExceptionDialog.show(null, e);
				} finally {
					if (inStream != null)
						try {
							inStream.close();
						} catch (IOException ioe) {
							LOGGER.error("Unable to close FileInputStream of "
									+ getPropertiesFile(), ioe);
						}
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
	 * Set the value as an integer and store the {@link Properties}
	 */
	public static final void set(Keys key, Integer value) {
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
	 * Returns the value as an {@link Integer}. If conversion fails, the default
	 * value is returned
	 */
	public static final String get(Keys key) {
		return get(key.toString());
	}

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

	public static String get(Keys key, String def) {
		return get(key.toString(), def);
	}

	public static Integer getInt(Keys key, Integer def) {
		return getInt(key.toString(), def);
	}

	private static final Integer getInt(String key, Integer defaultValue) {
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
//		LOGGER.debug("STORE AS PROPS");
		try {
			FOS = new FileOutputStream(getPropertiesFile());
			haveToCloseFOS = true;

			properties.store(FOS,
					"This is the properties file for AtlasStyler.");
		} catch (IOException e) {
			LOGGER.error("Can't write to " + getPropertiesFile().toString(), e);
		} finally {
			if (haveToCloseFOS)
				try {
					FOS.close();
					haveToCloseFOS = false;
				} catch (IOException e) {
					LOGGER.error("Can't close FOS!", e);
					ExceptionDialog.show(getOwner(), e);
				}
		}
	}

	/**
	 * Deletes the .properties in the ApplicationPreferences directory and
	 * creates a default AtlasStyler.properties
	 * 
	 * @param guiOwner
	 *            If not <code>null</code> a JDialog message will inform the
	 *            user.
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public static void resetProperties() {

		// Create the new one
		// LOGGER.error(e);
		LOGGER.info("Resetting " + getPropertiesFile().getAbsolutePath());
		// Delete the old one
		getPropertiesFile().delete();
		store();
	}

	public static void setOwner(Window owner) {
		ASProps.owner = owner;
	}

	public static Component getOwner() {
		return owner;
	}

}
