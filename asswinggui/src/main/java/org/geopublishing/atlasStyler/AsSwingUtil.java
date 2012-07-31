package org.geopublishing.atlasStyler;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.geopublishing.atlasStyler.ASProps.Keys;
import org.geopublishing.geopublisher.GpUtil;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JFileDialog;
import chrriis.dj.nativeswing.swtimpl.components.JFileDialog.DialogType;
import chrriis.dj.nativeswing.swtimpl.components.JFileDialog.SelectionMode;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.FileExtensionFilter;
import de.schmitzm.versionnumber.ReleaseUtil;

public class AsSwingUtil extends ASUtil {

	/**
	 * Setting up the logger from a XML configuration file. We do that again in GPPros, as it outputs log messages
	 * first. Does not change the configuration if there are already appenders defined.
	 */
	public static void initAsLogging() throws FactoryConfigurationError {
		if (Logger.getRootLogger().getAllAppenders().hasMoreElements())
			return;
		DOMConfigurator.configure(ASProps.class.getResource("/geopublishing_log4j.xml"));

		Logger.getRootLogger().addAppender(Logger.getLogger("dummy").getAppender("asFileLogger"));

		// Apply the LOG level configured in the user-specific application
		// .properties file
		String logLevelStr = ASProps.get(Keys.logLevel);
		if (logLevelStr != null) {
			Logger.getRootLogger().setLevel(Level.toLevel(logLevelStr));
		}

		// # TODO bugreport@wikisquare.de from properties
		ExceptionDialog.setMailDestinationAddress("tzeggai@wikisquare.de");

		// Add application version number to Exception mails
		ExceptionDialog.addAdditionalAppInfo(ReleaseUtil.getVersionInfo(GpUtil.class));
	}

	/**
	 * Performs a file choose using the Native OS-Dialog via SWT
	 * 
	 * @param parent
	 *            component for the dialog (can be {@code null})
	 * @param startFolder
	 *            start folder for the chooser (if {@code null} "/" is used)
	 * @param filter
	 *            defines which files can be selected
	 * @return {@code null} if the dialog was not approved
	 * 
	 *         TODO move to schmitz-swt
	 */
	public static File chooseFileOpen(Component parent, File startFolder, String title, FileExtensionFilter... filters) {

		try {
			NativeInterface.open();
			JFileDialog fileDialog = new JFileDialog();
			
			if (startFolder != null) {
				if (startFolder.isDirectory())
					fileDialog.setParentDirectory(startFolder.getAbsolutePath());
				else
					fileDialog.setParentDirectory(startFolder.getParent());
			}

			fileDialog.setDialogType(DialogType.OPEN_DIALOG_TYPE);

			String[] extensions = new String[0];
			for (FileExtensionFilter filter : filters) {
				extensions = LangUtil.extendArray(extensions, filter.toNativeFileFilter()[0]);
			}

			String[] descriptions = new String[0];
			for (FileExtensionFilter filter : filters) {
				descriptions = LangUtil.extendArray(descriptions, filter.toNativeFileFilter()[1]);
			}

			fileDialog.setExtensionFilters(extensions, descriptions, 0);
			fileDialog.setTitle(title);
			fileDialog.show(parent);

			String selectedFileName = fileDialog.getSelectedFileName();

			if (selectedFileName == null)
				return null;

			return new File(fileDialog.getParentDirectory(), selectedFileName);
		} catch (Exception e) {
			return GpUtil.chooseFileOpenFallback(parent, startFolder, title, filters);
		}
	}

	/**
	 * Performs a file SAVE choose as a fallback
	 * 
	 * @param parent
	 *            component for the dialog (can be {@code null})
	 * @param startFolder
	 *            start folder for the chooser (if {@code null} "/" is used)
	 * @param filter
	 *            defines which files can be selected. Only the last filter in the list will be offered due to
	 *            limitations
	 * @return {@code null} if the dialog was not approved
	 * 
	 *         TODO move to schmitz-swt
	 */
	public static File chooseFileSaveFallback(Component parent, File startFolder, String title,
			FileExtensionFilter... filters) {
		if (startFolder == null)
			startFolder = new File("/");

		JFileChooser chooser = new JFileChooser(startFolder);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);

		if (filters != null) {
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(filters[filters.length - 1].toJFileChooserFilter());
		}
		if (title != null)
			chooser.setDialogTitle(title);

		int ret = chooser.showOpenDialog(parent);
		if (ret == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		return null;
	}

	public static File chooseFileSave(Component parent, File startFolder, String title, FileExtensionFilter... filters) {
		try {
			JFileDialog fileDialog = new JFileDialog();
			// fileDialog.setTitle(GpSwingUtil.R("CreateAtlas.Dialog.Title"));
			
			if (startFolder != null) {
				if (startFolder.isDirectory())
					fileDialog.setParentDirectory(startFolder.getCanonicalPath());
				else {
					fileDialog.setParentDirectory(startFolder.getParent());
					fileDialog.setSelectedFileName(startFolder.getName());
				}
			}

			fileDialog.setDialogType(DialogType.SAVE_DIALOG_TYPE);
			fileDialog.setSelectionMode(SelectionMode.SINGLE_SELECTION);

			String[] extensions = new String[0];
			for (FileExtensionFilter filter : filters) {
				extensions = LangUtil.extendArray(extensions, filter.toNativeFileFilter()[0]);
			}

			String[] descriptions = new String[0];
			for (FileExtensionFilter filter : filters) {
				descriptions = LangUtil.extendArray(descriptions, filter.toNativeFileFilter()[1]);
			}

			fileDialog.setExtensionFilters(extensions, descriptions, 0);
			fileDialog.setTitle(title);
			
			fileDialog.show(parent);

			String selectedFileName = fileDialog.getSelectedFileName();

			if (selectedFileName == null)
				return null;

			return new File(fileDialog.getParentDirectory(), selectedFileName);
		} catch (Exception e) {
			return chooseFileSaveFallback(parent, startFolder, title, filters);
		}
	}

}
