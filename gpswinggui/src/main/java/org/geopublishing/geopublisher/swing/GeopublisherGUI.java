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
package org.geopublishing.geopublisher.swing;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;

import javax.jnlp.SingleInstanceListener;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.exceptions.AtlasRecoverableException;
import org.geopublishing.atlasViewer.http.Webserver;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.swing.AVDialogManager;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.geopublisher.ACETranslationPrinter;
import org.geopublishing.geopublisher.AMLImportEd;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.AtlasGPAFileFilter;
import org.geopublishing.geopublisher.CliOptions;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GPProps.Keys;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.gui.AtlasLanguagesConfigDialog;
import org.geopublishing.geopublisher.gui.EditAtlasParamsDialog;
import org.geopublishing.geopublisher.gui.GpFrame;
import org.geopublishing.geopublisher.gui.GpJSplitPane;
import org.geopublishing.geopublisher.gui.SimplyHTMLUtil;
import org.geopublishing.geopublisher.gui.export.ExportWizard;
import org.geopublishing.geopublisher.gui.importwizard.ImportWizard;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;

import rachel.http.loader.WebResourceManager;
import rachel.loader.FileResourceLoader;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.SHTMLPanelImpl;

import de.schmitzm.i18n.SwitchLanguageDialog;
import de.schmitzm.i18n.Translation;
import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.ResourceProvider;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.swingworker.AtlasStatusDialog;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;
import de.schmitzm.versionnumber.ReleaseUtil;

