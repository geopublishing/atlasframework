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
package org.geopublishing.atlasStyler.swing;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.jnlp.SingleInstanceListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASProps;
import org.geopublishing.atlasStyler.ASProps.Keys;
import org.geopublishing.atlasStyler.AsSwingUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.swing.importWizard.ImportWizard;
import org.geopublishing.atlasStyler.swing.importWizard.ImportWizardResultProducer_FILE;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;

import schmitzm.geotools.map.event.MapLayerListAdapter;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import schmitzm.lang.ResourceProvider;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.geotools.MapContextManagerInterface;
import skrueger.geotools.StyledFS;
import skrueger.geotools.StyledFeatureSourceInterface;
import skrueger.geotools.StyledLayerInterface;
import skrueger.i8n.Translation;
import skrueger.versionnumber.ReleaseUtil;
import skrueger.versionnumber.ReleaseUtil.License;

/**
 * This is the main GUI for the AtlasStyler standalone. It looks like a
 * condensed {@link AtlasViewer}, and its main purpose is to reach the "Style"
 * item in the tools menu.
 * 
 * @author Stefan A. Tzeggai
 * 
 */
public class AtlasStylerGUI extends JFrame implements SingleInstanceListener {

	static {
		// Vom Benutzer hinzugefügte Übersetzungen aktivieren
		ResourceProvider.setAutoResetResourceBundle(true, "Translation", true);
	}

	private static final long serialVersionUID = 1231321321258008431L;

	final static private Logger LOGGER = AsSwingUtil
			.createLogger(AtlasStylerGUI.class);

	private StylerMapView stylerMapView = null;

	final private HashMap<String, StyledFS> stledObjCache = new HashMap<String, StyledFS>();

	final private XMLCodeFrame xmlCodeFrame = new XMLCodeFrame(this,
			getStylerMapView().getMapManager());

	private HashMap<String, DataStore> openDatastores = new HashMap<String, DataStore>();

