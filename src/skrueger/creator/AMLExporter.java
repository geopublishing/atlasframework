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

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import schmitzm.geotools.io.GeoExportUtil;
import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.jfree.chart.style.ChartStyleUtil;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.jfree.feature.style.FeatureChartUtil;
import skrueger.AttributeMetadata;
import skrueger.RasterLegendData;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasCancelException;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.dp.Group;
import skrueger.atlas.dp.layer.DpLayer;
import skrueger.atlas.dp.layer.DpLayerRaster;
import skrueger.atlas.dp.layer.DpLayerRasterPyramid;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.dp.layer.LayerStyle;
import skrueger.atlas.dp.media.DpMediaPDF;
import skrueger.atlas.dp.media.DpMediaVideo;
import skrueger.atlas.exceptions.AtlasException;
import skrueger.atlas.exceptions.AtlasFatalException;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.atlas.http.Webserver;
import skrueger.atlas.internal.AMLUtil;
import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.atlas.map.MapRef;
import skrueger.creator.export.AtlasExportException;
import skrueger.i8n.Translation;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This class can export the Atlas-objects to AtlasMarkupLanguage (AtlasML)
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class AMLExporter {
	private final String CHARTSTYLE_FILEENDING = ".chart";
	final private Logger LOGGER = Logger.getLogger(AMLExporter.class);

	private AtlasStatusDialog statusWindow = null;

	private final AtlasConfigEditable ace;
	private boolean atlasXmlhasBeenBackupped = false;

	void info(final String msg) {
		statusWindow.setDescription(msg);
	}

	void info(final Translation msg) {
		info(msg.toString());
	}

	public AMLExporter(final AtlasConfigEditable ace) {
		this.ace = ace;
	}

	/**
	 * Saves the {@link AtlasConfigEditable} to projdir/atlas.xml
	 * 
	 * @return true if no exceptions where thrown.
	 * @throws Exception
	 */
	public boolean saveAtlasConfigEditable(final AtlasStatusDialog statusWindow)
			throws Exception {
		this.statusWindow = statusWindow;

		// Prepare the output file
		final File atlasXml = new File(ace.getAd(), AtlasConfig.ATLAS_XML_FILENAME);

		try {
			// ****************************************************************************
			// Trying to make a bakup
			// ****************************************************************************
			atlasXmlhasBeenBackupped = backupAtlasXML();

			/**
			 * Saves the default CRS of that atlas to a file called
			 * {@link AtlasConfig#DEFAULTCRS_FILENAME} in <code>ad</code>
			 * folder. Prefers to write the EPSG code of the CRS. Does not
			 * change the file it the contents are the same (SVN friendly).
			 */
			GeoExportUtil.writeProjectionFilePrefereEPSG(GeoImportUtil
					.getDefaultCRS(), new File(ace.getAd(),
					AtlasConfig.DEFAULTCRS_FILENAME));
			//
			// final String msg = "Saving Atlas " + ace.getTitle().toString()
			// + " to " + ace.getAd().getAbsolutePath() + "/atlas.xml.";
			// info(msg);

			// Prepare the DOM document for writing
			final Source source = new DOMSource(exportAtlasConfig());

			if (!atlasXml.exists() && atlasXmlhasBeenBackupped) {
				// LOGGER.info("atlas.xml is freshly created");
				atlasXml.createNewFile();
			}

			copyAtlasMLSchemaFile();

			// ****************************************************************************
			// Create the XML
			// ****************************************************************************
			final Result result = new StreamResult(new OutputStreamWriter(
					new FileOutputStream(atlasXml), "utf-8"));

			// with indenting to make it human-readable
			final TransformerFactory tf = TransformerFactory.newInstance();

			// TODO Ging mit xerces, geht nicht mehr mit xalan
			// tf.setAttribute("indent-number", new Integer(2));

			final Transformer xformer = tf.newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
			xformer.setOutputProperty(
					"{http://www.wikisquare.de/AtlasML.xsd}indent-amount", "4");
			xformer.setOutputProperty(
					"{http://xml.apache.org/xalan}indent-amount", "2");

			// Write the DOM document to the file
			xformer.transform(source, result);

			// LOGGER.debug(" saving AtlasConfig... finished.");
			return true;
		} catch (Exception e) {

			if (atlasXmlhasBeenBackupped)
				try {
					LOGGER.warn("copying "+AtlasConfig.ATLAS_XML_FILENAME+".bak to "+AtlasConfig.ATLAS_XML_FILENAME);
					AVUtil.copyFile(LOGGER, new File(ace.getAd(),
							AtlasConfig.ATLAS_XML_FILENAME+".bak"), atlasXml, false);
				} catch (final IOException ioEx) {
					LOGGER.error("error copying "+AtlasConfig.ATLAS_XML_FILENAME+".bak back.", ioEx);
					throw (ioEx);
					// statusWindow.exceptionOccurred(ioEx);
				}

			if (e instanceof AtlasCancelException)
				return false;
			else
				throw e;
		}

	}

	/**
	 * @return A XML {@link Document} that fully represents the
	 *         {@link AtlasConfig} of this atlas
	 * @throws AtlasException
	 * @author Stefan Alfons Krüger
	 * @throws IOException
	 */
	private final Document exportAtlasConfig() throws Exception {

		// String msg = "Converting Atlas '" + ace.getTitle() +
		// "' to AtlasML...";
		// info(msg);

		// Create a DOM builder and parse the fragment
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		Document document = null;
		// try {
		document = factory.newDocumentBuilder().newDocument();
		// } catch (final ParserConfigurationException e) {

		// msg = "Saving to AtlasML failed!";
		// info(msg);
		// throw new AtlasFatalException(msg, e);
		// }

		// XML root element
		final Element atlas = document.createElementNS(AMLUtil.AMLURI, "atlas");

		// Linking this XML to the AtlasML Schema
		final Attr namespaces = document.createAttributeNS(
				"http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
		namespaces
				.setValue("http://www.wikisquare.de/AtlasML http://localhost:"
						+ Webserver.DEFAULTPORT
						+ "/skrueger/atlas/resource/AtlasML.xsd");
		atlas.setAttributeNode(namespaces);

		// Storing the version this atlas.xml is being created with inside the
		// atlas.xml
		atlas.setAttribute(AMLUtil.ATT_majVersion, String.valueOf(AVUtil
				.getVersionMaj()));
		atlas.setAttribute(AMLUtil.ATT_minVersion, String.valueOf(AVUtil
				.getVersionMin()));
		atlas.setAttribute(AMLUtil.ATT_buildVersion, String.valueOf(AVUtil
				.getVersionBuild()));

		// <aml:name, desc, creator, copyright
		atlas.appendChild(exportTranslation(document, "name", ace.getTitle()));
		atlas.appendChild(exportTranslation(document, "desc", ace.getDesc()));
		atlas.appendChild(exportTranslation(document, "creator", ace
				.getCreator()));
		atlas.appendChild(exportTranslation(document, "copyright", ace
				.getCopyright()));

		// <aml:atlasversion>
		final Element atlasversion = document.createElementNS(AMLUtil.AMLURI,
				"atlasversion");
		atlasversion.appendChild(document.createTextNode(ace.getAtlasversion()
				.toString()));
		atlas.appendChild(atlasversion);

		// <aml:supportedLanguages>
		// Loops over List of supported Languagecodes
		final Element supportedLanguages = document.createElementNS(
				AMLUtil.AMLURI, "supportedLanguages");
		for (final String langcode : ace.getLanguages()) {
			final Element language = document.createElementNS(AMLUtil.AMLURI,
					"language");
			language.setAttribute("lang", langcode);
			supportedLanguages.appendChild(language);
		}
		atlas.appendChild(supportedLanguages);

		// Loop over all data pool entries and add them to the AML Document
		for (final DpEntry de : ace.getDataPool().values()) {
			Node exDpe = null;

			checkCancel();

			if (de instanceof DpLayer) {
				// Save the SLD for this layer
				final DpLayer dpl = (DpLayer) de;

				try {
					final Style style = dpl.getStyle();
					StylingUtil.saveStyleToSLD(style, DataUtilities
							.urlToFile(DataUtilities.changeUrlExt(dpl
									.getUrl(statusWindow), "sld"))); // TODO
																		// TODO
				} catch (final Exception e) {
					LOGGER.error("Could not transform Style for " + dpl, e);
					statusWindow.exceptionOccurred(e);
				}
			}

			if (de instanceof DpLayerVectorFeatureSource) {
				exDpe = exportDatapoolLayerVector(document,
						(DpLayerVectorFeatureSource) de);

			} else if (de instanceof DpLayerRaster) {
				exDpe = exportDatapoolLayerRaster(document, (DpLayerRaster) de);

			} else if (de instanceof DpLayerRasterPyramid) {
				exDpe = exportDatapoolLayerRasterPyramid(document,
						(DpLayerRasterPyramid) de);

			} else if (de instanceof DpMediaVideo) {
				exDpe = exportDatapoolMediaVideo(document, (DpMediaVideo) de);

			} else if (de instanceof DpMediaPDF) {
				exDpe = exportDatapoolMediaPdf(document, (DpMediaPDF) de);

			}
			atlas.appendChild(exDpe);

		}

		// The <aml:maps> tag
		atlas.appendChild(exportMapPool(document));

		// The <aml:group> tag
		atlas.appendChild(exportGroup(document, ace.getFirstGroup()));

		document.appendChild(atlas);
		// LOGGER.debug("AtlasConfig converted to JDOM Document");
		return document;
	}

	private void checkCancel() throws AtlasCancelException {
		if (statusWindow.isCanceled())
			throw new AtlasCancelException();
	}

	private Node exportDatapoolMediaPdf(final Document document,
			final DpMediaPDF dpe) {
		// LOGGER.debug("exportDatapoolMediaPDF " + dpe + " to AML");
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				"pdfMedia");
		element.setAttribute("id", dpe.getId());
		element.setAttribute("exportable", dpe.isExportable().toString());

		// Creating a aml:name tag...
		element
				.appendChild(exportTranslation(document, "name", dpe.getTitle()));

		// Creating aml:desc tag
		element.appendChild(exportTranslation(document, "desc", dpe.getDesc()));

		// Creating optinal aml:keywords tag
		if (!dpe.getKeywords().isEmpty())
			element.appendChild(exportTranslation(document, "keywords", dpe
					.getKeywords()));

		// Creating a aml:dataDirname tag...
		final Element datadirname = document.createElementNS(AMLUtil.AMLURI,
				"dataDirname");
		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
		element.appendChild(datadirname);

		// Creating a aml:filename tag...
		final Element filename = document.createElementNS(AMLUtil.AMLURI,
				"filename");
		filename.appendChild(document.createTextNode(dpe.getFilename()));
		element.appendChild(filename);

		return element;
	}

	/**
	 * Exports all the {@link Map}s that are defined in {@link MapPool}
	 * 
	 * @param document
	 *            {@link Document} to create the element for
	 * @param mapPool
	 *            {@link MapPool}
	 * @throws AtlasExportException
	 * @throws DOMException
	 * @throws AtlasCancelException
	 */
	private Node exportMapPool(final Document document) throws DOMException,
			AtlasExportException, AtlasCancelException {
		final MapPool mapPool = ace.getMapPool();

		final Element element = document.createElementNS(AMLUtil.AMLURI,
				"mapPool");

		if (mapPool.getStartMapID() != null
				&& mapPool.get(mapPool.getStartMapID()) != null) {
			element.setAttribute("startMap", mapPool.getStartMapID());
		}

		// maps MUST contain at least one map
		final Collection<Map> maps = mapPool.values();

		for (final Map map : maps) {
			checkCancel();
			element.appendChild(exportMap(document, map));
		}
		return element;
	}

	/**
	 * Exports a {@link Map} to AtlasML
	 * 
	 * @param document
	 * @param map
	 *            The {@link Map} to export
	 * @return
	 * @throws AtlasExportException
	 */
	private Node exportMap(final Document document, final Map map)
			throws AtlasExportException {
		// info("map: " + map.getTitle()); // i8n

		final Element element = document.createElementNS(AMLUtil.AMLURI, "map");
		element.setAttribute("id", map.getId());

		// Save the ratio of the left and the right components of the mapview
		final Double preferredRatio = map.getLeftRightRatio();
		if (preferredRatio != null && preferredRatio > 0) {
			element.setAttribute("leftRightRatio", preferredRatio.toString());
		}

		// Creating a aml:name tag...
		element
				.appendChild(exportTranslation(document, "name", map.getTitle()));

		// Creating aml:desc tag
		element.appendChild(exportTranslation(document, "desc", map.getDesc()));

		// Creating optinal aml:keywords tag
		if (!map.getKeywords().isEmpty())
			element.appendChild(exportTranslation(document, "keywords", map
					.getKeywords()));

		// Creating optional startViewEnvelope tag ...
		if (map.getDefaultMapArea() != null) {
			final Element startViewEnvelope = document.createElementNS(
					AMLUtil.AMLURI, "startViewEnvelope");
			final Envelope area = map.getDefaultMapArea();
			startViewEnvelope.appendChild(document.createTextNode(area
					.getMinX()
					+ ","
					+ area.getMinY()
					+ " "
					+ area.getMaxX()
					+ ","
					+ area.getMaxY()));
			element.appendChild(startViewEnvelope);
		}

		// Creating optional maxMapExtend tag ...
		if (map.getMaxExtend() != null) {
			final Element maxExtend = document.createElementNS(AMLUtil.AMLURI,
					"maxExtend");
			final Envelope area = map.getMaxExtend();
			maxExtend.appendChild(document.createTextNode(area.getMinX() + ","
					+ area.getMinY() + " " + area.getMaxX() + ","
					+ area.getMaxY()));
			element.appendChild(maxExtend);
		}

		// Are the vert. and hor. GridPanels visible in this map?
		element.setAttribute("scaleVisible", Boolean.valueOf(
				map.isScaleVisible()).toString());

		// Are the vert. and hor. GridPanels visible in this map?
		element.setAttribute("gridPanelVisible", Boolean.valueOf(
				map.isGridPanelVisible()).toString());
		// Which formatter to use for the map grid?
		element.setAttribute("gridPanelFormatter", map.getGridPanelFormatter()
				.getId());

		final File outputFile = new File(
				new File(ace.getHtmlDir(), map.getId()),
				Map.GRIDPANEL_CRS_FILENAME);
		try {
			GeoExportUtil.writeProjectionFilePrefereEPSG(map.getGridPanelCRS(),
					outputFile);
		} catch (final IOException e1) {
			throw new AtlasExportException("Failed to save CRS: "
					+ map.getGridPanelCRS() + " to " + outputFile, e1);
		}

		// Creating aml:datapoolRef tags for the layers and media of this map
		for (final DpRef dpr : map.getLayers()) {
			element.appendChild(exportDatapoolRef(document, dpr, map));
		}

		for (final DpRef dpr : map.getMedia()) {
			element.appendChild(exportDatapoolRef(document, dpr));
		}

		/***********************************************************************
		 * Exporting map.getAdditionalStyles and the selected style ID s
		 */
		for (final String layerID : map.getAdditionalStyles().keySet()) {
			final List<String> styles = map.getAdditionalStyles().get(layerID);

			if (styles.size() == 0)
				continue;

			final Element additionalStyles = document.createElementNS(
					AMLUtil.AMLURI, "additionalStyles");
			additionalStyles.setAttribute("layerID", layerID);
			final String selectedStyleID = map.getSelectedStyleID(layerID);
			if (selectedStyleID != null)
				additionalStyles.setAttribute("selectedStyleID",
						selectedStyleID);

			for (final String styleID : styles) {
				final Element styleidElement = document.createElementNS(
						AMLUtil.AMLURI, "styleid");
				styleidElement.appendChild(document.createTextNode(styleID));
				additionalStyles.appendChild(styleidElement);
			}

			element.appendChild(additionalStyles);
		}

		/***********************************************************************
		 * Exporting map.getAvailableCharts
		 */
		for (final String layerID : map.getAvailableCharts().keySet()) {
			if (!map.getAc().getDataPool().containsKey(layerID)) {
				// The corresponing DPE has been deleted?!
				continue;
			}
				
			final List<String> chartIDs = map.getAvailableChartIDsFor(layerID);

			if (chartIDs.size() == 0)
				continue;

			final Element availableCharts = document.createElementNS(
					AMLUtil.AMLURI, "availableCharts");
			availableCharts.setAttribute("layerID", layerID);

			for (final String chartID : chartIDs) {
				final Element styleidElement = document.createElementNS(
						AMLUtil.AMLURI, "chartID");
				styleidElement.appendChild(document.createTextNode(chartID));
				availableCharts.appendChild(styleidElement);
			}

			element.appendChild(availableCharts);
		}

		return element;
	}

	/**
	 * Creates an < aml:desc > JDOM {@link Node} for the given
	 * {@link Translation}
	 */
	private final Element exportTranslation(final Document document,
			final String tagname, final Translation translation) {
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				tagname);

		// Creating a sequence of <aml:translation> tags, minOccurs=1
		if (translation == null) {
			statusWindow
					.warningOccurred(
							this.getClass().getSimpleName(),
							null,
							"No translation given for "
									+ tagname
									+ "\nAtlasMarkupLanguage will probably not be valid.");
		} else {
			if (translation.size() == 0) {
				for (final String code : ace.getLanguages()) {
					translation.put(code, "");
				}

			}
			for (final String key : translation.keySet()) {
				final Element descTranslation = document.createElementNS(
						AMLUtil.AMLURI, "translation");
				descTranslation.setAttribute("lang", key);
				String string = translation.get(key);
				if (string == null)
					string = "";
				descTranslation.appendChild(document.createTextNode(string));
				element.appendChild(descTranslation);
			}
		}
		return element;
	}

	private final Element exportDatapoolMediaVideo(final Document document,
			final DpMediaVideo dpe) {
		// LOGGER.debug("exportDatapoolMediaVideo " + dpe + " to AML");
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				"videoMedia");
		element.setAttribute("id", dpe.getId());
		element.setAttribute("exportable", dpe.isExportable().toString());

		// info(dpe.getTitle());

		// Creating a aml:name tag...
		element
				.appendChild(exportTranslation(document, "name", dpe.getTitle()));

		// Creating aml:desc tag
		element.appendChild(exportTranslation(document, "desc", dpe.getDesc()));

		// Creating optinal aml:keywords tag
		if (!dpe.getKeywords().isEmpty())
			element.appendChild(exportTranslation(document, "keywords", dpe
					.getKeywords()));

		// Creating a aml:dataDirname tag...
		final Element datadirname = document.createElementNS(AMLUtil.AMLURI,
				"dataDirname");
		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
		element.appendChild(datadirname);

		// Creating a aml:filename tag...
		final Element filename = document.createElementNS(AMLUtil.AMLURI,
				"filename");
		filename.appendChild(document.createTextNode(dpe.getFilename()));
		element.appendChild(filename);

		return element;
	}

	/**
	 * Exports the DatapoolEntry to an AtlasML (XML) document branch
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * @throws IOException
	 *             Thrown if e.g. saving the .cpg fails
	 */
	private final Element exportDatapoolLayerVector(final Document document,
			final DpLayerVectorFeatureSource dpe) throws IOException {

		// info(dpe.getTitle()); // i8n

		if (dpe.isBroken()) {
			LOGGER.info("Trying to save a broken layer..." + dpe);
		}

		/*
		 * Saving the Charset to a .cpg file
		 */
		GpUtil.saveCpg(dpe);

		// Creating a aml:rasterLayer tag...
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				"vectorLayer");
		element.setAttribute("id", dpe.getId());
		element.setAttribute("exportable", dpe.isExportable().toString());

		/***********************************************************************
		 * The "showStylerInLegend" attribute is optional and defaults to true
		 ***********************************************************************/
		if (dpe.isStylerInLegend()) {
			element.setAttribute("showStylerInLegend", "true");
		} else {
			element.setAttribute("showStylerInLegend", "false");
		}

		/***********************************************************************
		 * The "showTableInLegend" attribute is optional and defaults to false
		 ***********************************************************************/
		if (dpe.isTableVisibleInLegend()) {
			element.setAttribute("showTableInLegend", "true");
		} else {
			element.setAttribute("showTableInLegend", "false");
		}

		/***********************************************************************
		 * The "filterInLegend" attribute is optional and defaults to false
		 ***********************************************************************/
		if (dpe.isFilterInLegend()) {
			element.setAttribute("showFilterInLegend", "true");
		} else {
			element.setAttribute("showFilterInLegend", "false");
		}

		// Creating a aml:name tag...
		element
				.appendChild(exportTranslation(document, "name", dpe.getTitle()));

		// Creating aml:desc tag
		element.appendChild(exportTranslation(document, "desc", dpe.getDesc()));

		// Creating optinal aml:keywords tag
		if (!dpe.getKeywords().isEmpty())
			element.appendChild(exportTranslation(document, "keywords", dpe
					.getKeywords()));

		// Creating a aml:dataDirname tag...
		final Element datadirname = document.createElementNS(AMLUtil.AMLURI,
				"dataDirname");
		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
		element.appendChild(datadirname);

		// Creating a aml:filename tag...
		final Element filename = document.createElementNS(AMLUtil.AMLURI,
				"filename");
		filename.appendChild(document.createTextNode(dpe.getFilename()));
		element.appendChild(filename);

		for (final AttributeMetadata attrib : dpe.getAttributeMetaDataMap()
				.values()) {
			final Element att = exportAttributeMetadata(dpe, document, attrib);
			if (att != null)
				element.appendChild(att);
		}

		/**
		 * Exporting the additional and optional LayerStyles
		 */
		for (final LayerStyle ls : dpe.getLayerStyles()) {
			element.appendChild(exportLayerStyle(document, ls));
		}

		/**
		 * This parameter is optional and is only created if needed.
		 */
		final Filter filter = dpe.getFilter();
		if (filter != Filter.INCLUDE) {
			// Creating a aml:filename tag...
			final Element filterRuleElement = document.createElementNS(
					AMLUtil.AMLURI, "filterRule");
			filterRuleElement.appendChild(document.createTextNode(CQL
					.toCQL(filter)));
			element.appendChild(filterRuleElement);
		}

		/**
		 * Exporting the list of charts for this layer if any exist. This has to
		 * be called, AFTER the attribute meta data has been exported. (When
		 * parsing the XML, we need the NODATA values first)
		 */
		exportChartStyleDescriptions(document, dpe, element);

		return element;

	}

	/**
	 * Exports the list of charts for this layer. Any old chart style files are
	 * first deleted.
	 */
	private void exportChartStyleDescriptions(final Document document,
			final DpLayerVectorFeatureSource dpe, final Element element) {

		final AtlasConfigEditable ace = (AtlasConfigEditable) dpe.getAtlasConfig();

		final File chartsFolder = new File(ace.getFileFor(dpe).getParentFile(),
				"charts");
		chartsFolder.mkdirs();
		/*
		 * Delete all .cs file before saving the charts that actually exist
		 */
		{

			final String[] oldChartFilenames = chartsFolder
					.list(new FilenameFilter() {

						@Override
						public boolean accept(final File dir, final String name) {
							if (name.endsWith(CHARTSTYLE_FILEENDING))
								return true;
							return false;
						}

					});
			for (final String oldChartFilename : oldChartFilenames) {
				final File chartSTyeFile = new File(chartsFolder,
						oldChartFilename);
				if (!chartSTyeFile.delete())
					throw new IllegalArgumentException(
							"Unable to delte the old chart description file "
									+ chartSTyeFile.getAbsolutePath());
			}
		}

		/* Iterate over all charts and create .chart files */
		for (final FeatureChartStyle chartStyle : dpe.getCharts()) {

			final String csFilename = chartStyle.getID()
					+ CHARTSTYLE_FILEENDING;

			final File chartFile = new File(chartsFolder, csFilename);

			try {
				/*
				 * Write the Chart to XML
				 */
				if (chartStyle instanceof FeatureChartStyle) {
					FeatureChartUtil.FEATURE_CHART_STYLE_FACTORY
							.writeStyleToFile((FeatureChartStyle) chartStyle,
									"chartStyle", chartFile);
				} else {
					ChartStyleUtil.CHART_STYLE_FACTORY.writeStyleToFile(
							chartStyle, "chartStyle", chartFile);
				}
			} catch (final Exception e) {
				LOGGER.error("Error writing the ChartStyle to XML ", e);
			}

			// Create an aml:chart tag...
			final Element chartElement = document.createElementNS(
					AMLUtil.AMLURI, "chart");
			chartElement.setAttribute("filename", csFilename);
			element.appendChild(chartElement);
		}
	}

	/**
	 * Exports a <layerStyle> tag
	 * 
	 * @param ac
	 * @param document
	 * @param ls
	 *            {@link LayerStyle} to export
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	private Element exportLayerStyle(final Document document,
			final LayerStyle ls) {
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				"layerStyle");

		element.setAttribute("filename", ls.getFilename());
		element.setAttribute("id", ls.getFilename());

		element
				.appendChild(exportTranslation(document, "title", ls.getTitle()));

		element.appendChild(exportTranslation(document, "desc", ls.getDesc()));

		return element;
	}

	/**
	 * Exports one single {@link AttributeMetadata} to an aml:dataAttribute tag.
	 * 
	 * @return {@link org.w3c.dom.Element} that represent the XML tag
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * @param dpe
	 *            For backward compatibility we also write the <code>col</code>
	 *            attribute. The 'col' attribute has been abandoned since >= 1.3
	 */
	private Element exportAttributeMetadata(
			final DpLayerVectorFeatureSource dpe, final Document document,
			final AttributeMetadata attrib) {
		// Creating a aml:rasterLayer tag...
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				AMLUtil.TAG_attributeMetadata);

		element.setAttribute(AMLUtil.ATT_namespace, attrib.getName()
				.getNamespaceURI());
		element.setAttribute(AMLUtil.ATT_localname, attrib.getName()
				.getLocalPart());

		element.setAttribute(AMLUtil.ATT_weight,
				new Integer(attrib.getWeight()).toString());
		element.setAttribute(AMLUtil.ATT_functionA, new Double(attrib
				.getFunctionA()).toString());
		element.setAttribute(AMLUtil.ATT_functionX, new Double(attrib
				.getFunctionX()).toString());

		try { // TODO backward compatibility. remove in 1.4
			final SimpleFeatureType schema = dpe.getFeatureSource().getSchema();
			boolean found = false;
			for (int i = 0; i < schema.getAttributeCount(); i++) {
				if (schema.getAttributeDescriptors().get(i).getName().equals(
						attrib.getName())) {
					element.setAttribute("col", String.valueOf(i));
					found = true;
					break;
				}
			}
			if (!found) {
				LOGGER.warn("No column for attrib " + attrib.getLocalName()
						+ " found. Throwing the metadata away...");
				return null;
			}
		} catch (final Exception e) {
			statusWindow
					.exceptionOccurred(new AtlasException(
							"Could not determine the indexes of attribute names '"
									+ attrib.getLocalName()
									+ "' of layer '"
									+ dpe.getTitle()
									+ "'.\n This is used for backward compatibility with GP 1.2 only.",
							e));
		}

		element.setAttribute("visible", String.valueOf(attrib.isVisible()));

		if (attrib.getUnit() != null && !attrib.getUnit().isEmpty())
			element.setAttribute("unit", attrib.getUnit());

		// Creating a aml:name tag...
		element.appendChild(exportTranslation(document, "name", attrib
				.getTitle()));

		// Creating a aml:desc tag...
		element.appendChild(exportTranslation(document, "desc", attrib
				.getDesc()));

		// Storing the NODATA values
		for (Object nodatavalue : attrib.getNodataValues()) {
			Element ndValue = document.createElementNS(AMLUtil.AMLURI,
					AMLUtil.TAG_nodataValue);
			ndValue.setTextContent(nodatavalue.toString());
			element.appendChild(ndValue);
		}

		return element;
	}

	/**
	 * Exports the {@link DpLayerRaster} to a AtlasML (XML) Document branch
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	private final Element exportDatapoolLayerRaster(final Document document,
			final DpLayerRaster dpe) {
		// info(dpe.getTitle()); // i8n

		// Creating a aml:rasterLayer tag...
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				"rasterLayer");
		element.setAttribute("id", dpe.getId());
		element.setAttribute("exportable", dpe.isExportable().toString());

		// Creating a aml:name tag...
		element
				.appendChild(exportTranslation(document, "name", dpe.getTitle()));

		// Creating aml:desc tag
		element.appendChild(exportTranslation(document, "desc", dpe.getDesc()));

		// Creating optinal aml:keywords tag
		if (!dpe.getKeywords().isEmpty())
			element.appendChild(exportTranslation(document, "keywords", dpe
					.getKeywords()));

		// Creating a aml:dataDirname tag...
		final Element datadirname = document.createElementNS(AMLUtil.AMLURI,
				"dataDirname");
		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
		element.appendChild(datadirname);

		// Creating a aml:filename tag...
		final Element filename = document.createElementNS(AMLUtil.AMLURI,
				"filename");
		filename.appendChild(document.createTextNode(dpe.getFilename()));
		element.appendChild(filename);

		// Creating aml:rasterLegendData
		element.appendChild(exportRasterLegendData(document, dpe
				.getLegendMetaData()));

		return element;
	}

	/**
	 * Export a aml:rasterLegendData tag
	 * 
	 * @param document
	 * @param legendMetaData
	 * @return
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	private Node exportRasterLegendData(final Document document,
			final RasterLegendData legendMetaData) {
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				"rasterLegendData");
		element.setAttribute("paintGaps", legendMetaData.isPaintGaps()
				.toString());

		for (final Double key : legendMetaData.getSortedKeys()) {
			final Element item = document.createElementNS(AMLUtil.AMLURI,
					"rasterLegendItem");
			item.setAttribute("value", key.toString());
			item.appendChild(exportTranslation(document, "label",
					legendMetaData.get(key)));
			element.appendChild(item);
		}
		return element;
	}

	/**
	 * Exports the {@link DpLayerRasterPyramid} to a AtlasML (XML) Document
	 * branch
	 * 
	 * @author Stefan Alfons Krüger
	 */
	private final Element exportDatapoolLayerRasterPyramid(
			final Document document, final DpLayerRasterPyramid dpe) {
		// LOGGER.debug("exportDatapoolLayerRasterPyramid " + dpe + " to AML");

		// Creating a aml:rasterLayer tag...
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				"pyramidRasterLayer");
		element.setAttribute("id", dpe.getId());
		element.setAttribute("exportable", dpe.isExportable().toString());

		// Creating a aml:name tag...
		element
				.appendChild(exportTranslation(document, "name", dpe.getTitle()));

		// Creating aml:desc tag
		element.appendChild(exportTranslation(document, "desc", dpe.getDesc()));

		// Creating optinal aml:keywords tag
		if (!dpe.getKeywords().isEmpty())
			element.appendChild(exportTranslation(document, "keywords", dpe
					.getKeywords()));

		// Creating a aml:dataDirname tag...
		final Element datadirname = document.createElementNS(AMLUtil.AMLURI,
				"dataDirname");
		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
		element.appendChild(datadirname);

		// Creating a aml:filename tag... (.properties file)
		final Element filename = document.createElementNS(AMLUtil.AMLURI,
				"filename");
		filename.appendChild(document.createTextNode(dpe.getFilename()));
		element.appendChild(filename);

		// Creating aml:rasterLegendData
		element.appendChild(exportRasterLegendData(document, dpe
				.getLegendMetaData()));

		// Creating an optional aml:transparentColor tag...
		final Color color = dpe.getInputTransparentColor();
		if (color != null) {
			final Element transparentColor = document.createElementNS(
					AMLUtil.AMLURI, "transparentColor");
			final String colorStr = "RGB(" + color.getRed() + ","
					+ color.getGreen() + "," + color.getBlue() + ")";
			transparentColor.appendChild(document.createTextNode(colorStr));
			element.appendChild(transparentColor);
		}

		return element;
	}

	/**
	 * Create a tree of <aml:group> and <aml:datapoolRef>
	 * 
	 * @param group
	 *            The {@link Group} to start with
	 * @throws AtlasFatalException
	 */
	private final Element exportGroup(final Document document, final Group group)
			throws AtlasFatalException {
		// LOGGER.debug("exportGroup " + group + " to AML");
		final Element element = document.createElementNS(AMLUtil.AMLURI,
				"group");

		// Store whether this is marked as the Help menu in an optional
		// attribute
		if (group.isHelpMenu()) {
			final String isHelpString = Boolean.valueOf(group.isHelpMenu())
					.toString();
			element.setAttribute("isHelpMenu", isHelpString);
		}

		// Store whether this is marked as the File menu in an optional
		// attribute
		if (group.isFileMenu()) {
			final String isFileString = Boolean.valueOf(group.isFileMenu())
					.toString();
			element.setAttribute("isFileMenu", isFileString);
		}

		// Creating a aml:name tag...
		element.appendChild(exportTranslation(document, "name", group
				.getTitle()));

		// Creating aml:desc tag
		element
				.appendChild(exportTranslation(document, "desc", group
						.getDesc()));

		// Creating optional aml:keywords tag
		if (!group.getKeywords().isEmpty())
			element.appendChild(exportTranslation(document, "keywords", group
					.getKeywords()));

		final Enumeration<TreeNode> children = group.children();
		while (children.hasMoreElements()) {
			final TreeNode nextElement = children.nextElement();
			if (nextElement instanceof Group) {
				final Group subGroup = (Group) nextElement;
				element.appendChild(exportGroup(document, subGroup));

			} else if (nextElement instanceof DpRef) {
				final DpRef mref = (DpRef) nextElement;
				element.appendChild(exportDatapoolRef(document, mref));

			} else if (nextElement instanceof MapRef) {
				final MapRef mref = (MapRef) nextElement;
				final Element datapoolRef = document.createElementNS(
						AMLUtil.AMLURI, "mapRef");
				datapoolRef.setAttribute("id", mref.getTargetId());
				element.appendChild(datapoolRef);

			} else {
				throw new AtlasFatalException("Can't export Group " + group
						+ " because of an unknown TreeNode " + nextElement
						+ " of class " + nextElement.getClass().getSimpleName());
			}
		}
		return element;
	}

	/**
	 * Creates an aml:datapoolRef tag
	 * 
	 * @param ref
	 *            {@link DpRef}
	 * @return A node that can be inserted into XML
	 */
	private Element exportDatapoolRef(final Document document, final DpRef ref) {
		final Element datapoolRef = document.createElementNS(AMLUtil.AMLURI,
				"datapoolRef");
		datapoolRef.setAttribute("id", ref.getTargetId());

		return datapoolRef;
	}

	/**
	 * DataPoolRefs used inside a <code>aml:map</code> definition can have two
	 * more attributes which are saved inside a {@link java.util.Map} in the
	 * {@link Map}.
	 * 
	 * @param document
	 * @param dpr
	 * @param map
	 * @return A node that can be inserted into XML
	 */
	private Node exportDatapoolRef(final Document document, final DpRef dpr,
			final Map map) {
		final Element datapoolRef = exportDatapoolRef(document, dpr);

		{ // Add the optional hideInLegend attribute if it is set. false =
			// default
			final Boolean hideme = map.getHideInLegendMap().get(
					dpr.getTargetId());
			if (hideme != null && hideme == true) {
				datapoolRef.setAttribute("hideInLegend", "true");
			}
		}

		{ // Add the optional minimizeInLegend attribute if it is set. false =
			// default
			final Boolean minimizeMe = map.getMinimizedInLegendMap().get(
					dpr.getTargetId());
			if (minimizeMe != null && minimizeMe == true) {
				datapoolRef.setAttribute("minimizeInLegend", "true");
			}
		}

		{ // Add the optional hidden attribute if it is set. false =
			// default
			final Boolean hidden = map.getHiddenFor(dpr.getTargetId());
			if (hidden != null && hidden == true) {
				datapoolRef.setAttribute("hidden", "true");
			}
		}

		{ // Add the optional selectable attribute if it is set. true =
			// default
			final Boolean selectable = map.isSelectableFor(dpr.getTargetId());
			if (selectable != null && selectable == false) {
				datapoolRef.setAttribute("selectable", "false");
			}
		}

		return datapoolRef;
	}

	private void copyAtlasMLSchemaFile() {
		try {
			// Copy Schema AtlasML.xsd to projectDir
			LOGGER.debug("Copy Schema AtlasML.xsd into " + ace.getAd());

			URL resourceSchema = AtlasConfig.class
					.getResource("resource/AtlasML.xsd");
			LOGGER.debug("schemaURL = " + resourceSchema);
			if (resourceSchema == null) {
				LOGGER.debug("schemaURL == null, try the new way");
				final String location = "skrueger/atlas/resource/AtlasML.xsd";
				resourceSchema = ace.getResLoMan().getResourceAsUrl(
						location);
				// LOGGER.debug("schemaURL (new) = " + resourceSchema);
			}

			// File schemaFile = new File(resourceSchema.toURI());
			org.apache.commons.io.FileUtils.copyURLToFile(resourceSchema,
					new File(ace.getAd(), "AtlasML.xsd"));
		} catch (final Exception e) {
			LOGGER.debug(" Error while copying AtlasML.xsd... ignoring");
			// statusWindow.exceptionOccurred(new AtlasException(e));
		}

	}

	/**
	 * Keeps up to three three backups of the atlas.xml file. Returns true if
	 * atlas.xml existed.
	 * 
	 * @throws IOException
	 *             if files can't be created.
	 */
	private boolean backupAtlasXML() throws IOException {
		File atlasXml = new File(ace.getAd(), AtlasConfig.ATLAS_XML_FILENAME);
		File bak1 = new File(ace.getAd(), AtlasConfig.ATLAS_XML_FILENAME+".bak");
		File bak2 = new File(ace.getAd(), AtlasConfig.ATLAS_XML_FILENAME+".bak.bak");
		File bak3 = new File(ace.getAd(), AtlasConfig.ATLAS_XML_FILENAME+".bak.bak.bak");

		if (bak2.exists())
			AVUtil.copyFile(LOGGER, bak2, bak3, false);
		if (bak1.exists())
			AVUtil.copyFile(LOGGER, bak1, bak2, false);
		if (atlasXml.exists()) {
			AVUtil.copyFile(LOGGER, atlasXml, bak1, false);
			return true;
		}
		return false;
	}

}