/**
 * The mighty mighty {@link GeopublisherGUI} is a tool that generates runnable
 * {@link AtlasViewerGUI} compilations.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class GeopublisherGUI implements ActionListener, SingleInstanceListener {

	static {
		GpUtil.initGpLogging();
		// Enable translations added by the user to the .Geopublishing directory
		ResourceProvider.setAutoResetResourceBundle(true, "Translation", true);
	}

	/**
	 * A enumeration of actions. Mainly accessible through the {@link JMenuBar}
	 */
	public enum ActionCmds {
		changeLnF, editAboutInfo, editAtlasLanguages, editAtlasParams, editPopupInfo, exitGP, exportAtlasTranslations, exportJarsAtlas, newAtlas, saveAtlas, showImagesInfo, previewAtlas, previewAtlasLive, exportAtlasCSV, importWizard
	}

	/** A singleton pattern for the {@link GeopublisherGUI} instance **/
	private static GeopublisherGUI instance = null;

	private static final Logger LOGGER = Logger
			.getLogger(GeopublisherGUI.class);

	static {
		LOGGER.debug("Adding new WebClassResourceLoader( "
				+ AtlasViewerGUI.class.getSimpleName()
				+ " ) to WebResourceManager");
		WebResourceManager
				.addResourceLoader(new rachel.http.loader.WebClassResourceLoader(
						AtlasViewerGUI.class));

		// Starting singleton WebServer
		try {
			new Webserver();
		} catch (final Exception e) {
			ExceptionDialog.show(null, e);
			System.exit(-3);
		}

		/**
		 * Doing some initializations
		 */
		AVUtil.fixBug4847375();
		SHTMLPanelImpl.setTextResources(null);
	}

	/**
	 * Creates or returns the single instance of Geopublisher
	 */
	public static GeopublisherGUI getInstance(boolean askToOpenLastAtlas) {
		if (instance == null) {
			// LOGGER.error(
			// "GeopublisherGUI instance is requested without arguments and it doesn't exists yet!",
			// new RuntimeException());
			instance = new GeopublisherGUI(askToOpenLastAtlas);
		}
		return instance;
	}

	/**
	 * Creates or returns the single instance of Geopublisher, if it has not yet
	 * been created, the user will not be asked to open any last opened atlas.
	 */
	public static GeopublisherGUI getInstance() {
		return getInstance(false);
	}

	/**
	 * Start routine for the {@link GeopublisherGUI}
	 * 
	 * @throws ParseException
	 */
	public static void main(final String[] args) {
		// try {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// } catch (InstantiationException e) {
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// e.printStackTrace();
		// } catch (UnsupportedLookAndFeelException e) {
		// e.printStackTrace();
		// }

		// String[] st = System.getProperty("java.class.path").split(":");
		// LOGGER.debug("Classpath:");
		// System.out.println("Classpath:");
		// for (String t : st) {
		// System.out.println(t);
		// LOGGER.debug(t);
		// }
		//
		// if (args.length == 0 && GraphicsEnvironment.isHeadless()) {
		// // Starting Geopublisher without any arguments
		// GeopublisherGUI.getInstance(true);
		// } else {
		int resultCode;
		resultCode = CliOptions.performArgs(args);
		if (resultCode != -1) {

			if (instance != null) {
				instance.exitGP(resultCode);
			}

			System.exit(resultCode);
		}
		// }

	}

	/**
	 * Convenience method to access the {@link GeopublisherGUI}s translation
	 * resources.
	 * 
	 * @param key
	 *            the key for the *Translation.properties file
	 * @param values
	 *            optional values
	 */
	public static String R(final String key, final Object... values) {
		return GpUtil.R(key, values);
	}

	/** null if no atlas is open for editing. Otherwise no Atlas is loaded. * */
	private AtlasConfigEditable ace;

	/**
	 * Keeps an instance of the {@link GpFrame}, the main window of Geopublisher
	 **/
	private GpFrame gpJFrame = null;

	/**
	 * Main constructor of {@link GeopublisherGUI}.
	 * 
	 * @param args
	 *            command line arguments
	 * **/
	public GeopublisherGUI(final boolean askToOpenLastAtlas) {
		LOGGER.info("Starting " + GeopublisherGUI.class.getSimpleName()
				+ "... " + ReleaseUtil.getVersionInfo(AVUtil.class));

		GpUtil.initGpLogging();

		System.setProperty("file.encoding", "UTF-8");

		/*
		 * Register as a SingleInstance for JNLP. Starting another instance of
		 * Geopublisher via JavaWebStart will fall back to this instance
		 */
		JNLPUtil.registerAsSingleInstance(GeopublisherGUI.this, true);

		/**
		 * Add an ExceptionHandler for all uncaught exceptions:
		 */

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(final Thread t, final Throwable e) {

				LOGGER.error("An uncaught exception happened on Thread " + t, e); // i8n

				if (e instanceof java.lang.ArrayIndexOutOfBoundsException) {
					final StackTraceElement stackTraceElement = e
							.getStackTrace()[2];
					if (stackTraceElement.getClassName().equals(
							"org.jdesktop.swingx.VerticalLayout")) {
						// Don't bother the user with this...
						return;
					}
				}

				ExceptionDialog.show(gpJFrame, e);
			}
		});

		// Cache the EPSG data
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					AVSwingUtil.initEPSG(getJFrame());

					if (askToOpenLastAtlas) {
						// Only open the load atlas dialog if we have already
						// opened an atlas before
						if (!GPProps.get(GPProps.Keys.LastOpenAtlasFolder, ".")
								.equals(".")) {
							loadAtlas();
						}
					}

				} catch (final Exception e) {
					ExceptionDialog.show(getJFrame(), e);
				}
			}
		});
	}

	/**
	 * Creates a mew GeopublsherGUI with a loaded atlas
	 */
	public GeopublisherGUI(AtlasConfigEditable ace) {
		this(false);
		loadAce(ace);

	}

	/***************************************************************************
	 * The {@link #actionPerformed(ActionEvent)} method centralizes all
	 * {@link javax.swing.Action}s that are performed in this Class
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final String cmd = e.getActionCommand();

		if (cmd.equals(ActionCmds.editAtlasParams.toString())) {
			if (ace == null)
				return;

			final EditAtlasParamsDialog editAtlasDialog = new EditAtlasParamsDialog(
					getJFrame(), ace);
			editAtlasDialog.setVisible(true);
			if (!editAtlasDialog.isCancelled()) {
				getJFrame().setTitle(
						R("ApplicationMainWindowTitle_with_open_atlas",
								ReleaseUtil.getVersionInfo(AVUtil.class), ace
										.getTitle().toString()));
			}

		}

		else if (cmd.equals(ActionCmds.editAtlasLanguages.toString())) {
			final AtlasLanguagesConfigDialog languageSelectionDialog = new AtlasLanguagesConfigDialog(
					getJFrame(), ace.getLanguages());
			languageSelectionDialog.setVisible(true);
		}

		else if (cmd.startsWith(ActionCmds.changeLnF.toString())) {
			final String lnfClassname = cmd.substring(ActionCmds.changeLnF
					.toString().length());
			try {
				UIManager.setLookAndFeel(lnfClassname);
				SwingUtilities.updateComponentTreeUI(getJFrame());
				getJFrame().updateMenu();

			} catch (final Exception ex) {
				LOGGER.error(
						"Trying to add a useless JMenu for LookAndFeel stiff",
						ex);
			}
		} else if (cmd.equals(ActionCmds.editAboutInfo.toString())) {

			final List<String> tabTitles = new ArrayList<String>(ace
					.getLanguages().size());

			for (int i = 0; i < ace.getLanguages().size(); i++) {
				final String titleTranslated = ace.getTitle().get(
						ace.getLanguages().get(i));
				final String title = R(
						"EditAboutWindow.TabName",
						titleTranslated == null || titleTranslated.equals("") ? "..."
								: titleTranslated,
						new Locale(ace.getLanguages().get(i))
								.getDisplayLanguage(new Locale(Translation
										.getActiveLang())));

				tabTitles.add(title);
			}

			SimplyHTMLUtil.openHTMLEditors(getJFrame(), ace,
					ace.getAboutHtMLFiles(getJFrame()), tabTitles,
					GpUtil.R("EditAboutWindow.EditorTitle"));

		} else if (cmd.equals(ActionCmds.editPopupInfo.toString())) {

			final List<String> tabTitles = new ArrayList<String>(ace
					.getLanguages().size());

			for (int i = 0; i < ace.getLanguages().size(); i++) {
				final String titleTranslated = ace.getTitle().get(
						ace.getLanguages().get(i));
				final String title = R(
						"EditPopupWindow.TabName",
						titleTranslated == null || titleTranslated.equals("") ? "..."
								: titleTranslated,
						new Locale(ace.getLanguages().get(i))
								.getDisplayLanguage(new Locale(Translation
										.getActiveLang())));

				tabTitles.add(title);
			}

			SimplyHTMLUtil.openHTMLEditors(getJFrame(), ace,
					ace.getPopupHtMLFiles(getJFrame()), tabTitles,
					GpUtil.R("EditPopupWindow.EditorTitle"));

			// The next time the atlas is viewed, the popup has to reappear!
			ace.getProperties()
					.set(getJFrame(),
							org.geopublishing.atlasViewer.AVProps.Keys.showPopupOnStartup,
							"true");
		}

		else if (cmd.equals(ActionCmds.exportAtlasTranslations.toString())) {
			/**
			 * Ask the user to select a save position
			 */

			final File startWithDir = new File(System.getProperty("user.home"),
					"translations.html");
			final JFileChooser dc = new JFileChooser(startWithDir);
			dc.setDialogType(JFileChooser.SAVE_DIALOG);
			dc.setDialogTitle(GeopublisherGUI
					.R("PrintTranslations.SaveHTMLDialog.Title"));
			dc.setSelectedFile(startWithDir);

			if ((dc.showSaveDialog(getJFrame()) != JFileChooser.APPROVE_OPTION)
					|| (dc.getSelectedFile() == null))
				return;

			final File exportFile = dc.getSelectedFile();

			exportFile.delete();

			/**
			 * Create HTML output
			 */
			final ACETranslationPrinter translationPrinter = new ACETranslationPrinter(
					ace);
			final String allTrans = translationPrinter.printAllTranslations();

			try {
				/**
				 * Save it to file, dirty
				 */
				final BufferedWriter out = new BufferedWriter(new FileWriter(
						exportFile));
				out.write(allTrans);
				out.close();

				/**
				 * Clean it with SimplyHTML
				 */
				DocumentPane documentPane = new DocumentPane(exportFile.toURI()
						.toURL(), 0);
				documentPane.saveDocument();
				documentPane = null;

				/**
				 * Open it
				 */
				AVSwingUtil.lauchHTMLviewer(getJFrame(), exportFile.toURI());

				JOptionPane.showMessageDialog(getJFrame(),
						R("PrintTranslations.OKWillOpenMsg"));
			} catch (final Exception eee) {
				ExceptionDialog.show(getJFrame(), eee);
			}

		} else if (cmd.equals(ActionCmds.showImagesInfo.toString())) {

			GPDialogManager.dm_AtlasIcons.getInstanceFor(getAce(), getJFrame());

		}

		else if (cmd.equals(ActionCmds.newAtlas.toString())) {
			createNewAtlas();
		} else if (cmd.equals(ActionCmds.saveAtlas.toString())) {
			GpSwingUtil.save(ace, getJFrame(), true);
		} else if (cmd.equals(ActionCmds.exportJarsAtlas.toString())) {
			// Starts a modal wizard dialog
			ExportWizard.showWizard(getJFrame(), ace);
		} else if (cmd.equals(ActionCmds.importWizard.toString())) {
			// Starts a modal wizard dialog
			ImportWizard.showWizard(getJFrame(), ace);
		} else if (cmd.equals(ActionCmds.previewAtlasLive.toString())) {

			/**
			 * Close any other preview instances
			 */
			if (AtlasViewerGUI.isRunning()) {
				AtlasViewerGUI.dispose();
			}

			// If no valid StartMap has been selected, we don't allow to open
			// the preview.
			final MapPool mapPool = ace.getMapPool();
			if (mapPool.getStartMapID() == null
					|| mapPool.get(mapPool.getStartMapID()) == null) {
				JOptionPane.showMessageDialog(getJFrame(),
						AtlasViewerGUI.R("AtlasViewer.error.noMapInAtlas"),
						AtlasViewerGUI.R("AtlasViewer.error.noMapInAtlas"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Saving is not needed in live preview

			/**
			 * Create and configure a new visible Instance
			 */
			final AtlasViewerGUI av = AtlasViewerGUI.getInstance();
			av.setPreviewMode(true);
			av.startGui(getAce());
		} else if (cmd.equals(ActionCmds.previewAtlas.toString())) {
			/**
			 * Close any other preview instances
			 */
			if (AtlasViewerGUI.isRunning()) {
				AtlasViewerGUI.dispose();
			}

			// If we can't save the atlas cancel.
			if (!SwingUtil.askYesNo(getJFrame(),
					GeopublisherGUI.R("PreviewAtlas.AskToSave")))
				return;

			if (!GpSwingUtil.save(ace, getJFrame(), false))
				return;

			// If no valid StartMap has been selected, we don't allow to open
			// the preview.
			final MapPool mapPool = ace.getMapPool();
			if (mapPool.getStartMapID() == null
					|| mapPool.get(mapPool.getStartMapID()) == null) {
				JOptionPane.showMessageDialog(getJFrame(),
						AtlasViewerGUI.R("AtlasViewer.error.noMapInAtlas"),
						AtlasViewerGUI.R("AtlasViewer.error.noMapInAtlas"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Create and configure a new visible Instance
			final AtlasViewerGUI av = AtlasViewerGUI.getInstance();
			av.setPreviewMode(false);
			av.setExitOnClose(false);
			// Prepare the ResLoMan, so it will parse the AtlasWorkingDir
			av.getAtlasConfig()
					.getResLoMan()
					.addResourceLoader(
							new FileResourceLoader(getAce().getAtlasDir()));
			av.importAcAndStartGui();
		}

		else if (cmd.equals(ActionCmds.exitGP.toString())) {

			exitGP(0);

		}

		else {
			// ******************************************************************
			// If we didn't recognize it unil here, we are missing something.
			// ******************************************************************

			ExceptionDialog.show(getJFrame(), new AtlasRecoverableException(
					"An internal error: An unknown ActionCommand " + cmd
							+ " was permormed."));
		}
	}

	/**
	 * Closes the open Atlas if any Atlas is open. Asks the user if he wants to
	 * save it before closing.
	 * 
	 * @param save
	 *            if <code>null</code> the user will be asked whether he wants
	 *            to save.
	 * 
	 * @return false only if the Cancel button was pressed and the atlas was not
	 *         closed
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public boolean closeAtlas(Boolean save) {

		getJFrame().saveWindowPosition();

		if (ace == null)
			return true;

		int res;
		if (save == null) {
			SwingUtil.checkOnEDT();

			res = JOptionPane.showConfirmDialog(getJFrame(),
					GpUtil.R("CloseAtlasDialog.SaveAtlas.msg"),
					GpUtil.R("CloseAtlasDialog.SaveAtlas.title"),
					JOptionPane.YES_NO_CANCEL_OPTION);
		} else
			res = save ? JOptionPane.YES_OPTION : JOptionPane.NO_OPTION;

		if (res == JOptionPane.YES_OPTION) {
			/**
			 * Nicely try to close all dialogs
			 */
			AVDialogManager.dm_AttributeTable.disposeAll();
			if (!GPDialogManager.dm_EditAttribute.closeAllInstances())
				return false;
			if (!GPDialogManager.dm_EditDpEntry.closeAllInstances())
				return false;
			if (!GPDialogManager.dm_MapComposer.closeAllInstances())
				return false;

			// null to we get no confirmation
			// if saving failed do not exit
			if (!GpSwingUtil.save(ace, getJFrame(), false))
				return false;
		} else if (res == JOptionPane.NO_OPTION) {
			/**
			 * Don't be nice! We are not saving
			 */
			AVDialogManager.dm_AttributeTable.disposeAll();
			GPDialogManager.dm_EditAttribute.disposeAll();
			GPDialogManager.dm_EditDpEntry.disposeAll();
			GPDialogManager.dm_MapComposer.disposeAll();
		} else {
			return false;
		}

		if (AtlasViewerGUI.isRunning())
			AtlasViewerGUI.dispose();

		ace.getMapPool().removeChangeListener(
				listenToMapPoolChangesAndClosePreviewAtlas);
		ace.getDataPool().removeChangeListener(
				listenToDataPoolChangesAndCloseAtlasViewerPreview);

		GpSwingUtil.cleanFolder(ace, save == null ? getJFrame() : null);

		ace.dispose();

		ace = null;

		// Update the GUI, no not having loaded an atlas
		getJFrame().updateAce();

		return true;
	}

	/**
	 * Creates a new empty Atlas from user input.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	private void createNewAtlas() {

		if (!closeAtlas(null))
			return;
		// If there was an open Atlas, it is closed now.

		/**
		 * Fix an ugly bug that disables the "Create Folder" button on Windows
		 * for the MyDocuments
		 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4847375
		 */
		AVUtil.fixBug4847375();

		final JFileChooser dc = new JFileChooser(new File(GPProps.get(
				GPProps.Keys.LastOpenAtlasFolder, "")).getParent());
		dc.setDialogTitle(GpUtil.R("CreateAtlas.Dialog.Title"));
		dc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dc.setAcceptAllFileFilterUsed(true);

		dc.setMultiSelectionEnabled(false);

		dc.setDialogType(JFileChooser.SAVE_DIALOG);

		if ((dc.showOpenDialog(getJFrame()) != JFileChooser.APPROVE_OPTION)
				|| (dc.getSelectedFile() == null))
			return;

		final File atlasDir = dc.getSelectedFile();

		// Wenn der Name einer nicht existierenden Datei angegeben wurde, dann
		// fragen ob wir das als Ordner estellen sollen
		if (!atlasDir.exists()) {
			if (!SwingUtil.askYesNo(
					getJFrame(),
					R("CreateAtlas.Dialog.CreateFolderQuestion",
							IOUtil.escapePath(atlasDir))))
				return;
			atlasDir.mkdirs();
		}

		if (!atlasDir.isDirectory()) {
			AVSwingUtil.showMessageDialog(getJFrame(),
					R("CreateAtlas.Dialog.ErrorItsAFile"));
			return;
		}

		if (atlasDir.list().length > 0) {
			if (!SwingUtil
					.askYesNo(
							getJFrame(),
							R("CreateAtlas.Dialog.ConfimDelete",
									atlasDir.getName(),
									NumberFormat
											.getInstance()
											.format(FileUtils
													.sizeOfDirectory(atlasDir) / 1024.))))
				return;
		}

		// Delete the folder and recreate it.
		if (atlasDir.list().length > 0) {
			try {
				FileUtils.deleteDirectory(atlasDir);
				atlasDir.mkdirs();
			} catch (final IOException e) {
			}
		}

		ace = new AtlasConfigEditable(atlasDir);
		// ace.setAtlasDir(atlasDir);
		GPProps.set(GPProps.Keys.LastOpenAtlasFolder,
				atlasDir.getAbsolutePath());

		final String activeLang = Translation.getActiveLang();
		if (!activeLang.equals("en")) {
			ace.setLanguages(activeLang, "en");
		} else {
			if (!activeLang.equals("de")) {
				ace.setLanguages(activeLang, "de");
			}
		}
		/**
		 * Ask the use to enter the languages supported by the atlas. Uses the
		 * Locale language + English as defaults.
		 */
		AtlasLanguagesConfigDialog languageSelectionDialog = new AtlasLanguagesConfigDialog(
				getJFrame(), ace.getLanguages());
		languageSelectionDialog.setVisible(true); // its a modal dialog
		boolean cancelled = languageSelectionDialog.isCancel();
		while (cancelled) {
			languageSelectionDialog = new AtlasLanguagesConfigDialog(
					getJFrame(), ace.getLanguages());
			languageSelectionDialog.setVisible(true);
			cancelled = languageSelectionDialog.isCancel();
		}

		// We have to save this now, so that ad/atlas.xml exists
		// Without ad/atlas.xml we will not find our resources
		GpSwingUtil.save(ace, getJFrame(), false);

		getJFrame().setContentPane(new GpJSplitPane(ace));

		actionPerformed(new ActionEvent(this, 1,
				ActionCmds.editAtlasParams.toString()));

		getJFrame().updateMenu();
	}

	/**
	 * Exists Geopublisher. Asks to save and may be canceled by the user.
	 * 
	 * @param exitCode
	 *            0 mean all ok.
	 */
	public void exitGP(final int exitCode) {

		JNLPUtil.registerAsSingleInstance(GeopublisherGUI.this, false);

		if (ace != null) {
			if (closeAtlas(null) == false)
				return;
			if (ace != null)
				return;
		}
		ace = null;

		AtlasViewerGUI.dispose();

		if (gpJFrame != null)
			gpJFrame.dispose();

		LOGGER.info("Geopublisher " + ReleaseUtil.getVersionInfo(AVUtil.class)
				+ " terminating with exitcode " + exitCode);

		// Store the Logging Level in ~/.Geopublisher/Geopublisher.properties
		{
			GPProps.set(Keys.logLevel, Logger.getRootLogger().getLevel()
					.toString());
			GPProps.store();
		}

		System.exit(exitCode);
	}

	/**
	 * @return <code>null</code> if no atlas is loaded. Otherwise the
	 *         configuration of the loaded atlas.
	 */
	public AtlasConfigEditable getAce() {
		return ace;
	}

	/**
	 * This method initializes jFrame
	 * 
	 * @return javax.swing.JFrame
	 */
	public GpFrame getJFrame() {
		if (gpJFrame == null) {
			SwingUtil.checkOnEDT();

			// Disabled, because it looked ugly!
			// /**
			// * On Windows we dare to set the LnF to Windows
			// */
			// if (AVUtil.getOSType() == OSfamiliy.windows) {
			// try {
			// UIManager.setLookAndFeel(UIManager
			// .getSystemLookAndFeelClassName());
			// } catch (Exception e) {
			// LOGGER.warn("Couldn't set the Look&Feel to Windows native");
			// }
			// }

			gpJFrame = new GpFrame(this);

		}

		return gpJFrame;
	}

	/**
	 * A listener that will close any AtlasViewer preview if maps are removed
	 */
	PropertyChangeListener listenToMapPoolChangesAndClosePreviewAtlas = new PropertyChangeListener() {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(
					MapPool.EventTypes.removeMap.toString())) {
				if (AtlasViewerGUI.isRunning()) {
					LOGGER.debug("Closing an open AtlasViewer because a Map has been removed from the MapPool");
					// Kill any other open instance
					AtlasViewerGUI.dispose();
				}

			}
		}

	};

	/**
	 * Listens to removed layers in the DataPool and closes the AtlasViewer
	 * preview.
	 */
	PropertyChangeListener listenToDataPoolChangesAndCloseAtlasViewerPreview = new PropertyChangeListener() {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {

			if (evt.getPropertyName().equals(
					DataPool.EventTypes.removeDpe.toString())) {
				if (AtlasViewerGUI.isRunning()) {
					LOGGER.debug("Closing an open AtlasViewer because a Dpe has been removed from the MapPool");
					// Kill any other open instance
					AtlasViewerGUI.dispose();
				}

			}
		}

	};

	/**
	 * Asks the user to select a directory and tries to open an atlas from
	 * there.
	 */
	public void loadAtlas() {

		SwingUtil.checkOnEDT();

		if (!closeAtlas(null))
			return;

		// **********************************************************************
		// Ask the user to select a directory and tries to open an atlas from
		// there...
		// **********************************************************************
		final String lastAtlasDirectory = GPProps.get(
				GPProps.Keys.LastOpenAtlasFolder, ".");
		final JFileChooser dc = new JFileChooser(new File(lastAtlasDirectory));
		dc.setSelectedFile(new File(lastAtlasDirectory + "/"
				+ AtlasConfigEditable.ATLAS_GPA_FILENAME));
		dc.addChoosableFileFilter(new AtlasGPAFileFilter());
		dc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		dc.setAcceptAllFileFilterUsed(false);

		dc.setDialogTitle(GpUtil.R("LoadAtlas.FileChooser.Title"));
		dc.setMultiSelectionEnabled(false);
		int rc = dc.showOpenDialog(gpJFrame);
		while (rc == JFileChooser.APPROVE_OPTION
				&& !AtlasConfig
						.isAtlasDir(dc.getSelectedFile().getParentFile())) {
			AVSwingUtil.showMessageDialog(
					gpJFrame,
					GpUtil.R("LoadAtlasError.Directory_not_recognized", dc
							.getSelectedFile().getParentFile().getName()));
			rc = dc.showOpenDialog(gpJFrame);
		}

		if (rc != JFileChooser.APPROVE_OPTION) {
			// Cancel pressed
			return;
		}

		loadAtlasFromDir(dc.getSelectedFile().getParentFile());

	}

	/**
	 * Closes any open atlas opens an atlas from the given directory.
	 */
	public void loadAtlasFromDir(final File atlasDir) {

		SwingUtil.checkOnEDT();

		if (!closeAtlas(null))
			return;

		GPProps.set(GPProps.Keys.LastOpenAtlasFolder,
				atlasDir.getAbsolutePath());

		final AtlasStatusDialog statusWindow = new AtlasStatusDialog(
				getJFrame(), null, GeopublisherGUI.R(
						"AtlasLoader.processinfo.loading", atlasDir.getName()));
		final AtlasSwingWorker<AtlasConfigEditable> aceLoader = new AtlasSwingWorker<AtlasConfigEditable>(
				statusWindow) {

			@Override
			protected AtlasConfigEditable doInBackground() throws Exception {
				final AtlasConfigEditable ace = new AMLImportEd()
						.parseAtlasConfig(statusDialog, atlasDir);

				System.gc(); // Try to throw away as much memory as possible

				return ace;
			}
		};
		try {
			loadAce(aceLoader.executeModal());
		} catch (final CancellationException ex) {
			ace = null;
			getJFrame().updateAce();
		} catch (final Exception ex) {
			ace = null;
			getJFrame().updateAce();
			ExceptionDialog.show(getJFrame(), ex);
		}
	}

	public void loadAce(AtlasConfigEditable ace2) {
		this.ace = ace2;
		/*******************************************************
		 * Matching available and installed languages
		 */
		final Locale locale = Locale.getDefault();
		if (ace.getLanguages().contains(locale.getLanguage())) {
			Translation.setActiveLang(locale.getLanguage());
		} else {
			// a modal dialog popup
			SwitchLanguageDialog switchLanguageDialog = new SwitchLanguageDialog(
					getJFrame(), ace.getLanguages(), false);
			switchLanguageDialog.setVisible(true);
		}

		GpSwingUtil.validate(ace, getJFrame());

		ace.getMapPool().addChangeListener(
				listenToMapPoolChangesAndClosePreviewAtlas);

		ace.getDataPool().addChangeListener(
				listenToDataPoolChangesAndCloseAtlasViewerPreview);

		getJFrame().updateAce();
	}

	@Override
	public void newActivation(final String[] arg0) {
		LOGGER.info("A second instance of Geopublisher has been started.. The single instance if requesting focus now...");
		if (gpJFrame != null) {
			gpJFrame.requestFocus();
			gpJFrame.toFront();
		}
	}

	/**
	 * 
	 */
	private void printHelpAndExit() {
		final String message = R("CommandLineHelp");

		LOGGER.info(message);
		if (!GraphicsEnvironment.isHeadless()) {
			JOptionPane.showMessageDialog(
					null,
					message,
					R("CommandLineHelp.title",
							ReleaseUtil.getVersionInfo(AVUtil.class)),
					JOptionPane.INFORMATION_MESSAGE);
			System.exit(-1);
		} else
			System.exit(-1);
	}

	public static boolean isInstanciated() {
		return instance != null;
	}

}
