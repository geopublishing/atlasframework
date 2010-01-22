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

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import schmitzm.swing.ExceptionDialog;

/**
 * Singleton and delegation pattern to the {@link AtlasViewer}'s properties
 * 
 * Saving is not implemented
 * 
 * All the keys are members of enum {@link Keys}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public class AVProps {
	private static final Logger LOGGER = Logger.getLogger(AVProps.class);

	private static final Properties properties = new Properties();

	/**
	 * The string, that points to an .properties file in the root atlas folder
	 * structure. (above ad)*
	 */
	public static final String PROPERTIESFILE_RESOURCE_NAME = "av.properties";

	private static final String COMMENTS = "AtlasViewer properties file";

	/**
	 * List of all valid Keys in the .properties file. Keys are mapped by
	 * toString to Strings which equals their variable Name
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 */
	public enum Keys {
		maxMosaicTiles, // max number of tiles a mosaic my consist of
		legendIconHeight, // Size of the Icons in the Legend
		legendIconWidth, // Size of the Icons in the Legend
		antialiasingMaps, // AntiAliase the Maps (needs much performance)

		// TODO der ist komisch
		antialiasingHTML // AntiAliase the HTML Editor Panels 0=false else
		// =true
		, LastExportFolder
		// Last folder anything was exported to
		, showPopupOnStartup
	}

	private static String propertiesFilename;

	private static String appDirname;

	private static File propertiesFile;

	private static FileOutputStream FOS;

	private static boolean haveToCloseFOS;

	// ****************************************************************************
	// 
	// The .properties file will be opened and parsed
	//
	// ****************************************************************************
	static {
		init("av.properties", ".AtlasViewer");
		// try {
		// final InputStream resourceAsStream = AtlasConfig.getResLoMan()
		// .getResourceAsStream(PROPERTIESFILE_RESOURCE_NAME);
		// if (resourceAsStream == null) {
		// log.warn("No *.properties found at '"
		// + PROPERTIESFILE_RESOURCE_NAME
		// + "'! Not parsing properties file...");
		// System.err.println("No *.properties found at '"
		// + PROPERTIESFILE_RESOURCE_NAME
		// + "'! Not parsing properties file...");
		// } else {
		// // ResLoMan provided an InputStream to the properties, so lets
		// // parse it...
		// properties.load(resourceAsStream);
		// }
		// } catch (Exception e) {
		// log.error("Error reading the properties file '"
		// + PROPERTIESFILE_RESOURCE_NAME + "'");
		// }
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
//		LOGGER.info("Initialising the AV Properties");

		String chartsetName = Charset.defaultCharset().name();
		LOGGER.info("Native JVM Charset is " + chartsetName);
		
		String fileEncodingName = System.getProperty("file.encoding");
		LOGGER.info("Fileencoding is " + fileEncodingName);
		
//		if (!chartsetName.equals("UTF-8")) throw new RuntimeException("JVM has to run in UTF-8. Please start JVM with '-Dfile.encoding=UTF-8'.");

		AVProps.propertiesFilename = propertiesFilename;
		AVProps.appDirname = appDirname;

		/**
		 * It is not a problem if the propertiesFile for the AtlasViewer can't
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
				resetProperties(null);
			} else {
				try {
					properties.load(new FileInputStream(getPropertiesFile()));
				} catch (Exception e) {
					ExceptionDialog.show(null, e);
				}
			}
		}

		return propertiesFile;
	}

	/**
	 * Save the changes to the .properties file
	 */
	public static void store(Component owner) {
		LOGGER.debug("STORE AV PROPS");
		try {
			FOS = new FileOutputStream(getPropertiesFile());
			haveToCloseFOS = true;

			properties.store(FOS,
					"This is the properties file for AtlasViewer.");
		} catch (IOException e) {
			LOGGER.error("Can't write to " + getPropertiesFile().toString(), e);
		} finally {
			if (haveToCloseFOS)
				try {
					FOS.close();
					haveToCloseFOS = false;
				} catch (IOException e) {
					LOGGER.error("Can't close FOS!", e);
					ExceptionDialog.show(owner, e);
				}
		}
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
		// final String msg = AtlasViewer.RESOURCE
		// .getString("avprops.could_not_find_default_properties_in_file");

		// Delete the old one
		getPropertiesFile().delete();

		// Create the new one
		LOGGER.info("Resetting " + getPropertiesFile().getAbsolutePath());
		// If we don't have a .properties file, we copy the one from the jar
		URL inJar = null;
		try {
			inJar = AtlasConfig.getResLoMan().getResourceAsUrl(
					PROPERTIESFILE_RESOURCE_NAME);
			// LOGGER.debug("inJar = " + inJar);

			if (inJar == null)
				throw new RuntimeException("Can't find original av.properties");

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
								+ " file has been created in "
								+ getPropertiesFile().getAbsolutePath());
					}
				});
			}

		} catch (Exception e1) {
			if (guiOwner != null) {
				ExceptionDialog.show(guiOwner, e1);
			} else {
				LOGGER.error("Can't find original av.properties", e1);
			}
		}

	}

	/**
	 * Request a Value from the {@link Properties} by providing a defaultValue
	 * in case the value doesn't exist
	 * 
	 * @return never null
	 */
	public static final String get(Keys key, String defaultValue) {
		return properties.getProperty(key.toString(), defaultValue);
	}

	/**
	 * Set a Value from the {@link Properties}
	 */
	public static final void set(Component owner, Keys key, String value) {
		properties.setProperty(key.toString(), value);
		store(owner);
	}

	/**
	 * Request a Value from the {@link Properties}
	 * 
	 * @return null if key is not defined
	 */
	public static final String get(Keys key) {
		return properties.getProperty(key.toString());
	}

	/**
	 * Returns the value as an {@link Integer}. If conversion fails, the default
	 * value is returned
	 */
	public static final Integer getInt(Keys key, Integer defaultValue) {
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
									+ " can't be converted to Integer. Returning default value "
									+ defaultValue, e);
		}
		return defaultValue;
	}

	/**
	 * Save the {@link Properties} to the given {@link File}
	 * 
	 * @param propertiesFile
	 * @throws IOException
	 */
	public static void save(File propertiesFile) throws IOException {
		properties.store(new FileWriter(propertiesFile), COMMENTS);
	}

	public static boolean getBoolean(Keys key, boolean defaultValue) {
		try {
			final String string = get(key);
			if (string == null)
				return defaultValue;
			return Boolean.valueOf(string.trim());
		} catch (Exception e) {
			LOGGER
					.warn(
							"The property value saved for "
									+ key
									+ " can't be converted to Boolean. Returning default value "
									+ defaultValue, e);
		}
		return defaultValue;
	}

}
