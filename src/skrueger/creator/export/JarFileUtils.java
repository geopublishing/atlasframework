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
package skrueger.creator.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.log4j.Logger;

import skrueger.atlas.AVUtil;

/**
 * Copies all entryies of one {@link JarFile} to another, but removes .project
 * and .classpath files. The new file then replaces the old file.
 * 
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public class JarFileUtils {
	final static private Logger LOGGER = Logger.getLogger(JarFileUtils.class);

	/**
	 * Copies all entryies of one {@link JarFile} to another, but removes
	 * .project and .classpath files. The new file then replaces the old file.
	 * 
	 * @param jarToClean
	 *            The {@link JarFile} to clean from the unwanted files.
	 * 
	 * @throws IOException
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public static void removeClasspaths(String jarToClean) throws IOException {

		String jartoEdit = jarToClean;

		// String newFile = args[1];

		File tempJar = null;

		tempJar = File.createTempFile(AVUtil.ATLAS_TEMP_FILE_ID, null);

		JarFile jar = null;

		jar = new JarFile(jartoEdit);

		boolean delFlag = false;

		JarOutputStream newJar =

		new JarOutputStream(

		new FileOutputStream(tempJar));

		byte buffer[] = new byte[1024];

		int bytesRead;

		// FileInputStream fis = new FileInputStream(newFile);

		Enumeration entries = jar.entries();

		while (entries.hasMoreElements()) {

			JarEntry entry = (JarEntry) entries.nextElement();

			String name = entry.getName();

			if (name.equalsIgnoreCase(".classpath")) {
				LOGGER.debug("Removing one .classpath");
				continue;
			}
			if (name.equalsIgnoreCase(".project")) {
				LOGGER.debug("Removing one .project");
				continue;
			}

			InputStream is = jar.getInputStream(entry);

			newJar.putNextEntry(entry);

			while ((bytesRead = is.read(buffer)) != -1) {

				newJar.write(buffer, 0, bytesRead);

			}

		}

		delFlag = true;

		newJar.close();

		jar.close();

		if (!delFlag) {

			tempJar.delete();

		}

		if (delFlag) {

			File origFile = new File(jartoEdit);

			origFile.delete();

			tempJar.renameTo(origFile);

		}
	}

}
