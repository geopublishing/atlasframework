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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster_Reader;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVector;
import org.geopublishing.atlasViewer.http.AtlasProtocol;
import org.geopublishing.atlasViewer.http.Webserver;
import org.geopublishing.atlasViewer.map.Map;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.map.MapLayer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.schmitzm.geotools.MapContextManagerInterface;
import de.schmitzm.geotools.data.amd.AttributeMetadataImpl;
import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.gui.SelectableXMapPane;
import de.schmitzm.geotools.gui.XMapPane;
import de.schmitzm.geotools.map.event.FeatureSelectedEvent;
import de.schmitzm.geotools.map.event.GridCoverageValueSelectedEvent;
import de.schmitzm.geotools.map.event.ObjectSelectionEvent;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.i18n.I18NUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.io.IOUtil;
import de.schmitzm.swing.SpringUtilities;
import de.schmitzm.swing.SwingUtil;

/**
 * This {@link JPanel} shows information about a mouse-click into the
 * {@link SelectableXMapPane}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class ClickInfoPanel extends JPanel {
	private static final long serialVersionUID = -8908251402235590979L;

	private static final JPanel EMPTYPANEL = new JPanel();

	static private final Logger LOGGER = Logger.getLogger(ClickInfoPanel.class);

	private static final JLabel CRS_LABEL = new JLabel(
			GpCoreUtil.R("ClickInfoPanel.CRS.label") + ":",
			SwingConstants.TRAILING);

	final static String CRSValueToolTip = GpCoreUtil
			.R("ClickInfoPanel.CRS.tooltip");

	private static final JLabel X_LABEL = new JLabel("X" + ":",
			SwingConstants.TRAILING); // i8n

	private static final JLabel Y_LABEL = new JLabel("Y" + ":",
			SwingConstants.TRAILING); // i8n

	private static final Border LINE_BORDER = BorderFactory
			.createLineBorder(Color.lightGray);

	/** the default "normal" font * */
	private static final Font DEFAULT_FONT = new JLabel().getFont().deriveFont(
			Font.PLAIN);

	/** The {@link MapLayer} that the clicked object comes from * */
	private MapLayer layer;

	private final MapContextManagerInterface layerManager;

	private JComponent infopanel;

	final static String cordinatesMsg = GpCoreUtil
			.R("ClickInfoPanel.BorderFactory.CreateTitledBorder.cordinates");

	protected int countVisibleAttribsWithContent = 0;

	private final Component owner;

	protected AtlasConfig atlasConfig;

	public ClickInfoPanel(final MapContextManagerInterface layerManager,
			final Component owner, final AtlasConfig atlasConfig) {
		super(new SpringLayout());

		this.layerManager = layerManager;
		this.owner = owner;
		this.atlasConfig = atlasConfig;

	}

	/**
	 * Creates a {@link JPanel} which displays information about the
	 * Raster-Pixel that was clicked with the mouse
	 * 
	 * @param gridValSelectionEvent
	 * @return A title framed {@link JPanel} with Raster-Information
	 */
	private JPanel getRasterInfoPanel(
			final GridCoverageValueSelectedEvent gridValSelectionEvent) {

		resetRowCounter();

		final JPanel panel = new JPanel(new SpringLayout());
		panel.setOpaque(true);

		final MapLayer layer = gridValSelectionEvent.getSourceLayer();
		final Object layerSourceObject = FeatureUtil
				.getWrappedGeoObject((FeatureSource<SimpleFeatureType, SimpleFeature>) layer
						.getFeatureSource());

		final JTextArea valueLabel = new JTextArea();
		valueLabel.setEditable(false);
		valueLabel.setAlignmentX(1f);

		if ((layerSourceObject instanceof GridCoverage2D)
				|| (layerSourceObject instanceof AbstractGridCoverage2DReader)) {

			try {
				// **************************************************************
				// Do some transformations from the Map CRS to the Layer CRS
				// **************************************************************
				final double[] gcValue;
				gcValue = gridValSelectionEvent.getSelectionResult();

				// **************************************************************
				// Different behavior, if there is only one band...
				// **************************************************************
				if (gcValue.length > 1) {

					// **********************************************************
					// List all bands
					// **********************************************************
					int i = 0;
					for (final double value : gcValue) {
						i++;
						JLabel defLabel = new JLabel(GpCoreUtil.R("ClickInfoPanel.label_for_band", i));
						JLabel key = defLabel;
						
						StyledLayerInterface<?> styledObjectFor = layerManager.getStyledObjectFor(layer);
						
						if (styledObjectFor instanceof DpLayerRaster_Reader) {
							DpLayerRaster_Reader dplrr = (DpLayerRaster_Reader) styledObjectFor;
							key = new JLabel(dplrr.getBandNames()[i-1].toString()+":");
							if(dplrr.getBandNames()[i-1].toString().isEmpty()){
								key = defLabel;
							}
						}
						key.setFont(DEFAULT_FONT);
						panel.add(key);

						final String doubleFomatted = NumberFormat
								.getNumberInstance(Locale.getDefault()).format(
										value);

						final JLabel valueLabeln = new JLabel();
						valueLabeln.setText(doubleFomatted);
						key.setLabelFor(valueLabeln);
						panel.add(valueLabeln);
					}
				} else {
					// **********************************************************
					// Trying to get a Label for this raster-value
					// **********************************************************

					final Double value = gcValue[0];
					final String doubleFomatted = NumberFormat
							.getNumberInstance(Locale.getDefault()).format(
									value);
					final RasterLegendData legendMetaData = layerManager
							.getLegendMetaData(layer);
					if (legendMetaData != null) {

						// Lookup any entry in the legend table for this value
						final Translation translation = legendMetaData
								.get(value);
						if (translation != null && !translation.isEmpty()) {
							// **************************************************
							// If we find a label for this value, show it
							// **************************************************
							valueLabel.setText(translation.toString() + " ("
									+ doubleFomatted + ")");

						} else {
							// **************************************************
							// , otherwise just show the value
							// **************************************************
							valueLabel.setText(doubleFomatted);
						}
					} else {
						// ******************************************************
						// fallback.. no RasterLegendData found
						// ******************************************************
						valueLabel.setText(doubleFomatted);
					}

					final JLabel key = new JLabel(
							GpCoreUtil
									.R("ClickInfoPanel.label_for_raster_value"));
					key.setFont(DEFAULT_FONT);
					key.setLabelFor(valueLabel);
					panel.add(key);
					panel.add(valueLabel);
					countVisibleAttribsWithContent++;

				}

				// Lay out the panel.
				SpringUtilities.makeCompactGrid(panel, gcValue.length + 1, 2, // rows
						// ,
						// cols
						5, 0, // initX, initY
						5, getYPad()); // xPad, yPad

			} catch (final Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
		}

		// **********************************************************************
		// Set a Border and store the title
		// **********************************************************************
		// titleString = layerManager.getTitleFor(layer);
		panel.setBorder(BorderFactory.createTitledBorder(LINE_BORDER,
				GpCoreUtil.R("ClickInfoPanel.titledBorder.bands")));

		return panel;
	}

	private JPanel getPositionInfoPanel(
			final ObjectSelectionEvent<?> objectSelectionEvent) {
		layer = objectSelectionEvent.getSourceLayer();
		final XMapPane mapPane = objectSelectionEvent.getSource();
		final String humanReadableCrsString = mapPane.getMapContext()
				.getCoordinateReferenceSystem().getName().getCode()
				.replace('_', ' ');

		final double x = objectSelectionEvent.getSelectionRange().getMaxX();
		final double y = objectSelectionEvent.getSelectionRange().getMaxY();

		final Point2D selectionPoint = new Point2D.Double(x, y);

		// **********************************************************************
		// Creating a panel that displays the CRS and the position within and
		// cache it
		// **********************************************************************
		JPanel positionPanel = new JPanel(new MigLayout("wrap 2"));
		{
			JTextArea xTextLabel = new JTextArea(NumberFormat
					.getNumberInstance(Locale.getDefault()).format(
							selectionPoint.getX()));
			xTextLabel.setEditable(false);

			JTextArea yTextLabel = new JTextArea(NumberFormat
					.getNumberInstance(Locale.getDefault()).format(
							selectionPoint.getY()));
			yTextLabel.setEditable(false);

			JLabel crsLabel = new JLabel(humanReadableCrsString);
			CRS_LABEL.setLabelFor(crsLabel);
			CRS_LABEL.setToolTipText(CRSValueToolTip);
			crsLabel.setToolTipText(CRSValueToolTip);

			positionPanel.add(CRS_LABEL);
			positionPanel.add(crsLabel);

			X_LABEL.setLabelFor(xTextLabel);
			positionPanel.add(X_LABEL);
			positionPanel.add(xTextLabel, "sgx");

			Y_LABEL.setLabelFor(yTextLabel);
			positionPanel.add(Y_LABEL);
			positionPanel.add(yTextLabel, "sgx");
		}

		positionPanel.setBorder(BorderFactory.createTitledBorder(LINE_BORDER,
				cordinatesMsg));

		return positionPanel;
	}

	/**
	 * Creates a {@link JPanel} which displays information about the first
	 * {@link SimpleFeature} of the given {@link FeatureCollection}
	 * 
	 * @param fc
	 * @return
	 */
	private JComponent getOneFeatureInfoPanel(
			final FeatureCollection<SimpleFeatureType, SimpleFeature> fc) {

		resetRowCounter();

		final JPanel panel = new JPanel(new SpringLayout());
		panel.setOpaque(true);

		SimpleFeature feature;

		final FeatureIterator<SimpleFeature> iterator = fc.features();
		try {
			if (iterator.hasNext()) {
				feature = iterator.next();
			} else {
				return new JLabel(GpCoreUtil.R("ClickInfoPanel.no_feature"));
			}
		} finally {
			fc.close(iterator);
		}

		JComponent valueComponent = new JLabel();

		// LOGGER.debug("LayerManager = " + layerManager);
		final List<AttributeMetadataImpl> visibleAttribs = layerManager
				.getVisibleAttribsFor(layer);

		// Sort by weight!
		ArrayList<AttributeMetadataImpl> sortedVisibleAtts = new ArrayList<AttributeMetadataImpl>(
				visibleAttribs);
		Collections.sort(sortedVisibleAtts);

		for (final AttributeMetadataImpl col : sortedVisibleAtts) {

			try {
				/**
				 * This try/catch intercepts exceptions, if the DBF has been
				 * changed after import. If a column is referenced, which
				 * doesn't exist any more, just ignore the column and not cancel
				 * the whole info-window.
				 * 
				 * 
				 * Exception in thread "AWT-EventQueue-0"
				 * java.lang.ArrayIndexOutOfBoundsException: 60 at
				 * org.geotools.feature
				 * .DefaultFeature.getAttribute(DefaultFeature.java:207) at
				 * org.geopublishing
				 * .atlasViewer.swing.ClickInfoPanel.getOneFeatureInfoPanel(
				 * ClickInfoPanel.java:388) at
				 * org.geopublishing.atlasViewer.swing.ClickInfoPanel.
				 * setSelectionEvent(ClickInfoPanel.java:631) *
				 */

				final String attributeName = col.getTitle().toString();

				// final JTextArea key = new JTextArea();
				// key.setText("<html><p align='right'>"+attributeName +
				// ":</p></html>");
				// key.setWrapStyleWord(true);
				// key.setEditable(false);
				final JLabel key = new JLabel("<html>" + attributeName
						+ ":</html>", SwingConstants.TRAILING);
				key.setFont(DEFAULT_FONT);

				// Get the value of the attribute. The filter method replaces
				// NODATA values with null.
				final Object attribute = col.fiterNodata(feature
						.getAttribute(col.getName()));

				if (attribute != null) {

					String valueString = attribute.toString();

					try {
						// **********************************************************
						// Trying to interpret the value as a Number
						// **********************************************************
						valueString = NumberFormat.getNumberInstance(
								new Locale(Translation.getActiveLang()))
								.format(attribute);
						// **********************************************************
						// Interpreting it as as a String => JTextlabel
						// **********************************************************
						final JTextArea ta = new JTextArea(valueString
								+ col.getUnit());
						ta.setEditable(false);
						valueComponent = ta;

					} catch (final Exception e) {

						try {
							valueString = attribute.toString();

							// **********************************************************
							// Trying to interpretate the value as a Atlas-Link
							// to
							// an
							// Image
							// img://filename
							// **********************************************************
							if (AtlasProtocol.IMAGE.test(valueString)) {
								final String imageFilename = AtlasProtocol.IMAGE
										.cutOff(valueString);

								final DpLayerVector dpe = (DpLayerVector) layerManager
										.getStyledObjectFor(layer);
								try {
									final URL imgURL = IOUtil
											.extendURL(
													IOUtil.getParentUrl(AVSwingUtil
															.getUrl(dpe, owner)),
													imageFilename);

									// TODO OPEN FILE IGNORE CASE fuer
									// windows/linux
									// inkompatibilitaeten

									final BufferedImage img = ImageIO
											.read(imgURL);
									final ImageIcon imageIcon = new ImageIcon(
											img);
									valueComponent = new JLabel(imageIcon);
									valueComponent
											.setAlignmentX(JLabel.LEFT_ALIGNMENT);
								} catch (final Exception e1) {
									LOGGER.warn(e1);

									// If we are running from the GP, show an
									// error. If we are running without GP, just
									// hide the line!
									if (AtlasViewerGUI.isRunning()) {
										valueComponent = null;
									} else {
										valueComponent = new JLabel("<html>"
												+ imageFilename + "<br>"
												+ e1.getLocalizedMessage());
									}
								}
							}

							else
							/**********************************************************
							 * Trying to interpret the value as an
							 * pdf://filename.pdf link. <br/>
							 * The PDF is expected to be stored relative to the
							 * vector-data file, e.g. the ESRI Shapefile.
							 **********************************************************/
							if (AtlasProtocol.PDF.test(valueString)) {

								/**
								 * Determine the URL of this layer geo-files
								 */
								final StyledLayerInterface<?> styledObj = layerManager
										.getStyledObjectFor(layer);
								if (styledObj instanceof DpLayerVector) {
									final String pdfFilename = AtlasProtocol.PDF
											.cutOff(valueString);

									final DpLayerVector dpe = (DpLayerVector) styledObj;
									final URL pdfURL = IOUtil.extendURL(IOUtil
											.getParentUrl(AVSwingUtil.getUrl(
													dpe, owner)), pdfFilename);

									valueComponent = new JButton(
											new AbstractAction(pdfFilename,
													Icons.ICON_PDF_SMALL) {

												@Override
												public void actionPerformed(
														final ActionEvent e) {

													AtlasProtocol.PDF
															.performPDF(
																	ClickInfoPanel.this,
																	pdfURL,
																	pdfFilename);

												}

											});
								}
							} else

							/**
							 * Is this a map:// link?
							 */
							if (AtlasProtocol.MAP.test(valueString)
									&& (atlasConfig != null)) {

								final String mapId = AtlasProtocol.MAP
										.cutOff(valueString);
								Map map = atlasConfig.getMapPool().get(mapId);

								if (map == null) {
									if (AtlasViewerGUI.isRunning())
										valueComponent = null;
									else
										valueComponent = new JLabel(
												"ERROR: mapID "
														+ mapId
														+ " can't be found in the MapPool!"); // i8n

								} else {
									valueComponent = new JButton(
											new AbstractAction(map.getTitle()
													.toString(),
													Icons.ICON_MAP_SMALL) {

												@Override
												public void actionPerformed(
														final ActionEvent e) {

													AtlasProtocol.MAP
															.performMap(
																	ClickInfoPanel.this,
																	mapId,
																	atlasConfig);

												};
											});

									if (!I18NUtil.isEmpty(map.getDesc()
											.toString()))
										valueComponent.setToolTipText(map
												.getDesc().toString());
								}

							} // internal map links

							else
							// **********************************************************
							// Trying to interpret the value as a link to a HTML
							// file
							// **********************************************************
							if (AtlasProtocol.HTML.test(valueString)) {

								/**
								 * Determine the URL of this layer geo-files
								 */
								final StyledLayerInterface<?> styledObj = layerManager
										.getStyledObjectFor(layer);
								if (styledObj instanceof DpLayerVector) {
									String targetPath = AtlasProtocol.HTML
											.cutOff(valueString);

									final DpLayerVector dpe = (DpLayerVector) styledObj;
									final URL url;

									/**
									 * Is it an internal HTML document, or an
									 * external one?
									 */
									if (targetPath.toLowerCase().startsWith(
											"http://")) {
										url = new URL(targetPath);
									} else {
										url = IOUtil.extendURL(IOUtil
												.getParentUrl(AVSwingUtil
														.getUrl(dpe, owner)),
												targetPath);

									}

									/**
									 * Used as the button's label and the
									 * internal browsers window title.
									 */
									final String title = targetPath;

									valueComponent = new JButton(
											new AbstractAction("",
													Icons.ICON_HTML_SMALL) {

												@Override
												public void actionPerformed(
														final ActionEvent e) {

													AtlasProtocol.HTML
															.performHtml(
																	ClickInfoPanel.this,
																	url, title,
																	atlasConfig);

												};
											});
									valueComponent
											.setToolTipText(GpCoreUtil
													.R("ClickInfoPanel.OpenHTMLButton.TT",
															title));
								}
							}

							else
							// **********************************************************
							// Trying to interpret the value as a link to a HTML
							// file
							// **********************************************************
							if (AtlasProtocol.BROWSER.test(valueString)) {
								final String targetPath = AtlasProtocol.BROWSER
										.cutOff(valueString);

								/**
								 * Use the filename without the postfix as the
								 * buttons name. TODO Use something that the
								 * user may translate.
								 */
								final String documentTitle = IOUtil
										.getBaseFileName(new File(targetPath));

								final DpLayerVector dpe = (DpLayerVector) layerManager
										.getStyledObjectFor(layer);

								final URL url;
								/**
								 * Here we have to decide, whether it point's to
								 * an atlas-internal HTML file, or to a online
								 * URL.
								 */

								if (targetPath.startsWith("http://")) {
									/**
									 * We open an external URL like:
									 * http://www.bahn.de
									 */
									url = new URL(targetPath);
								} else {
									/**
									 * We open an internal HTML document and end
									 * up with something like: http://localhost
									 * :8282/ad/asas/info.html
									 */
									url = new URL("http", "localhost",
											Webserver.PORT, ("/ad/data/"
													+ dpe.getDataDirname()
													+ "/" + targetPath));
								}

								valueComponent = new JButton(
										new AbstractAction("",
												Icons.ICON_HTML_SMALL) {

											@Override
											public void actionPerformed(
													final ActionEvent e) {

												AtlasProtocol.BROWSER
														.performBrowser(
																ClickInfoPanel.this,
																url);

											};
										});
								valueComponent
										.setToolTipText(GpCoreUtil
												.R("ClickInfoPanel.OpenHTMLinBrowserButton.TT",
														documentTitle));
							}

							else
							// **********************************************************
							// Interpret it as as a JTextLabel
							// **********************************************************
							{
								final JTextArea ta = new JTextArea(valueString
										+ " " + col.getUnit());
								ta.setEditable(false);
								valueComponent = ta;
							}
						} catch (Exception male) {
							LOGGER.error("Presenting " + valueString
									+ " in the ClickInfoPanel:", male);

							// If we are running from the GP, show an
							// error. If we are running without GP, just
							// hide the line!
							if (AtlasViewerGUI.isRunning()) {
								valueComponent = null;
							} else {
								valueComponent = new JLabel("<html>"
										+ valueString + "<br>"
										+ male.getLocalizedMessage());
							}
						}
					}

					// **************************************************************
					// If the value field is an empty string, we do not show
					// anything.
					// **************************************************************
					if (valueComponent != null) {
						// key.setLabelFor(valueComponent);
						// **********************************************************
						// Setting the ToolTipTexts for the two Components
						// **********************************************************
						final Translation desc = col.getDesc();
						if (!I18NUtil.isEmpty(desc)) {
							valueComponent.setToolTipText(desc.toString());
							key.setToolTipText(desc.toString());
						}
						panel.add(key);
						panel.add(valueComponent);

						countVisibleAttribsWithContent++;
					}

				} // attribute != null

			} catch (final ArrayIndexOutOfBoundsException e) {
				LOGGER.warn("The info-tool failed to get a column's value.", e);
			}

		} // for

		// **********************************************************************
		// Lay out the panel, if we have any visible attributes
		// **********************************************************************
		if (panel.getComponentCount() > 0) {
			SpringUtilities.makeCompactGrid(panel,
					countVisibleAttribsWithContent, 2, // rows
					// ,
					// cols
					5, 0, // initX, initY
					5, getYPad()); // xPad, yPad

			final String msg = GpCoreUtil
					.R("ClickInfoPanel.BorderFactory.CreateTitledBorder.attributes");
			panel.setBorder(BorderFactory.createTitledBorder(LINE_BORDER, msg)); // i8ndone
		}

		return panel;
	}

	/**
	 * Not every visible attribute always contains a value! For the optimal
	 * layout we have to count them.
	 */
	private void resetRowCounter() {
		countVisibleAttribsWithContent = 0;
	}

	/**
	 * @return The appropriate vertical padding (for the {@link SpringLayout},
	 *         depending on the total number of visible attributes on the
	 *         screen.
	 */
	private int getYPad() {
		return countVisibleAttribsWithContent > 20 ? 1 : 3;
	}

	/**
	 * Tell the {@link ClickInfoPanel} about newly selected Objects
	 */
	public void setSelectionEvent(
			final ObjectSelectionEvent<?> objectSelectionEvent) {

		layer = objectSelectionEvent.getSourceLayer();

		JPanel positionPanel;
		// **********************************************************************
		// Decide what kind of object was selected
		// **********************************************************************
		if (objectSelectionEvent instanceof FeatureSelectedEvent) {
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = (FeatureCollection<SimpleFeatureType, SimpleFeature>) objectSelectionEvent
					.getSelectionResult();
			infopanel = getOneFeatureInfoPanel(features);
			positionPanel = getPositionInfoPanel(objectSelectionEvent);
		} else if (objectSelectionEvent instanceof GridCoverageValueSelectedEvent) {
			final GridCoverageValueSelectedEvent gridSelection = (GridCoverageValueSelectedEvent) objectSelectionEvent;
			infopanel = getRasterInfoPanel(gridSelection);
			positionPanel = getPositionInfoPanel(gridSelection);
		} else {
			infopanel = EMPTYPANEL;
			positionPanel = EMPTYPANEL;
		}
		removeAll();

		int countRows = 0;

		add(infopanel);
		countRows++;

		add(positionPanel);
		countRows++;

		SpringUtilities.makeCompactGrid(this, countRows, 1, 0, 0, 0, getYPad());
		//
		SwingUtil.getParentWindow(this).pack();
		SwingUtil.getParentWindow(this).pack();

	}
}
