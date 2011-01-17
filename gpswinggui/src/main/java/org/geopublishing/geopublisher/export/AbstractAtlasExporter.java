package org.geopublishing.geopublisher.export;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpUtil;
import org.netbeans.spi.wizard.ResultProgressHandle;

import skrueger.versionnumber.ReleaseUtil;

abstract public class AbstractAtlasExporter implements AtlasExporter {

	final static Logger LOGGER = Logger.getLogger(AbstractAtlasExporter.class);

	/** Internal counting for the percentage bar **/
	int currSteps = 0;

	/** Internal counting for the percentage bar **/
	protected int totalSteps = 1;

	/**
	 * Promote export progress to GUI.
	 */
	protected void info(final String msg) {
		if (progress != null) {

			if (currSteps < totalSteps) {
				currSteps++;
			} else {
				currSteps++;
				totalSteps = currSteps + 3;
				LOGGER.warn("overcounting " + currSteps + " to " + totalSteps);
			}
			progress.setProgress(msg, currSteps, totalSteps);
		} else {
			LOGGER.info(msg);
		}
	}

	ResultProgressHandle progress;

	private static final String version = ReleaseUtil
			.getVersionMaj(AVUtil.class)
			+ "."
			+ ReleaseUtil.getVersionMin(AVUtil.class);

	/**
	 * Are we exporting from SNAPSHOT relases, then the exported atlas need
	 * -SNAPSHOT jars also
	 **/
	private static final String snapshot = ReleaseUtil.getVersionInfo(
			GpUtil.class).contains("SNAPSHOT") ? "-SNAPSHOT" : "";

	/**
	 * Files next to atlas.gpa will be copied to the folders without putting
	 * them in a JAR. This can be useful for example for PDF files that should
	 * be referencable from within the atlas, but also reside uncompressed on
	 * the CD root directory.
	 */
	protected static final FilenameFilter filterForRootLevelFiles = new FilenameFilter() {

		@Override
		public boolean accept(final File dir, final String name) {

			// This is a list of files expected in ad that shall not be copied
			if (dir.isDirectory() && name.equalsIgnoreCase(".cvs"))
				return false;
			if (dir.isFile() && name.equalsIgnoreCase("build.xml"))
				return false;
			if (dir.isFile() && name.equalsIgnoreCase("pom.xml"))
				return false;
			if (dir.isDirectory() && name.equalsIgnoreCase(".svn"))
				return false;
			if (name.equalsIgnoreCase(AtlasConfigEditable.ATLAS_GPA_FILENAME))
				return false;
			if (name.equalsIgnoreCase(AVProps.PROPERTIESFILE_RESOURCE_NAME))
				return false;
			if (name.equalsIgnoreCase(AtlasConfig.ATLASDATA_DIRNAME))
				return false;
			return true;
		}
	};

	/**
	 * Resource location of the <code>license.html</code> with the license of
	 * AtlasViewer.</code>
	 */
	public static final String LICENSEHTML_RESOURCE_NAME = "/export/license.html";

	private volatile boolean aborted = false;

	protected final AtlasConfigEditable ace;

	public final String ATLAS_TEMP_FILE_EXPORTINSTANCE_ID = AVUtil.ATLAS_TEMP_FILE_BASE_ID
			+ "_EXPORT_" + System.currentTimeMillis();

	/**
	 * Allows to tell the exporter to NOT delte all temp directories. This is
	 * usefull if atlases are exported parallel
	 */
	private boolean keepTempFiles = false;

	public AbstractAtlasExporter(AtlasConfigEditable ace) {
		this.ace = ace;
	}

	/**
	 * Can be called from external to abort the export process
	 */
	public void abort() {
		aborted = true;
	}

	/**
	 * Monitors the isRunning method of {@link #progress}. If it has been
	 * canceled throws an {@link AtlasExportCancelledException}.
	 * 
	 * @throws AtlasExportCancelledException
	 */
	void checkAbort() throws AtlasCancelException {
		// if (progress != null && !progress.isRunning())
		if (aborted)
			throw new AtlasCancelException();
	}

	/**
	 * This approach could be problematic if there are more than one exports
	 * running at a time.
	 */
	public void deleteOldTempExportDirs() {

		/**
		 * On the command line -t can be specified to NOT delte any temp files.
		 * This needed in version 1.6 to run exports in parallel.<br/>
		 * TODO Temp file management must be improved, so that every instacne
		 * just deletes its own temp files after execution.
		 */
		// if (isKeepTempFiles())
		// return;

		/**
		 * Delete any old/parallel export directories
		 */
		// final IOFileFilter oldDirs = FileFilterUtils
		// .makeDirectoryOnly(FileFilterUtils
		// .prefixFileFilter(ATLAS_TEMP_FILE_EXPORTINSTANCE_ID ));
		// final String[] list = IOUtil.getTempDir().list(oldDirs);
		// for (final String deleteOldTempDirName : list) {
		// try {
		// FileUtils.deleteDirectory(new File(IOUtil.getTempDir(),
		// deleteOldTempDirName));
		// } catch (final Exception e) {
		// ExceptionDialog.show(null, e);
		// }
		// }

		AVUtil.cleanupTempDir(ATLAS_TEMP_FILE_EXPORTINSTANCE_ID,
				AVUtil.ATLAS_TEMP_FILE_BASE_ID);
	}

	public boolean isKeepTempFiles() {
		return keepTempFiles;
	}

	public void setKeepTempFiles(boolean deleteTempFiles) {
		this.keepTempFiles = deleteTempFiles;
	}

	public static String getSnapshot() {
		return snapshot;
	}

	public static String getVersion() {
		return version;
	}

}
