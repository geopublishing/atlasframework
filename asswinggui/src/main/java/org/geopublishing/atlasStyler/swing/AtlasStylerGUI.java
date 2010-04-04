/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.swing;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.jnlp.SingleInstanceListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.geopublishing.atlasStyler.ASProps;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.ASProps.Keys;
import org.geopublishing.atlasStyler.swing.AtlasStylerSaveLayerToSLDAction;
import org.geopublishing.atlasStyler.swing.OpenDataFileChooser;
import org.geopublishing.atlasStyler.swing.SaveSLDXMLFileChooser;
import org.geopublishing.atlasStyler.swing.StylerMapView;
import org.geopublishing.atlasStyler.swing.XMLCodeFrame;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.AtlasSwingWorker;
import org.geopublishing.atlasViewer.swing.AtlasViewer;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;
import org.geopublishing.atlasViewer.swing.internal.AtlasTask;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.geotools.map.event.MapLayerListAdapter;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.geotools.MapContextManagerInterface;
import skrueger.geotools.StyledFS;
import skrueger.geotools.StyledFeatureSourceInterface;
import skrueger.geotools.StyledLayerInterface;

/**
 * This is the main GUI for the AtlasStyler standalone. It looks like a
 * condensed {@link AtlasViewer}, and its main purpose is to reach the "Style"
 * {@link MenuItem} in the tools menu.
 * 
 * @author Stefan A. Krüger
 * 
 */
public class AtlasStylerGUI extends JFrame implements SingleInstanceListener {
	final static private Logger LOGGER = ASUtil
			.createLogger(AtlasStylerGUI.class);

	private StylerMapView stylerMapView = null;

	private HashMap<String, StyledFS> stledObjCache = new HashMap<String, StyledFS>();

	final private XMLCodeFrame xmlCodeFrame = new XMLCodeFrame(this,
			getStylerMapView().getMapManager());

	/**
	 * This is the default constructor
	 */
	public AtlasStylerGUI() {
		// Setting up the logger from a XML configuration file
		URL log4jXmlUrl = ClassLoader.getSystemResource("as_log4j.xml");
		if (log4jXmlUrl == null) {
			log4jXmlUrl = AtlasStylerGUI.class.getResource("as_log4j.xml");
		}
		if (log4jXmlUrl != null) {
			DOMConfigurator.configure(log4jXmlUrl);
		} else {
			System.err.println("as_log4j.xml not found... No logging :-(");
		}

		// Atlas Viewer is starting
		LOGGER.info("Starting AtlasStyler.. " + AVSwingUtil.getVersionInfo());

		AtlasTask<Object> startupTask = new AtlasTask<Object>(null, AtlasStyler
				.R("AtlasStylerGUI.processMsg.starting_atlas_stlyer")) {

			@Override
			protected Object doInBackground() throws Exception {
				AVSwingUtil.logLGPLCopyright(LOGGER);
				/*
				 * Register as a JNLP SingleInstance
				 */
				JNLPUtil.registerAsSingleInstance(AtlasStylerGUI.this, true);

				// AVSwingUtil.cacheEPSG(null);

				return null;
			}

			@Override
			protected void done() {
				super.done();
			}

		};
		startupTask.execute();

		initialize();

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
		 * Setting a nice AtlasStyler icon
		 */
		List<Image> icons = new ArrayList<Image>(2);

		ClassLoader cl = ASUtil.class.getClassLoader();
		final String imagePackageName = "icons/";
		icons.add(new ImageIcon(cl.getResource(imagePackageName
				+ "as_icon16.png")).getImage());
		icons.add(new ImageIcon(cl.getResource(imagePackageName
				+ "as_icon32.png")).getImage());
		icons.add(new ImageIcon(cl.getResource(imagePackageName
				+ "as_icon64.png")).getImage());
		setIconImages(icons);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {

		this.setSize(800, 600);
		this.setContentPane(getJContentPane());
		String AtlasStyler_MainWindowTitle = "AtlasStyler "
				+ AVSwingUtil.getVersionInfoShort();
		this.setTitle(AtlasStyler_MainWindowTitle);

	}

	/**
	 * Closes the GUI of the stand-alone {@link AtlasStylerGUI}
	 * 
	 * @param exitCode
	 *            Code to pass to System.exit()
	 */
	protected void exitAS(int exitCode) {

		/*
		 * What is that?
		 */
		if (getMapManager() == null)
			return;

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

		/*
		 * Unregister from JNLP SingleInstanceService
		 */
		JNLPUtil.registerAsSingleInstance(AtlasStylerGUI.this, false);

		System.exit(exitCode);

	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		JPanel jContentPane = new JPanel();
		jContentPane.setLayout(new BorderLayout());
		jContentPane.add(getStylerMapView(), BorderLayout.CENTER);
		jContentPane.add(getJToolBar(), BorderLayout.NORTH);
		return jContentPane;
	}

	/**
	 * This method initializes stylerMapView
	 * 
	 * @return skrueger.sld.gui.StylerMapView
	 */
	private StylerMapView getStylerMapView() {
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

		if (!AVSwingUtil.askYesNo(AtlasStylerGUI.this, AtlasStyler.R(
				"AtlasStylerGUI.saveToSLDFileQuestion", styledFS.getTitle(),
				sldFile.getAbsolutePath())))
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
		jToolBar.add(getJButtonAddLayer());
		jToolBar.add(getJButtonAntiAliasing());
		jToolBar.add(getJTButtonShowXML());
		jToolBar.add(getJTButtonExportAsSLD());

		JButton optionsButton = new JButton(new AbstractAction(AtlasStyler
				.R("Options.ButtonLabel")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				new ASOptionsDialog(AtlasStylerGUI.this);
			}
		});

		jToolBar.add(optionsButton);
		return jToolBar;
	}

