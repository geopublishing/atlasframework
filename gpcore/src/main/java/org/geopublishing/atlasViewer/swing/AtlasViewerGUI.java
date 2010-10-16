/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.io.DirectoryWalker.CancelException;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.dp.AMLImport;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.media.DpMedia;
import org.geopublishing.atlasViewer.exceptions.AtlasRecoverableException;
import org.geopublishing.atlasViewer.http.FileWebResourceLoader;
import org.geopublishing.atlasViewer.http.Webserver;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.swing.internal.AtlasMenuItem;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;
import org.geotools.swing.ExceptionMonitor;

import rachel.http.loader.WebClassResourceLoader;
import rachel.http.loader.WebResourceManager;
import rachel.loader.ClassResourceLoader;
import rachel.loader.FileResourceLoader;
import rachel.loader.ResourceLoaderManager;
import schmitzm.geotools.GTUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.lang.ResourceProvider;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.geotools.MapView;
import skrueger.geotools.StyledLayerInterface;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.SwitchLanguageDialog;
import skrueger.i8n.Translation;
import skrueger.versionnumber.ReleaseUtil;
import skrueger.versionnumber.ReleaseUtil.License;

/**
 * {@link AtlasViewerGUI} main class
 * 
 * @author Stefan Alfons Tzeggai
 */

public class AtlasViewerGUI implements ActionListener, SingleInstanceListener {
	public static Logger LOGGER = Logger.getLogger(AtlasViewerGUI.class);

	static {
		// Vom Benutzer hinzugefügte Übersetzungen aktivieren
		ResourceProvider.setAutoResetResourceBundle(true, "Translation", true);
	}

	/**
	 * Determines, whether to do a System.exit() when the Application is closed.
	 * This has to be set to false if started as a preview-atlas.
	 */
	private boolean exitOnClose = true;

	static {

		// Used to find AtlasML.xsd via the local webserver
		// System.out
		// .println("Adding new ClassResourceLoader( AtlasViewer.class ) to WebResourceManager");
		WebResourceManager
				.addResourceLoader(new rachel.http.loader.WebClassResourceLoader(
						AtlasViewerGUI.class));
	}

	/**
	 * Convenience method to access the {@link AtlasViewerGUI}s translation
	 * resources.
	 * 
	 * @param key
	 *            the key for the AtlasViewerTranslation.properties file
	 * @param values
	 *            optinal values
	 */
	public static String R(String key, Object... values) {
		return AVSwingUtil.R(key, values);
	}

	/**
	 * The main JFrame of the AtlasViewer.
	 */
	private JFrame atlasJFrame;

	/** The unique main atlasConfig that this AtlasViewer will present * */
	private AtlasConfig atlasConfig = new AtlasConfig();

	/** The singleton instance of this AtlasViewer */
	private static AtlasViewerGUI instance;

	/** The main {@link Map} that the user is watching */
	private Map map;

	/**
	 * If we have an open {@link MapView}, then its this one. If {@link #map} ==
	 * null, then {@link #mapView}==null
	 */
	private AtlasMapView mapView;

	private AtlasMenuBar atlasMenuBar;

