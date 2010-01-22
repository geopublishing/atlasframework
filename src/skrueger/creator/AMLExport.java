///*******************************************************************************
// * Copyright (c) 2009 Stefan A. Krüger.
// * 
// * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
// * http://www.geopublishing.org
// * 
// * Geopublisher is part of the Geopublishing Framework hosted at:
// * http://wald.intevation.org/projects/atlas-framework/
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License (license.txt)
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// * or try this link: http://www.gnu.org/licenses/gpl.html
// * 
// * Contributors:
// *     Stefan A. Krüger - initial API and implementation
// ******************************************************************************/
//package skrueger.creator;
//
//import java.awt.Color;
//import java.awt.Component;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FilenameFilter;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.net.URL;
//import java.util.Collection;
//import java.util.Enumeration;
//import java.util.List;
//
//import javax.swing.tree.TreeNode;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.OutputKeys;
//import javax.xml.transform.Result;
//import javax.xml.transform.Source;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//
//import org.apache.log4j.Logger;
//import org.geotools.data.DataUtilities;
//import org.geotools.filter.text.cql2.CQL;
//import org.geotools.styling.Style;
//import org.opengis.feature.simple.SimpleFeatureType;
//import org.opengis.filter.Filter;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;
//import org.w3c.dom.Attr;
//import org.w3c.dom.DOMException;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//
//import schmitzm.geotools.io.GeoExportUtil;
//import schmitzm.geotools.io.GeoImportUtil;
//import schmitzm.geotools.styling.StylingUtil;
//import schmitzm.jfree.chart.style.ChartStyleUtil;
//import schmitzm.jfree.feature.style.FeatureChartStyle;
//import schmitzm.jfree.feature.style.FeatureChartUtil;
//import schmitzm.swing.ExceptionDialog;
//import skrueger.AttributeMetadata;
//import skrueger.RasterLegendData;
//import skrueger.atlas.AVUtil;
//import skrueger.atlas.AtlasConfig;
//import skrueger.atlas.dp.DpEntry;
//import skrueger.atlas.dp.DpRef;
//import skrueger.atlas.dp.Group;
//import skrueger.atlas.dp.layer.DpLayer;
//import skrueger.atlas.dp.layer.DpLayerRaster;
//import skrueger.atlas.dp.layer.DpLayerRasterPyramid;
//import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
//import skrueger.atlas.dp.layer.LayerStyle;
//import skrueger.atlas.dp.media.DpMediaPDF;
//import skrueger.atlas.dp.media.DpMediaVideo;
//import skrueger.atlas.exceptions.AtlasException;
//import skrueger.atlas.exceptions.AtlasFatalException;
//import skrueger.atlas.http.Webserver;
//import skrueger.atlas.internal.AMLUtil;
//import skrueger.atlas.internal.ProgressListener;
//import skrueger.atlas.map.Map;
//import skrueger.atlas.map.MapPool;
//import skrueger.atlas.map.MapRef;
//import skrueger.creator.export.AtlasExportException;
//import skrueger.i8n.Translation;
//
//import com.vividsolutions.jts.geom.Envelope;
//
///**
// * This class can export the Atlas-objects to AtlasMarkupLanguage (AtlasML)
// * 
// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
// * 
// */
//public class AMLExport {
//	private static final String CHARTSTYLE_FILEENDING = ".chart";
//
//	final static private Logger LOGGER = Logger.getLogger(AMLExport.class);
//
//	// TODO DOKU listener oder sauber machen
//	public static ProgressListener pl;
//
//	private static Component owner;
//
//	static void info(String msg) {
//		if (pl != null)
//			pl.info(msg);
//	}
//
//	/**
//	 * Writes the default CRS of that atlas to a file called
//	 * {@link AtlasConfig#DEFAULTCRS_FILENAME} in ad folder.
//	 * 
//	 * @throws IOException
//	 */
//	public static void saveDefaultCRS(AtlasConfigEditable ace)
//			throws IOException {
//		CoordinateReferenceSystem crs = GeoImportUtil.getDefaultCRS();
//		GeoExportUtil.writeProjectionFilePrefereEPSG(crs, new File(ace.getAd(),
//				AtlasConfig.DEFAULTCRS_FILENAME));
//
//		URL defaultCrsUrl = AtlasConfig.getResLoMan().getResourceAsUrl(
//				"ad/" + AtlasConfig.DEFAULTCRS_FILENAME);
//		if (defaultCrsUrl == null) {
//			LOGGER.debug("No ad/" + AtlasConfig.DEFAULTCRS_FILENAME
//					+ " not found. Using " + GeoImportUtil.getDefaultCRS());
//		}
//	}
//
//	/**
//	 * @return A XML {@link Document} that fully represents the
//	 *         {@link AtlasConfig} of this atlas
//	 * @throws AtlasException
//	 * @author Stefan Alfons Krüger
//	 * @throws IOException
//	 */
//	public static final Document exportAtlasConfig(
//			AtlasConfigEditable atlasConfig) throws AtlasException, IOException {
//
//		String msg = "Converting Atlas '" + atlasConfig.getTitle()
//				+ "' to AtlasML...";
//		LOGGER.debug(msg);
//		info(msg);
//
//		// Create a DOM builder and parse the fragment
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		Document document = null;
//		try {
//			document = factory.newDocumentBuilder().newDocument();
//		} catch (ParserConfigurationException e) {
//
//			msg = "Saving to AtlasML failed!";
//			info(msg);
//			throw new AtlasFatalException(msg, e);
//		}
//
//		// XML root element
//		Element atlas = document.createElementNS(AMLUtil.AMLURI, "atlas");
//
//		// Linking this XML to the AtlasML Schema
//		Attr namespaces = document.createAttributeNS(
//				"http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
//		namespaces.setValue("http://www.wikisquare.de/AtlasML http://localhost:"
//				+ Webserver.DEFAULTPORT
//				+ "/skrueger/atlas/resource/AtlasML.xsd");
//		atlas.setAttributeNode(namespaces);
//		
//		// Storing the version this atlas.xml is being created with inside the atlas.xml
//		atlas.setAttribute(AMLUtil.ATT_majVersion, String.valueOf(AVUtil.getVersionMaj()));
//		atlas.setAttribute(AMLUtil.ATT_minVersion, String.valueOf(AVUtil.getVersionMin()));
//		atlas.setAttribute(AMLUtil.ATT_buildVersion, String.valueOf(AVUtil.getVersionBuild()));
//
//		// <aml:name, desc, creator, copyright
//		atlas.appendChild(exportTranslation(atlasConfig, document, "name",
//				atlasConfig.getTitle()));
//		atlas.appendChild(exportTranslation(atlasConfig, document, "desc",
//				atlasConfig.getDesc()));
//		atlas.appendChild(exportTranslation(atlasConfig, document, "creator",
//				atlasConfig.getCreator()));
//		atlas.appendChild(exportTranslation(atlasConfig, document, "copyright",
//				atlasConfig.getCopyright()));
//
//		// <aml:atlasversion>
//		Element atlasversion = document.createElementNS(AMLUtil.AMLURI, "atlasversion");
//		atlasversion.appendChild(document.createTextNode(atlasConfig
//				.getAtlasversion().toString()));
//		atlas.appendChild(atlasversion);
//
//		// <aml:supportedLanguages>
//		// Loops over List of supported Languagecodes
//		Element supportedLanguages = document.createElementNS(AMLUtil.AMLURI,
//				"supportedLanguages");
//		for (String langcode : atlasConfig.getLanguages()) {
//			Element language = document.createElementNS(AMLUtil.AMLURI, "language");
//			language.setAttribute("lang", langcode);
//			supportedLanguages.appendChild(language);
//		}
//		atlas.appendChild(supportedLanguages);
//
//		// Loop over all Datapoolentrys and add the created AML
//		for (DpEntry de : atlasConfig.getDataPool().values()) {
//			Node exDpe = null;
//
//			if (de instanceof DpLayer) {
//				// Save the SLD for this layer
//				DpLayer dpl = (DpLayer)de;
//				
//				try {
//					Style style = dpl.getStyle();
//					StylingUtil.saveStyleToSLD(style, DataUtilities.urlToFile(  DataUtilities.changeUrlExt(dpl.getUrl(owner),"sld") ));
//				} catch (Exception e) {
//					LOGGER.error("Could not transform Style for "+dpl,e);
//					ExceptionDialog.show(owner, e);
//				}
//			}
//			
//			if (de instanceof DpLayerVectorFeatureSource) {
//				exDpe = exportDatapoolLayerVector(atlasConfig, document,
//						(DpLayerVectorFeatureSource) de);
//
//			} else if (de instanceof DpLayerRaster) {
//				exDpe = exportDatapoolLayerRaster(document, (DpLayerRaster) de);
//
//			} else if (de instanceof DpLayerRasterPyramid) {
//				exDpe = exportDatapoolLayerRasterPyramid(document,
//						(DpLayerRasterPyramid) de);
//
//			} else if (de instanceof DpMediaVideo) {
//				exDpe = exportDatapoolMediaVideo(document, (DpMediaVideo) de);
//
//			} else if (de instanceof DpMediaPDF) {
//				exDpe = exportDatapoolMediaPdf(document, (DpMediaPDF) de);
//
//			}
//			atlas.appendChild(exDpe);
//
//		}
//
//		// The <aml:maps> tag
//		atlas.appendChild(exportMapPool(atlasConfig, document, atlasConfig
//				.getMapPool()));
//
//		// The <aml:group> tag
//		atlas.appendChild(exportGroup(document, atlasConfig.getFirstGroup()));
//
//		document.appendChild(atlas);
//		// LOGGER.debug("AtlasConfig converted to JDOM Document");
//		return document;
//	}
//
//	private static Node exportDatapoolMediaPdf(Document document, DpMediaPDF dpe) {
//		// LOGGER.debug("exportDatapoolMediaPDF " + dpe + " to AML");
//		Element element = document.createElementNS(AMLUtil.AMLURI, "pdfMedia");
//		element.setAttribute("id", dpe.getId());
//		element.setAttribute("exportable", dpe.isExportable().toString());
//
//		// Creating a aml:name tag...
//		element.appendChild(exportTranslation(dpe.getAc(), document, "name",
//				dpe.getTitle()));
//
//		// Creating aml:desc tag
//		element.appendChild(exportTranslation(dpe.getAc(), document, "desc",
//				dpe.getDesc()));
//
//		// Creating optinal aml:keywords tag
//		if (!dpe.getKeywords().isEmpty())
//			element.appendChild(exportTranslation(dpe.getAc(), document,
//					"keywords", dpe.getKeywords()));
//
//		// Creating a aml:dataDirname tag...
//		Element datadirname = document.createElementNS(AMLUtil.AMLURI, "dataDirname");
//		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
//		element.appendChild(datadirname);
//
//		// Creating a aml:filename tag...
//		Element filename = document.createElementNS(AMLUtil.AMLURI, "filename");
//		filename.appendChild(document.createTextNode(dpe.getFilename()));
//		element.appendChild(filename);
//
//		return element;
//	}
//
//	/**
//	 * Exports all the {@link Map}s that are defined in {@link MapPool}
//	 * 
//	 * @param document
//	 *            {@link Document} to create the element for
//	 * @param mapPool
//	 *            {@link MapPool}
//	 * @throws AtlasExportException
//	 * @throws DOMException
//	 */
//	public static Node exportMapPool(AtlasConfigEditable ace,
//			Document document, MapPool mapPool) throws DOMException,
//			AtlasExportException {
//		Element element = document.createElementNS(AMLUtil.AMLURI, "mapPool");
//
//		if (mapPool.getStartMapID() != null
//				&& mapPool.get(mapPool.getStartMapID()) != null) {
//			element.setAttribute("startMap", mapPool.getStartMapID());
//		}
//
//		// maps MUST contain at least one map
//		Collection<Map> maps = mapPool.values();
//
//		for (Map map : maps) {
//			element.appendChild(exportMap(ace, document, map));
//		}
//		return element;
//	}
//
//	/**
//	 * Exports a {@link Map} to AtlasML
//	 * 
//	 * @param document
//	 * @param map
//	 *            The {@link Map} to export
//	 * @return
//	 * @throws AtlasExportException
//	 */
//	private static Node exportMap(AtlasConfigEditable ace, Document document,
//			Map map) throws AtlasExportException {
//		Element element = document.createElementNS(AMLUtil.AMLURI, "map");
//		element.setAttribute("id", map.getId());
//
//		// Save the ratio of the left and the right components of the mapview
//		Double preferredRatio = map.getLeftRightRatio();
//		if (preferredRatio != null && preferredRatio > 0) {
//			element.setAttribute("leftRightRatio", preferredRatio.toString());
//		}
//
//		// Creating a aml:name tag...
//		element.appendChild(exportTranslation(ace, document, "name", map
//				.getTitle()));
//
//		// Creating aml:desc tag
//		element.appendChild(exportTranslation(ace, document, "desc", map
//				.getDesc()));
//
//		// Creating optinal aml:keywords tag
//		if (!map.getKeywords().isEmpty())
//			element.appendChild(exportTranslation(ace, document, "keywords",
//					map.getKeywords()));
//
//		// Creating optional startViewEnvelope tag ...
//		if (map.getDefaultMapArea() != null) {
//			Element startViewEnvelope = document.createElementNS(AMLUtil.AMLURI,
//					"startViewEnvelope");
//			Envelope area = map.getDefaultMapArea();
//			startViewEnvelope.appendChild(document.createTextNode(area
//					.getMinX()
//					+ ","
//					+ area.getMinY()
//					+ " "
//					+ area.getMaxX()
//					+ ","
//					+ area.getMaxY()));
//			element.appendChild(startViewEnvelope);
//		}
//
//		// Creating optional maxMapExtend tag ...
//		if (map.getMaxExtend() != null) {
//			Element maxExtend = document.createElementNS(AMLUtil.AMLURI, "maxExtend");
//			Envelope area = map.getMaxExtend();
//			maxExtend.appendChild(document.createTextNode(area.getMinX() + ","
//					+ area.getMinY() + " " + area.getMaxX() + ","
//					+ area.getMaxY()));
//			element.appendChild(maxExtend);
//		}
//
//		// Are the vert. and hor. GridPanels visible in this map?
//		element.setAttribute("scaleVisible", Boolean.valueOf(
//				map.isScaleVisible()).toString());
//
//		// Are the vert. and hor. GridPanels visible in this map?
//		element.setAttribute("gridPanelVisible", Boolean.valueOf(
//				map.isGridPanelVisible()).toString());
//		// Which formatter to use for the map grid?
//		element.setAttribute("gridPanelFormatter", map.getGridPanelFormatter()
//				.getId());
//
//		final File outputFile = new File(
//				new File(ace.getHtmlDir(), map.getId()),
//				Map.GRIDPANEL_CRS_FILENAME);
//		try {
//			GeoExportUtil.writeProjectionFilePrefereEPSG(map.getGridPanelCRS(),
//					outputFile);
//		} catch (IOException e1) {
//			throw new AtlasExportException("Failed to save CRS: "
//					+ map.getGridPanelCRS() + " to " + outputFile, e1);
//		}
//
//		// Creating aml:datapoolRef tags for the layers and media of this map
//		for (DpRef dpr : map.getLayers()) {
//			element.appendChild(exportDatapoolRef(document, dpr, map));
//		}
//
//		for (DpRef dpr : map.getMedia()) {
//			element.appendChild(exportDatapoolRef(document, dpr));
//		}
//
//		/***********************************************************************
//		 * Exporting map.getAdditionalStyles and the selected style ID s
//		 */
//		for (String layerID : map.getAdditionalStyles().keySet()) {
//			List<String> styles = map.getAdditionalStyles().get(layerID);
//
//			if (styles.size() == 0)
//				continue;
//
//			Element additionalStyles = document.createElementNS(AMLUtil.AMLURI,
//					"additionalStyles");
//			additionalStyles.setAttribute("layerID", layerID);
//			String selectedStyleID = map.getSelectedStyleID(layerID);
//			if (selectedStyleID != null)
//				additionalStyles.setAttribute("selectedStyleID",
//						selectedStyleID);
//
//			for (String styleID : styles) {
//				Element styleidElement = document.createElementNS(AMLUtil.AMLURI,
//						"styleid");
//				styleidElement.appendChild(document.createTextNode(styleID));
//				additionalStyles.appendChild(styleidElement);
//			}
//
//			element.appendChild(additionalStyles);
//		}
//
//		/***********************************************************************
//		 * Exporting map.getAvailableCharts
//		 */
//		for (String layerID : map.getAvailableCharts().keySet()) {
//			List<String> chartIDs = map.getAvailableChartIDsFor(layerID);
//
//			if (chartIDs.size() == 0)
//				continue;
//
//			Element availableCharts = document.createElementNS(AMLUtil.AMLURI,
//					"availableCharts");
//			availableCharts.setAttribute("layerID", layerID);
//
//			for (String chartID : chartIDs) {
//				Element styleidElement = document.createElementNS(AMLUtil.AMLURI,
//						"chartID");
//				styleidElement.appendChild(document.createTextNode(chartID));
//				availableCharts.appendChild(styleidElement);
//			}
//
//			element.appendChild(availableCharts);
//		}
//
//		return element;
//	}
//
//	/**
//	 * Creates an < aml:desc > JDOM {@link Node} for the given
//	 * {@link Translation}
//	 */
//	public final static Element exportTranslation(AtlasConfig ac,
//			Document document, String tagname, Translation translation) {
//		Element element = document.createElementNS(AMLUtil.AMLURI, tagname);
//
//		// Creating a sequence of <aml:translation> tags, minOccurs=1
//		if (translation == null) {
//			ExceptionDialog
//					.show(
//							getOwner(),
//							new IllegalStateException(
//									"No translation given for "
//											+ tagname
//											+ "\nAtlasMarkupLanguage will probably not be valid."));
//		} else {
//			if (translation.size() == 0) {
//				for (String code : ac.getLanguages()) {
//					translation.put(code, "");
//				}
//
//			}
//			for (String key : translation.keySet()) {
//				Element descTranslation = document.createElementNS(AMLUtil.AMLURI,
//						"translation");
//				descTranslation.setAttribute("lang", key);
//				String string = translation.get(key);
//				if (string == null)
//					string = "";
//				descTranslation.appendChild(document.createTextNode(string));
//				element.appendChild(descTranslation);
//			}
//		}
//		return element;
//	}
//
//	public final static Element exportDatapoolMediaVideo(Document document,
//			DpMediaVideo dpe) {
//		// LOGGER.debug("exportDatapoolMediaVideo " + dpe + " to AML");
//		Element element = document.createElementNS(AMLUtil.AMLURI, "videoMedia");
//		element.setAttribute("id", dpe.getId());
//		element.setAttribute("exportable", dpe.isExportable().toString());
//
//		// Creating a aml:name tag...
//		element.appendChild(exportTranslation(dpe.getAc(), document, "name",
//				dpe.getTitle()));
//
//		// Creating aml:desc tag
//		element.appendChild(exportTranslation(dpe.getAc(), document, "desc",
//				dpe.getDesc()));
//
//		// Creating optinal aml:keywords tag
//		if (!dpe.getKeywords().isEmpty())
//			element.appendChild(exportTranslation(dpe.getAc(), document,
//					"keywords", dpe.getKeywords()));
//
//		// Creating a aml:dataDirname tag...
//		Element datadirname = document.createElementNS(AMLUtil.AMLURI, "dataDirname");
//		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
//		element.appendChild(datadirname);
//
//		// Creating a aml:filename tag...
//		Element filename = document.createElementNS(AMLUtil.AMLURI, "filename");
//		filename.appendChild(document.createTextNode(dpe.getFilename()));
//		element.appendChild(filename);
//
//		return element;
//	}
//
//	/**
//	 * Exports the DatapoolEntry to an AtlasML (XML) document branch
//	 * 
//	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	 *         Kr&uuml;ger</a>
//	 * @throws IOException
//	 *             Thrown if e.g. saving the .cpg fails
//	 */
//	public final static Element exportDatapoolLayerVector(
//			AtlasConfigEditable ace, Document document,
//			DpLayerVectorFeatureSource dpe) throws IOException {
//		// LOGGER.debug("exportDatapoolLayerVector " + dpe + " to AML");
//
//		if (dpe.isBroken()) {
//			LOGGER.info("Trying to save a broken layer..." + dpe);
//		}
//
//		/*
//		 * Saving the Charset to a .cpg file
//		 */
//		GpUtil.saveCpg(dpe);
//
//		// Creating a aml:rasterLayer tag...
//		final Element element = document.createElementNS(AMLUtil.AMLURI, "vectorLayer");
//		element.setAttribute("id", dpe.getId());
//		element.setAttribute("exportable", dpe.isExportable().toString());
//
//		/***********************************************************************
//		 * The "showStylerInLegend" attribute is optional and defaults to true
//		 ***********************************************************************/
//		if (dpe.isStylerInLegend()) {
//			element.setAttribute("showStylerInLegend", "true");
//		} else {
//			element.setAttribute("showStylerInLegend", "false");
//		}
//
//		/***********************************************************************
//		 * The "showTableInLegend" attribute is optional and defaults to false
//		 ***********************************************************************/
//		if (dpe.isTableVisibleInLegend()) {
//			element.setAttribute("showTableInLegend", "true");
//		} else {
//			element.setAttribute("showTableInLegend", "false");
//		}
//
//		/***********************************************************************
//		 * The "filterInLegend" attribute is optional and defaults to false
//		 ***********************************************************************/
//		if (dpe.isFilterInLegend()) {
//			element.setAttribute("showFilterInLegend", "true");
//		} else {
//			element.setAttribute("showFilterInLegend", "false");
//		}
//
//		// Creating a aml:name tag...
//		element.appendChild(exportTranslation(dpe.getAc(), document, "name",
//				dpe.getTitle()));
//
//		// Creating aml:desc tag
//		element.appendChild(exportTranslation(dpe.getAc(), document, "desc",
//				dpe.getDesc()));
//
//		// Creating optinal aml:keywords tag
//		if (!dpe.getKeywords().isEmpty())
//			element.appendChild(exportTranslation(dpe.getAc(), document,
//					"keywords", dpe.getKeywords()));
//
//		// Creating a aml:dataDirname tag...
//		final Element datadirname = document.createElementNS(AMLUtil.AMLURI,
//				"dataDirname");
//		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
//		element.appendChild(datadirname);
//
//		// Creating a aml:filename tag...
//		final Element filename = document.createElementNS(AMLUtil.AMLURI, "filename");
//		filename.appendChild(document.createTextNode(dpe.getFilename()));
//		element.appendChild(filename);
//
//		for (AttributeMetadata attrib : dpe.getAttributeMetaDataMap().values()) {
//			Element att = exportDataAttribute(dpe, dpe.getAc(), document,
//					attrib);
//			if (att != null)
//				element.appendChild(att);
//		}
//
//		/**
//		 * Exporting the additional and optional LayerStyles
//		 */
//		for (LayerStyle ls : dpe.getLayerStyles()) {
//			element.appendChild(exportLayerStyle(dpe.getAc(), document, ls));
//		}
//
//		/**
//		 * This parameter is optional and is only created if needed.
//		 */
//		Filter filter = dpe.getFilter();
//		if (filter != Filter.INCLUDE) {
//			// Creating a aml:filename tag...
//			final Element filterRuleElement = document.createElementNS(AMLUtil.AMLURI,
//					"filterRule");
//			filterRuleElement.appendChild(document.createTextNode( CQL.toCQL(filter)));
//			element.appendChild(filterRuleElement);
//		}
//
//		/**
//		 * Exporting the list of charts for this layer if any exist
//		 */
//		exportChartStyleDescriptions(document, dpe, element);
//
//		return element;
//
//	}
//
//	/**
//	 * Exports the list of charts for this layer. Any old chart style files are
//	 * first deleted.
//	 */
//	private static void exportChartStyleDescriptions(final Document document,
//			final DpLayerVectorFeatureSource dpe, final Element element) {
//
//		final AtlasConfigEditable ace = (AtlasConfigEditable) dpe.getAc();
//
//		File chartsFolder = new File(ace.getFileFor(dpe).getParentFile(),
//				"charts");
//		chartsFolder.mkdirs();
//		/*
//		 * Delete all .cs file before saving the charts that actually exist
//		 */
//		{
//
//			String[] oldChartFilenames = chartsFolder
//					.list(new FilenameFilter() {
//
//						@Override
//						public boolean accept(File dir, String name) {
//							if (name.endsWith(CHARTSTYLE_FILEENDING))
//								return true;
//							return false;
//						}
//
//					});
//			for (String oldChartFilename : oldChartFilenames) {
//				File chartSTyeFile = new File(chartsFolder, oldChartFilename);
//				if (!chartSTyeFile.delete())
//					throw new IllegalArgumentException(
//							"Unable to delte the old chart description file "
//									+ chartSTyeFile.getAbsolutePath());
//			}
//		}
//
//		/* Iterate over all charts and create .chart files */
//		for (FeatureChartStyle chartStyle : dpe.getCharts()) {
//
//			String csFilename = chartStyle.getID() + CHARTSTYLE_FILEENDING;
//
//			File chartFile = new File(chartsFolder, csFilename);
//
//			try {
//				/*
//				 * Write the Chart to XML
//				 */
//				if (chartStyle instanceof FeatureChartStyle) {
//					FeatureChartUtil.FEATURE_CHART_STYLE_FACTORY
//							.writeStyleToFile((FeatureChartStyle) chartStyle,
//									"chartStyle", chartFile);
//				} else {
//					ChartStyleUtil.CHART_STYLE_FACTORY.writeStyleToFile(
//							chartStyle, "chartStyle", chartFile);
//				}
//			} catch (Exception e) {
//				LOGGER.error("Error writing the ChartStyle to XML ", e);
//			}
//
//			// Create an aml:chart tag...
//			final Element chartElement = document.createElementNS(AMLUtil.AMLURI,
//					"chart");
//			chartElement.setAttribute("filename", csFilename);
//			element.appendChild(chartElement);
//		}
//	}
//
//	/**
//	 * Exports a <layerStyle> tag
//	 * 
//	 * @param ac
//	 * @param document
//	 * @param ls
//	 *            {@link LayerStyle} to export
//	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	 *         Kr&uuml;ger</a>
//	 */
//	private static Element exportLayerStyle(AtlasConfig ac, Document document,
//			LayerStyle ls) {
//		Element element = document.createElementNS(AMLUtil.AMLURI, "layerStyle");
//
//		element.setAttribute("filename", ls.getFilename());
//		element.setAttribute("id", ls.getFilename());
//
//		element.appendChild(exportTranslation(ac, document, "title", ls
//				.getTitle()));
//
//		element.appendChild(exportTranslation(ac, document, "desc", ls
//				.getDesc()));
//
//		return element;
//	}
//
//	/**
//	 * Exports one single {@link AttributeMetadata} to an aml:dataAttribute tag.
//	 * 
//	 * @return {@link org.w3c.dom.Element} that represent the XML tag
//	 * 
//	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	 *         Kr&uuml;ger</a>
//	 * @param dpe
//	 *            For backwardcompatibility we also write the col attribute. The
//	 *            'col' attribute has been abandoned in 1.3
//	 */
//	private static Element exportDataAttribute(DpLayerVectorFeatureSource dpe,
//			AtlasConfig ac, Document document, AttributeMetadata attrib) {
//		// Creating a aml:rasterLayer tag...
//		Element element = document.createElementNS(AMLUtil.AMLURI, "dataAttribute");
//
//		element.setAttribute(AMLUtil.ATT_namespace, attrib.getName().getNamespaceURI());
//		element.setAttribute(AMLUtil.ATT_localname, attrib.getName().getLocalPart());
//		
//		element.setAttribute(AMLUtil.ATT_weight, new Integer(attrib.getWeight()).toString());
//		element.setAttribute(AMLUtil.ATT_functionA, new Double(attrib.getFunctionA()).toString());
//		element.setAttribute(AMLUtil.ATT_functionX, new Double(attrib.getFunctionX()).toString());
//
//		try { // TODO backward compatibility. remove in 1.4
//			final SimpleFeatureType schema = dpe.getFeatureSource().getSchema();
//			boolean found = false;
//			for (int i = 0; i < schema.getAttributeCount(); i++) {
//				if (schema.getAttributeDescriptors().get(i).getName().equals(
//						attrib.getName())) {
//					element.setAttribute("col", String.valueOf(i));
//					found = true;
//					break;
//				}
//			}
//			if (!found) {
//				LOGGER.warn("No column for attrib " + attrib.getLocalName()
//						+ " found. Throwing the metadata away...");
//				return null;
//			}
//		} catch (Exception e) {
//			ExceptionDialog
//					.show(
//							owner,
//							new AtlasException(
//									"Could not determine the indexes of attribute names '"
//											+ attrib.getLocalName()
//											+ "' of layer '"
//											+ dpe.getTitle()
//											+ "'.\n This is used for backward compatibility with GP 1.2 only.",
//									e));
//		}
//
//		element.setAttribute("visible", String.valueOf(attrib.isVisible()));
//
//		if (attrib.getUnit() != null && !attrib.getUnit().isEmpty())
//			element.setAttribute("unit", attrib.getUnit());
//
//		// Creating a aml:name tag...
//		element.appendChild(exportTranslation(ac, document, "name", attrib
//				.getTitle()));
//
//		// Creating a aml:desc tag...
//		element.appendChild(exportTranslation(ac, document, "desc", attrib
//				.getDesc()));
//
//		return element;
//	}
//
//	/**
//	 * Exports the {@link DpLayerRaster} to a AtlasML (XML) Document branch
//	 * 
//	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	 *         Kr&uuml;ger</a>
//	 */
//	public final static Element exportDatapoolLayerRaster(Document document,
//			DpLayerRaster dpe) {
//
//		// Creating a aml:rasterLayer tag...
//		Element element = document.createElementNS(AMLUtil.AMLURI, "rasterLayer");
//		element.setAttribute("id", dpe.getId());
//		element.setAttribute("exportable", dpe.isExportable().toString());
//
//		// Creating a aml:name tag...
//		element.appendChild(exportTranslation(dpe.getAc(), document, "name",
//				dpe.getTitle()));
//
//		// Creating aml:desc tag
//		element.appendChild(exportTranslation(dpe.getAc(), document, "desc",
//				dpe.getDesc()));
//
//		// Creating optinal aml:keywords tag
//		if (!dpe.getKeywords().isEmpty())
//			element.appendChild(exportTranslation(dpe.getAc(), document,
//					"keywords", dpe.getKeywords()));
//
//		// Creating a aml:dataDirname tag...
//		Element datadirname = document.createElementNS(AMLUtil.AMLURI, "dataDirname");
//		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
//		element.appendChild(datadirname);
//
//		// Creating a aml:filename tag...
//		Element filename = document.createElementNS(AMLUtil.AMLURI, "filename");
//		filename.appendChild(document.createTextNode(dpe.getFilename()));
//		element.appendChild(filename);
//
//		// Creating aml:rasterLegendData
//		element.appendChild(exportRasterLegendData(dpe.getAc(), document, dpe
//				.getLegendMetaData()));
//		
//		return element;
//	}
//
//	/**
//	 * Export a aml:rasterLegendData tag
//	 * 
//	 * @param document
//	 * @param legendMetaData
//	 * @return
//	 * 
//	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	 *         Kr&uuml;ger</a>
//	 */
//	private static Node exportRasterLegendData(AtlasConfig ac,
//			Document document, RasterLegendData legendMetaData) {
//		Element element = document.createElementNS(AMLUtil.AMLURI, "rasterLegendData");
//		element.setAttribute("paintGaps", legendMetaData.isPaintGaps()
//				.toString());
//
//		for (Double key : legendMetaData.getSortedKeys()) {
//			Element item = document.createElementNS(AMLUtil.AMLURI, "rasterLegendItem");
//			item.setAttribute("value", key.toString());
//			item.appendChild(exportTranslation(ac, document, "label",
//					legendMetaData.get(key)));
//			element.appendChild(item);
//		}
//		return element;
//	}
//
//	/**
//	 * Exports the {@link DpLayerRasterPyramid} to a AtlasML (XML) Document
//	 * branch
//	 * 
//	 * @author Stefan Alfons Krüger
//	 */
//	public final static Element exportDatapoolLayerRasterPyramid(
//			Document document, DpLayerRasterPyramid dpe) {
//		// LOGGER.debug("exportDatapoolLayerRasterPyramid " + dpe + " to AML");
//
//		// Creating a aml:rasterLayer tag...
//		Element element = document
//				.createElementNS(AMLUtil.AMLURI, "pyramidRasterLayer");
//		element.setAttribute("id", dpe.getId());
//		element.setAttribute("exportable", dpe.isExportable().toString());
//
//		// Creating a aml:name tag...
//		element.appendChild(exportTranslation(dpe.getAc(), document, "name",
//				dpe.getTitle()));
//
//		// Creating aml:desc tag
//		element.appendChild(exportTranslation(dpe.getAc(), document, "desc",
//				dpe.getDesc()));
//
//		// Creating optinal aml:keywords tag
//		if (!dpe.getKeywords().isEmpty())
//			element.appendChild(exportTranslation(dpe.getAc(), document,
//					"keywords", dpe.getKeywords()));
//
//		// Creating a aml:dataDirname tag...
//		Element datadirname = document.createElementNS(AMLUtil.AMLURI, "dataDirname");
//		datadirname.appendChild(document.createTextNode(dpe.getDataDirname()));
//		element.appendChild(datadirname);
//
//		// Creating a aml:filename tag... (.properties file)
//		Element filename = document.createElementNS(AMLUtil.AMLURI, "filename");
//		filename.appendChild(document.createTextNode(dpe.getFilename()));
//		element.appendChild(filename);
//
//		// Creating aml:rasterLegendData
//		element.appendChild(exportRasterLegendData(dpe.getAc(), document, dpe
//				.getLegendMetaData()));
//
//		// Creating an optional aml:transparentColor tag...
//		final Color color = dpe.getInputTransparentColor();
//		if (color != null) {
//			Element transparentColor = document.createElementNS(AMLUtil.AMLURI,
//					"transparentColor");
//			final String colorStr = "RGB(" + color.getRed() + ","
//					+ color.getGreen() + "," + color.getBlue() + ")";
//			transparentColor.appendChild(document.createTextNode(colorStr));
//			element.appendChild(transparentColor);
//		}
//
//		return element;
//	}
//
//	/**
//	 * Create a tree of <aml:group> and <aml:datapoolRef>
//	 * 
//	 * @param group
//	 *            The {@link Group} to start with
//	 * @throws AtlasFatalException
//	 */
//	public final static Element exportGroup(Document document, Group group)
//			throws AtlasFatalException {
//		// LOGGER.debug("exportGroup " + group + " to AML");
//		Element element = document.createElementNS(AMLUtil.AMLURI, "group");
//
//		// Store whether this is marked as the Help menu in an optional
//		// attribute
//		if (group.isHelpMenu()) {
//			String isHelpString = Boolean.valueOf(group.isHelpMenu())
//					.toString();
//			element.setAttribute("isHelpMenu", isHelpString);
//		}
//
//		// Store whether this is marked as the File menu in an optional
//		// attribute
//		if (group.isFileMenu()) {
//			String isFileString = Boolean.valueOf(group.isFileMenu())
//					.toString();
//			element.setAttribute("isFileMenu", isFileString);
//		}
//
//		// Creating a aml:name tag...
//		element.appendChild(exportTranslation(group.getAc(), document, "name",
//				group.getTitle()));
//
//		// Creating aml:desc tag
//		element.appendChild(exportTranslation(group.getAc(), document, "desc",
//				group.getDesc()));
//
//		// Creating optinal aml:keywords tag
//		if (!group.getKeywords().isEmpty())
//			element.appendChild(exportTranslation(group.getAc(), document,
//					"keywords", group.getKeywords()));
//
//		Enumeration<TreeNode> children = group.children();
//		while (children.hasMoreElements()) {
//			TreeNode nextElement = children.nextElement();
//			if (nextElement instanceof Group) {
//				Group subGroup = (Group) nextElement;
//				element.appendChild(exportGroup(document, subGroup));
//
//			} else if (nextElement instanceof DpRef) {
//				DpRef mref = (DpRef) nextElement;
//				element.appendChild(exportDatapoolRef(document, mref));
//
//			} else if (nextElement instanceof MapRef) {
//				MapRef mref = (MapRef) nextElement;
//				Element datapoolRef = document
//						.createElementNS(AMLUtil.AMLURI, "mapRef");
//				datapoolRef.setAttribute("id", mref.getTargetId());
//				element.appendChild(datapoolRef);
//
//			} else {
//				throw new AtlasFatalException("Can't export Group " + group
//						+ " because of an unknown TreeNode " + nextElement
//						+ " of class " + nextElement.getClass().getSimpleName());
//			}
//		}
//		return element;
//	}
//
//	/**
//	 * Creates an aml:datapoolRef tag
//	 * 
//	 * @param ref
//	 *            {@link DpRef}
//	 * @return A node that can be inserted into XML
//	 */
//	private static Element exportDatapoolRef(Document document, DpRef ref) {
//		Element datapoolRef = document.createElementNS(AMLUtil.AMLURI, "datapoolRef");
//		datapoolRef.setAttribute("id", ref.getTargetId());
//
//		return datapoolRef;
//	}
//
//	/**
//	 * DataPoolRefs used inside a <code>aml:map</code> definition can have two
//	 * more attributes which are saved inside a {@link java.util.Map} in the
//	 * {@link Map}.
//	 * 
//	 * @param document
//	 * @param dpr
//	 * @param map
//	 * @return A node that can be inserted into XML
//	 */
//	private static Node exportDatapoolRef(Document document, DpRef dpr, Map map) {
//		Element datapoolRef = exportDatapoolRef(document, dpr);
//
//		{ // Add the optional hideInLegend attribute if it is set. false =
//			// default
//			Boolean hideme = map.getHideInLegendMap().get(dpr.getTargetId());
//			if (hideme != null && hideme == true) {
//				datapoolRef.setAttribute("hideInLegend", "true");
//			}
//		}
//
//		{ // Add the optional minimizeInLegend attribute if it is set. false =
//			// default
//			Boolean minimizeMe = map.getMinimizedInLegendMap().get(
//					dpr.getTargetId());
//			if (minimizeMe != null && minimizeMe == true) {
//				datapoolRef.setAttribute("minimizeInLegend", "true");
//			}
//		}
//
//		{ // Add the optional hidden attribute if it is set. false =
//			// default
//			Boolean hidden = map.getHiddenFor(dpr.getTargetId());
//			if (hidden != null && hidden == true) {
//				datapoolRef.setAttribute("hidden", "true");
//			}
//		}
//
//		{ // Add the optional selectable attribute if it is set. true =
//			// default
//			Boolean selectable = map.isSelectableFor(dpr.getTargetId());
//			if (selectable != null && selectable == false) {
//				datapoolRef.setAttribute("selectable", "false");
//			}
//		}
//
//		return datapoolRef;
//	}
//
//	/**
//	 * Saves the {@link AtlasConfigEditable} to projdir/atlas.xml
//	 * 
//	 * @return true if no exceptions where thrown.
//	 */
//	public static boolean saveAtlasConfigEditable(Component owner,
//			AtlasConfigEditable ace) {
//		AMLExport.setOwner(owner);
//		// ****************************************************************************
//		// Trying to make a bakup
//		// ****************************************************************************
//		try {
//			AVUtil.copyFile(LOGGER, new File(ace.getAd(), "atlas.xml.bak.bak"),
//					new File(ace.getAd(), "atlas.xml.bak.bak.bak"), false);
//			AVUtil.copyFile(LOGGER, new File(ace.getAd(), "atlas.xml.bak"),
//					new File(ace.getAd(), "atlas.xml.bak.bak"), false);
//			AVUtil.copyFile(LOGGER, new File(ace.getAd(), "atlas.xml"),
//					new File(ace.getAd(), "atlas.xml.bak"), false);
//		} catch (FileNotFoundException e0) {
//			// No problem .bak.bak not existing yet.
//		} catch (IOException e1) {
//			LOGGER
//					.info("Making backups of atlas.xml failed:"
//							+ e1.getMessage());
//			LOGGER.error("Making backups of atlas.xml failed:", e1);
//		}
//
//		final String msg = "Saving Atlas " + ace.getTitle().toString() + " to "
//				+ ace.getAd().getAbsolutePath() + "/atlas.xml.";
//		LOGGER.debug(msg);
//		info(msg);
//
//		try {
//			// Prepare the DOM document for writing
//			Source source = new DOMSource(AMLExport.exportAtlasConfig(ace));
//			
//			// Prepare the output file
//			File file = new File(ace.getAd(), "atlas.xml");
//			if (!file.exists()) {
//				LOGGER.info("atlas.xml is beeing freshly created");
//				file.createNewFile();
//			}
//			// Result result = new StreamResult(file);
//			Result result = new StreamResult(new OutputStreamWriter(
//					new FileOutputStream(file), "utf-8"));
//
//			// with indenting to make it human-readable
//			TransformerFactory tf = TransformerFactory.newInstance();
//
//			// TODO Ging mit xerces, geht nicht mehr mit xalan
//			// tf.setAttribute("indent-number", new Integer(2));
//
//			Transformer xformer = tf.newTransformer();
//			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
//			xformer.setOutputProperty(
//					"{http://www.wikisquare.de/AtlasML.xsd}indent-amount", "4");
//			xformer.setOutputProperty(
//					"{http://xml.apache.org/xalan}indent-amount", "2");
//
//			// Write the DOM document to the file
//			xformer.transform(source, result);
//
//			try {
//				// Copy Schema AtlasML.xsd to projectDir
//				LOGGER.debug("Copy Schema AtlasML.xsd into " + ace.getAd());
//
//				URL resourceSchema = AtlasConfig.class
//						.getResource("resource/AtlasML.xsd");
//				LOGGER.debug("schemaURL = " + resourceSchema);
//				if (resourceSchema == null) {
//					LOGGER.debug("schemaURL == null, try the new way");
//					final String location = "skrueger/atlas/resource/AtlasML.xsd";
//					resourceSchema = AtlasConfig.getResLoMan()
//							.getResourceAsUrl(location);
//					// LOGGER.debug("schemaURL (new) = " + resourceSchema);
//				}
//
//				// File schemaFile = new File(resourceSchema.toURI());
//				org.apache.commons.io.FileUtils.copyURLToFile(resourceSchema,
//						new File(ace.getAd(), "AtlasML.xsd"));
//			} catch (Exception e) {
//				ExceptionDialog.show(owner, new AtlasException(e));
//				LOGGER.debug(" Error while copying AtlasML.xsd... ignoring");
//			}
//
//			LOGGER.debug(" saving AtlasConfig... finished.");
//			return true;
//		} catch (Exception e) {
//			LOGGER.error(" saving AtlasConfig... failed with exception:", e);
//			ExceptionDialog.show(owner, e);
//			return false;
//		}
//
//	}
//
//	public static void setOwner(Component owner) {
//		AMLExport.owner = owner;
//	}
//
//	public static Component getOwner() {
//		return owner;
//	}
//
//}