	private JToggleButton getJButtonAntiAliasing() {
		final JToggleButton jToggleButtonAntiAliasing = new JToggleButton(
				AtlasStyler
						.R("AtlasStylerGUI.toolbarButton.toggle_antialiasing"));
		jToggleButtonAntiAliasing.setSelected(ASProps.getInt(
				ASProps.Keys.antialiasingMaps, 1) != 1);

		jToggleButtonAntiAliasing.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						boolean b = !jToggleButtonAntiAliasing.isSelected();
						ASProps.set(ASProps.Keys.antialiasingMaps, b ? "1"
								: "0");

						getStylerMapView().getMapPane().setAntiAliasing(b);
						getStylerMapView().getMapPane().repaint();
					}
				});
			}
		});
		return jToggleButtonAntiAliasing;
	}

	public JToggleButton getJTButtonShowXML() {
		final JToggleButton jButtonShowXML = new JToggleButton(AtlasStyler
				.R("AtlasStylerGUI.toolbarButton.show_xml"));
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
		final JButton jButtonExportAsSLD = new JButton(AtlasStyler
				.R("AtlasStylerGUI.toolbarButton.exportSLD"));

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
						if (!AVSwingUtil
								.askOKCancel(
										AtlasStylerGUI.this,
										AtlasStyler
												.R(
														"AtlasStylerGUI.saveStyledLayerDescFileDialogTitle.OverwriteQuestion",
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
								"Filename changed to "
										+ exportSLDFile.getName()); // i8n
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
								LOGGER
										.info("Ein Layer aus dem MapContextManagerInterface ist kein StyledFeatureSourceInterface. Es wird ignoriert: "
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
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonAddLayer() {
		JButton jButtonAddLayer = new JButton(AtlasStyler
				.R("AtlasStylerGUI.toolbarButton.open_vector"));

		jButtonAddLayer.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				File getLastOpenDir = null;
				String lastFilePath = ASProps
						.get(ASProps.Keys.lastImportDirectory);
				if (lastFilePath != null)
					getLastOpenDir = new File(lastFilePath);

				OpenDataFileChooser chooser = new OpenDataFileChooser(
						getLastOpenDir);

				// properties
				chooser.setVisible(true);
				int result = chooser.showOpenDialog(AtlasStylerGUI.this);

				final File selectedFile = chooser.getSelectedFile();

				if (selectedFile == null
						|| result != JFileChooser.APPROVE_OPTION)
					return;

				ASProps.set(ASProps.Keys.lastImportDirectory, selectedFile
						.getAbsolutePath());
				
				AtlasStatusDialog statusDialog = new AtlasStatusDialog(
						AtlasStylerGUI.this);

				AtlasSwingWorker<Void> openFileWorker = new AtlasSwingWorker<Void>(
						statusDialog) {

					@Override
					protected Void doInBackground() throws IOException,
							InterruptedException {
						addShapeLayer(selectedFile);
						return null;
					}

				};
				try {
					openFileWorker.executeModal();
				} catch (CancellationException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				}


			}
		});
		return jButtonAddLayer;
	}

	/**
	 * Basic method to add a Shapefile to the legend/map
	 * 
	 * @param openFile
	 *            the file to open. May be a ZIP that contains a Shape.
	 */
	public void addShapeLayer(File openFile) {
		try {
			URL urlToShape;

			if (openFile.getName().toLowerCase().endsWith("zip")) {
				urlToShape = GeoImportUtil.uncompressShapeZip(openFile);
			} else {
				urlToShape = DataUtilities.fileToURL(openFile);
			}

			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put("url", urlToShape);
			ShapefileDataStore dataStore = (ShapefileDataStore) DataStoreFinder
					.getDataStore(params);

			// test for any .prj file
			CoordinateReferenceSystem prjCRS = null;
			File prjFile = IOUtil.changeFileExt(openFile, "prj");
			if (prjFile.exists()) {
				try {
					prjCRS = GeoImportUtil.readProjectionFile(prjFile);
				} catch (Exception e) {
					prjCRS = null;
					if (!AVSwingUtil
							.askOKCancel(
									this,
									AtlasStyler
											.R(
													"AtlasStylerGUI.importShapePrjBrokenWillCreateDefaultFor",
													e.getMessage(), prjFile
															.getName(),
													GeoImportUtil
															.getDefaultCRS()
															.getName())))
						return;
				}
			} else {
				if (!AVSwingUtil
						.askOKCancel(
								this,
								AtlasStyler
										.R(
												"AtlasStylerGUI.importShapePrjNotFoundWillCreateDefaultFor",
												prjFile.getName(),
												GeoImportUtil.getDefaultCRS()
														.getName())))
					return;
			}

			if (prjCRS == null) {
				dataStore.forceSchemaCRS(GeoImportUtil.getDefaultCRS());
			}

			// After optionally forcing the CRS we get the FS
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSourceTry = dataStore
					.getFeatureSource(dataStore.getTypeNames()[0]);

			StyledFS styledFS;

			File sldFile = IOUtil.changeFileExt(openFile, "sld");

			// Handle if .SLD exists instead
			if (!sldFile.exists()
					&& IOUtil.changeFileExt(openFile, "SLD").exists()) {
				AVSwingUtil
						.showMessageDialog(this,
								"Hey you #&%\"§* windows noob. Change the file ending to .sld and try again!");
				return;
			}

			styledFS = new StyledFS(featureSourceTry, sldFile);

			// styledFS.getCrs();

			if (styledFS.getStyle() == null) {
				// Einen default Style erstellen
				{
					AVSwingUtil.showMessageDialog(this, AtlasStyler.R(
							"AtlasStylerGUI.importVectorLayerNoSLD", styledFS
									.getSldFile()));
					styledFS.setStyle(ASUtil.createDefaultStyle(styledFS));
				}
			}

			addLayer(styledFS);

		} catch (Exception e2) {
			LOGGER.info(e2);
			ExceptionDialog.show(AtlasStylerGUI.this, e2);
			return;
		}
	}

	public void addLayer(StyledFS styledFS) {
		/**
		 * If the Style is null, we have to create some style
		 */
		if (styledFS.getStyle() == null) {
			final Style createStyle = ASUtil.SB.createStyle();
			styledFS.setStyle(createStyle);
		}
		stledObjCache.put(styledFS.getId(), styledFS);
		getMapManager().addStyledLayer(styledFS);
	}

	public MapContextManagerInterface getMapManager() {
		return getStylerMapView().getMapManager();
	}

	public static void main(final String[] args) throws IOException {

		try {

			if (!ASProps.get(Keys.language, "system")
					.equalsIgnoreCase("system")) {
				Locale.setDefault(new Locale(ASProps.get(Keys.language, "en")));
			}
		} catch (Exception e) {
			LOGGER.error("Could not set locale to "
					+ ASProps.get(Keys.language), e);
		}

		// Locale.setDefault(new Locale("en"));

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AtlasStylerGUI asg = new AtlasStylerGUI();

				if (args.length != 0) {
					for (String param : args) {

						if ((param.startsWith("\"")) && (param.endsWith("\""))) {
							param = param.substring(1, param.length() - 1);
						}

						System.out.println("Opening as File " + param);
						asg.addShapeLayer(new File(param));
					}

				}
				asg.setVisible(true);
			}
		});
	}

	/**
	 * Called via SingleInstanceListener / SingleInstanceService. Does nothing
	 * except requesting the focus for the given application.
	 */
	@Override
	public void newActivation(String[] arg0) {
		LOGGER
				.info("A second instance of AtlasViewer has been started.. The single instance if requesting focus now...");
		requestFocus();
		toFront();
	}

}