	/**
	 * The constructor loads a {@link AtlasConfig}, otherwise no start is
	 * possible.<br/>
	 * All data is expected in JARs. These JARs can be downloaded via JWS or be
	 * stored in a local path
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	private AtlasViewerGUI() {

		// Atlas Viewer is starting
		LOGGER.info("Starting AtlasViewer.. "
				+ ReleaseUtil.getVersionInfo(AVUtil.class));
		LOGGER.info(ReleaseUtil.getLicense(License.LGPL3, "AtlasViewer"));
		
		/*
		 * Register this as single instance
		 */
		JNLPUtil.registerAsSingleInstance(AtlasViewerGUI.this, true);

	}

	private void openFirstMap() {
		MapPool mapPool = getAtlasConfig().getMapPool();
		// **************************************************************
		// Which map to start with? We first check it attribute
		// startMap
		// points to a valid Map
		// Otherwise a more or less random map will be shown (the
		// first
		// in the HashMap)
		// **************************************************************
		if (mapPool.size() > 0) {
			if (mapPool.getStartMapID() != null
					&& mapPool.get(mapPool.getStartMapID()) != null) {
				setMap(mapPool.get(mapPool.getStartMapID()));
			} else {
				// progressWindow.progress(1f);
				LOGGER.warn("No default start-up map selected, trying first one");
				setMap(mapPool.get(0));
			}
		} else {
			final String msgNoMapFound = R("AtlasViewer.error.noMapInAtlas");
			getJFrame().setContentPane(new JLabel(msgNoMapFound));
		}
	}

	/**
	 * Calling this the first time, initiates parsing the atlas.xml
	 */
	public static AtlasViewerGUI getInstance() {
		if (instance == null) {
			instance = new AtlasViewerGUI();
		}
		return instance;
	}

	/**
	 * @return <code>true</code> if an instance of {@link AtlasViewerGUI} exists
	 *         in the JVM. This can be used by components to determine whether
	 *         they are running from within Geopublisher or AtlasViewer without
	 *         having any dependency to the Geopublisher classes.
	 */
	public static boolean isRunning() {
		return instance != null;
	};

	/**
	 * This method initializes the main {@link AtlasViewerGUI} {@link JFrame}.
	 * If an icon ({@link AtlasConfig.JWSICON_RESOURCE_NAME}) has been defined
	 * for this atlas it will be set as the {@link JFrame} icon.
	 */
	public final JFrame getJFrame() {
		if (atlasJFrame == null) {
			SwingUtil.checkOnEDT();

			/**
			 * Disabled, because Artuhr wants to make screenshots with Metal On
			 * Windows we dare to set the LnF to Windows if
			 * (GuiAndTools.getOSType() == OSfamiliy.windows) { try {
			 * UIManager.setLookAndFeel(UIManager
			 * .getSystemLookAndFeelClassName()); } catch (Exception e) {
			 * LOGGER.warn("Couldn't set the Look&Feel to Windows native"); } }
			 */

			atlasJFrame = new JFrame();

			if (getAtlasConfig().getIconURL() != null) {
				atlasJFrame.setIconImage(new ImageIcon(getAtlasConfig()
						.getIconURL()).getImage());
			}

			atlasJFrame
					.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			atlasJFrame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					exitAV(0);
				}

			});

			atlasJFrame.setJMenuBar(getAtlasMenuBar());

			updateTitle();

			atlasJFrame.setPreferredSize(new Dimension(800, 600));
			atlasJFrame.pack();

			SwingUtil.centerFrameOnScreen(atlasJFrame);

			int state = atlasJFrame.getExtendedState();

			// Set the maximized bits
			state |= Frame.MAXIMIZED_BOTH;
			// Maximize the frame
			atlasJFrame.setExtendedState(state);

			atlasJFrame.setVisible(true);
		}
		return atlasJFrame;
	}

	/**
	 * Loads a new {@link Map} into the mainPanel / contentPane of the
	 * {@link AtlasViewerGUI}. The previous {@link Map}'s {@link DpEntry}s will
	 * be removed from cache if they are not used by the new {@link Map}. This
	 * convenience method delegates to a {@link #setMap(Map, boolean)} with
	 * force == <code>false</code>.
	 * 
	 * @param newMap
	 *            new {@link Map} to show in the {@link AtlasViewerGUI}
	 */
	public void setMap(final Map newMap) {
		setMap(newMap, false);
	}

	/**
	 * The method
	 * {@link #createLocalCopyFromURL(Component, URL, String, String)} uses this
	 * hashMap to remember which {@link URL}s have already been stored locally.
	 */
	private static HashMap<URL, File> cachedLocalCopiedFiles = new HashMap<URL, File>();

	/**
	 * Loads a new {@link Map} into the mainPanel / contentPane of the
	 * {@link AtlasViewerGUI}. The previous {@link Map}'s {@link DpEntry}s will
	 * be removed from cache if they are not used by the new {@link Map}.
	 * 
	 * @param newMap
	 *            new {@link Map} to show in the {@link AtlasViewerGUI}
	 * 
	 * @param force
	 *            If force == <code>false</code>, the map is only changed if the
	 *            old map != new map. If the map has to be repainted due to a
	 *            change of the language, paramter force should be
	 *            <code>true</code>.
	 */
	public void setMap(final Map newMap, boolean force) {
		// LOGGER.debug("setMap called!");

		/**
		 * Unless we force it, we don't reload the same map again.
		 */
		if ((!force) && map == newMap)
			return;

		if (newMap == null) {
			return;
		}

		AtlasStatusDialog statusDialog = new AtlasStatusDialog(getJFrame(),
				null, R("AmlViewer.process.opening_map", newMap.getTitle()));

		// Remember the status of the toolbar
		Integer lastMapsTool = -99;
		if (getJFrame().getContentPane() instanceof AtlasMapView) {
			AtlasMapView amv = (AtlasMapView) getJFrame().getContentPane();
			lastMapsTool = amv.getSelectedTool();
		}

		// in case the map loading is canceled, we jump back to the last map
		// String lastMapId = map != null ? map.getId() : null;
		AtlasSwingWorker<Boolean> startupTask = new AtlasSwingWorker<Boolean>(
				statusDialog) {

			@Override
			protected Boolean doInBackground() throws Exception {

				// **************************************************************
				// The previous {@link Map}'s {@link DpEntry}s will be
				// un-cached if they are not used by the new {@link Map}
				// **************************************************************
				// LOGGER.info("map  = " + map);
				if (map != null) {

					Container cp = getJFrame().getContentPane();

					if (cp instanceof AtlasMapView) {
						AtlasMapView amv = (AtlasMapView) cp;
						amv.dispose();
					}

					// Un-cache all parts of the old map that are not needed in
					// the new map
					map.uncache(newMap);
				}

				if (JNLPUtil.isAtlasDataFromJWS(atlasConfig)) {
					LOGGER.debug("atlas data comes from JWS, so we download all parts if needed first..");
					publish(R("AmlViewer.process.downloading_map",
							newMap.getTitle()));
					newMap.downloadMap(statusDialog);
				}

				return true;

			}

		};
		try {
			startupTask.executeModal();

			final AtlasMapView mapView_ = new AtlasMapView(getJFrame(),
					getAtlasConfig());
			mapView_.setMap(newMap);

			mapView_.initialize();
			if (lastMapsTool >= 0)
				getMapView().setSelectedTool(lastMapsTool);

			map = newMap;
			setMapView(mapView_);

		} catch (ExecutionException e) {
			if (e.getCause() instanceof CancelException)
				// tries to load the default map, which should not
				openFirstMap();
			else
				ExceptionMonitor.show(getJFrame(), e);
		} catch (InterruptedException e) {
		} catch (CancellationException e) {
		}
	}

	/**
	 * This is the {@link ActionListener} for the {@link JMenuItem}s and
	 * {@link AtlasMenuItem}s
	 */
	public void actionPerformed(ActionEvent evt) {
		final String cmd = evt.getActionCommand();
		try {
			LOGGER.debug("evaluating ActionCommand string = " + cmd);

			if (cmd.startsWith(AtlasMenuItem.ACTIONCMD_MAPPOOL_PREFIX)) {
				// A new Map was selected
				// The MapID is in the ActionCommandString ( mappool123321 )

				final String id = cmd
						.substring(AtlasMenuItem.ACTIONCMD_MAPPOOL_PREFIX
								.length());
				Map map = getAtlasConfig().getMapPool().get(id);
				setMap(map);

			} else if (cmd.startsWith(AtlasMenuItem.ACTIONCMD_DATAPOOL_PREFIX)) {
				final String id = cmd
						.substring(AtlasMenuItem.ACTIONCMD_DATAPOOL_PREFIX
								.length());
				DpEntry<? extends ChartStyle> dpe = getAtlasConfig()
						.getDataPool().get(id);

				if (!dpe.isLayer()) {
					LOGGER.debug("ActionCommand for a Media");
					DpMedia media = (DpMedia) dpe;
					// getURL macht das sowieso media.seeJAR(getJFrame());
					media.show(getJFrame());
				} else {
					try {
						// if (getMapView() == null) {
						// AVSwingUtil.showMessageDialog(getJFrame(), // TODO
						// // Sollte
						// // eigentlich
						// // ja
						// // garnicht
						// // passieren
						// "what to do with that layer?");
						// } else {

						// Calling the mapView to add the Layer
						getMapView().addStyledLayer(
								(StyledLayerInterface<?>) dpe);
						// }
					} catch (Throwable e) {
						ExceptionDialog.show(getJFrame(), e);
					}
				}

			}

			else if (cmd.equals("exit")) {
				exitAV(0);
			}

			else if (cmd.equals("about")) {
				/**
				 * Opens a modal about window.
				 */
				JDialog aboutWindow = new AtlasAboutDialog(getJFrame(), true,
						getAtlasConfig());
				aboutWindow.setVisible(true);
			}

			else if (cmd.equals("antiAliasing")) {
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						boolean b = AtlasViewerGUI.this.getAtlasMenuBar()
								.getJCheckBoxMenuItemAntiAliasing()
								.isSelected();
						getAtlasConfig().getProperties().set(getJFrame(),
								AVProps.Keys.antialiasingMaps, b ? "1" : "0");
						getMapView().getMapPane().setAntiAliasing(b);
						getMapView().getMapPane().repaint();
					}
				});
			} else {
				ExceptionDialog.show(getJFrame(),
						new AtlasRecoverableException(
								"A unknown ActionCommand was lost. ActionCommand was '"
										+ cmd + "'"));
			}
		} catch (Throwable e) {
			LOGGER.error("error while performing ActionCommand '" + cmd + "'",
					e);
			ExceptionDialog.show(getJFrame(), e);
		}
	}

	/**
	 * Closes the AtlasViewer. This can be triggered from the menu or from a
	 * seriouse exception during initialization.
	 */
	private void exitAV(int errorCode) {
		// **************************************************************
		// System is exiting...
		// **************************************************************
		dispose();

		instance = null;

		/**
		 * This checks wether we are running on our own, or from the GP
		 */
		if (exitOnClose) {

			/*
			 * Only remove the SingleInstanceService if we have NOT been started
			 * as a preview
			 */
			JNLPUtil.registerAsSingleInstance(AtlasViewerGUI.this, false);

			LOGGER.info("Returning exit code = " + errorCode
					+ " and System.exit()");
			System.exit(errorCode);
		} else {
			LOGGER.info("Not returning exit code = " + errorCode
					+ " because we are running inside another application.");
		}

	}

	/**
	 * This method initializes jJMenuBar
	 * 
	 * @return javax.swing.JMenuBar
	 */
	private AtlasMenuBar getAtlasMenuBar() {
		if (atlasMenuBar == null) {
			atlasMenuBar = new AtlasMenuBar(this);
		}
		return atlasMenuBar;
	}

	/**
	 * Updates the Title of the AtlasViewer to the shown {@link Map}'s name
	 * (single map mode)
	 */
	public void updateTitle() {
		String atlasName = getAtlasConfig().getTitle().toString();

		String mapName = "";
		if (map != null) {
			mapName = map.getTitle().toString() + " - ";
		}
		getJFrame().setTitle(mapName + atlasName);
	}

	/**
	 * @return A {@link JMenuItem} that opens a modal {@link GroupsDialog} .
	 */
	private JMenuItem getShowGroupsMenuItem() {
		JMenuItem menuitemGroups = new JMenuItem(
				R("AtlasViewer.FileMenu.ShowThematicGroups"));
		menuitemGroups.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				GroupsDialog groupsDialog = new GroupsDialog(getJFrame(),
						getAtlasConfig());
				groupsDialog.setVisible(true);
			}

		});
		return menuitemGroups;
	}

	/**
	 * Main method to start the {@link AtlasViewerGUI}.
	 * 
	 * @param args
	 *            The first argument may point to a directory that contains the
	 *            atlas root folder
	 * 
	 */
	public static void main(String[] args) {

		DpEntry.cleanupTemp();

		// Setup the ResLoMan
		setupResLoMan(args);

		try {
			// final URL log4jURL = AtlasConfig.getResLoMan().getResourceAsUrl(
			// "av_log4j.xml");

			// final URL log4jURL = AtlasViewerGUI.class.getClassLoader()
			// .getResource("av_log4j.xml");

			// System.out.println("Configuring log4j from " + log4jURL);
			// DOMConfigurator.configure(log4jURL);

			AVUtil.initAtlasLogging();

		} catch (Throwable e) {
			ExceptionDialog.show(null, e);
		}

		System.out.println("Classpath entries2:");
		String[] st = System.getProperty("java.class.path").split(":");
		for (String t : st) {
			System.out.println(t);
			LOGGER.debug(t);
		}

		AtlasViewerGUI.getInstance().importAcAndStartGui();

		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6476706
		// System.exit(0);
	}

	public void importAcAndStartGui() {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				// **********************************************************************
				// Starting the AtlasViewer on another Thread.
				// **********************************************************************
				AtlasStatusDialog statusDialog = new AtlasStatusDialog(null);// no
				// jframe!
				AtlasSwingWorker<AtlasConfig> startupWorker = new AtlasSwingWorker<AtlasConfig>(
						statusDialog) {

					@Override
					protected AtlasConfig doInBackground() throws Exception {
						// waitDialog.setVisible(true);
						// waitDialog.setModal(true);

						publish(R("AtlasViewer.process.EPSG_codes_caching"));
						GTUtil.initEPSG();

						// Starting the internal WebServer
						new Webserver(true);

						publish(R("dialog.title.wait"));
						new AMLImport().parseAtlasConfig(statusDialog,
								getAtlasConfig(), true);

						return getAtlasConfig();
					}

				};

				try {
					startupWorker.executeModal();
				} catch (Exception e) {
					ExceptionDialog.show(null, e); // no jframe!
					LOGGER.error("can't start atlas", e);
					exitAV(-99);
				}

				startGui();

			}
		});

	}

	/**
	 * Allows to pass a fully configured {@link AtlasConfig} to
	 * 
	 * @param atlasConfig
	 */
	public void startGui(AtlasConfig atlasConfig) {
		if (this.atlasConfig != null && this.atlasConfig != atlasConfig) {
			this.atlasConfig.dispose();
		}
		this.atlasConfig = atlasConfig;

		startGui();
	}

	// TODO needs doku
	protected void startGui() {

		/***************************************************************
		 * Match available and installed languages
		 */
		// TODO separate
		Locale locale = Locale.getDefault();
		if (getAtlasConfig().getLanguages().contains(locale.getLanguage())) {
			Translation.setActiveLang(locale.getLanguage(), true);
		} else {
			// As the owner we do not provide getJFrame() but null!
			// We are not on EDT and do not want to initiate the
			// JFrame creation.
			SwitchLanguageDialog switchLanguageDialog = new SwitchLanguageDialog(
					null, getAtlasConfig().getLanguages(), true);
			// Will not appear if there is only one language to select from
			switchLanguageDialog.setVisible(true);
		}

		/**
		 * Open the AtlasPopupDialog while the atlas is still loading
		 */
		// LOGGER.debug("popup props say: "+AVProps
		// .getBoolean(Keys.showPopupOnStartup, true));
		// LOGGER.debug("popup URL says = "+getAtlasConfig().getPopupHTMLURL());
		if (getAtlasConfig().getPopupHTMLURL() != null
				&& getAtlasConfig()
						.getProperties()
						.getBoolean(
								org.geopublishing.atlasViewer.AVProps.Keys.showPopupOnStartup,
								true)) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					/**
					 * Opens a modal about window.
					 */
					JDialog aboutWindow = new AtlasPopupDialog(getJFrame(),
							true, getAtlasConfig());
					aboutWindow.setVisible(true);
				}
			});
		}

		updateLangMenu();

		openFirstMap();
	}

	/**
	 * Setting up the {@link ResourceLoaderManager}
	 * 
	 * Has to be done in the <code>main</code> method at startup of the AV or
	 * GP.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * @param args
	 *            optional command line String[] args. If an existing directory
	 *            is passed, the directory will be used as the resource base.
	 */
	public static void setupResLoMan(String[] args) {
		Boolean resourcesComeFromFilesystem = false;

		try {

			if (args == null)
				return;

			String paramPath;

			if (args.length >= 1) {

				if (args.length == 1) {
					paramPath = args[0];
				} else {
					// we have many arguments

					/**
					 * * If the pathname passed contains spaces, it might be
					 * interpreted like many strings... so we connect them to
					 * one...
					 */
					// This is just a try..
					paramPath = "";
					for (String s : args) {
						paramPath = paramPath + s + " ";
					}

					paramPath = paramPath.trim();

					LOGGER.info("We had more than one argument on the command line... AV tried to concat them to one path with spaces: '"
							+ paramPath + "'");
				}

				if (paramPath.endsWith("/")) {
					// LOGGER.debug("Removing trailing /");
					paramPath = paramPath.substring(0, paramPath.length() - 1);
				}

				File atlasDir = new File(paramPath);
				if (AtlasConfig.isAtlasDir(atlasDir)) {
					resourcesComeFromFilesystem = true;

					// Add that folder to the ResLoMan
					LOGGER.debug("Adding new FileResourceLoader( "
							+ atlasDir.getAbsolutePath() + " ) to ResLoMan");

					resourcesComeFromFilesystem = true;

					getInstance()
							.getAtlasConfig()
							.getResLoMan()
							.addResourceLoader(new FileResourceLoader(atlasDir));

					LOGGER.debug("Adding new FileWebResourceLoader( "
							+ atlasDir.getAbsolutePath()
							+ " ) to WebResourceManager");
					WebResourceManager
							.addResourceLoader(new FileWebResourceLoader(
									atlasDir));

				} else {
					final String msg = "A non existing directory was passed on command line. Will be ignored!\n"
							+ atlasDir.getAbsolutePath();
					LOGGER.warn(msg);
					JOptionPane.showMessageDialog(null, msg);
				}
			}
		} finally {
			if (!resourcesComeFromFilesystem) {
				// If we are started without a path to a working copy as an
				// argument, we expect all stuff to be on the class-path.

				getInstance()
						.getAtlasConfig()
						.getResLoMan()
						.addResourceLoader(
								new ClassResourceLoader(AtlasViewerGUI.class));

				WebResourceManager
						.addResourceLoader(new WebClassResourceLoader(
								AtlasViewerGUI.class));
			}
		}

	}

	/**
	 * If the AtlasViewer is used in a preview mode (e.g. started via
	 * Geopublisher, this helps the GC. Also calls {@link DpEntry#cleanupTemp()}
	 * . Variable Instance is set to <code>null</code>.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static void dispose() {

		DpEntry.cleanupTemp();

		if (instance != null) {
			Map primaryMap = instance.getPrimaryMap();
			if (primaryMap != null) {

				Container cp = instance.getJFrame().getContentPane();

				if (cp instanceof AtlasMapView) {
					LOGGER.info("Uncaching old map items while disposing AtlasViewer");
					AtlasMapView amv = (AtlasMapView) cp;

					amv.dispose();
				}

				primaryMap.uncache(null);
				instance.getJFrame().dispose();
			}
			instance = null;
		}

	}

	/**
	 * ATM the ATlasViewer can only show one map at the time....
	 * 
	 * @return Returns <code>null</code> or the active {@link Map}
	 */
	public Map getPrimaryMap() {
		return map;
	}

	public final void setMapView(AtlasMapView mapView) {
		this.mapView = mapView;
		updateMenu();
		// atlasJFrame.dispose();
		// atlasJFrame = null;
		getJFrame().setContentPane(getMapView());
		mapView.revalidate();
		updateTitle();
	}

	private void updateMenu() {
		atlasMenuBar = null;
		languageSubMenu = null;
		atlasJFrame.setJMenuBar(getAtlasMenuBar());
		updateLangMenu();
	}

	public final AtlasMapView getMapView() {
		return mapView;
	}

	public AtlasConfig getAtlasConfig() {
		return atlasConfig;
	}

	/**
	 * If set to <code>false</code>, this instance of the {@link AtlasViewerGUI}
	 * is also removed from the {@link SingleInstanceService}
	 * 
	 * @param exitOnClose
	 *            Do a System.exit() when exiting the {@link AtlasViewerGUI} ?
	 */
	public void setExitOnClose(boolean exitOnClose) {
		this.exitOnClose = exitOnClose;
		if (exitOnClose == false) {
			/*
			 * Un-register this as single instance
			 */
			JNLPUtil.registerAsSingleInstance(AtlasViewerGUI.this, false);
		}
	}

	public boolean isExitOnClose() {
		return exitOnClose;
	}

	/**
	 * Returns the map that is visible in the {@link AtlasViewerGUI}.
	 * 
	 * @return <code>null</code> if no map is shown.
	 */
	public Map getMap() {
		return map;
	}

	private JMenu languageSubMenu;

	/**
	 * Change the {@link JMenu} languageSubMenu that allows changing the
	 * displayed language Attention, not to replace the object in the
	 * {@link JMenu} structure Call this after changes to atlasConfig.langages.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 *         Note: This method is double in {@link AtlasViewerGUI} and
	 *         Geopublisher
	 */
	private void updateLangMenu() {
		if (getAtlasConfig() == null)
			return;

		// Assuming that the language was changed, update the windows title
		// getJFrame().setTitle("Loaded ace.getName().toString());

		SwingUtil.checkOnEDT();

		if (languageSubMenu == null) {

			languageSubMenu = new JMenu(
					AtlasViewerGUI
							.R("AtlasViewer.FileMenu.LanguageSubMenu.change_language"));

			languageSubMenu.setFont(AtlasMenuItem.BIGFONT);

			languageSubMenu
					.setToolTipText(AtlasViewerGUI
							.R("AtlasViewer.FileMenu.LanguageSubMenu.change_language_tt"));
			languageSubMenu.setIcon(Icons.ICON_FLAGS_SMALL);
		} else {
			// Remove all old MenuItems
			languageSubMenu.removeAll();
		}

		// If there is only one language, the whole menu is disabled and a
		// different tooltip appears.
		if (getAtlasConfig().getLanguages().size() == 1) {
			getLanguageSubMenu()
					.setToolTipText(
							R("AtlasViewer.FileMenu.LanguageSubMenu.change_language_notAvailable_tt",
									getAtlasConfig().getLanguages().get(0)));
			getLanguageSubMenu().setEnabled(false);
			return;
		}

		for (String langCode : getAtlasConfig().getLanguages()) {

			// Not show the option to switch to actual language...
			if (langCode.equals(Translation.getActiveLang()))
				continue;

			/**
			 * Lookup a country where they speak the language, so we can print
			 * the language in local tounge.
			 */
			Locale locale = I8NUtil.getFirstLocaleForLang(langCode);

			JMenuItem langMenuItem = new AtlasMenuItem(
					new AbstractAction(
							AtlasViewerGUI
									.R("AtlasViewer.FileMenu.LanguageSubMenu.Menuitem.switch_language_to",
											locale.getDisplayLanguage(locale),
											locale.getDisplayLanguage(),
											langCode)) {

						public void actionPerformed(ActionEvent e) {
							try {
								Translation.setActiveLang(e.getActionCommand());

								// To force the recreation of the Menus, we have
								// to null some of the elements
								// updateMenu();

								// Update the GUI
								updateLangMenu();

								setMap(map, true);
							} catch (Throwable ex) {
								ExceptionDialog.show(getJFrame(), ex);
							}
						}

					});
			langMenuItem.setActionCommand(langCode);
			getLanguageSubMenu().add(langMenuItem);
		}
	}

	public JMenu getLanguageSubMenu() {
		if (languageSubMenu == null) {
			updateLangMenu();
		}
		return languageSubMenu;
	}

	/**
	 * Called via SingleInstanceListener / SingleInstanceService. Shows a
	 * splashscreen and bring the existing instance to the front.
	 */
	@Override
	public void newActivation(String[] arg0) {
		LOGGER.info("A second instance of AtlasViewer has been started.. The single instance if requesting focus now...");

		/*
		 * Showing the Spalshscreen for one secong
		 */
		try {
			final URL splashscreenUrl = atlasConfig
					.getResource(AtlasConfig.SPLASHSCREEN_RESOURCE_NAME);
			if (splashscreenUrl != null) {
				JWindow splashWindow = new JWindow(atlasJFrame);
				JPanel panel = new JPanel(new BorderLayout());
				ImageIcon icon = new ImageIcon(splashscreenUrl);
				panel.add(new JLabel(icon), BorderLayout.CENTER);
				panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				splashWindow.getContentPane().add(panel);
				splashWindow.getRootPane().setOpaque(true);
				splashWindow.pack();
				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
				splashWindow.setLocation(
						(int) (d.getWidth() - splashWindow.getWidth()) / 2,
						(int) (d.getHeight() - splashWindow.getHeight()) / 2);
				splashWindow.setVisible(true);
				Thread.sleep(1500);
				splashWindow.dispose();
				splashWindow = null;
			}
		} catch (Exception e) {
			LOGGER.warn(
					"Singleinstance.newActivation had problems while showing the splashscreen:",
					e);
		}

		if (getJFrame() != null) {
			if (!getJFrame().isShowing())
				getJFrame().setVisible(true);

			/* In case that it has been iconified */
			getJFrame().setExtendedState(Frame.NORMAL);

			getJFrame().requestFocus();
			getJFrame().toFront();
		}
	}

}
