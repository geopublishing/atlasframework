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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.jnlp.SingleInstanceListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rachel.http.loader.WebResourceManager;
import schmitzm.lang.LangUtil;
import schmitzm.lang.ResourceProvider;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.AVDialogManager;
import skrueger.atlas.AVProps;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.JNLPUtil;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.exceptions.AtlasRecoverableException;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.atlas.http.Webserver;
import skrueger.atlas.map.MapPool;
import skrueger.atlas.swing.AtlasSwingWorker;
import skrueger.creator.gui.EditAtlasParamsDialog;
import skrueger.creator.gui.GpFrame;
import skrueger.creator.gui.GpJSplitPane;
import skrueger.creator.gui.LanguageSelectionDialog;
import skrueger.creator.gui.SimplyHTMLUtil;
import skrueger.creator.gui.export.ExportWizard;
import skrueger.i8n.SwitchLanguageDialog;
import skrueger.i8n.Translation;
import skrueger.swing.CancelButton;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.SHTMLPanelImpl;

/**
 * The mighty mighty {@link AtlasCreator} is a tool that generates runnable
 * {@link AtlasViewer} compilations.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class AtlasCreator implements ActionListener, SingleInstanceListener {
	/**
	 * A enumeration of actions. Mainly accessible through the {@link JMenuBar}
	 */
	public enum ActionCmds {
		changeLnF, editAboutInfo, editAtlasLanguages, editAtlasParams, editPopupInfo, exitGP, exportAtlasTranslations, exportJarsAtlas, newAtlas, saveAtlas, showImagesInfo, testAV, exportAtlasCSV
	}

	/** A singleton pattern for the {@link AtlasCreator} instance **/
	private static AtlasCreator instance = null;

	private static final Logger LOGGER = Logger.getLogger(AtlasCreator.class);

	/**
	 * {@link ResourceProvider}, der die Lokalisation fuer GUI-Komponenten des
	 * Package {@code skrueger.swing} zur Verfuegung stellt. Diese sind in
	 * properties-Datein unter {@code skrueger.swing.resource.locales}
	 * hinterlegt.
	 */
	protected final static ResourceProvider RESOURCE = new ResourceProvider(
			LangUtil.extendPackagePath(AtlasCreator.class,
					"resource.locales.Geopublisher"), Locale.ENGLISH);

	static {
		AtlasConfig.setupResLoMan();

		System.out
				.println("Adding new ClassResourceLoader( AtlasViewer.class ) to WebResourceManager");
		WebResourceManager
				.addResourceLoader(new rachel.http.loader.ClassResourceLoader(
						AtlasViewer.class));

		// Starting singleton WebServer
		try {
			new Webserver(true);
		} catch (Exception e) {
			ExceptionDialog.show(null, e);
			System.exit(-3);
		}

		/**
		 * Doing dome initializations
		 */
		AVUtil.fixBug4847375();
		SHTMLPanelImpl.setTextResources(null);
	}

	/**
	 * Creates or returns the single instance of GeoPublisher
	 */
	public static AtlasCreator getInstance() {
		if (instance == null) {
			LOGGER
					.error(
							"Geopublisher instance is requested without arguments and it doesn't exists yet!",
							new RuntimeException());
			instance = new AtlasCreator(new ArrayList<String>());
		}
		return instance;
	}

	/**
	 * Creates and returns a single instance of GeoPublisher, evaluating any
	 * arguments passed on the command line.
	 */
	public static AtlasCreator getInstance(List<String> args) {
		if (instance != null) {
			LOGGER
					.error(
							"Geopublisher instance is requested with arguments but it exists already!",
							new RuntimeException());
			instance = null;
		}
		instance = new AtlasCreator(args);
		return instance;
	}

	/**
	 * Start routine for the {@link AtlasCreator}
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedLookAndFeelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		System.setProperty("file.encoding", "UTF-8");

		// Setting up log4j
		URL log4jXmlUrl = AtlasConfig.getResLoMan().getResourceAsUrl(
				"gp_log4j.xml");
		DOMConfigurator.configure(log4jXmlUrl);

		getInstance(Arrays.asList(args));
	}

	/**
	 * Convenience method to access the {@link AtlasCreator}s translation
	 * resources.
	 * 
	 * @param key
	 *            the key for the Geopublisher.properties file
	 * @param values
	 *            optional values
	 */
	public static String R(String key, Object... values) {
		return RESOURCE.getString(key, values);
	}

	/** null if no atlas is open for editing. Otherwise no Atlas is loaded. * */
	private AtlasConfigEditable ace;

	/**
	 * Keeps an instance of the {@link GpFrame}, the main window of GeoPublisher
	 **/
	private GpFrame gpJFrame = null;

	/**
	 * Main constructor of {@link AtlasCreator}.
	 * 
	 * @param args
	 *            command line arguments
	 * **/
	public AtlasCreator(List<String> args) {

		LOGGER.info("This is GeoPublisher " + AVUtil.getVersionInfo());

		evaluateArgs(args);

		/** Output information about the GPL license **/
		AVUtil.logGPLCopyright(LOGGER);

		/*
		 * Register as a SingleInstance for JNLP. Starting another instance of
		 * AtlasCreator via JavaWebStart will fall back to this instance
		 */
		JNLPUtil.registerAsSingleInstance(AtlasCreator.this, true);

		/**
		 * Add an ExceptionHandler for all uncaught exceptions:
		 */

		Thread
				.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					public void uncaughtException(Thread t, Throwable e) {

						LOGGER
								.error(
										"An uncaught exception happened on Thread "
												+ t, e); // i8n

						if (e instanceof java.lang.ArrayIndexOutOfBoundsException) {
							StackTraceElement stackTraceElement = e
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
					AVUtil.cacheEPSG(getJFrame());

					// Only open the load atlas dialog if we have already
					// opened an atlas before
					if (!GPProps.get(GPProps.Keys.LastOpenAtlasFolder, ".")
							.equals("."))
						loadAtlas();
				} catch (Exception e) {
					ExceptionDialog.show(getJFrame(), e);
				}
			}
		});

		//
		// SwingUtilities.invokeLater(new Runnable() {
		//
		// @Override
		// public void run() {
		// JFrame frame = getJFrame();
		//
		// final AtlasStatusDialog progressWindow = new AtlasStatusDialog(
		// frame, AtlasViewer
		// .R("AtlasViewer.process.EPSG_codes_caching"),
		// AtlasViewer.R("AtlasViewer.process.EPSG_codes_caching"));
		// // final ProgressWindow window = new S(frame);
		// SwingWorker<Object, Object> worker = new SwingWorker<Object,
		// Object>() {
		//
		// @Override
		// protected Object doInBackground() throws Exception {
		// progressWindow.started();
		// AVUtil.cacheEPSG();
		// return null;
		// }
		//
		// @Override
		// protected void done() {
		// super.done();
		// try {
		// get();
		// } catch (Exception e) {
		// ExceptionDialog.show(null, e);
		// } finally {
		// progressWindow.complete();
		// // progressWindow.dispose();
		// }
		//
		// // Only open the load atlas dialog if we have already
		// // opened an atlas before
		// if (!GPProps.get(GPProps.Keys.LastOpenAtlasFolder, ".")
		// .equals("."))
		// loadAtlas();
		// }
		//
		// };
		// worker.execute();
		//
		// }
		//
		// });

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

			EditAtlasParamsDialog editAtlasDialog = new EditAtlasParamsDialog(
					getJFrame(), ace);
			editAtlasDialog.setVisible(true);
			if (!editAtlasDialog.isCancelled()) {
				getJFrame().setTitle(
						R("ApplicationMainWindowTitle_with_open_atlas", AVUtil
								.getVersionInfoShort(), ace.getTitle()
								.toString()));
			}

		}

		else if (cmd.equals(ActionCmds.editAtlasLanguages.toString())) {
			LanguageSelectionDialog languageSelectionDialog = new LanguageSelectionDialog(
					getJFrame(), ace.getLanguages());
			languageSelectionDialog.setVisible(true);
			if (!languageSelectionDialog.isCancel()) {

			}
		}

		else if (cmd.startsWith(ActionCmds.changeLnF.toString())) {
			String lnfClassname = cmd.substring(ActionCmds.changeLnF.toString()
					.length());
			try {
				UIManager.setLookAndFeel(lnfClassname);
				SwingUtilities.updateComponentTreeUI(getJFrame());
				getJFrame().updateMenu();

			} catch (Exception ex) {
				LOGGER.error(
						"Trying to add a useless JMenu for LookAndFeel stiff",
						ex);
			}
		} else if (cmd.equals(ActionCmds.editAboutInfo.toString())) {

			List<String> tabTitles = new ArrayList<String>(ace.getLanguages()
					.size());

			for (int i = 0; i < ace.getLanguages().size(); i++) {
				String titleTranslated = ace.getTitle().get(
						ace.getLanguages().get(i));
				String title = R(
						"EditAboutWindow.TabName",
						titleTranslated == null || titleTranslated.equals("") ? "..."
								: titleTranslated, new Locale(ace
								.getLanguages().get(i))
								.getDisplayLanguage(new Locale(Translation
										.getActiveLang())));

				tabTitles.add(title);
			}

			SimplyHTMLUtil.openHTMLEditors(getJFrame(), ace, ace
					.getAboutHtMLFiles(getJFrame()), tabTitles,
					AtlasCreator.RESOURCE
							.getString("EditAboutWindow.EditorTitle"));

		} else if (cmd.equals(ActionCmds.editPopupInfo.toString())) {

			List<String> tabTitles = new ArrayList<String>(ace.getLanguages()
					.size());

			for (int i = 0; i < ace.getLanguages().size(); i++) {
				String titleTranslated = ace.getTitle().get(
						ace.getLanguages().get(i));
				String title = R(
						"EditPopupWindow.TabName",
						titleTranslated == null || titleTranslated.equals("") ? "..."
								: titleTranslated, new Locale(ace
								.getLanguages().get(i))
								.getDisplayLanguage(new Locale(Translation
										.getActiveLang())));

				tabTitles.add(title);
			}

			SimplyHTMLUtil.openHTMLEditors(getJFrame(), ace, ace
					.getPopupHtMLFiles(getJFrame()), tabTitles,
					AtlasCreator.RESOURCE
							.getString("EditPopupWindow.EditorTitle"));

			// The next time the atlas is viewed, the popup has to reappear!
			AVProps.set(getJFrame(),
					skrueger.atlas.AVProps.Keys.showPopupOnStartup, "true");
		}

		else if (cmd.equals(ActionCmds.exportAtlasTranslations.toString())) {
			/**
			 * Ask the user to select a save position
			 */

			File startWithDir = new File(System.getProperty("user.home"),
					"translations.html");
			JFileChooser dc = new JFileChooser(startWithDir);
			dc.setDialogType(JFileChooser.SAVE_DIALOG);
			dc.setDialogTitle(AtlasCreator.RESOURCE
					.getString("PrintTranslations.SaveHTMLDialog.Title"));
			dc.setSelectedFile(startWithDir);

			if ((dc.showSaveDialog(getJFrame()) != JFileChooser.APPROVE_OPTION)
					|| (dc.getSelectedFile() == null))
				return;

			File exportFile = dc.getSelectedFile();

			exportFile.delete();

			/**
			 * Create HTML output
			 */
			ACETranslationPrinter translationPrinter = new ACETranslationPrinter(
					ace);
			String allTrans = translationPrinter.printAllTranslations();

			try {
				/**
				 * Save it to file, dirty
				 */
				BufferedWriter out = new BufferedWriter(new FileWriter(
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
				AVUtil.lauchHTMLviewer(getJFrame(), exportFile.toURI());

				JOptionPane.showMessageDialog(getJFrame(), RESOURCE
						.getString("PrintTranslations.OKWillOpenMsg"));
			} catch (Exception eee) {
				ExceptionDialog.show(getJFrame(), eee);
			}

		} else if (cmd.equals(ActionCmds.showImagesInfo.toString())) {

			// TODO TODO TODO

			final JDialog d = new JDialog(getJFrame(), AtlasCreator.RESOURCE
					.getString("PersonalizeImages_MenuEntryLabel"));

			StringBuffer msg = new StringBuffer();
			msg.append("<html>");
			msg.append("<h2>"
					+ AtlasCreator.RESOURCE
							.getString("PersonalizeImagesExplanationText")
					+ "</h2>");
			msg.append("<ul>");

			// Where to find the autorun.inf icon image
			msg
					.append("<li>"
							+ AtlasCreator.RESOURCE
									.getString("PersonalizeImagesExplanationText_AutorunIcon")
							+ "<br><i>");
			// msg.append(new File(ace.getAtlasDir(),
			// AtlasConfig.AUTORUNICON_RESOURCE_NAME).toString()
			// + "</i>");

			// Where to find the JWS icon image
			msg
					.append("<li>"
							+ AtlasCreator.RESOURCE
									.getString("PersonalizeImagesExplanationText_JWSIcon")
							+ "<br><i>");
			msg.append(new File(ace.getAtlasDir(),
					AtlasConfig.JWSICON_RESOURCE_NAME).toString()
					+ "</i>");

			// Where to find the splashscreen image
			msg
					.append("<li>"
							+ AtlasCreator.RESOURCE
									.getString("PersonalizeImagesExplanationText_Splashscreen")
							+ "<br><i>");
			msg.append(new File(ace.getAtlasDir(),
					AtlasConfig.SPLASHSCREEN_RESOURCE_NAME).toString()
					+ "</i>");

			// Where to find the flying logo image
			msg
					.append("<li>"
							+ AtlasCreator.RESOURCE
									.getString("PersonalizeImagesExplanationText_FlyingLogo")
							+ "<br><i>");
			msg.append(new File(ace.getAtlasDir(),
					AtlasConfig.MAPICON_RESOURCE_NAME).toString()
					+ "</i>");

			msg.append("</ul>");
			msg.append("</html>");
			JLabel infoLabel = new JLabel(msg.toString());

			JPanel cp = new JPanel(new BorderLayout());
			cp.add(infoLabel, BorderLayout.NORTH);

			Box previewLabel = Box.createVerticalBox();

			// TODO Icon can not be shown
			// URL urlAutorunIconFallback =
			// AtlasConfig.getResLoMan().getResourceAsUrl(AtlasConfig.AUTORUNICON_RESOURCE_NAME_FALLBACK);
			// URL urlAutorunIcon =
			// AtlasConfig.getResLoMan().getResourceAsUrl(AtlasConfig.AUTORUNICON_RESOURCE_NAME);
			// JLabel previeAutorunIcon = new JLabel("Autorun: icon.ico", new
			// ImageIcon( GuiAndTools.exists(urlAutorunIcon) ? urlAutorunIcon :
			// urlAutorunIconFallback), JLabel.CENTER);
			// previeAutorunIcon.setBorder(
			// BorderFactory.createTitledBorder("icon.ico"));
			// previewLabel.add(previeAutorunIcon);

			URL urlJWSIconFallback = AtlasConfig.getResLoMan()
					.getResourceAsUrl(
							AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK);
			URL urlJWSIcon = AtlasConfig.getResLoMan().getResourceAsUrl(
					AtlasConfig.JWSICON_RESOURCE_NAME);
			JLabel previewJWSIcon = new JLabel("JWS: icon.gif",
					new ImageIcon(AVUtil.exists(urlJWSIcon) ? urlJWSIcon
							: urlJWSIconFallback), SwingConstants.CENTER);
			previewJWSIcon.setBorder(BorderFactory
					.createTitledBorder("Java Web Start icon"));
			previewLabel.add(previewJWSIcon);
			cp.add(previewLabel, BorderLayout.CENTER);

			URL urlSplashscreenFallback = AtlasConfig.getResLoMan()
					.getResourceAsUrl(
							AtlasConfig.SPLASHSCREEN_RESOURCE_NAME_FALLBACK);
			URL urlSplashscreen = AtlasConfig.getResLoMan().getResourceAsUrl(
					AtlasConfig.SPLASHSCREEN_RESOURCE_NAME);
			JLabel previewSplashscreen = new JLabel("JWS: splashscreen.png",
					new ImageIcon(
							AVUtil.exists(urlSplashscreen) ? urlSplashscreen
									: urlSplashscreenFallback),
					SwingConstants.CENTER);
			previewSplashscreen.setBorder(BorderFactory
					.createTitledBorder("Java Web Start splashscreen"));
			previewLabel.add(previewSplashscreen);
			cp.add(previewLabel, BorderLayout.CENTER);

			JPanel buttonsPanel = new JPanel(new FlowLayout());

			/**
			 * A Button to open the ad-foder
			 */
			buttonsPanel
					.add(new JButton(
							new AbstractAction(
									AtlasCreator.RESOURCE
											.getString("PersonalizeImages_OpenADFolderButton_label")) {

								@Override
								public void actionPerformed(ActionEvent e) {
									AVUtil.openOSFolder(ace.getAd());
								}

							}));

			/**
			 * A Button to close the window
			 */
			buttonsPanel.add(new CancelButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					d.dispose();
				}

			}));
			cp.add(buttonsPanel, BorderLayout.SOUTH);

			d.setContentPane(cp);
			d.pack();
			d.setVisible(true);
		}

		else if (cmd.equals(ActionCmds.newAtlas.toString())) {
			createNewAtlas();
		} else if (cmd.equals(ActionCmds.saveAtlas.toString())) {
			ace.save(getJFrame(), true);
		} else if (cmd.equals(ActionCmds.exportJarsAtlas.toString())) {
			// Starts a modal wizard dialog
			ExportWizard.showWizard(getJFrame(), ace);
		}

		else if (cmd.equals(ActionCmds.testAV.toString())) {

			/**
			 * Close any other preview instances
			 */
			if (AtlasViewer.isRunning()) {
				AtlasViewer.dispose();
			}

			// If not valid StartMap has been selected, we don't allow to open
			// the preview.
			final MapPool mapPool = ace.getMapPool();
			if (mapPool.getStartMapID() == null
					|| mapPool.get(mapPool.getStartMapID()) == null) {
				JOptionPane.showMessageDialog(getJFrame(), AtlasViewer
						.R("AtlasViewer.error.noMapInAtlas"), AtlasViewer
						.R("AtlasViewer.error.noMapInAtlas"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// If we can't save the atlas cancel.
			if (!ace.save(getJFrame(), false))
				return;

			/**
			 * Create and configure a new visible Instance
			 */
			AtlasViewer av = AtlasViewer.getInstance();
			av.setExitOnClose(false);
			av.startGui();
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
	 * @return false only if the Cancel button was pressed and the atlas was not
	 *         closed
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public boolean closeAtlas() {

		getJFrame().saveWindowPosition();

		if (ace == null)
			return true;

		AVUtil.checkThatWeAreOnEDT();

		int res = JOptionPane.showConfirmDialog(getJFrame(),
				AtlasCreator.RESOURCE
						.getString("CloseAtlasDialog.SaveAtlas.msg"),
				AtlasCreator.RESOURCE
						.getString("CloseAtlasDialog.SaveAtlas.title"),
				JOptionPane.YES_NO_CANCEL_OPTION);

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
			if (!ace.save(getJFrame(), false))
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

		if (AtlasViewer.isRunning())
			AtlasViewer.dispose();

		ace.getMapPool().removeChangeListener(
				listenToMapPoolChangesAndClosePreviewAtlas);
		ace.getDataPool().removeChangeListener(
				listenToDataPoolChangesAndCloseAtlasViewerPreview);

		ace.uncache();
		ace = null;

		AtlasConfigEditable.resetResLoMan();
		getJFrame().updateAce();

		return true;
	}

	/**
	 * Creates a new empty Atlas from user input.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	private void createNewAtlas() {

		if (!closeAtlas())
			return;
		// If there was an open Atlas, it is closed now.

		/**
		 * Fix an ugly bug that disables the "Create Folder" button on Windows
		 * for the MyDocuments
		 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4847375
		 */
		AVUtil.fixBug4847375();

		JFileChooser dc = new JFileChooser(new File(GPProps.get(
				GPProps.Keys.LastOpenAtlasFolder, "")).getParent());
		dc.setDialogTitle(RESOURCE.getString("CreateAtlas.Dialog.Title"));
		dc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dc.setAcceptAllFileFilterUsed(true);

		dc.setMultiSelectionEnabled(false);

		dc.setDialogType(JFileChooser.SAVE_DIALOG);

		if ((dc.showOpenDialog(getJFrame()) != JFileChooser.APPROVE_OPTION)
				|| (dc.getSelectedFile() == null))
			return;

		File atlasDir = dc.getSelectedFile();

		// Wenn der Name einer nicht existierenden Datei angegeben wurde, dann
		// fragen ob wir das als Ordner estellen sollen
		if (!atlasDir.exists()) {
			if (!AVUtil.askYesNo(getJFrame(), R(
					"CreateAtlas.Dialog.CreateFolderQuestion", atlasDir
							.getAbsolutePath())))
				return;
			atlasDir.mkdirs();
		}

		if (!atlasDir.isDirectory()) {
			AVUtil.showMessageDialog(getJFrame(),
					R("CreateAtlas.Dialog.ErrorItsAFile"));
			return;
		}

		if (atlasDir.list().length > 0) {
			if (!AVUtil.askYesNo(getJFrame(), R(
					"CreateAtlas.Dialog.ConfimDelete", atlasDir.getName(),
					NumberFormat.getInstance().format(
							FileUtils.sizeOfDirectory(atlasDir) / 1024.))))
				return;
		}

		try {
			FileUtils.deleteDirectory(atlasDir);
		} catch (IOException e) {
		}

		ace = new AtlasConfigEditable();
		ace.setAtlasDir(atlasDir);
		GPProps.set(GPProps.Keys.LastOpenAtlasFolder, atlasDir
				.getAbsolutePath());

		String activeLang = Translation.getActiveLang();
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
		LanguageSelectionDialog languageSelectionDialog = new LanguageSelectionDialog(
				getJFrame(), ace.getLanguages());
		languageSelectionDialog.setVisible(true); // its a modal dialog
		boolean cancelled = languageSelectionDialog.isCancel();
		while (cancelled) {
			languageSelectionDialog = new LanguageSelectionDialog(getJFrame(),
					ace.getLanguages());
			languageSelectionDialog.setVisible(true);
			cancelled = languageSelectionDialog.isCancel();
		}

		// ******************************************************************
		// The Atlas was configured successful
		// ******************************************************************

		// ace.createDirectoryStructure();

		// We have to save this now, so that ad/atlas.xml exists
		// Without ad/atlas.xml we will not find our resources
		ace.save(getJFrame(), false);

		getJFrame().setContentPane(new GpJSplitPane(ace));

		actionPerformed(new ActionEvent(this, 1, ActionCmds.editAtlasParams
				.toString()));

		getJFrame().updateMenu();
	}

	/**
	 * Evaluates the command line arguments. May react with GUI or commandline
	 * messages.
	 */
	private void evaluateArgs(List<String> args) {
		boolean printHelpAndExit = false;

		for (String arg : args) {
			boolean understood = false;

			// Was help requested?
			if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")
					|| arg.equalsIgnoreCase("-?") || arg.equalsIgnoreCase("/?")) {
				understood = true;
				printHelpAndExit = true;
			}

			if (!understood) {
				LOGGER.info("Not understood: " + arg);
				printHelpAndExit = true;
				break;
			}

		}

		if (printHelpAndExit) {
			printHelpAndExit();
		}

	}

	/**
	 * Exists GeoPublisher. Asks to save and may be canceled by the user.
	 * 
	 * @param exitCode
	 *            0 mean all ok.
	 */
	public void exitGP(int exitCode) {

		JNLPUtil.registerAsSingleInstance(AtlasCreator.this, false);

		if (ace != null) {
			if (closeAtlas() == false)
				return;
			if (ace != null)
				return;
		}
		ace = null;

		AtlasViewer.dispose();

		if (gpJFrame != null)
			gpJFrame.dispose();

		LOGGER.info("AtlasCreator " + AVUtil.getVersionInfo()
				+ " terminated normally.");

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
			AVUtil.checkThatWeAreOnEDT();

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
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(
					MapPool.EventTypes.removeMap.toString())) {
				if (AtlasViewer.isRunning()) {
					LOGGER
							.debug("Closing an open AtlasViewer because a Map has been removed from the MapPool");
					// Kill any other open instance
					AtlasViewer.dispose();
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
		public void propertyChange(PropertyChangeEvent evt) {

			if (evt.getPropertyName().equals(
					DataPool.EventTypes.removeDpe.toString())) {
				if (AtlasViewer.isRunning()) {
					LOGGER
							.debug("Closing an open AtlasViewer because a Dpe has been removed from the MapPool");
					// Kill any other open instance
					AtlasViewer.dispose();
				}

			}
		}

	};

	/**
	 * Asks the user to select a directory and tries to open an atlas from
	 * there...
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void loadAtlas() {

		AVUtil.checkThatWeAreOnEDT();

		if (!closeAtlas())
			return;

		// **********************************************************************
		// Ask the user to select a directory and tries to open an atlas from
		// there...
		// **********************************************************************
		File atlasDir;
		final String lastAtlasDirectory = GPProps.get(
				GPProps.Keys.LastOpenAtlasFolder, ".");
		final JFileChooser dc = new JFileChooser(new File(lastAtlasDirectory));
		dc.setSelectedFile(new File(lastAtlasDirectory + "/"
				+ AtlasConfigEditable.ATLAS_GPA_FILENAME));
		AtlasXMLFileFilter filter = new AtlasXMLFileFilter();
		dc.addChoosableFileFilter(filter);
		dc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		dc.setAcceptAllFileFilterUsed(false);

		dc.setDialogTitle(RESOURCE.getString("LoadAtlas.FileChooser.Title"));
		dc.setMultiSelectionEnabled(false);
		int rc = dc.showOpenDialog(gpJFrame);
		while (rc == JFileChooser.APPROVE_OPTION
				&& !AMLImportEd
						.isAtlasDir(dc.getSelectedFile().getParentFile())) {
			AVUtil.showMessageDialog(gpJFrame, AtlasCreator.RESOURCE.getString(
					"LoadAtlasError.Directory_not_recognized", dc
							.getSelectedFile().getParentFile().getName()));
			rc = dc.showOpenDialog(gpJFrame);
		}

		if (rc != JFileChooser.APPROVE_OPTION) {
			// Cancel pressed
			return;
		}

		atlasDir = dc.getSelectedFile().getParentFile();

		loadAtlasFromDir(atlasDir);

	}

	private void loadAtlasFromDir(final File atlasDir) {

		AVUtil.checkThatWeAreOnEDT();

		if (!closeAtlas())
			return;

		GPProps.set(GPProps.Keys.LastOpenAtlasFolder, atlasDir
				.getAbsolutePath());

		AtlasStatusDialog statusWindow = new AtlasStatusDialog(getJFrame(),
				null, AtlasCreator.R("AtlasLoader.processinfo.loading",
						atlasDir.getAbsolutePath()));
		AtlasSwingWorker<AtlasConfigEditable> aceLoader = new AtlasSwingWorker<AtlasConfigEditable>(
				statusWindow) {

			@Override
			protected AtlasConfigEditable doInBackground() throws Exception {
				AtlasConfigEditable ace = AMLImportEd.parseAtlasConfig(
						statusDialog, atlasDir);

				System.gc(); // Try to throw away as much memory as possible

				return ace;
			}

		};
		try {
			ace = aceLoader.executeModal();

			/*******************************************************
			 * Matching available and installed languages
			 */
			Locale locale = Locale.getDefault();
			if (ace.getLanguages().contains(locale.getLanguage())) {
				Translation.setActiveLang(locale.getLanguage());
			} else {
				new SwitchLanguageDialog(getJFrame(), ace.getLanguages());
			}

			ace.validate(getJFrame());

			ace.getMapPool().addChangeListener(
					listenToMapPoolChangesAndClosePreviewAtlas);

			ace.getDataPool().addChangeListener(
					listenToDataPoolChangesAndCloseAtlasViewerPreview);

		} catch (final Exception ex) {
			ExceptionDialog.show(getJFrame(), ex);
			ace = null;
		} finally {
			getJFrame().updateAce();
		}
	}

	@Override
	public void newActivation(String[] arg0) {
		LOGGER
				.info("A second instance of AtlasCreator has been started.. The single instance if requesting focus now...");
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
			JOptionPane.showMessageDialog(null, message, R(
					"CommandLineHelp.title", AVUtil.getVersionInfoShort()),
					JOptionPane.INFORMATION_MESSAGE);
			System.exit(-1);
		} else
			System.exit(-1);
	}

}