	/**
	 * This is the default constructor
	 */
	public AtlasStylerGUI() {
		LOGGER.info("Starting " + AtlasStylerGUI.class.getSimpleName() + "... "
				+ ReleaseUtil.getVersionInfo(AVUtil.class));
		//
		// // Setting up the logger from a XML configuration file. This is also
		// // done in ASProps, as it is eventually called earlier.
		// DOMConfigurator.configure(AsSwingUtil.class.getResource("/as_log4j.xml"));

		AsSwingUtil.initAsLogging();

		// Output information about the LGPL license
		LOGGER.info(ReleaseUtil.getLicense(License.LGPL3, "AtlasStyler"));

		System.setProperty("file.encoding", "UTF-8");

		JNLPUtil.registerAsSingleInstance(AtlasStylerGUI.this, true);

		initialize();

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				exitAS(0);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				exitAS(0);
			}

		});

		/**
		 * Setting a nice AtlasStylerGUI icons
		 */
		List<Image> icons = new ArrayList<Image>(2);
		ClassLoader cl = AsSwingUtil.class.getClassLoader();
		final String imagePackageName = "icons/";
		icons.add(new ImageIcon(cl.getResource(imagePackageName
				+ "as_icon16.png")).getImage());
		icons.add(new ImageIcon(cl.getResource(imagePackageName
				+ "as_icon32.png")).getImage());
		icons.add(new ImageIcon(cl.getResource(imagePackageName
				+ "as_icon64.png")).getImage());
		setIconImages(icons);

		AVSwingUtil.initEPSG(AtlasStylerGUI.this);
	}

	private void initialize() {
		this.setSize(750, 510);

		this.setJMenuBar(createMenuBar());

		this.setContentPane(getJContentPane());
		String AtlasStyler_MainWindowTitle = "AtlasStyler "
				+ ReleaseUtil.getVersionInfo(AVUtil.class);
		this.setTitle(AtlasStyler_MainWindowTitle);

		// In Xubuntu (OS-Geo Live DVD) the JFrame otherwise is hidden behind
		// the top-bar.
		SwingUtil.centerFrameOnScreen(this);
	}

	/**
	 * Creates a nre {@link JMenuBar} instance
	 */
	private JMenuBar createMenuBar() {
		JMenuBar jMenuBar = new JMenuBar();

		JMenu fileMenu = new JMenu(AsSwingUtil.R("MenuBar.FileMenu"));

		jMenuBar.add(fileMenu);

		{ // Import WIzard
			JMenuItem mi = new JMenuItem(new AbstractAction(
					AsSwingUtil.R("MenuBar.FileMenu.ImportWizard")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					ImportWizard.showWizard(AtlasStylerGUI.this,
							AtlasStylerGUI.this);
				}
			});
			mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
					Event.CTRL_MASK, true));
			fileMenu.add(mi);
		}

		fileMenu.add(SwingUtil.createChangeLog4JLevelJMenu());

		{ // Exit
			JMenuItem mi = new JMenuItem(
					new AbstractAction(
							AtlasViewerGUI
									.R("AtlasViewer.FileMenu.ExitMenuItem.exit_application"),
							Icons.ICON_EXIT_SMALL) {

						@Override
						public void actionPerformed(ActionEvent e) {
							exitAS(0);
						}
					});
			fileMenu.add(mi);
		}

		return jMenuBar;
	}

	/**
	 * Closes the GUI of the stand-alone {@link AtlasStylerGUI}
	 * 
	 * @param exitCode
	 *            Code to pass to System.exit()
	 */
	protected void exitAS(int exitCode) {

		/*
		 * Ask the use to save the changed SLDs
		 */
		List<StyledLayerInterface<?>> styledObjects = getMapManager()
				.getStyledObjects();
		for (StyledLayerInterface<?> styledObj : styledObjects) {
			if (styledObj instanceof StyledFS) {
				StyledFS stedFS = (StyledFS) styledObj;

				askToSaveSld(stedFS);

			} else {
				JOptionPane
						.showMessageDialog(
								AtlasStylerGUI.this,
								"The type of "
										+ styledObj.getTitle()
										+ " is not recognized. That must be a bug. Sorry."); // i8n
				continue;
			}

		}
		dispose();

		for (DataStore ds : openDatastores.values()) {
			if (ds != null)
				ds.dispose();
		}
		openDatastores.clear();

		/*
		 * Unregister from JNLP SingleInstanceService
		 */
		JNLPUtil.registerAsSingleInstance(AtlasStylerGUI.this, false);

		// Store the Logging Level in ~/.AtlasStyler/atlasStyler.properties
		{
			ASProps.set(Keys.logLevel, Logger.getRootLogger().getLevel()
					.toString());
			ASProps.store();
		}

		System.exit(exitCode);

	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		JPanel jContentPane = new JPanel(new BorderLayout());
		jContentPane.add(getStylerMapView(), BorderLayout.CENTER);
		jContentPane.add(getJToolBar(), BorderLayout.NORTH);
		return jContentPane;
	}

	StylerMapView getStylerMapView() {
		if (stylerMapView == null) {
			stylerMapView = new StylerMapView(this);
			stylerMapView.getMapManager().addMapLayerListListener(
					new MapLayerListListener() {

						public void layerAdded(MapLayerListEvent event) {
						}

						public void layerChanged(MapLayerListEvent event) {
						}

						public void layerMoved(MapLayerListEvent event) {
						}

						public void layerRemoved(MapLayerListEvent event) {
							String id = event.getLayer().getTitle();
							LOGGER.debug("layer id=" + id + " removed");

							askToSaveSld(stledObjCache.get(id));
							stledObjCache.remove(id);

							// Dispose the datastore when removing the layer
							DataStore openDs = openDatastores.get(id);
							if (openDs != null)
								openDs.dispose();
						}

					});
		}
		return stylerMapView;
	}

	/**
	 * A very basic dialog to asking the user to store the .SLD file for a
	 * layer.
	 * 
	 * @param styledFS
	 *            The {@link StyledFS} that links to the geodata.
	 */
	protected void askToSaveSld(StyledFS styledFS) {

		File sldFile = styledFS.getSldFile();

		// Only ask to save if the style has changed
		if (!StylingUtil.isStyleDifferent(styledFS.getStyle(), sldFile))
			return;

		if (sldFile == null) {
			// There is no .SLD set so far. Lets ask the user where to save it.

			styledFS.setSldFile(sldFile);
			// TODO no SLD, ask the user!
			throw new RuntimeException(
					"Not yet implemented. Please contact the authors.");
		}

		if (!AVSwingUtil.askYesNo(
				AtlasStylerGUI.this,
				AtlasStyler.R("AtlasStylerGUI.saveToSLDFileQuestion",
						styledFS.getTitle(), IOUtil.escapePath(sldFile))))
			return;

		Style style = styledFS.getStyle();

		if (style == null) {
			AVSwingUtil.showMessageDialog(AtlasStylerGUI.this, "The Style for "
					+ styledFS.getTitle()
					+ " is null. That must be a bug. Not saving.");
			return;
		}

		new AtlasStylerSaveLayerToSLDAction(this, styledFS)
				.actionPerformed(null);

	}

	/**
	 * This method initializes jToolBar
	 * 
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getJToolBar() {
		JToolBar jToolBar = new JToolBar();

		jToolBar.setFloatable(false);

		AbstractAction importWiazrdAction = new AbstractAction(
				AtlasStyler.R("MenuBar.FileMenu.ImportWizard")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				ImportWizard.showWizard(AtlasStylerGUI.this,
						AtlasStylerGUI.this);
			}
		};
		importWiazrdAction.putValue(Action.LONG_DESCRIPTION,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK, true));
		jToolBar.add(importWiazrdAction);

		jToolBar.add(getJTButtonShowXML());
		jToolBar.add(getJTButtonExportAsSLD());

		JButton optionsButton = new JButton(new AbstractAction(
				AtlasStyler.R("Options.ButtonLabel")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				new ASOptionsDialog(AtlasStylerGUI.this, AtlasStylerGUI.this);
			}
		});

		jToolBar.add(optionsButton);
		return jToolBar;
	}

	public JToggleButton getJTButtonShowXML() {
		final JToggleButton jButtonShowXML = new JToggleButton(
				AtlasStyler.R("AtlasStylerGUI.toolbarButton.show_xml"));
		jButtonShowXML.setSelected(false);

		jButtonShowXML.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean anAus = !xmlCodeFrame.isVisible();
				xmlCodeFrame.setVisible(anAus);
				// jButtonShowXML.setSelected(anAus);
				xmlCodeFrame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						jButtonShowXML.setSelected(false);
					}
				});
			}

		});
		return jButtonShowXML;
	}

	/**
	 * A button to export all layers in form of one SLD XML file (starting a
	 * StyledLayerDescriptor tag)
	 */
	private JButton getJTButtonExportAsSLD() {
		final JButton jButtonExportAsSLD = new JButton(
				AtlasStyler.R("AtlasStylerGUI.toolbarButton.exportSLD"));

		jButtonExportAsSLD.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					File saveDir = null;
					String lastPath = ASProps
							.get(ASProps.Keys.lastExportDirectory);
					if (lastPath != null)
						saveDir = new File(lastPath);

					SaveSLDXMLFileChooser chooser = new SaveSLDXMLFileChooser(
							saveDir);

					int result = chooser.showSaveDialog(AtlasStylerGUI.this);

					File exportSLDFile = chooser.getSelectedFile();
					if (exportSLDFile == null
							|| result != JFileChooser.APPROVE_OPTION
							|| exportSLDFile.isDirectory())
						return;

					ASProps.set(ASProps.Keys.lastExportDirectory, exportSLDFile
							.getParentFile().getAbsolutePath());

					// If the file exists, the user will be asked about
					// overwriting it
					if (exportSLDFile.exists()) {
						if (!AVSwingUtil.askOKCancel(
								AtlasStylerGUI.this,
								AtlasStyler
										.R("AtlasStylerGUI.saveStyledLayerDescFileDialogTitle.OverwriteQuestion",
												exportSLDFile.getName())))
							return;
					}

					// Evt. wird .sld angehangen.
					String filenamelc = exportSLDFile.getName().toLowerCase();
					if (!filenamelc.endsWith(".sld")
							&& !filenamelc.endsWith(".xml")
							&& !filenamelc.endsWith(".se")) {
						exportSLDFile = new File(exportSLDFile.getParentFile(),
								filenamelc + ".sld");
						JOptionPane.showMessageDialog(AtlasStylerGUI.this,
								AsSwingUtil.R(
										"AtlasStylerGUI.FileNameChangeTo.msg",
										exportSLDFile.getName()));
					}

					// // Export
					// Charset charset = Charset.forName("UTF-8");
					StyledLayerDescriptor sldTag = CommonFactoryFinder
							.getStyleFactory(null)
							.createStyledLayerDescriptor();

					/*******
					 * Export aller Styles als ein SLD Tag
					 */
					for (StyledLayerInterface smi : getMapManager()
							.getStyledObjects()) {

						try {
							if (!(smi instanceof StyledFS)) {
								LOGGER.info("Ein Layer aus dem MapContextManagerInterface ist kein StyledFeatureSourceInterface. Es wird ignoriert: "
										+ smi.getTitle());
								continue;
							}

							StyledFeatureSourceInterface featureGeoObj = (StyledFeatureSourceInterface) smi;
							String name = featureGeoObj.getGeoObject()
									.getSchema().getTypeName();
							NamedLayer namedLayer = CommonFactoryFinder
									.getStyleFactory(null).createNamedLayer();
							namedLayer.setName(name);
							namedLayer.addStyle(smi.getStyle());

							sldTag.addStyledLayer(namedLayer);
						} catch (Exception e1) {
							ExceptionDialog.show(AtlasStylerGUI.this, e1);
						}
					}

					Writer w = null;
					final SLDTransformer aTransformer = new SLDTransformer();
					// if (charset != null) {
					// aTransformer.setEncoding(charset);
					// }
					aTransformer.setIndentation(2);
					final String xml = aTransformer.transform(sldTag);
					w = new FileWriter(exportSLDFile);
					w.write(xml);
					w.close();
				} catch (IOException e1) {
					ExceptionDialog.show(AtlasStylerGUI.this, e1);
				} catch (TransformerException e2) {
					ExceptionDialog.show(AtlasStylerGUI.this, e2);
				}

			}

		});

		/**
		 * Activate the button when more than one layer is available
		 */
		jButtonExportAsSLD.setEnabled(getMapManager().getMapContext()
				.getLayerCount() > 0);
		getMapManager().addMapLayerListListener(new MapLayerListAdapter() {

			@Override
			public void layerRemoved(MapLayerListEvent arg0) {
				jButtonExportAsSLD.setEnabled(getMapManager().getMapContext()
						.getLayerCount() > 0);
			}

			@Override
			public void layerAdded(MapLayerListEvent arg0) {
				jButtonExportAsSLD.setEnabled(true);
			}
		});

		return jButtonExportAsSLD;
	}

	/**
	 * Adds a layer to the map context to style it.
	 */
	public boolean addLayer(StyledFS styledFS) {

		if (styledFS.getStyle() == null) {
			// Einen default Style erstellen
			{
				// AVSwingUtil.showMessageDialog(this, AtlasStyler.R(
				// "AtlasStylerGUI.importVectorLayerNoSLD", styledFS
				// .getSldFile().getName()));
				styledFS.setStyle(AsSwingUtil.createDefaultStyle(styledFS));
			}
		}

		stledObjCache.put(styledFS.getId(), styledFS);
		return getMapManager().addStyledLayer(styledFS);
	}

	public MapContextManagerInterface getMapManager() {
		return getStylerMapView().getMapManager();
	}

	/**
	 * AtlasStylerGUI main method.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) throws IOException {
		// System.out.println("Classpath: "+System.getProperty("java.class.path"));

		// Set the locale for running the application
		try {
			if (!ASProps.get(Keys.language, "system")
					.equalsIgnoreCase("system")) {

				Translation.setDefaultLocale(new Locale(ASProps.get(
						Keys.language, "en")));

			}
		} catch (Exception e) {
			LOGGER.error(
					"Could not set locale to " + ASProps.get(Keys.language), e);
			ExceptionDialog.show(e);
		}

		// Setup the AXIS order property TODO in settings GUI!
		Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
				ASProps.get(Keys.FORCE_LONGITUDE_FIRST_AXIS_ORDER, true));

		/**
		 * Check for addFix arguments. If found, the GUI will not start,
		 */
		boolean addedIndexes = checkFixIndexCreation(args);

		if (!addedIndexes) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					AtlasStylerGUI asg = new AtlasStylerGUI();

					// TODO Switch to Apache Commons-CLI
					if (args.length != 0) {
						for (String param : args) {

							if ((param.startsWith("\""))
									&& (param.endsWith("\""))) {
								param = param.substring(1, param.length() - 1);
							}

							File fileParamter = new File(param);

							if (fileParamter.exists()) {
								LOGGER.info("Opening command line argument "
										+ param + " as file.");
								ImportWizardResultProducer_FILE.addShapeLayer(
										asg, fileParamter, asg);
							}
						}

					}
					asg.setVisible(true);
				}
			});

		} else {
			LOGGER.info("Not starting GUI because command line parameter addFix was passed");
			System.out
					.println("Not starting GUI because command line parameter addFix was passed");
		}

	}

	/**
	 * This method is evaluating command line arguments to look for
	 * "addFix=FILE" paramters. If at least one "addFix=" parameter is passen,
	 * the method returns <code>true</code> and the application is expected not
	 * to start a GUI.
	 */
	private static boolean checkFixIndexCreation(String[] args) {
		boolean fixParamFound = false;

		if (args.length != 0) {
			for (String param : args) {

				// Allows "addFix=/Eigene Dateien/Meine Dateien" etc...
				if ((param.startsWith("\"")) && (param.endsWith("\""))) {
					param = param.substring(1, param.length() - 1);
				}

				// Here we just handle addFix paramters (for now)
				if (!param.startsWith("addFix="))
					continue;

				param = param.substring(7);

				// Remember that a paramter was interpreted and the mnethod has
				// to return true
				fixParamFound = true;

				final File fileParamter = new File(param);

				if (!fileParamter.exists()) {
					LOGGER.warn("Not understanding " + param
							+ " as file to add an index.");
					continue;
				}

				if (!fileParamter.canWrite()) {
					LOGGER.warn("Can't write to " + param
							+ " due to missing permissions, skipping.");
					continue;
				}

				// Starting the .fix creation
				final URL shpUrl = DataUtilities.fileToURL(fileParamter);
				try {
					final IndexedShapefileDataStore ds = new IndexedShapefileDataStore(
							shpUrl);
					try {
						ds.createSpatialIndex();
					} catch (final IOException e) {
						LOGGER.warn("", e);
					} finally {
						ds.dispose();
					}

					LOGGER.info("Added .fix to " + fileParamter);

				} catch (final MalformedURLException e) {
					LOGGER.warn("Error adding a .fix to " + param, e);
				}
			}
		}

		return fixParamFound;
	}

	/**
	 * Called via SingleInstanceListener / SingleInstanceService. Does nothing
	 * except requesting the focus for the given application.
	 */
	@Override
	public void newActivation(String[] arg0) {
		LOGGER.info("A second instance of AtlasViewer has been started.. The single instance if requesting focus now...");
		requestFocus();
		toFront();
	}

	public void addOpenDatastore(String layerId, DataStore ds) {
		openDatastores.put(layerId, ds);
	}

}
