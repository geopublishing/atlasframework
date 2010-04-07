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
package org.geopublishing.geopublisher.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.atlasViewer.swing.BugReportmailer;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPBugReportmailer;
import org.geopublishing.geopublisher.GeopublisherGUI;
import org.geopublishing.geopublisher.UncacheAtlasAction;
import org.geopublishing.geopublisher.GeopublisherGUI.ActionCmds;
import org.geopublishing.geopublisher.gui.datapool.DataPoolJTable;
import org.geopublishing.geopublisher.gui.datapool.DraggableDatapoolJTable;
import org.geopublishing.geopublisher.gui.map.DesignMapViewJDialog;
import org.geopublishing.geopublisher.gui.map.MapPoolJTable;
import org.geotools.data.DataUtilities;

import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.creator.GPDialogManager;
import skrueger.creator.GPProps;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;
import skrueger.swing.HeapBar;

public class GpFrame extends JFrame {
	private static final Logger LOGGER = Logger.getLogger(GpFrame.class);

	/** A reference to the existing Geopublisher **/
	private final GeopublisherGUI gp;

	/**
	 * The heap bar starts a timer that updates it automatically. Hence we just
	 * want one instance of it, otherwise there would be many threads.
	 **/
	protected HeapBar singleHeapBar;

	/** A reference to the {@link GpJSplitPane} **/
	private volatile GpJSplitPane gsSplitPane;

	/** The status bar displayed at the bottom of the frame **/
	private GpStatusBar statusBar;

	/**
	 * Just a convenience method to access Geopublisher translation
	 */
	private String R(String key, Object... values) {
		return GeopublisherGUI.R(key, values);
	}

	/**
	 * A listener to exchange all components on language change.
	 */
	PropertyChangeListener localeChangeListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					// Close all open MapComposer instances.
					GPDialogManager.dm_MapComposer.forceCloseAllInstances();

					updateAce();

