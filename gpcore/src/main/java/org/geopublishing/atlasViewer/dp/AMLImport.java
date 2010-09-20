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
package org.geopublishing.atlasViewer.dp;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRasterPyramid;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSourceShapefile;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.dp.media.DpMediaPDF;
import org.geopublishing.atlasViewer.dp.media.DpMediaVideo;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.exceptions.AtlasFatalException;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.atlasViewer.exceptions.AtlasRecoverableException;
import org.geopublishing.atlasViewer.internal.AMLUtil;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.map.MapRef;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geotools.feature.NameImpl;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jfree.util.Log;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import rachel.ResourceManager;
import rachel.loader.ResourceLoaderManager;
import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.gui.GridPanelFormatter;
import schmitzm.geotools.gui.ScalePanel.ScaleUnits;
import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.chart.style.ChartStyleUtil;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.jfree.feature.style.FeatureChartUtil;
import schmitzm.lang.LangUtil;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadataImpl;
import skrueger.RasterLegendData;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.geotools.io.GeoImportUtilURL;
import skrueger.i8n.Translation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class AMLImport {

	private static final Logger LOGGER = Logger.getLogger(AMLImport.class);

	private static AtlasStatusDialogInterface statusDialog;

	/**
	 * If set to <code>true</code> by {@link #checkUpgrades(Node)},
	 * {@link AMLImport} will try to convert the filters automatically OR throw
	 * them away if conversion not possible.
	 **/
	private static boolean upgradeFiltersToCQL = false;

	static void info(String msg) {
		if (statusDialog != null)
			statusDialog.setDescription(msg);
	}

	/**
	 * Report a warning to the GUI status dialog, if a status dialog is
	 * available. Otherwise logs the warning.
	 * 
	 * @param topic
	 * @param msg
	 */
	static void warn(String topic, String msg) {
		if (statusDialog != null)
			statusDialog.warningOccurred(topic, null, msg);
		else
			Log.warn(topic + ":" + msg);
	}

	/**
	 * Parses Atlas XML file and returns a DOM document. If validating is true,
	 * the contents is validated against the XSD specified in the file.
	 */
	public DocumentBuilder getDocumentBuilder(final boolean validating)
			throws SAXException, IOException, ParserConfigurationException {
		// Create a builder factory
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();

		factory.setNamespaceAware(true);
		final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
		final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
		factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

		factory.setValidating(false);

		// Create the builder and parse the file
		final DocumentBuilder documentBuilder = factory.newDocumentBuilder();

		documentBuilder.setErrorHandler(new ErrorHandler() {

			public void error(final SAXParseException exception)
					throws SAXException {
				LOGGER.error("ErrorHandler.error", exception);
				throw exception;
			}

			public void fatalError(final SAXParseException exception)
					throws SAXException {
				LOGGER.error("ErrorHandler.fataError", exception);
				throw exception;

			}

			public void warning(final SAXParseException exception)
					throws SAXException {
				LOGGER.warn("ErrorHandler.warning", exception);
			}

		});

		return documentBuilder;
	}

	/**
	 * Creates a {@link AtlasConfig} from AtlasML
	 * 
	 * The {@link ResourceManager} is responsible to deliver the
	 * <code>ad/atlas.xml</code> and has to be set up before!
	 * 
	 * @param statusDialog
	 * 
	 * @param atlasConfig
	 *            needs be have a {@link ResourceLoaderManager} set up, so that
	 *            atlas.xml can be found!
	 */
	public void parseAtlasConfig(AtlasStatusDialogInterface statusDialog,
			final AtlasConfig atlasConfig, boolean validate)
			throws IOException, AtlasException {

		AMLImport.statusDialog = statusDialog;

		final InputStream atlasXmlAsStream = atlasConfig
				.getResourceAsStream(AtlasConfig.ATLASDATA_DIRNAME + "/"
						+ AtlasConfig.ATLAS_XML_FILENAME);

		if (atlasXmlAsStream == null) {
			throw new AtlasFatalException(
					AVUtil.R("AmlImport.error.cant_find_atlas.xml"));
		}

		/**
		 * If validation is requested, we first check for the reachability of
		 * the AtlasML.xsd. The atlas.xml defines
		 * http://localhost:7272/skrueger/atlas/resource/AtlasML.xsd as the XSD
		 * location. But the port of the running Webserver might actually be
		 * different. So we first check whether the XSD can be found before the
		 * parser fails.
		 * http://localhost:7272/skrueger/atlas/resource/AtlasML.xsd
		 */

		try {
			// // if (validate)
			// // info(AtlasViewer.R("info.validating_atlas.xml"));
			// URL testSchemaUrl = new URL(
			// "http://localhost:7272/skrueger/atlas/resource/AtlasML.xsd");
			// try {
			// testSchemaUrl.openStream().close();
			// } catch (Exception e) {
			// LOGGER
			// .warn("Validation disabled because 'http://localhost:7272/skrueger/atlas/resource/AtlasML.xsd' can't be found. My Webserver is running on "
			// + Webserver.PORT);
			// validate = false;
			// }

			if (validate)
				LOGGER.debug("Since switching to Geotools 2.6 the Xerces libary is used for XML parsing and somehow the validation of atlas.xml doesn't work anymore. It is disabled.");
			validate = false;

			DocumentBuilder builder = getDocumentBuilder(validate);
			Document xml = builder.parse(atlasXmlAsStream);
			parseAtlasConfig(atlasConfig, xml);

		} catch (final SAXException ex) {
			LOGGER.error("Validation failed with a SAXException..");
			// TODO Sometimes we start the software when another webserver is
			// running and providing the Schema. But when we reach here, the
			// second webserver has been closed and the Schema is not available
			// anymore. Maybe we should just start a webserver here
			throw new AtlasImportException(ex);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new AtlasImportException(e);
		}

	}

	/**
	 * Parse the given atlas.gpa into an {@link AtlasConfig}. It will also
	 * 
	 * @param atlasConfigEditable
	 * @param atlasDir
	 *            a {@link File} pointing to either <code>atlas.gpa</code> or
	 *            the folder containing an <code>atlas.gpa</code>
	 */
	public AtlasConfigEditable parseAtlasConfig(
			AtlasStatusDialogInterface statusDialog, File atlasDir)
			throws AtlasException {

		if (!atlasDir.isDirectory())
			atlasDir = atlasDir.getParentFile();

		AMLImport.statusDialog = statusDialog;

		AtlasConfigEditable atlasConfig = new AtlasConfigEditable(atlasDir);

		try {
			Document xml;
			DocumentBuilder builder;
			builder = getDocumentBuilder(false);

			File atlasXmlFile = new File(atlasDir,
					AtlasConfig.ATLASDATA_DIRNAME + "/"
							+ AtlasConfig.ATLAS_XML_FILENAME);

			xml = builder.parse(atlasXmlFile);
			parseAtlasConfig(atlasConfig, xml);

			return atlasConfig;

		} catch (final Exception ex) {
			throw new AtlasImportException(ex);
		}
	}

	/**
	 * Parse/fill the given {@link AtlasConfig} instance from
	 * {@link org.w3c.dom.Document}. This method does also look for a file with
	 * a default CRS definition and extra fonts.
	 * 
	 * @throws AtlasException
	 */
	public final void parseAtlasConfig(final AtlasConfig ac, final Document xml)
			throws AtlasException {

		readDefaultCRS(ac);

		NodeList nodes;
		// LOGGER.debug("Parsing DOM to AtlasConfig...");

		// Checking the GP version this atlas.xml has been created with
		nodes = xml.getElementsByTagNameNS(AMLUtil.AMLURI, "atlas");
		Node atlasNode = nodes.item(0);
		checkUpgrades(atlasNode);

		// parsing name, desc, creator and copyright
		nodes = xml.getElementsByTagNameNS(AMLUtil.AMLURI, "name");
		ac.setTitle(AMLImport.parseTranslation(ac.getLanguages(), nodes.item(0)));

		nodes = xml.getElementsByTagNameNS(AMLUtil.AMLURI, "desc");
		ac.setDesc(AMLImport.parseTranslation(ac.getLanguages(), nodes.item(0)));

		nodes = xml.getElementsByTagNameNS(AMLUtil.AMLURI, "creator");
		ac.setCreator(AMLImport.parseTranslation(ac.getLanguages(),
				nodes.item(0)));

		nodes = xml.getElementsByTagNameNS(AMLUtil.AMLURI, "copyright");
		ac.setCopyright(AMLImport.parseTranslation(ac.getLanguages(),
				nodes.item(0)));

		// aml:atlasversion
		ac.setAtlasversion(Float.valueOf(xml
				.getElementsByTagNameNS(AMLUtil.AMLURI, "atlasversion").item(0)
				.getFirstChild().getNodeValue()));

		// aml:supportedLanguages
		// aml:language lang="de"
		// aml:language lang="en"
		final Node supportedLanguagesNode = xml.getElementsByTagNameNS(
				AMLUtil.AMLURI, "supportedLanguages").item(0);
		nodes = supportedLanguagesNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node languageNode = nodes.item(i);
			if (languageNode.getLocalName() == null)
				continue;
			final String langCode = languageNode.getAttributes()
					.getNamedItem("lang").getNodeValue();
			// LOGGER.debug("aml:supportedLanguages: adding " + langCode);
			ac.getLanguages().add(langCode);
		}

		// LOGGER.debug("Starting to parse rasters...");

		// "normal" aml:rasterLayer
		final NodeList rasterLayers = xml.getElementsByTagNameNS(
				AMLUtil.AMLURI, "rasterLayer");
		for (int i = 0; i < rasterLayers.getLength(); i++) {
			final Node node = rasterLayers.item(i);
			final DpLayerRaster raster = AMLImport.parseDatapoolLayerRaster(
					node, ac);
			ac.getDataPool().add(raster);
		}

		// LOGGER.debug("Starting to parse pyramid layers...");

		// aml:rasterPyramidLayer
		final NodeList rasterPyramidLayers = xml.getElementsByTagNameNS(
				AMLUtil.AMLURI, "pyramidRasterLayer");
		for (int i = 0; i < rasterPyramidLayers.getLength(); i++) {
			final Node node = rasterPyramidLayers.item(i);
			final DpLayerRasterPyramid pyramid = AMLImport
					.parseDatapoolLayerRasterPyramid(node, ac);
			ac.getDataPool().add(pyramid);
		}

		// LOGGER.debug("Starting to parse vector layers...");
		// aml:vectorLayer
		final NodeList vectorLayer = xml.getElementsByTagNameNS(AMLUtil.AMLURI,
				"vectorLayer");
		for (int i = 0; i < vectorLayer.getLength(); i++) {
			final Node node = vectorLayer.item(i);

			final DpLayerVectorFeatureSource vector = AMLImport
					.parseDatapoolLayerVector(node, ac);
			ac.getDataPool().add(vector);
			// LOGGER.info("Importing vector " + vector.getTitle()
			// + " via the new FeatureSource format!");
		}

		// LOGGER.debug("Starting to parse video layers...");
		// aml:videoMedia
		final NodeList videoMedia = xml.getElementsByTagNameNS(AMLUtil.AMLURI,
				"videoMedia");
		for (int i = 0; i < videoMedia.getLength(); i++) {
			final Node node = videoMedia.item(i);
			final DpMediaVideo vector = AMLImport.parseDatapoolMediaVideo(node,
					ac);
			ac.getDataPool().add(vector);
		}

		// LOGGER.debug("Starting to parse PDF layers...");
		// aml:pdfMedia
		final NodeList pdfMedia = xml.getElementsByTagNameNS(AMLUtil.AMLURI,
				"pdfMedia");
		for (int i = 0; i < pdfMedia.getLength(); i++) {
			final Node node = pdfMedia.item(i);
			final DpMediaPDF pdf = AMLImport.parseDatapoolPdfVideo(node, ac);
			ac.getDataPool().add(pdf);
		}

		// LOGGER.debug("Starting to parse maps...");
		// aml:mapPool
		final Node rootMapPoolNode = xml.getElementsByTagNameNS(AMLUtil.AMLURI,
				"mapPool").item(0);
		if (rootMapPoolNode != null) {
			ac.setMapPool(AMLImport.parseMapPool(rootMapPoolNode, ac));
			Node startMapAttrib = rootMapPoolNode.getAttributes().getNamedItem(
					"startMap");
			if (startMapAttrib != null) {
				ac.getMapPool().setStartMapID(startMapAttrib.getNodeValue());
			}

		} else
			LOGGER.info("No <aml:group> defined in the atlas.xml, but group is optional");

		final Node rootGroupNode = xml.getElementsByTagNameNS(AMLUtil.AMLURI,
				"group").item(0);
		if (rootGroupNode != null) {
			final Group firstGroup = AMLImport.parseGroup(rootGroupNode, ac);
			// LOGGER.debug("Setting first group = "
			// + firstGroup.getTitle().toOneLine());
			ac.setFirstGroup(firstGroup);
		} else
			LOGGER.info("No <aml:group> defined in the atlas.xml, but group is optional");

		// Check if any extra fonts are defined
		final Node rootFontsNode = xml.getElementsByTagNameNS(AMLUtil.AMLURI,
				AMLUtil.TAG_FONTS).item(0);
		if (rootFontsNode != null) {
			parseFonts(ac, rootFontsNode);
		}

		ac.registerFonts();
	}

	protected void parseFonts(AtlasConfig ac, Node rootFontsNode) {
		final NodeList childNodes = rootFontsNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (!AMLUtil.TAG_FONT.equals(childNode.getLocalName()))
				continue;

			String relFontPath = childNode.getAttributes()
					.getNamedItem(AMLUtil.ATT_FONT_FILENAME).getTextContent();

			String resourceLocation = AtlasConfig.ATLASDATA_DIRNAME + "/"
					+ AtlasConfig.FONTS_DIRNAME + "/" + relFontPath;
			InputStream is = ac.getResourceAsStream(resourceLocation);
			if (is == null) {
				warn("Fonts", "The font " + relFontPath
						+ " could not be found at " + resourceLocation);
			} else
				try {
					Font font = Font.createFont(Font.TRUETYPE_FONT, is);
					ac.getFonts().add(font);
					LOGGER.debug("Registered a new TTF font: " + font.getName());
				} catch (Exception e) {
					Log.error("Couldn't load or register font " + relFontPath,
							e);
				}
		}
	}

	/**
	 * Checking the version of the atlas.xml against the version of this GP. If
	 * upgrade changes are necessary, the needed flags are set here.
	 * 
	 * @param maj
	 * @param min
	 * @param build
	 */
	private static void checkUpgrades(Node atlasNode) {

		Node nodeMajVersion = atlasNode.getAttributes().getNamedItem(
				AMLUtil.ATT_majVersion);
		Node nodeMinVersion = atlasNode.getAttributes().getNamedItem(
				AMLUtil.ATT_minVersion);

		// if no version information is defined in atlas.xml, we assume it's
		// 1.2 b1, because versioning has been introduced in 1.3
		int maj = nodeMajVersion == null ? 1 : Integer.valueOf(nodeMajVersion
				.getNodeValue().toString());
		int min = nodeMinVersion == null ? 2 : Integer.valueOf(nodeMinVersion
				.getNodeValue().toString());

		// Check if the filters have to be converted from MartinFilters to ECQL
		double majMin = new Double(maj) + new Double(min) / 10.;

		if (majMin < 1.3) {
			upgradeFiltersToCQL = true;
			LOGGER.info("atlas.xml has been created with a version prior to 1.3. Filters will be converted to CQL!");
		}

	}

	/**
	 * Looks for a file called {@link AtlasConfig#DEFAULTCRS_FILENAME} in
	 * ad-folder containing a default CRS definition in the atlas. If found,
	 * it's applied to
	 * {@link GeoImportUtil#setDefaultCRS(org.opengis.referencing.crs.CoordinateReferenceSystem)}
	 */
	public static void readDefaultCRS(AtlasConfig atlasConfig) {
		String resourceLocation = AtlasConfig.ATLASDATA_DIRNAME + "/"
				+ AtlasConfig.DEFAULTCRS_FILENAME;
		URL defaultCrsUrl = atlasConfig.getResource(resourceLocation);

		if (defaultCrsUrl == null) {
			String warnMessage = resourceLocation
					+ " not found. Using standard "
					+ GeoImportUtil.getDefaultCRS().getName();
			LOGGER.debug(warnMessage);

			warn("default crs", warnMessage);
			return;
		}

		/*
		 * The file exists, read it now
		 */
		try {
			CoordinateReferenceSystem defaultCRS = GeoImportUtilURL
					.readProjectionFile(defaultCrsUrl);
			GeoImportUtil.setDefaultCRS(defaultCRS); // TODO Default CRS must be
			// part of
			// AtlasCOnfig!!!
		} catch (IOException e) {
			LOGGER.error(
					"Error reading " + AtlasConfig.DEFAULTCRS_FILENAME
							+ ". Fallback to"
							+ GeoImportUtil.getDefaultCRS().getName(), e);
			warn("Default CRS", "Error reading "
					+ AtlasConfig.DEFAULTCRS_FILENAME);
			// ExceptionDialog.show(owner, e, null,"Error reading "
			// + AtlasConfig.DEFAULTCRS_FILENAME);
		}

		LOGGER.info("Default CRS: " + GeoImportUtil.getDefaultCRS().getName());
	}

	/**
	 * Parse a aml:PdfMedia tag to a {@link DpMediaPDF} instance
	 * 
	 * @param node
	 * @param ac
	 * @return
	 * @throws AtlasRecoverableException
	 */
	private static DpMediaPDF parseDatapoolPdfVideo(Node node, AtlasConfig ac)
			throws AtlasRecoverableException {
		if (ac == null) {
			throw new IllegalArgumentException(
					"ac= null in parseMediaPdfVideo(Node node, Atlasconfig ac)");
		}
		final DpMediaPDF dpe = new DpMediaPDF(ac);

		dpe.setId(node.getAttributes().getNamedItem("id").getNodeValue());
		// ****************************************************************************
		// The exportable attribute is optional and defaults to false
		// ****************************************************************************
		try {
			dpe.setExportable(Boolean.valueOf(node.getAttributes()
					.getNamedItem("exportable").getNodeValue()));
		} catch (Exception e) {
			dpe.setExportable(false);
		}

		final NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node n = childNodes.item(i);
			final String name = n.getLocalName();
			// Cancel if it's an attribute
			if (!n.hasChildNodes())
				continue;

			if (name.equals("filename")) {
				final String value = n.getFirstChild().getNodeValue();
				dpe.setFilename(value);
			} else if (name.equals("name")) {
				dpe.setTitle(AMLImport.parseTranslation(ac.getLanguages(), n));
			} else if (name.equals("dataDirname")) {
				final String value = n.getFirstChild().getNodeValue();
				dpe.setDataDirname(value);
			} else if (name.equals("desc")) {
				dpe.setDesc(AMLImport.parseTranslation(ac.getLanguages(), n));
			} else if (name.equals("keywords")) {
				dpe.setKeywords(AMLImport.parseTranslation(ac.getLanguages(), n));
			}
		}
		return dpe;
	}

	/**
	 * Parses the node that is of aml:descType
	 * 
	 * @param node
	 *            {@link Node} to parse
	 * 
	 * @throws AtlasRecoverableException
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * @author <a href="mailto:schmitzm@bonn.edu">Martin Schmitz</a> (University
	 *         of Bonn/Germany)
	 */
	public final static Translation parseTranslation(
			final List<String> languages, final Node node)
			throws AtlasRecoverableException {
		Translation trans = new Translation();

		if (node != null) // we usually expect that
		{

			final NodeList childNodes = node.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {
				final Node translation = childNodes.item(i);

				final String name = translation.getLocalName();
				if (name == null)
					continue;

				// lang attribute
				String lang = translation.getAttributes().getNamedItem("lang")
						.getNodeValue();

				// MS-01.sn: if "lang" attribute is missing, set default is sets
				// set the default, if no language code is set
				if (lang == null)
					lang = Translation.DEFAULT_KEY;
				// MS-01.en
				final Node textNode = translation.getFirstChild();
				if (textNode == null) {
					// Empty Text => don't expect a textnode
					trans.put(lang, "");
				} else {
					trans.put(lang, textNode.getNodeValue());
					// trans.put(lang, textNode.getTextContent()); ging bei
					// martin nicht..
				}

			}

			if (trans.size() == 0) {
				// ****************************************************************************
				// MS-01.so if no <translation> is given, the value of the node
				// should
				// be used as a default translation
				// martin
				// MS-01.en
				// ****************************************************************************

				LOGGER.debug("Wir denken, dass kein <translation> angegeben wurde, und setzen '"
						+ node.getFirstChild().getNodeValue()
						+ "' als default.");
				trans = new Translation(languages, node.getFirstChild()
						.getNodeValue());
				// trans = new Translation( node.getNodeValue() );
			}
		}
		return trans;
	}

	/**
	 * Parses an AtlasML branch and fills values
	 * 
	 * @throws AtlasException
	 * @throws AtlasException
	 * @throws AtlasException
	 */
	public final static DpMediaVideo parseDatapoolMediaVideo(final Node node,
			final AtlasConfig ac) throws AtlasException {
		if (ac == null)
			throw new IllegalArgumentException(
					"ac= null in parseMediaMediaVideo(Node node, Atlasconfig ac)");

		final DpMediaVideo dpe = new DpMediaVideo(ac);

		dpe.setId(node.getAttributes().getNamedItem("id").getNodeValue());
		// ****************************************************************************
		// The exportable attribute is optional and defaults to false
		// ****************************************************************************
		try {
			dpe.setExportable(Boolean.valueOf(node.getAttributes()
					.getNamedItem("exportable").getNodeValue()));
		} catch (Exception e) {
			dpe.setExportable(false);
		}

		final NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node n = childNodes.item(i);
			final String name = n.getLocalName();
			// Cancel if it's an attribute
			if (!n.hasChildNodes())
				continue;

			if (name.equals("filename")) {
				final String value = n.getFirstChild().getNodeValue();
				dpe.setFilename(value);
			} else if (name.equals("name")) {
				dpe.setTitle(AMLImport.parseTranslation(ac.getLanguages(), n));
			} else if (name.equals("dataDirname")) {
				final String value = n.getFirstChild().getNodeValue();
				dpe.setDataDirname(value);
			} else if (name.equals("desc")) {
				dpe.setDesc(AMLImport.parseTranslation(ac.getLanguages(), n));
			} else if (name.equals("keywords")) {
				dpe.setKeywords(AMLImport.parseTranslation(ac.getLanguages(), n));
			}
		}
		return dpe;
	}

	/**
	 * Parses an AtlasML branch and fills values
	 * 
	 * @throws AtlasRecoverableException
	 */
	public final static DpLayerRaster parseDatapoolLayerRaster(final Node node,
			final AtlasConfig ac) throws AtlasRecoverableException {

		// LOGGER.debug("parseDatapoolLayerRaster");

		final DpLayerRaster dpe = new DpLayerRaster(ac);

		dpe.setId(node.getAttributes().getNamedItem("id").getNodeValue());
		// ****************************************************************************
		// The exportable attribute is optional and defaults to false
		// ****************************************************************************
		try {
			dpe.setExportable(Boolean.valueOf(node.getAttributes()
					.getNamedItem("exportable").getNodeValue()));
		} catch (Exception e) {
			dpe.setExportable(false);
		}

		final NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node n = childNodes.item(i);
			final String name = n.getLocalName();
			// Cancel if it's an attribute
			if (!n.hasChildNodes())
				continue;

			if (name.equals("filename")) {
				final String value = n.getFirstChild().getNodeValue();
				dpe.setFilename(value);
			} else {
				if (name.equals("name")) {
					final Translation transname = AMLImport.parseTranslation(
							ac.getLanguages(), n);
					dpe.setTitle(transname);
				} else if (name.equals("dataDirname")) {
					final String value = n.getFirstChild().getNodeValue();
					dpe.setDataDirname(value);
					// dpe.setDataDirectory(new File(ac.getDataDir(), value));
				} else if (name.equals("desc")) {
					dpe.setDesc(parseTranslation(ac.getLanguages(), n));
				} else if (name.equals("rasterLegendData")) {
					dpe.setLegendMetaData(parseRasterLegendData(ac, n));
				} else if (name.equals("keywords")) {
					dpe.setKeywords(parseTranslation(ac.getLanguages(), n));
				}
			}
		}
		return dpe;
	}

	/**
	 * Parses the {@link RasterLegendData} from AtlasML
	 * 
	 * @param n
	 *            Node that is called "rasterLagendData"
	 * @return {@link RasterLegendData}, maybe without entries, but never null.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * @throws AtlasRecoverableException
	 */
	private static RasterLegendData parseRasterLegendData(AtlasConfig ac,
			Node node) throws AtlasRecoverableException {
		RasterLegendData rld = new RasterLegendData(false);
		rld.setPaintGaps(Boolean.valueOf(node.getAttributes()
				.getNamedItem("paintGaps").getNodeValue()));

		final NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node n = childNodes.item(i);
			final String name = n.getLocalName();

			// Cancel if it's an attribute
			if (!n.hasChildNodes())
				continue;

			if (name.equals("rasterLegendItem")) {
				final Node paintGapsAttrib = n.getAttributes().getNamedItem(
						"value");
				Double value = Double.valueOf(paintGapsAttrib.getNodeValue());

				// first and only item should be the label
				final Node labeln = n.getChildNodes().item(1);
				Translation label = parseTranslation(ac.getLanguages(), labeln);

				rld.put(value, label);
			}
		}

		return rld;
	}

	/**
	 * Parses an AtlasML branch and fills values
	 * 
	 * @throws AtlasRecoverableException
	 */
	public final static DpLayerRasterPyramid parseDatapoolLayerRasterPyramid(
			final Node node, final AtlasConfig ac)
			throws AtlasRecoverableException {
		if (ac == null)
			throw new IllegalArgumentException("ac == null");
		final DpLayerRasterPyramid dpe = new DpLayerRasterPyramid(ac);

		dpe.setId(node.getAttributes().getNamedItem("id").getNodeValue());
		final NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node n = childNodes.item(i);
			final String name = n.getLocalName();
			// Cancel if it's an attribute
			if (!n.hasChildNodes())
				continue;

			if (name.equals("filename")) {
				final String value = n.getFirstChild().getNodeValue();
				dpe.setFilename(value);
			} else {
				if (name.equals("name")) {
					final Translation transname = AMLImport.parseTranslation(
							ac.getLanguages(), n);

					dpe.setTitle(transname);
				} else if (name.equals("dataDirname")) {
					final String value = n.getFirstChild().getNodeValue();
					dpe.setDataDirname(value);
				} else if (name.equals("desc")) {
					dpe.setDesc(AMLImport.parseTranslation(ac.getLanguages(), n));
				} else if (name.equals("rasterLegendData")) {
					dpe.setLegendMetaData(parseRasterLegendData(ac, n));
				} else if (name.equals("keywords")) {
					dpe.setKeywords(AMLImport.parseTranslation(
							ac.getLanguages(), n));
				} else if (name.equals("transparentColor")) {
					final String colorStr = n.getFirstChild().getNodeValue();
					dpe.setInputTransparentColor(SwingUtil.parseColor(colorStr));
				}
			}
		}
		return dpe;
	}

	/**
	 * Parses a aml:group tag
	 * 
	 * @param node
	 *            sml:group-node
	 * @param ac
	 *            {@link AtlasConfig} to get the {@link DataPool} to map the
	 *            datapoolRef-IDs
	 */
	public final static Group parseGroup(final Node node, final AtlasConfig ac)
			throws AtlasRecoverableException {
		final Group group = new Group(ac);
		// LOGGER.debug("Parsing a new group...");

		/**
		 * Is this group supposed to be the special Help or File menu?
		 */
		if (node.getAttributes() != null) {
			Node isFileRaw = node.getAttributes().getNamedItem("isFileMenu");
			if (isFileRaw != null && isFileRaw.getNodeValue() != null
					&& isFileRaw.getNodeValue().equals("true")) {
				group.setFileMenu(true);
			}

			Node isHelpRaw = node.getAttributes().getNamedItem("isHelpMenu");
			if (isHelpRaw != null && isHelpRaw.getNodeValue() != null
					&& isHelpRaw.getNodeValue().equals("true")) {
				group.setHelpMenu(true);
			}
		}

		final NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node childNode = childNodes.item(i);
			final String tagName = childNode.getLocalName();

			// Cancel if it's an attribute
			if (tagName == null)
				continue;

			if (tagName.equals("name")) {
				group.setTitle(parseTranslation(ac.getLanguages(), childNode));
			} else if (tagName.equals("desc")) {
				group.setDesc(parseTranslation(ac.getLanguages(), childNode));
			} else if (tagName.equals("keywords")) {
				group.setKeywords(parseTranslation(ac.getLanguages(), childNode));
			} else if (tagName.equals("datapoolRef")) {
				final String id = childNode.getAttributes().getNamedItem("id")
						.getNodeValue();
				final DpEntry testDpe = ac.getDataPool().get(id);
				if (testDpe == null) {
					warn("menu structure",
							"<datapoolRef> traget id can't be found in the Datapool. id="
									+ id + "\n Ignoring."); // i8n

				} else {
					// LOGGER.debug(" Adding datapoolRef " + testDpe
					// + " to group " + group);
					group.add(new DpRef<DpEntry<? extends ChartStyle>>(testDpe));
				}
			} else if (tagName.equals("mapRef")) {
				final String id = childNode.getAttributes().getNamedItem("id")
						.getNodeValue();
				final Map testMap = ac.getMapPool().get(id);
				if (testMap == null) {
					warn("menu structure",
							"<mapRef> traget id can't be found in the Datapool. id="
									+ id + "\n Ignoring."); // i8n
				} else {
					// LOGGER.debug("Adding mapRef " + testMap + " to group "
					// + group);
					group.add(new MapRef(testMap, ac.getMapPool()));
				}
			} else if (tagName.equals("group")) {
				group.add(parseGroup(childNode, ac));
			}
		}
		return group;
	}

	/**
	 * Parses an AtlasML branch and fills values to create an
	 * {@link DpLayerVectorShpOld} object.
	 * 
	 * @throws AtlasRecoverableException
	 * 
	 *             TODO TODO TODO Hier muss eine URL hin!
	 * @throws AtlasCancelException
	 */
	public final static DpLayerVectorFeatureSource parseDatapoolLayerVector(
			final Node node, final AtlasConfig ac)
			throws AtlasRecoverableException {
		if (ac == null)
			throw new IllegalArgumentException("ac == null");

		// TODO If we have Shapefiles and WFS Sources, this will need a factory
		// here...
		// TODO ATM we only expect Shapefiles here!
		final DpLayerVectorFeatureSource dplvfs = new DpLayerVectorFeatureSourceShapefile(
				ac);

		try {
			dplvfs.setId(node.getAttributes().getNamedItem("id").getNodeValue());
			// LOGGER.info("ID = "+dplvfs.getId());

			/***********************************************************************
			 * The "showStylerInLegend" attribute is optional and defaults to
			 * true
			 ***********************************************************************/
			try {
				dplvfs.setStylerInLegend(Boolean.valueOf(node.getAttributes()
						.getNamedItem("showStylerInLegend").getNodeValue()));
			} catch (Exception e) {
				dplvfs.setStylerInLegend(true);
			}

			/***********************************************************************
			 * The "showTableInLegend" attribute is optional and defaults to
			 * true
			 ***********************************************************************/
			try {
				dplvfs.setTableInLegend(Boolean.valueOf(node.getAttributes()
						.getNamedItem("showTableInLegend").getNodeValue()));
			} catch (Exception e) {
				dplvfs.setTableInLegend(true);
			}

			/***********************************************************************
			 * The "showFilterInLegend" attribute is optional and defaults to
			 * false
			 ***********************************************************************/
			try {
				dplvfs.setFilterInLegend(Boolean.valueOf(node.getAttributes()
						.getNamedItem("showFilterInLegend").getNodeValue()));
			} catch (Exception e) {
				dplvfs.setFilterInLegend(true);
			}

			/***********************************************************************
			 * The "exportable" attribute is optional and defaults to false
			 ***********************************************************************/
			try {
				dplvfs.setExportable(Boolean.valueOf(node.getAttributes()
						.getNamedItem("exportable").getNodeValue()));
			} catch (Exception e) {
				dplvfs.setExportable(false);
			}

			final NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				final Node n = childNodes.item(i);
				// Cancel if it's an attribute
				// if (!n.hasChildNodes())
				// continue;

				final String name = n.getLocalName();
				if (name == null)
					continue;

				if (name.equals("filename")) {
					final String value = n.getFirstChild().getNodeValue();
					dplvfs.setFilename(value);
				} else if (name.equals("name")) {
					final Translation transname = parseTranslation(
							ac.getLanguages(), n);
					// info("parsing vector " + transname);
					dplvfs.setTitle(transname);
				} else if (name.equals("dataDirname")) {
					final String value = n.getFirstChild().getNodeValue();
					dplvfs.setDataDirname(value);
				} else if (name.equals("desc")) {
					dplvfs.setDesc(parseTranslation(ac.getLanguages(), n));
				} else if (name.equals("keywords")) {
					dplvfs.setKeywords(parseTranslation(ac.getLanguages(), n));
				} else if (name.equals(AMLUtil.TAG_attributeMetadata)) {
					try {
						final AttributeMetadataImpl attribute = parseAttributeMetadata(
								dplvfs, ac, n);
						if (attribute != null) {
							dplvfs.getAttributeMetaDataMap().put(
									attribute.getName(), attribute);
						}

					} catch (AtlasRecoverableException e) {
						warn("Parsing attribute descriptions", e.getMessage());
					} catch (RuntimeException e) {
						if (AtlasViewerGUI.isRunning()) {
							// warn("layer nich verfügbar aber das ist nicht schlimm",
							// e.getMessage());
						} else
							throw e;
					}
				} else if (name.equals("layerStyle")) {
					dplvfs.addLayerStyle(parseLayerStyle(ac, n, dplvfs));
				} else if (name.equals("filterRule")) {
					String filterString = n.getFirstChild().getNodeValue();

					if (upgradeFiltersToCQL && filterString != null
							&& !filterString.isEmpty()) {
						try {
							String convertedFilterString = AMLUtil
									.upgradeMartinFilter2ECQL(filterString);
							dplvfs.setFilterRule(convertedFilterString);
						} catch (CQLException filterParserEx) {
							LOGGER.error(
									"Converting filter "
											+ filterString
											+ " to CQL failed! Setting filter to no-filter",
									filterParserEx);
							warn(dplvfs.getTitle().toString(),
									"Failed to convert old filter\n  "
											+ filterString
											+ "\n to the new ECQL filter language. This setting is lost.");
							dplvfs.setFilterRule("");
						}
					} else {
						dplvfs.setFilterRule(filterString);
					}

				} else if (name.equals("chart") && dplvfs.getUrl() != null) {
					try {
						dplvfs.getCharts().add(
								parseFeatureChartStyle(ac, n, dplvfs));
					} catch (Exception e) {
						// Broken URL or file doesn't exist
						LOGGER.warn("Could not load chartStyle: " + e);
					}
				}
			}

		} catch (Exception e) {
			warn("A layer has been ignored due to errors:", e.getMessage());
			return null;
		}

		return dplvfs;
	}

	/**
	 * Parses the metainformation in the XML and creates a
	 * {@link FeatureChartStyle} from the referenced charts/blabla.xml file.
	 * 
	 * @param dplvfs
	 *            may not be <code>null</code>.
	 */
	private static FeatureChartStyle parseFeatureChartStyle(AtlasConfig ac,
			Node node, final DpLayerVectorFeatureSource dplvfs)
			throws IOException {
		final String filenameValue = node.getAttributes()
				.getNamedItem("filename").getNodeValue();

		URL url = dplvfs.getUrl();

		String urlStr = url.toString();
		// LOGGER.debug("urlstr ="+urlStr);
		String substring = urlStr.substring(0, urlStr.lastIndexOf("/") + 1);
		// LOGGER.debug("sub "+substring);
		urlStr = substring + "charts/" + filenameValue;
		URL chartStyleURL = new URL(urlStr);
		FeatureChartStyle featureChartStyle = (FeatureChartStyle) ChartStyleUtil
				.readStyleFromXML(chartStyleURL,
						FeatureChartUtil.FEATURE_CHART_STYLE_FACTORY);

		// System.out.println("after loading\n"+"  "+featureChartStyle);

		AttributeMetadataMap attributeMetaDataMap = dplvfs
				.getAttributeMetaDataMap();

		// Check if the attributes still exist or whether their
		// uppercase/lowercase mode changed
		if (FeatureChartUtil.correctAttributeNames(featureChartStyle,
				dplvfs.getSchema())) {
			// How to handle chartstyles that had attributes removed?
		}

		FeatureChartUtil.passNoDataValues(attributeMetaDataMap,
				featureChartStyle);

		// System.out.println("after correcting\n"+"  "+featureChartStyle);
		return featureChartStyle;
	}

	/**
	 * Parses a <layerStyle> tag
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	private static LayerStyle parseLayerStyle(AtlasConfig ac, Node node,
			DpLayer<?, ? extends ChartStyle> dpLayer)
			throws AtlasRecoverableException {

		final String filename = node.getAttributes().getNamedItem("filename")
				.getNodeValue();

		final NodeList childNodes = node.getChildNodes();
		Translation name = null;
		Translation desc = null;
		for (int i = 0; i < childNodes.getLength(); i++) {
			if (childNodes.item(i).getLocalName() == null)
				continue;

			if (childNodes.item(i).getLocalName().equals("title")) {
				name = parseTranslation(ac.getLanguages(), childNodes.item(i));
			} else if (childNodes.item(i).getLocalName().equals("desc")) {
				desc = parseTranslation(ac.getLanguages(), childNodes.item(i));
			}
		}

		return new LayerStyle(filename, name, desc, dpLayer);

	}

	// TODO
	// private static void checkCancel() throws AtlasCancelException {
	// if (statusDialog != null && statusDialog.isCanceled())
	// throw new AtlasCancelException();
	// }

	/**
	 * Parses a node that is of type < aml : dataAttribute > to a
	 * {@link AttributeMetadataImpl}
	 * 
	 * @param dplvfs
	 *            only used to map old colIdx => new attribute Names
	 * 
	 * @return {@link AttributeMetadataImpl} or <code>null</code>, if the
	 *         attribute doesn't exist anymore.
	 * @throws AtlasRecoverableException
	 * @throws AtlasCancelException
	 */
	public static AttributeMetadataImpl parseAttributeMetadata(
			DpLayerVectorFeatureSource dplvfs, AtlasConfig ac, final Node node)
			throws AtlasRecoverableException, AtlasCancelException {

		String localname;
		String nameSpace;
		Integer weight = 0;
		Double functionX = 1.;
		Double functionA = 0.;
		try {
			nameSpace = String.valueOf(node.getAttributes()
					.getNamedItem(AMLUtil.ATT_namespace).getNodeValue());
			localname = String.valueOf(node.getAttributes()
					.getNamedItem(AMLUtil.ATT_localname).getNodeValue());
			weight = Integer.valueOf(node.getAttributes()
					.getNamedItem(AMLUtil.ATT_weight).getNodeValue());
			functionX = Double.valueOf(node.getAttributes()
					.getNamedItem(AMLUtil.ATT_functionX).getNodeValue());
			functionA = Double.valueOf(node.getAttributes()
					.getNamedItem(AMLUtil.ATT_functionA).getNodeValue());
		} catch (Exception e) {

			functionX = 1.;
			functionA = 0.;

			// LOGGER.debug("Converting old colIdx data because",e);

			try {
				final Integer col = Integer.valueOf(node.getAttributes()
						.getNamedItem("col").getNodeValue());

				AttributeDescriptor attributeDescriptor = dplvfs
						.getFeatureSource().getSchema()
						.getAttributeDescriptors().get(col);

				nameSpace = attributeDescriptor.getName().getNamespaceURI();
				localname = attributeDescriptor.getName().getLocalPart();
				weight = col;

				// String msg =
				// "Converting old attribute meta-data using colIdx="
				// + col + " to new meta.data with atibLocalName="+localname;

				// info(msg);
				// LOGGER.debug(msg);

			} catch (Exception ee) {
				LOGGER.warn(dplvfs.getId()
						+ " is broken. Can not import old colIdx-based attributeMetadata");
				warn(dplvfs.getTitle().toString(),
						dplvfs.getId()
								+ " is broken. Can not import old colIdx-based attributeMetadata");
				return null;
			}
		}

		final String visibleValue = node.getAttributes()
				.getNamedItem("visible").getNodeValue();

		final boolean visible = Boolean.valueOf(visibleValue);

		String unit = "";
		if (node.getAttributes().getNamedItem("unit") != null) {
			unit = node.getAttributes().getNamedItem("unit").getNodeValue();
		}

		// The NameImpl may not be constructed with a "" as namespace, but a
		// null!
		// NameImpl nameImpl = new NameImpl(nameSpace != null ? nameSpace
		// .isEmpty() ? null : nameSpace : null, localname);

		NameImpl correctedAttName = FeatureUtil.findBestMatchingAttribute(
				dplvfs.getSchema(), localname);
		if (correctedAttName == null)
			throw new AtlasRecoverableException(
					"Couldn't find any existing attribute in "
							+ dplvfs.getFilename()
							+ " that the described attribute '"
							+ localname
							+ "' could belong to. Maybe the attribute has been deleted? The meatdata is thrown away.");

		// Creating the object
		AttributeMetadataImpl attributeMetadata = new AttributeMetadataImpl(
				correctedAttName, visible, unit);
		attributeMetadata.setWeight(weight);
		attributeMetadata.setFunctionA(functionA);
		attributeMetadata.setFunctionX(functionX);

		// Parsing the childres

		final NodeList childNodes = node.getChildNodes();
		// Translation name = null;
		// Translation desc = null;
		for (int i = 0; i < childNodes.getLength(); i++) {
			if (childNodes.item(i).getLocalName() == null)
				continue;

			if (childNodes.item(i).getLocalName().equals("name")) {
				attributeMetadata.setTitle(parseTranslation(ac.getLanguages(),
						childNodes.item(i)));
			} else if (childNodes.item(i).getLocalName().equals("desc")) {
				attributeMetadata.setDesc(parseTranslation(ac.getLanguages(),
						childNodes.item(i)));
			} else if (childNodes.item(i).getLocalName()
					.equals(AMLUtil.TAG_nodataValue)) {
				// NODATA values

				Node item = childNodes.item(i);
				if (item != null) {
					String textValue = item.getTextContent();
					// Depending on the schema we have to transform the String
					// to Number.
					AttributeDescriptor attDesc = dplvfs.getSchema()
							.getDescriptor(correctedAttName);

					Class<?> binding = attDesc.getType().getBinding();

					if (attDesc != null
							&& Number.class.isAssignableFrom(binding)) {
						// Add the NODATA value parsed accoring to the binding
						try {

							Number noDataValue = LangUtil.parseNumberAs(
									textValue, binding);
							attributeMetadata.getNodataValues()
									.add(noDataValue);

						} catch (Exception e) {
							ExceptionDialog.show(new RuntimeException(
									"NODATA value '" + textValue
											+ "' can't be parsed as numeric.",
									e));
							attributeMetadata.getNodataValues().add(textValue);
						}
					} else {
						// Add the NODATA value as a String
						attributeMetadata.getNodataValues().add(textValue);
					}
				}

			}

		}
		return attributeMetadata;
	}

	/**
	 * Parses a node that is of type < aml:maps > to a {@link MapPool} object
	 * 
	 * @throws AtlasRecoverableException
	 */
	public final static Map parseMap(final Node node, final AtlasConfig ac)
			throws AtlasRecoverableException {
		// LOGGER.debug("Parsing a new Map...");

		// Reading the map's ID
		final Map map = new Map(node.getAttributes().getNamedItem("id")
				.getNodeValue(), ac);

		// Whether any maxMapExtend should be applied in Geopublisher's
		// MapComposer
		if (node.getAttributes().getNamedItem(
				AMLUtil.ATT_PREVIEW_MAX_MAPEXTEND_IN_GP) != null) {
			map.setPreviewMapExtendInGeopublisher(Boolean.valueOf(node
					.getAttributes()
					.getNamedItem(AMLUtil.ATT_PREVIEW_MAX_MAPEXTEND_IN_GP)
					.getNodeValue()));
		} else {
			// Default is false
			map.setPreviewMapExtendInGeopublisher(false);
		}

		// Reading the map's legend left/right ratio
		if (node.getAttributes().getNamedItem("leftRightRatio") != null) {
			Double leftRightRatio = Double.valueOf(node.getAttributes()
					.getNamedItem("leftRightRatio").getNodeValue());
			map.setLeftRightRatio(leftRightRatio);
		} else {
			// 0 => auto mode
			map.setLeftRightRatio(0.);
		}

		// Shall the map scale be shown?
		if (node.getAttributes().getNamedItem(AMLUtil.ATT_MAP_SCALE_VISIBLE) != null) {
			map.setScaleVisible(Boolean
					.valueOf(node.getAttributes()
							.getNamedItem(AMLUtil.ATT_MAP_SCALE_VISIBLE)
							.getNodeValue()));
		}

		// Shall the map scale be shown?
		if (node.getAttributes().getNamedItem(AMLUtil.ATT_MAP_SCALE_UNITS) != null) {
			map.setScaleUnits(ScaleUnits.valueOf(node.getAttributes()
					.getNamedItem(AMLUtil.ATT_MAP_SCALE_UNITS).getNodeValue()));
		}

		// Shall the map show horizontal and vertical gridPanels?
		if (node.getAttributes().getNamedItem("gridPanelVisible") != null) {
			map.setGridPanelVisible(Boolean.valueOf(node.getAttributes()
					.getNamedItem("gridPanelVisible").getNodeValue()));
		}

		// Which formatter shall be used?
		if (node.getAttributes().getNamedItem("gridPanelFormatter") != null) {
			map.setGridPanelFormatter(GridPanelFormatter.getFormatterByID(node
					.getAttributes().getNamedItem("gridPanelFormatter")
					.getNodeValue()));
		}

		// Which CRS shall be used? If non is defined, use
		// GeoImportUtil.getDefaultCRS()
		{
			URL gridCrsURl = ac.getResLoMan().getResourceAsUrl(
					AtlasConfig.ATLASDATA_DIRNAME + "/"
							+ AtlasConfig.HTML_DIRNAME + "/" + map.getId()
							+ "/" + Map.GRIDPANEL_CRS_FILENAME);
			if (gridCrsURl == null) {
				map.setGridPanelCRS(GeoImportUtil.getDefaultCRS());
			} else {
				try {
					map.setGridPanelCRS(GeoImportUtilURL
							.readProjectionFile(gridCrsURl));
				} catch (IOException e) {
					LOGGER.warn("Unable to read " + gridCrsURl, e);
					map.setGridPanelCRS(GeoImportUtil.getDefaultCRS());
				}
			}
		}

		final NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node childNode = childNodes.item(i);
			final String tagName = childNode.getLocalName();

			// Cancel if it's an attribute
			if (tagName == null)
				continue;

			if (tagName.equals("name")) {
				map.setTitle(parseTranslation(ac.getLanguages(), childNode));
			} else if (tagName.equals("desc")) {
				map.setDesc(parseTranslation(ac.getLanguages(), childNode));
			} else if (tagName.equals("keywords")) {
				map.setKeywords(parseTranslation(ac.getLanguages(), childNode));
			} else if (tagName.equals("startViewEnvelope")) {
				final String value = childNode.getFirstChild().getNodeValue();

				String[] pairs = value.split(" ");
				final double cornersV[][] = new double[2][2];
				String pair[];
				for (int iii = 0; iii < 2; iii++) {
					pair = pairs[iii].split(",");
					cornersV[iii][0] = Double.parseDouble(pair[0]);
					cornersV[iii][1] = Double.parseDouble(pair[1]);
				}
				// TODO defaultMapArea has to store the crs!?! Map should store
				// it's crs?!
				map.setDefaultMapArea(new ReferencedEnvelope(cornersV[0][0],
						cornersV[1][0], cornersV[0][1], cornersV[1][1], null));

			} else if (tagName.equals("maxExtend")) {
				final String value = childNode.getFirstChild().getNodeValue();

				String[] pairs = value.split(" ");
				final double cornersV[][] = new double[2][2];
				String pair[];
				for (int iii = 0; iii < 2; iii++) {
					pair = pairs[iii].split(",");
					cornersV[iii][0] = Double.parseDouble(pair[0]);
					cornersV[iii][1] = Double.parseDouble(pair[1]);
				}
				map.setMaxExtend(new Envelope(new Coordinate(cornersV[0][0],
						cornersV[0][1]), new Coordinate(cornersV[1][0],
						cornersV[1][1])));

			} else if (tagName.equals("datapoolRef")) {
				final String id = childNode.getAttributes().getNamedItem("id")
						.getNodeValue();
				final DpEntry<? extends ChartStyle> testDpe = ac.getDataPool()
						.get(id);
				if (testDpe == null) {
					warn("map:" + map.getTitle().toString(),
							"<datapoolRef> attribute id can't be found in the Datapool. id="
									+ id);
				} else {

					// LOGGER.debug(" Adding datapoolRef " + testDpe +
					// " to Map "
					// + map);

					final DpRef<DpEntry<? extends ChartStyle>> dpRef = new DpRef<DpEntry<? extends ChartStyle>>(
							testDpe);

					// Checking for the optional attribute
					// <code>hideInLegend</code>
					Node namedItem = childNode.getAttributes().getNamedItem(
							"hideInLegend");
					if (namedItem != null) {
						final String hideLayerInLegend = namedItem
								.getNodeValue();
						map.getHideInLegendMap().put(dpRef.getTargetId(),
								Boolean.valueOf(hideLayerInLegend));
					}

					// Checking for the optional (default = false) attribute
					// <code>minimizeInLegend</code>
					namedItem = childNode.getAttributes().getNamedItem(
							"minimizeInLegend");
					if (namedItem != null) {
						final String minimizeLayerInLegend = namedItem
								.getNodeValue();
						map.getMinimizedInLegendMap().put(dpRef.getTargetId(),
								Boolean.valueOf(minimizeLayerInLegend));
					}

					// Checking for the optional attribute
					// <code>hidden</code> that controls the status of the
					// "eye symbol"
					namedItem = childNode.getAttributes()
							.getNamedItem("hidden");
					if (namedItem != null) {
						final String hidden = namedItem.getNodeValue();
						map.setHiddenFor(dpRef.getTargetId(),
								Boolean.valueOf(hidden));
					}

					// Checking for the optional attribute
					// <code>selectable</code> that controls the status of the
					// "eye symbol"
					namedItem = childNode.getAttributes().getNamedItem(
							"selectable");
					if (namedItem != null) {
						final String hidden = namedItem.getNodeValue();
						map.setSelectableFor(dpRef.getTargetId(),
								Boolean.valueOf(hidden));
					}

					map.add(dpRef);
				}
			} else if (tagName.equals("additionalStyles")) {

				String layerID = childNode.getAttributes()
						.getNamedItem("layerID").getNodeValue();
				Node whichIsSelectedAttribute = childNode.getAttributes()
						.getNamedItem("selectedStyleID");

				ArrayList<String> styles = new ArrayList<String>();

				String selectedStyleID = null;
				if (whichIsSelectedAttribute != null) {
					selectedStyleID = whichIsSelectedAttribute.getNodeValue();
					map.setSelectedStyleID(layerID, selectedStyleID);
				}

				final NodeList additionalStyleschildNodes = childNode
						.getChildNodes();
				for (int ii = 0; ii < additionalStyleschildNodes.getLength(); ii++) {
					final Node additionalStyleschildNode = additionalStyleschildNodes
							.item(ii);
					final String additionalStylestagName = additionalStyleschildNode
							.getLocalName();

					// Cancel if it's an attribute
					if (additionalStylestagName == null)
						continue;

					if (additionalStylestagName.equals("styleid")) {
						// Do one more check here. If the referenced
						// LayerStyleID doesn't exist, omit it.

						String styleId = additionalStyleschildNode
								.getTextContent();

						DpLayer<?, ? extends ChartStyle> dpl = (DpLayer<?, ? extends ChartStyle>) ac
								.getDataPool().get(layerID);

						boolean foundIt = false;
						if (dpl != null)
							for (LayerStyle layerStyle : dpl.getLayerStyles()) {
								if (layerStyle.getID().equals(styleId)) {
									styles.add(styleId);
									foundIt = true;
									break;
								}
							}

						/**
						 * This warning only makes sense in GP.. in AV these
						 * layers might simple not have been exported.
						 */

						if (!AtlasViewerGUI.isRunning() && !foundIt) {
							String msg = "Map '"
									+ map.getTitle()
									+ "' defines an additional style with ID='"
									+ styleId
									+ "' for layer '"
									+ layerID
									+ "', but it's not known to the layer. It's ignored.";
							LOGGER.warn(msg);
							warn("map:" + map.getTitle().toString(), msg);
						}
					}
				}
				map.getAdditionalStyles().put(layerID, styles);
			} else if (tagName.equals("availableCharts")) {

				String layerID = childNode.getAttributes()
						.getNamedItem("layerID").getNodeValue();

				ArrayList<String> availChartsIDs = new ArrayList<String>();

				final NodeList additionalStyleschildNodes = childNode
						.getChildNodes();
				for (int ii = 0; ii < additionalStyleschildNodes.getLength(); ii++) {
					final Node availableChartChildNode = additionalStyleschildNodes
							.item(ii);
					final String additionalStylestagName = availableChartChildNode
							.getLocalName();

					// Cancel if it's an attribute
					if (additionalStylestagName == null)
						continue;

					if (additionalStylestagName.equals("chartID")) {
						// Do one more check here. If the referenced
						// ChartStyle-ID doesn't exist, omit it.

						String styleId = availableChartChildNode
								.getTextContent();

						DpLayer<?, ChartStyle> dpl = (DpLayer<?, ChartStyle>) ac
								.getDataPool().get(layerID);

						boolean foundIt = false;
						for (ChartStyle chart : dpl.getCharts()) {
							if (chart.getID().equals(styleId)) {
								availChartsIDs.add(styleId);
								foundIt = true;
								break;
							}
						}

						if (!AtlasViewerGUI.isRunning() && !foundIt) {
							String msg = "Map '"
									+ map.getTitle()
									+ "' defines an available chart with ID='"
									+ styleId
									+ "' for layer '"
									+ layerID
									+ "', but it's not known to the layer. It's ignored.";
							LOGGER.error(msg);
							warn("map:" + map.getTitle().toString(), msg);
						}

					}
				}
				map.getAvailableCharts().put(layerID, availChartsIDs);
			}

		}
		return map;
	}

	/**
	 * Parses a < aml:mapPool > element
	 * 
	 * @author Stefan Alfons Tzeggai
	 * @throws AtlasRecoverableException
	 */
	public static MapPool parseMapPool(final Node node,
			final AtlasConfig atlasConfig) throws AtlasRecoverableException {
		final MapPool mapPool = new MapPool();

		info("Parsing MapPool...");

		final NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node childNode = childNodes.item(i);
			final String tagName = childNode.getLocalName();

			// Cancel if it's an attribute
			if (tagName == null)
				continue;

			if (tagName.equals("map")) {
				final Map map = parseMap(childNode, atlasConfig);
				mapPool.add(map);
			}
		}
		return mapPool;
	}

	public AMLImport() {
		// TODO Auto-generated constructor stub
	}

}
