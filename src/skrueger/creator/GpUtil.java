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
import java.io.FileWriter;
import java.text.DecimalFormat;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;

import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.swing.formatter.MbDecimalFormatter;

public class GpUtil {

	private static final Logger LOGGER = Logger.getLogger(GpUtil.class);

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
	 * This {@link IOFileFilter} returns <code>false</code> for folder that
	 * should be omitted during export and when calculating the size of a
	 * {@link DpEntry} folder. This filter omits SVN and CSV folder.
	 */
	public static final IOFileFilter BlacklistedFoldersFilter = FileFilterUtils
			.makeCVSAware(FileFilterUtils.makeSVNAware(null));

	public static final MbDecimalFormatter MbDecimalFormatter = new MbDecimalFormatter();

	/**
	 * Checks if a filename is OK for the AV. Asks the use to accespt the
	 * changed name
	 * 
	 * @param owner
	 *            GUI owner
	 * @param nameCandidate
	 *            Filename to check, e.g. bahn.jpg
	 * @return <code>null</code> if the user didn't accept the new filename.
	 * 
	 * @throws AtlasImportException
	 *             if the user doesn't like the change of the filename.
	 */
	public static String cleanFilenameWithUI(Component owner,
			String nameCandidate) throws AtlasImportException {
		String cleanName = AVUtil.cleanFilename(nameCandidate);

		if (!cleanName.equals(nameCandidate)) {
			/**
			 * The candidate was not clean. Ask the user to accept the new name
			 * or cancel.
			 */

			if (!AVUtil.askOKCancel(owner, AtlasCreator.R("Cleanfile.Question",
					nameCandidate, cleanName))) {
				throw new AtlasImportException(AtlasCreator.R(
						"Cleanfile.Denied.ImportCancelled", nameCandidate));
			}
		}

		return cleanName;
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
	public static void saveCpg(DpLayerVectorFeatureSource dpe) {
		try {
			final AtlasConfigEditable ace = (AtlasConfigEditable) dpe.getAc();

			final File cpgFile = IOUtil.changeFileExt(ace.getFileFor(dpe),
					"cpg");

			final String whatToWrite = dpe.getCharset().name();

			if (cpgFile.exists()) {
				String origContent = IOUtil.readFileAsString(cpgFile);
				if (whatToWrite.equals(origContent)) {
					// if the file already exists and contains the same content,
					// do not write it.
					return;
				} else
					cpgFile.delete();
			}

			final FileWriter cpgWriter = new FileWriter(cpgFile);
			try {
				// LOGGER.debug("Writing CPG = " + dpe.getCharset().name()
				// + " to file");

				cpgWriter.write(whatToWrite);
			} finally {
				cpgWriter.flush();
				cpgWriter.close();
			}

		} catch (Exception e) {
			final String errMessage = "Unable to store the codepage settings for "
					+ dpe + "\nThe export will be continued."; // i8n
			LOGGER.warn(errMessage, e);
			ExceptionDialog.show(AtlasCreator.getInstance().getJFrame(), e,
					null, errMessage);
		}

	}

}