					/**
					 * Trigger a repaint of all components
					 */
					repaint(1000);
				}
			});
		}

	};

	public AtlasConfigEditable getAce() {
		return gp.getAce();
	}

	public GpFrame(final GeopublisherGUI gp) {

		this.gp = gp;
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (gp.closeAtlas()) {

					gp.exitGP(0);
				}
			}
		});

		// React to changes of the locale
		Translation.addLocaleChangeListener(localeChangeListener);

		setTitle(R("ApplicationMainWindowTitle", AVUtil.getVersionInfo()));

		setSize(new Dimension(GPProps.getInt(GPProps.Keys.gpWindowWidth, 750),
				GPProps.getInt(GPProps.Keys.gpWindowHeight, 600)));

		Boolean newStart = GPProps.get(GPProps.Keys.gpWindowWidth) == null;
		if (newStart) {
			SwingUtil.centerFrameOnScreen(this);
		} else {
			// TODO Fensterposition setzen und merken!?
		}

		// MMaximize the JFrame, depending on the last saved state.
		setExtendedState(GPProps.getInt(GPProps.Keys.windowMaximized, 0));

		// Setting the GP icons for this frame
		try {
			final List<Image> icons = new ArrayList<Image>(3);
			icons.add(new ImageIcon(Icons.class
					.getResource("/icons/gp_icon16.png")).getImage());
			icons.add(new ImageIcon(Icons.class
					.getResource("/icons/gp_icon32.png")).getImage());
			icons.add(new ImageIcon(Icons.class
					.getResource("/icons/gp_icon64.png")).getImage());
			setIconImages(icons);
		} catch (Exception e) {
			ExceptionDialog.show(this, e);
		}

		updateAce();

		setVisible(true);
	}

	/**
	 * This method initializes the jJMenuBar. No MenuItem is cached. All are
	 * recreated when this method is called. This {@link JMenu} is dependent a
	 * lot on the {@link #gp.getAce()}
	 * 
	 * @return a fresh javax.swing.JMenuBar
	 */
	protected JMenuBar createMenuBar() {
		AtlasConfigEditable ace = gp.getAce();
		if (ace == null) {
			setTitle(R("ApplicationMainWindowTitle", AVUtil.getVersionInfo()));
		} else {
			setTitle(R("ApplicationMainWindowTitle_with_open_atlas", AVUtil
					.getVersionInfo(), ace.getTitle().toString()));
		}

		JMenuBar jMenuBar = new JMenuBar();

		jMenuBar.add(getFileMenu());
		jMenuBar.add(getAtlasMenu());
		jMenuBar.add(getOptionsMenu());

		jMenuBar.invalidate();

		return jMenuBar;
	}

	/**
	 * This {@link JMenu} allows to load, save and create an atlas. Plus the
	 * exit button.
	 */
	private JMenu getFileMenu() {
		JMenuItem menuItem;

		AtlasConfigEditable ace = gp.getAce();

		JMenu fileMenu = new JMenu(R("MenuBar.FileMenu"));

		// ******************************************************************
		// "New Atlas" Menu Item - newAtlasMenuItem
		// ******************************************************************
		JMenuItem newAtlasMenuItem = new JMenuItem(R("MenuBar.FileMenu.New"));
		newAtlasMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				Event.CTRL_MASK, true));
		newAtlasMenuItem.addActionListener(gp);
		newAtlasMenuItem.setActionCommand(ActionCmds.newAtlas.toString());
		newAtlasMenuItem.setEnabled(ace == null);
		fileMenu.add(newAtlasMenuItem);

		// ******************************************************************
		// "Load Atlas" Menu Item - loadAtlasMenuItem
		// ******************************************************************
		JMenuItem loadAtlasMenuItem = new JMenuItem(new AbstractAction(
				R("MenuBar.FileMenu.Load")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				gp.loadAtlas();
			}

		});
		loadAtlasMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,
				0, true));
		loadAtlasMenuItem.setEnabled(ace == null);
		fileMenu.add(loadAtlasMenuItem);

		// ******************************************************************
		// "Close Atlas" Menu Item
		// ******************************************************************
		JMenuItem closeAtlasMenuItem = new JMenuItem(new AbstractAction(
				R("MenuBar.FileMenu.Close")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				gp.closeAtlas();
			}
		});
		closeAtlasMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.CTRL_DOWN_MASK, true));
		closeAtlasMenuItem.setEnabled(ace != null);
		fileMenu.add(closeAtlasMenuItem);

		// ******************************************************************
		// "Save Atlas" Menu Item - saveAtlasMenuItem
		// ******************************************************************
		if (ace != null) {
			JMenuItem saveAtlasMenuItem = new JMenuItem(
					R("MenuBar.FileMenu.Save"));
			saveAtlasMenuItem.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, true));
			saveAtlasMenuItem.setActionCommand(ActionCmds.saveAtlas.toString());
			saveAtlasMenuItem.addActionListener(gp);
			fileMenu.add(saveAtlasMenuItem);
		}

		// ******************************************************************
		// "Import data into the atlas using a wizard
		// ******************************************************************
		if (ace != null) {
			fileMenu
					.add(new GpMenuItem(R("MenuBar.FileMenu.ImportWizard"),
							null, ActionCmds.importWizard, null, KeyStroke
									.getKeyStroke(KeyEvent.VK_I,
											Event.CTRL_MASK, true)));
		}

		// ******************************************************************
		// "Export Atlas as JARs" Menu Item - exportJarsMenuItem
		// ******************************************************************
		if (ace != null) {
			fileMenu.add(new GpMenuItem(R("MenuBar.FileMenu.Export"), null,
					ActionCmds.exportJarsAtlas, null, KeyStroke.getKeyStroke(
							KeyEvent.VK_E, Event.CTRL_MASK, true)));
		}

		// ******************************************************************
		// "Test atlas without creating JARs" Menu Item - testAVMenuItem
		// ******************************************************************
		if (ace != null) {
			fileMenu.add(new GpMenuItem(R("MenuBar.FileMenu.Preview"),
					R("MenuBar.FileMenu.Preview.TT"), ActionCmds.previewAtlas,
					new ImageIcon(GPProps.class
							.getResource("resource/testRun.png")), KeyStroke
							.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_MASK,
									true)));

			fileMenu.add(new GpMenuItem(R("MenuBar.FileMenu.LivePreview"),
					R("MenuBar.FileMenu.LivePreview.TT"),
					ActionCmds.previewAtlasLive, new ImageIcon(GPProps.class
							.getResource("resource/testRun.png")), KeyStroke
							.getKeyStroke(KeyEvent.VK_F5, 0, true)));

		}

		// ******************************************************************
		// "Exit" Menu Item - exitMenuItem
		// ******************************************************************
		menuItem = new GpMenuItem(AtlasViewerGUI
				.R("AtlasViewer.FileMenu.ExitMenuItem.exit_application"), null,
				ActionCmds.exitGP, Icons.ICON_EXIT_SMALL);
		fileMenu.add(menuItem);
		return fileMenu;
	}

	/**
	 * @return the {@link DraggableDatapoolJTable} that represents the
	 *         {@link DataPool}
	 */
	public DataPoolJTable getDatapoolJTable() {
		return getGpSplitPane().getDatapoolJTable();
	}

	/**
	 * @return the {@link MapPoolJTable} that represents the {@link MapPool}
	 */
	public MapPoolJTable getMappoolJTable() {
		return getGpSplitPane().getMappoolJTable();
	}

	/**
	 * Will recreate the {@link JMenuBar} of this {@link JFrame}. Should be
	 * called after an {@link AtlasConfigEditable} has been loaded, closed or
	 * any language changes.
	 */
	public void updateMenu() {
		JMenuBar mBar = createMenuBar();
		setJMenuBar(mBar);

		// Helps against the problem, that the menu bar is sometimes not
		// clickable
		validate();
		repaint();
	}

	/**
	 * Will make the needed changes when another atlas has been loaded, closed
	 * or created. This will also automatically update the {@link JMenuBar}.<br/>
	 * When calling this method, we expect that the gp.getAce() contains the
	 * {@link AtlasConfigEditable} that should be displayed.
	 */
	public void updateAce() {
		JPanel contentPane = new JPanel(new BorderLayout());

		// need a new menu, a new splitpane and a new status bar
		if (gsSplitPane != null)
			gsSplitPane.dispose();

		gsSplitPane = null;
		statusBar = null;

		contentPane.add(getGpSplitPane(), BorderLayout.CENTER);
		contentPane.add(getGpStatusBar(), BorderLayout.SOUTH);

		setContentPane(contentPane);

		updateMenu();

		validate();
	}

	/**
	 * @return a single instance of the {@link HeapBar}
	 */
	public HeapBar getHeapBar() {
		if (singleHeapBar == null)
			singleHeapBar = new HeapBar();
		return singleHeapBar;
	}

	public GpStatusBar getGpStatusBar() {
		if (statusBar == null) {
			statusBar = new GpStatusBar(this);
		}
		return statusBar;
	}

	public GpJSplitPane getGpSplitPane() {
		if (gsSplitPane == null) {
			gsSplitPane = new GpJSplitPane(gp.getAce());
		}
		return gsSplitPane;
	}

	/**
	 * Saves the dimensions of the main frame and the state of the internal
	 * split pane to the .properties file.
	 */
	public void saveWindowPosition() {

		// Remember the State of the Windows
		GPProps.set(GPProps.Keys.windowMaximized, getExtendedState());

		GPProps.set(GPProps.Keys.gpWindowWidth, (Integer) getSize().width);
		GPProps.set(GPProps.Keys.gpWindowHeight, (Integer) getSize().height);

		// TODO This will be different
		if (getContentPane() instanceof GpJSplitPane) {
			GpJSplitPane gpSplit = (GpJSplitPane) getContentPane();

			GPProps.set(GPProps.Keys.gpWindowLeftDividerLocation,
					(Integer) gpSplit.getLeftDividerLocation());
			GPProps.set(GPProps.Keys.gpWindowRightDividerLocation,
					(Integer) gpSplit.getRightDividerLocation());
		}
		GPProps.store();
	}

	/**
	 * Creates the options menu. It contains general settings that are not
	 * directly related to the loaded atlas.
	 */
	private JMenu getOptionsMenu() {
		final AtlasConfigEditable ace = gp.getAce();

		JMenu optionsMenu = new JMenu(R("MenuBar.OptionsMenu"));

		if (ace != null) {
			// Option to re-read all the information that is NOT stored in the
			// atlas.xml, but in the ad/ folders.
			final JMenuItem uncacheMenuItem = new JMenuItem(
					new UncacheAtlasAction(this, ace));
			uncacheMenuItem
					.setToolTipText(R("MenuBar.OptionsMenu.ClearCaches.tt"));
			uncacheMenuItem.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_R, Event.CTRL_MASK, true));
			optionsMenu.add(uncacheMenuItem);
		}
		//
		// // TODO Should only appear when start with debug option
		// final JCheckBoxMenuItem rendererSwitchCheckBox = new
		// JCheckBoxMenuItem();
		// rendererSwitchCheckBox.setAction(new AbstractAction(
		// "use ShapefileRenderer") {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// GTUtil
		// .setGTRendererType(rendererSwitchCheckBox.isSelected() ?
		// GTRendererType.ShapefileRenderer
		// : GTRendererType.StreamingRenderer);
		// }
		// });
		// optionsMenu.add(rendererSwitchCheckBox);
		// rendererSwitchCheckBox.setSelected(true);

		// ******************************************************************
		// Switch Geopublisher language
		// ******************************************************************
		if (ace != null && ace.getLanguages().size() > 1)
			optionsMenu.add(getChangeLangJMenu());

		// TODO Switch startup language

		// ******************************************************************
		// Set rendering for the Geopublisher application
		// ******************************************************************
		JCheckBoxMenuItem jCheckBoxMenuItemAntiAliasingAC = new JCheckBoxMenuItem(
				new AbstractAction(
						R("MenuBar.OptionsMenu.Checkbox.QualityRenderingGP")) {

					@Override
					public void actionPerformed(ActionEvent e) {
						boolean useAntiAliase = ((JCheckBoxMenuItem) e
								.getSource()).isSelected();
						GPProps.set(GPProps.Keys.antialiasingMaps,
								useAntiAliase ? "1" : "0");

						// Update all open DesignMapViewialogs
						DesignMapViewJDialog.setAntiAliasing(useAntiAliase);
					}

				});
		jCheckBoxMenuItemAntiAliasingAC.setSelected(GPProps.getInt(
				GPProps.Keys.antialiasingMaps, 1) == 1);
		optionsMenu.add(jCheckBoxMenuItemAntiAliasingAC);

		// ******************************************************************
		// Send logfiles to author by email
		// ******************************************************************
		JMenuItem jMenuItemSendLog = new JMenuItem(new AbstractAction(
				R("MenuBar.OptionsMenu.SendLogToAuthor")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				BugReportmailer bugReport = new GPBugReportmailer();
				bugReport.send(GpFrame.this);
			}

		});
		optionsMenu.add(jMenuItemSendLog);

		// ******************************************************************
		// Send logfiles to author by email
		// ******************************************************************
		JMenuItem jMenuItemShowlog = new JMenuItem(new AbstractAction(
				R("MenuBar.OptionsMenu.OpenLogFile")) {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					File logFile = new File(IOUtil.getTempDir(),
							GPBugReportmailer.GEOPUBLISHERLOG)
							.getCanonicalFile();
					try {

						Desktop.getDesktop().edit(logFile);
					} catch (Exception usoe) {
						Desktop.getDesktop().browse(
								DataUtilities.fileToURL(logFile).toURI());
					}
				} catch (Exception ee) {
					ExceptionDialog.show(GpFrame.this, ee);

				}
			}

		});
		optionsMenu.add(jMenuItemShowlog);

		/**
		 * Allow to switch LookAndFeel
		 */
		if (UIManager.getInstalledLookAndFeels().length > 1)
			optionsMenu.add(getLnFJMenu());

		return optionsMenu;
	}

	private JMenuItem getLnFJMenu() {
		JMenu lnfJMenu = new JMenu(R("MenuBar.OptionsMenu.ChangeLookAndFeel"));

		/** the look and feels available in the system */
		for (UIManager.LookAndFeelInfo lnf : UIManager
				.getInstalledLookAndFeels()) {

			if (UIManager.getLookAndFeel().getName().equals(lnf.getName())) {
				continue;
			}

			JMenuItem oneLnFJmenuItem = new JMenuItem(lnf.getName());
			oneLnFJmenuItem.setActionCommand(ActionCmds.changeLnF.toString()
					+ lnf.getClassName());
			oneLnFJmenuItem.addActionListener(gp);

			lnfJMenu.add(oneLnFJmenuItem);
		}

		return lnfJMenu;
	}

	/**
	 * Change the {@link JMenu} languageSubMenu that allows changing the
	 * displayed language Attention, not to replace the object in the
	 * {@link JMenu} structure Call this after changes to atlasConfig.languages.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 *         Note: This method is double in {@link AtlasViewerGUI} and
	 *         {@link GeopublisherGUI}
	 */
	private JMenu getChangeLangJMenu() {
		AVUtil.checkThatWeAreOnEDT();
		// Assuming that the language was changed, update the windows title
		// getJFrame().setTitle("Loaded ace.getName().toString());

		AtlasConfigEditable ace = gp.getAce();

		JMenu languageSubMenu = new JMenu(AtlasViewerGUI
				.R("AtlasViewer.FileMenu.LanguageSubMenu.change_language"));
		languageSubMenu.setToolTipText(AtlasViewerGUI
				.R("AtlasViewer.FileMenu.LanguageSubMenu.change_language_tt"));
		languageSubMenu.setIcon(Icons.ICON_FLAGS_SMALL);

		for (String code : ace.getLanguages()) {

			// Not show the option to switch to actual language...
			if (code.equals(Translation.getActiveLang()))
				continue;

			/**
			 * Lookup a country where they speak the language, so we can print
			 * the language in local tounge.
			 */
			Locale locale = I8NUtil.getLocaleFor(code);

			JMenuItem langMenuItem = new JMenuItem(
					new AbstractAction(
							AtlasViewerGUI
									.R(
											"AtlasViewer.FileMenu.LanguageSubMenu.Menuitem.switch_language_to",
											locale.getDisplayLanguage(locale),
											locale.getDisplayLanguage(), code)) {

						public void actionPerformed(ActionEvent e) {
							String actionCommand = e.getActionCommand();
							Translation.setActiveLang(actionCommand);
						}

					});

			langMenuItem.setActionCommand(code);
			languageSubMenu.add(langMenuItem);

		}
		return languageSubMenu;
	}

	/**
	 * This {@link JMenu} contains {@link MenuItem}s that configure the loaded
	 * atlas. The menu is disabled if no atlas is loaded.
	 */
	private JMenu getAtlasMenu() {

		JMenu atlasJMenu = new JMenu(R("MenuBar.AtlasMenu"));

		final AtlasConfigEditable ace = gp.getAce();

		if (ace == null) {
			atlasJMenu.setEnabled(false);
		} else {

			atlasJMenu.add(new GpMenuItem(
					R("MenuBar.AtlasMenu.ChangeAtlasParams"),
					ActionCmds.editAtlasParams));

			atlasJMenu.add(new GpMenuItem(
					R("MenuBar.AtlasMenu.PersonalizeImages"),
					ActionCmds.showImagesInfo));

			atlasJMenu.add(new GpMenuItem(R("MenuBar.AtlasMenu.EditPopupInfo"),
					ActionCmds.editPopupInfo));

			atlasJMenu.add(new GpMenuItem(R("MenuBar.AtlasMenu.EditAboutInfo"),
					ActionCmds.editAboutInfo));

			// ******************************************************************
			// Set rendering quality for the atlas-product
			// ******************************************************************
			JCheckBoxMenuItem jCheckBoxMenuItemAntiAliasingAV = new JCheckBoxMenuItem(
					new AbstractAction(
							R("MenuBar.AtlasMenu.Checkbox.QualityRenderingAV")) {

						@Override
						public void actionPerformed(ActionEvent e) {
							boolean b = ((JCheckBoxMenuItem) e.getSource())
									.isSelected();
							getAce().getProperties().set(GpFrame.this,
									AVProps.Keys.antialiasingMaps,
									b ? "1" : "0");
						}

					});
			jCheckBoxMenuItemAntiAliasingAV
					.setSelected(getAce().getProperties().getInt(
							AVProps.Keys.antialiasingMaps, 1) == 1);
			atlasJMenu.add(jCheckBoxMenuItemAntiAliasingAV);

			atlasJMenu.add(new GpMenuItem(
					R("MenuBar.AtlasMenu.ChangeLanguages"),
					ActionCmds.editAtlasLanguages, Icons.ICON_FLAGS_SMALL));

			atlasJMenu.add(new GpMenuItem(
					R("MenuBar.AtlasMenu.PrintTranslations"),
					ActionCmds.exportAtlasTranslations));

			// Export the data pool as CSV
			atlasJMenu.add(new JMenuItem(new GPExportCSVAction(
					R("MenuBar.AtlasMenu.ExportCSV"), ace, GpFrame.this)));

			/**
			 * A an item to change the default CRS used in the atlas
			 */
			JMenuItem jMenuItemDefaultCRS = new JMenuItem(new AbstractAction(
					R("MenuBar.OptionsMenu.SetDefaultCRS")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO should implement {@link CancellableDialogAdapter}
					DefaultCRSSelectionJDialog defaultCRSSelectionJDialog = new DefaultCRSSelectionJDialog(
							GpFrame.this, ace);
					defaultCRSSelectionJDialog.setModal(true);
					defaultCRSSelectionJDialog.setVisible(true);
				}

			});
			atlasJMenu.add(jMenuItemDefaultCRS);

		}

		return atlasJMenu;
	}

	/**
	 * Extension of {@link JMenuItem} that automatically set's the
	 * {@link GeopublisherGUI} instance as the {@link ActionListener}
	 */
	class GpMenuItem extends JMenuItem {

		public GpMenuItem(String label, ImageIcon imageIcon,
				ActionCmds actionCmd, KeyStroke keyStroke) {
			this(label, null, actionCmd, null, null);
		}

		public GpMenuItem(String label, String tooltip, ActionCmds actionCmd,
				ImageIcon iconFlagsSmall, KeyStroke keyStroke) {
			super(label);
			if (tooltip != null && !tooltip.isEmpty())
				setToolTipText(tooltip);
			setActionCommand(actionCmd.toString());
			addActionListener(gp);
			if (iconFlagsSmall != null)
				setIcon(iconFlagsSmall);
			if (keyStroke != null) {
				setAccelerator(keyStroke);
			}
		}

		public GpMenuItem(String label, String tooltip, ActionCmds actionCmd,
				ImageIcon icon) {
			this(label, tooltip, actionCmd, icon, null);
		}

		public GpMenuItem(String label, ActionCmds cmd, ImageIcon icon) {
			this(label, null, cmd, icon);
		}

		public GpMenuItem(String label, ActionCmds cmd) {
			this(label, cmd, null);
		}

	}

}
