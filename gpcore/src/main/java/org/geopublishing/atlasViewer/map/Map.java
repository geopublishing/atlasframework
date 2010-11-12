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
package org.geopublishing.atlasViewer.map;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.dp.media.DpMedia;
import org.geopublishing.atlasViewer.http.Webserver;
import org.geopublishing.atlasViewer.swing.JNLPSwingUtil;
import org.geopublishing.geopublisher.GpUtil;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.geotools.gui.GeotoolsGUIUtil;
import schmitzm.geotools.gui.GridPanel;
import schmitzm.geotools.gui.GridPanelFormatter;
import schmitzm.geotools.gui.ScalePane;
import schmitzm.geotools.gui.ScalePanel.ScaleUnits;
import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.geotools.Copyable;
import skrueger.geotools.MapView;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;
import skrueger.swing.swingworker.AtlasStatusDialogInterface;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A {@link Map} is a collection of references to {@link DpEntry} and HTML info
 * pages The {@link MapView} can present a {@link Map}
 * 
 * @author Stefan Alfons Tzeggai
 * 
 */
public class Map extends DefaultMutableTreeNode implements Comparable<Object>,
		Copyable<Map> {
	final static private Logger LOGGER = Logger.getLogger(Map.class);

	public static final String GRIDPANEL_CRS_FILENAME = "gridPanel.prj";

	/**
	 * A list that holds the {@link DpRef} s that are of type {@link DpLayer}
	 */
	private List<DpRef<DpLayer<?, ? extends ChartStyle>>> layers = new LinkedList<DpRef<DpLayer<?, ? extends ChartStyle>>>();

	/**
	 * Define a preferred ratio between the left and right components of the
	 * MapView. If <code>null</code> or 0f it will use the automatic mode.
	 */
	private Double leftRightRatio = 0.;

	/**
	 * A list that holds the {@link DpRef} s that are of type {@link DpMedia}
	 */
	List<DpRef<DpMedia<? extends ChartStyle>>> media = new LinkedList<DpRef<DpMedia<? extends ChartStyle>>>();

	private Translation title = new Translation(), desc = new Translation(),
			keywords = new Translation();

	final private java.util.Map<String, Boolean> minimizedInLegendMap = new HashMap<String, Boolean>();
	final private java.util.Map<String, Boolean> hideInLegendMap = new HashMap<String, Boolean>();
	final private java.util.Map<String, Boolean> hidden = new HashMap<String, Boolean>();
	final private java.util.Map<String, Boolean> selectable = new HashMap<String, Boolean>();

	/** A map has an unique ID {@link String} */
	private String id;

	/** The default MapArea to show when the map opens * */
	private Envelope defaultMapArea;

	/** A link to the {@link AtlasConfig} where this {@link Map} is configured. **/
	private AtlasConfig ac = null;

	/** Shall the map scale be visible? **/
	private boolean scaleVisible = true;
	/** Shall the horiz. and vertical {@link GridPanel}s be visible at all? **/
	private boolean gridPanelVisible = true;
	/**
	 * The {@link CoordinateReferenceSystem} used in the horiz. and vertical
	 * {@link GridPanel}s
	 **/
	private CoordinateReferenceSystem gridPanelCRS = GeoImportUtil
			.getDefaultCRS();
	/**
	 * The {@link GridPanelFormatter} used in the horiz. and vertical
	 * {@link GridPanel}s
	 **/
	private GridPanelFormatter gridPanelFormatter;

	/**
	 * Creates a {@link Map}. The ID may not start with a number!
	 */
	public Map(final String id, final AtlasConfig ac) {
		if (!id.startsWith("map"))
			throw new IllegalArgumentException(
					"Map IDs have to start with 'map'");
		this.id = id;
		this.ac = ac;
	}

	/**
	 * Creates a {@link Map} and defines a random ID
	 */
	public Map(final AtlasConfig ac) {
		this(GpUtil.getRandomID("map"), ac);
	}

	/**
	 * This {@link java.util.Map} holds a {@link List} of {@link ChartStyle}-IDs
	 * for every {@link DpLayer} (referred to by its ID) *
	 */
	final private java.util.Map<String, ArrayList<String>> availableCharts = new HashMap<String, ArrayList<String>>();

	/**
	 * This {@link java.util.Map} holds a {@link List} of {@link LayerStyle}-IDs
	 * for every {@link DpLayer} (referred to by its ID) *
	 */
	private java.util.Map<String, ArrayList<String>> additionalStyles = new HashMap<String, ArrayList<String>>();

	/** This {@link java.util.Map} links LayerIDs to {@link LayerStyle} - IDs * */
	volatile private java.util.Map<String, String> selectedStyleID = new HashMap<String, String>();

	volatile private ArrayList<String> missingHTMLLanguages;

	/**
	 * The biggest visible extend for this map. If <code>null</code>, the user
	 * may zoom out as much as he wants.
	 **/
	private Envelope maxExtend = null;

	/** The units to show the scale in **/
	private ScaleUnits scaleUnits = ScaleUnits.METRIC;

	/**
	 * If <code>true</code>, the max. map extend will be applied in the
	 * MapComposer. Otherwise it will will only be applied in the exported Atlas
	 **/
	private boolean previewMapExtendInGeopublisher = false;

	private Double quality;
	long qualityLastCalced;

	/**
	 * Caches a list of all CRS names used in this map
	 */
	private ArrayList<String> cacheUsedCrs;
	/**
	 * Remebers when this list has been calculated the last time
	 */
	private long cacheUsedCrsLastCheckedTime;

	/**
	 * Resets the cache that remembers for which languages the HTML info pages
	 * exist.
	 */
	public void resetMissingHTMLinfos() {
		missingHTMLLanguages = null;
	}

	/**
	 * Removes all Layers aka DpLayerReferences from the {@link Map}
	 */
	public void clearLayerList() {
		layers.clear();
	}

	/**
	 * Removes all memory-intensive cached objects that are not needed by the
	 * {@link Map} newMap
	 * 
	 * @param newMap
	 *            If <code>null</code>, then all referenced {@link DpEntry}s are
	 *            uncached..
	 * 
	 */
	public void uncache(final Map newMap) {
		// LOGGER.debug("Uncaching map " + getId() + " aka " + getTitle());

		List<DpRef<DpLayer<?, ? extends ChartStyle>>> newLayers;
		List<DpRef<DpMedia<? extends ChartStyle>>> newMedia;

		if (newMap != null) {
			newLayers = newMap.getLayers();
			newMedia = newMap.getMedia();
		} else {

			// Creates them empty so all DpEnties are uncached..
			newLayers = new LinkedList<DpRef<DpLayer<?, ? extends ChartStyle>>>();
			newMedia = new LinkedList<DpRef<DpMedia<? extends ChartStyle>>>();
		}

		// Uncaching all DpMedia if not needed in next Map
		for (final DpRef<DpMedia<? extends ChartStyle>> dpr : getMedia()) {
			if (!newMedia.contains(dpr))
				dpr.getTarget().uncache();
		}

		// Uncaching all DpLayers if not needed in next Map
		for (final DpRef<DpLayer<?, ? extends ChartStyle>> dpr : getLayers()) {
			if (!newLayers.contains(dpr))
				dpr.getTarget().uncache();

			// Forget about the style changes. This may not be called from GP!
			dpr.getTarget().resetStyleChanges();
		}

		cacheUsedCrs = null;
		quality = null;
	}

	/**
	 * Ideally a {@link Map} only uses one CRS, as especially raster re-sampling
	 * is very slow If first layer is always defining the CRS.. so moving the
	 * layers can change the base CRS!
	 * 
	 * @return null if no Layer is set yet
	 */
	public CoordinateReferenceSystem getLayer0Crs() {

		// TODO Add a forceCrs as a Map property!
		final int size = getLayers().size();
		if (size > 0) {
			// The last layer's CRS equals the first layer in the MapContext
			return (getLayers().get(size - 1).getTarget()).getCrs();
		}
		return null;
	}

	@Override
	/*
	 * id and number of layers must equal
	 */
	public boolean equals(final Object obj) {
		if (obj instanceof Map) {
			final Map map = (Map) obj;
			return map.getId().equals(getId())
					&& map.getLayers().size() == getLayers().size()
					&& map.getMedia().size() == getMedia().size();
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * getId().hashCode() * getLayers().size() * 52
				* getMedia().size() * 7;
	}

	/**
	 * creates a new {@link Map} object with the same parameters and the same
	 * id! change the id before adding it to the mapPool
	 * 
	 * @return
	 */
	@Override
	public Map copy() {
		// final Map newMap = (Map) super.clone();
		final Map newMap = new Map(id, getAc());
		// newMap.setId(id);
		// newMap.setAtlasConfig(getAc());
		copyTo(newMap);
		return newMap;
	}

	/**
	 * Define the AtlasConfig this {@link Map} belongs to. Primarely written for
	 * importing maps from other atlases.
	 */
	public void setAtlasConfig(final AtlasConfig newAtlasConfig) {
		ac = newAtlasConfig;
	}

	/**
	 * Add a {@link DpRef} to the {@link Map} Used when loading or editing the
	 * {@link Map} This method identifies the reference as either Layer or
	 * Media.. The first Ref to a {@link DpLayer} sets the CRS for this
	 * {@link Map}
	 * 
	 * @param dpRef
	 *            {@link DpRef} to add.
	 */
	public void add(final DpRef<DpEntry<? extends ChartStyle>> dpRef) {
		add(dpRef, -1);
	}

	/**
	 * Add a {@link DpRef} to the {@link Map} at the given position Used when
	 * loading or editing the {@link Map} This method identifies the reference
	 * as either Layer or Media.. The first Ref to a {@link DpLayer} sets the
	 * CRS for this {@link Map}
	 * 
	 * @param dpRef
	 *            {@link DpRef} to add.
	 */
	public void add(final DpRef dpRef, int whereIdx) {
		if ((whereIdx < 0) || (whereIdx > getLayers().size())) {
			whereIdx = getLayers().size();
		}

		final DpEntry<?> dpe = dpRef.getTarget();
		if (dpe.isLayer()) {
			layers.add(whereIdx, dpRef);
		} else {
			media.add(whereIdx, dpRef);
		}
	}

	final public Translation getTitle() {
		return title;
	}

	final public Translation getDesc() {
		return desc;
	}

	final public String getId() {
		return id;
	}

	final public void setId(final String id) {
		this.id = id;
	}

	final public void setTitle(final Translation title) {
		this.title = title;
	}

	final public void setDesc(final Translation desc) {
		this.desc = desc;
	}

	@Override
	final public String toString() {
		String returnStr = "";
		if (!I8NUtil.isEmpty(title))
			return returnStr += title.toString() + ", ";
		returnStr += ", ID=" + getId();
		if (!I8NUtil.isEmpty(desc))
			returnStr += ", Desc=" + getDesc();
		return returnStr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	final public int compareTo(final Object o) {
		if (o instanceof Map) {
			final Map map = (Map) o;
			if (map.getTitle() != null) {
				// Internationalisierte Sortierung ;-)
				return (map.getTitle().toString().compareTo(title.toString()));
			}
			return (map.getId().compareTo(id));
		}
		return 0;
	}

	final public Translation getKeywords() {
		return keywords;
	}

	final public void setKeywords(final Translation keywords) {
		this.keywords = keywords;
	}

	/**
	 * Returns the {@link List} of {@link DpRef} to all {@link DpLayer}s
	 * referenced by this {@link Map}.
	 */
	final public List<DpRef<DpLayer<?, ? extends ChartStyle>>> getLayers() {
		return layers;
	}

	final public void setLayers(
			final LinkedList<DpRef<DpLayer<?, ? extends ChartStyle>>> newLayers) {
		this.layers = newLayers;
	}

	/**
	 * Returns the {@link List} of {@link DpRef} to {@link DpMedia} referenced
	 * by this {@link Map}.
	 */
	final public List<DpRef<DpMedia<? extends ChartStyle>>> getMedia() {
		return media;
	}

	/**
	 * Returns a {@link List} of {@link DpRef} to all Layers and Media (DpEntry)
	 * referenced by this map.
	 */
	final public List<DpRef<?>> getDpes() {
		List<DpRef<?>> allEntries = new ArrayList<DpRef<?>>();
		allEntries.addAll(getLayers());
		allEntries.addAll(getMedia());
		return allEntries;
	}

	/**
	 * Set the List of {@link DpRef}s that are Media and are related to this
	 * {@link Map}
	 * 
	 * @param media
	 */
	final public void setMedia(
			final List<DpRef<DpMedia<? extends ChartStyle>>> media) {
		this.media = media;
	}

	/**
	 * Returns true if the Ref is already in the map
	 * 
	 * @param datapoolRef
	 */
	final public boolean has(final DpRef datapoolRef) {
		for (final DpRef dpr : layers) {
			if (dpr.equals(datapoolRef))
				return true;
		}
		for (final DpRef dpr : media) {
			if (dpr.equals(datapoolRef))
				return true;
		}
		return false;
	}

	/**
	 * @return A {@link URL} to the HTML info for this {@link Map}. The
	 *         {@link URL} will point to a <code>index_LANGCODE.html</code>
	 *         file. <code>null</code> is returned, if no html file is found for
	 *         the active language.
	 */
	final public URL getInfoURL() {
		// LOGGER.debug("Map info URL = "
		// + getInfoURL(Translation.getActiveLang()));
		return getInfoURL(Translation.getActiveLang());
	}

	/**
	 * @return A {@link URL} to the HTML info for this {@link Map}. The
	 *         {@link URL} will point to a <code>index_LANGCODE.html</code>
	 *         file. <code>null</code> is returned, if no HTML file is found for
	 *         the active language.
	 */
	private URL getInfoURL(final String activeLang) {
		// That was too slow String:
		// urlString = Webserver.getDocumentBase() + "/" +
		// getId() + "/" + "index.html";
		final String urlString = "http://127.0.0.1:" + Webserver.PORT + "/"
				+ AtlasConfig.ATLASDATA_DIRNAME + "/"
				+ AtlasConfig.HTML_DIRNAME + "/" + getId() + "/" + "index_"
				+ Translation.getActiveLang() + ".html";
		// LOGGER.debug("URLstring fuer HTML mapinfo = " + urlString);

		try {
			final URL url = new URL(urlString);
			url.openStream().close();
			return url;
		} catch (final IOException ee) {
			// LOGGER.debug("Not possible to access " + urlString, ee);
			// That is OK. They don't have to exist.
			return null;
		}
	}

	/**
	 * Retunes the default MapArea (which will be shown when the Map starts...)
	 */
	public Envelope getDefaultMapArea() {
		return defaultMapArea;
	}

	/**
	 * Define the default MapArea (to start the Map with...)
	 */
	public void setDefaultMapArea(final Envelope envelope) {
		this.defaultMapArea = envelope;
	}

	/**
	 * This {@link java.util.Map} holds a {@link List} of {@link LayerStyle}-IDs
	 * for every {@link DpLayer} (refered to by its ID). The list of IDs is a
	 * subset of all IDs available for that layer. Only the available styles are
	 * listed here.
	 */
	public java.util.Map<String, ArrayList<String>> getAdditionalStyles() {
		return additionalStyles;
	}

	/**
	 * This {@link java.util.Map} holds a {@link List} of {@link LayerStyle}-IDs
	 * for every {@link DpLayer} (refered to by its ID) *
	 */
	public void setAdditionalStyles(
			final java.util.Map<String, ArrayList<String>> additionalStyles) {
		this.additionalStyles = additionalStyles;
	}

	/**
	 * Tell the map, which user defined style to use
	 * 
	 * @param styleID
	 *            #Filename of {@link LayerStyle} or <code>null</code> for
	 *            default.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void setSelectedStyleID(final String layerID, final String styleID) {
		// LOGGER.debug("setSelectedStyleID for layerID " + layerID + " => "
		// + styleID+" MAP="+this);
		selectedStyleID.put(layerID, styleID);
	}

	/**
	 * Which user defined style ID is selected at the moment (or which to use as
	 * default).
	 * 
	 * @return <code>null</code> if no layer is selected, or no additional
	 *         layers exist.
	 * 
	 * @param styleID
	 *            #Filename of {@link LayerStyle} or <code>null</code> for
	 *            default.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public String getSelectedStyleID(final String layerID) {
		// LOGGER.debug("getSelectedStyleID for layerID " + layerID + " ="
		// + selectedStyleID.get(layerID));
		return selectedStyleID.get(layerID);
	}

	/**
	 * Which user defined style is selected at the moment (or which to use as
	 * default).
	 * 
	 * @return <code>null</code> if no additional layers exist.
	 * 
	 * @param styleID
	 *            #Filename of {@link LayerStyle} or <code>null</code> for
	 *            default.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public LayerStyle getSelectedStyle(final String layerID) {
		final String lsID = getSelectedStyleID(layerID);
		if (lsID == null)
			return null;

		for (final DpRef<DpLayer<?, ? extends ChartStyle>> dpr : getLayers()) {
			if (dpr.getTargetId().equals(layerID)) {
				final DpLayer<?, ? extends ChartStyle> target = dpr.getTarget();

				if (target instanceof DpLayer) {
					final DpLayer<?, ? extends ChartStyle> dpl = target;
					return dpl.getLayerStyleByID(lsID);
				}

			}
		}
		return null;
	}

	public java.util.Map<String, String> getSelectedStyleIDs() {
		return selectedStyleID;
	}

	/**
	 * If we are in JWS mode, check if parts of the {@link Map} have to be
	 * downloaded and download them all together
	 * 
	 * @param owner
	 *            GUI element to refer to
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 * @throws IOException
	 *             JWS is used and data is not cached and we can't download it!
	 */
	public void downloadMap(final AtlasStatusDialogInterface statusDialog)
			throws IOException {

		/**
		 * Create a list of uncached parts
		 */
		final String[] partsToDownload = JNLPUtil.countPartsToDownload(this);

		if (partsToDownload.length == 0) {
			// All is cached
			LOGGER.debug("  all is cached.");
			return;
		}

		JNLPSwingUtil.loadPart(partsToDownload, statusDialog);
	}

	/**
	 * @return A {@link Double} value (quality index) representing how much
	 *         metadata has been created for this {@link Map}
	 */
	public Double getQuality() {
		// long start = System.currentTimeMillis();

		if (quality == null
				|| (System.currentTimeMillis() - qualityLastCalced) > 1000) {

			qualityLastCalced = System.currentTimeMillis();

			final List<String> languages = ac.getLanguages();

			final double qmTitle = I8NUtil.qmTranslation(languages, getTitle());
			final double qmDesc = I8NUtil.qmTranslation(languages, getDesc());

			// final double qmKeywords = I8NUtil.qmTranslation(languages,
			// getKeywords());
			double qmKeywords = 1;

			/**
			 * Count HTML Infos
			 */
			final double countHTMLexist = (double) languages.size()
					- (double) getMissingHTMLLanguages().size();
			final double qmHTML = countHTMLexist / ac.getLanguages().size();

			final double qmMap = (qmTitle * 4. + qmDesc * 2. + qmKeywords * 1. + qmHTML * 4.) / 11.;

			Double averageLayerQuality = 0.;
			if (getLayers().size() > 0) {
				averageLayerQuality = getAverageLayerQuality();
			}

			quality = (averageLayerQuality * 3. + qmMap * 1.) / 4.;

			// Subtract 20% if MaxExtend is not set.
			if (getMaxExtend() == null) {
				quality *= 0.8;
			}

		}

		// System.out.println(System.currentTimeMillis()-start);

		return quality;

	}

	/**
	 * @return A {@link List<String>} of language codes that can't be found as
	 *         HTML info files.
	 */
	public List<String> getMissingHTMLLanguages() {

		// if (missingHTMLLanguages == null) {
		missingHTMLLanguages = new ArrayList<String>();

		// synchronized (missingHTMLLanguages) {
		for (final String l : getAc().getLanguages()) {
			if (getInfoURL(l) == null) {
				missingHTMLLanguages.add(l);
			}
		}
		// }
		// }
		return missingHTMLLanguages;
	}

	/**
	 * @return the {@link AtlasConfig} where this map is described.
	 */
	public AtlasConfig getAc() {
		return ac;
	}

	/**
	 * @return the average quality index of the layers in this {@link Map}
	 */
	public Double getAverageLayerQuality() {
		if (getLayers().size() == 0)
			return null;
		Double averageLayerQM = 0.;
		for (final DpRef dpr : getLayers()) {
			averageLayerQM += dpr.getTarget().getQuality();
		}
		return averageLayerQM / getLayers().size();
	}

	/**
	 * @return A {@link java.util.Map} which knows which Layers should start
	 *         minimized in the legend {@link MapContext}
	 */
	public java.util.Map<String, Boolean> getMinimizedInLegendMap() {
		return minimizedInLegendMap;
	}

	/**
	 * @return A {@link java.util.Map} which knows which Layers should not
	 *         appear in the legend, even though they are part of the
	 *         {@link MapContext}
	 */
	public java.util.Map<String, Boolean> getHideInLegendMap() {
		return hideInLegendMap;
	}

	/**
	 * @return Is this layer k in the legend for the given DpEntry-IDs. Never
	 *         returns <code>null</code>.
	 */
	public boolean getHideInLegendFor(final String id) {
		return hideInLegendMap.get(id) != null ? hideInLegendMap.get(id)
				: false;
	}

	/**
	 * Define a preferred ratio between the left and right components of the
	 * MapView. If <code>null</code> or 0f it will use the automatic mode.
	 */
	public void setLeftRightRatio(final Double leftRightRatio) {
		this.leftRightRatio = leftRightRatio;
	}

	/**
	 * The preferred ratio between the left and right components of the MapView.
	 * If <code>null</code> or 0f it will use the automatic mode.
	 */
	public Double getLeftRightRatio() {
		return leftRightRatio;
	}

	/**
	 * Define if a layer of this {@link Map} shall start as a hidden layer in
	 * AV, that means with the eye symbol closed.
	 * 
	 * @param id
	 *            ID of the layer to be queried.
	 * @return never <code>null</code>. <code>false</code> is default.
	 */
	public Boolean getHiddenFor(final String id) {
		return hidden.get(id) != null ? hidden.get(id) : false;
	}

	public void setHiddenFor(final String id, final Boolean isHidden) {
		hidden.put(id, isHidden);
	}

	/**
	 * HashMap of DpLayerID <-> List of ChartStyle-IDs that are available for
	 * viewing in this layer
	 */
	public java.util.Map<String, ArrayList<String>> getAvailableCharts() {
		return availableCharts;
	}

	/**
	 * List of {@link ChartStyle}-IDs that are available in this {@link Map} for
	 * the given {@link DpLayer}.
	 * 
	 * @return never <code>null</code>, but rather an empty list.
	 */
	public ArrayList<String> getAvailableChartIDsFor(final String dpLayerID) {

		if (!availableCharts.containsKey(dpLayerID)) {
			// To avoid null checks, we create an empty ArrayList here and
			// insert it into the Map
			final ArrayList<String> arrayList = new ArrayList<String>();
			availableCharts.put(dpLayerID, arrayList);
		}

		final ArrayList<String> availChartsForLayer = availableCharts
				.get(dpLayerID);

		/**
		 * Now we double check, that all IDs are returned, that really still
		 * exist.
		 */
		final ArrayList<String> clone = (ArrayList<String>) availChartsForLayer
				.clone();
		availChartsForLayer.clear();
		for (String id : clone) {
			final DpEntry<? extends ChartStyle> dpEntry = ac.getDataPool().get(
					dpLayerID);
			for (ChartStyle cs : dpEntry.getCharts()) {
				if (cs.getID().equals(id)) {
					if (!availChartsForLayer.contains(id))
						availChartsForLayer.add(id);
					continue;
				}
			}
		}

		// System.out.println("avail:"+availChartsForLayer.size()+"    "+availChartsForLayer);

		return availChartsForLayer;
	}

	/**
	 * Returns true if the Map referenced this {@link DpEntry} as a layer oder
	 * media.
	 */
	public boolean containsDpe(final String dpeId) {

		/* Scan the layers */
		for (final DpRef<DpLayer<?, ? extends ChartStyle>> dpr : getLayers()) {
			if (dpr.getTargetId().equals(dpeId))
				return true;
		}

		/* Scan the media */
		for (final DpRef<DpMedia<? extends ChartStyle>> dpr : getMedia()) {
			if (dpr.getTargetId().equals(dpeId))
				return true;
		}

		return false;
	}

	/**
	 * Defines whether a given LayerID will be selectable in this map.
	 * Selectablility/Clickablility concerns the ClickInfo tool.
	 * 
	 * @param dpeId
	 *            The ID of the layer / dpe
	 * @param isSelectable
	 *            If <code>true</code>, the layer is searched when clicking with
	 *            InfoClickTool
	 */
	public void setSelectableFor(final String dpeId, final Boolean isSelectable) {
		selectable.put(dpeId, isSelectable);
	}

	/**
	 * Returns whether the layer identified by the id is selectable with the
	 * InfoClickTool. Default is <code>true</code>.
	 * 
	 * @param dpeId
	 *            The ID of the layer / dpe
	 */
	public Boolean isSelectableFor(final String dpeId) {
		return selectable.get(dpeId) == null ? Boolean.TRUE : selectable
				.get(dpeId);
	}

	/**
	 * Defines the biggest visible envelope in the map. The user will not be
	 * able to zoom out to see anything outside this area.
	 */
	public void setMaxExtend(Envelope envelope) {
		maxExtend = envelope;
	}

	/**
	 * Returns the biggest visible envelope in the map. The user will not be
	 * able to zoom out to see anything outside this area. If <code>null</code>,
	 * the user may zoom out as much as she wants.
	 */
	public Envelope getMaxExtend() {
		return maxExtend;
	}

	public void setGridPanelVisible(boolean gridPanelVisible) {
		this.gridPanelVisible = gridPanelVisible;
	}

	public boolean isGridPanelVisible() {
		return gridPanelVisible;
	}

	public void setGridPanelCRS(CoordinateReferenceSystem gridPanelCRS) {
		this.gridPanelCRS = gridPanelCRS;
	}

	public CoordinateReferenceSystem getGridPanelCRS() {
		return gridPanelCRS;
	}

	public void setGridPanelFormatter(GridPanelFormatter gridPanelFormatter) {
		this.gridPanelFormatter = gridPanelFormatter;
	}

	/**
	 * Returns an instance of the {@link GridPanelFormatter} that shall be used
	 * for the map.
	 */
	public GridPanelFormatter getGridPanelFormatter() {
		// Usually the gridPanelFormatter is set while loading the atlas
		if (gridPanelFormatter == null)
			// this is just a fallback for new maps
			try {
				gridPanelFormatter = GridPanelFormatter.FORMATTERS[0]
						.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		return gridPanelFormatter;
	}

	/**
	 * Shall a scale be shown in the map window?
	 */
	public void setScaleVisible(boolean scaleVisible) {
		this.scaleVisible = scaleVisible;
	}

	/**
	 * Shall a scale be shown in the map window?
	 */
	public boolean isScaleVisible() {
		return scaleVisible;
	}

	/**
	 * Add/Overwrites all parameters to the given map, except the ID!
	 * 
	 * @param map
	 *            copy source {@link Map}
	 * @param newMap
	 *            copy destination {@link Map}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	public Map copyTo(final Map newMap) {

		newMap.setDefaultMapArea(getDefaultMapArea());

		// newMap.setHTML(map.getHTML());

		// Copy the Layer-Refs
		final LinkedList<DpRef<DpLayer<?, ? extends ChartStyle>>> newLayers = new LinkedList<DpRef<DpLayer<?, ? extends ChartStyle>>>();
		for (final DpRef<DpLayer<?, ? extends ChartStyle>> dpr : getLayers()) {
			final DpRef<DpLayer<?, ? extends ChartStyle>> clone = dpr.clone();
			newLayers.add(clone);
		}
		newMap.setLayers(newLayers);

		// Copy the media-Ref
		final LinkedList<DpRef<DpMedia<? extends ChartStyle>>> newMedia = new LinkedList<DpRef<DpMedia<? extends ChartStyle>>>();
		for (final DpRef<DpMedia<? extends ChartStyle>> mr : getMedia()) {
			final DpRef<DpMedia<? extends ChartStyle>> clone = mr.clone();
			newMap.add(clone);
		}
		newMap.setMedia(newMedia);

		// Copy the state of minimizeInLegend
		for (final String key : getMinimizedInLegendMap().keySet()) {
			newMap.getMinimizedInLegendMap().put(key,
					getMinimizedInLegendMap().get(key));
		}
		// Copy the state of hideInLegend
		for (final String key : getHideInLegendMap().keySet()) {
			newMap.getHideInLegendMap().put(key, getHideInLegendMap().get(key));
		}

		// Copy the state of hidden layers - layers with closed eyes closed
		for (final String key : hidden.keySet()) {
			newMap.setHiddenFor(key, getHiddenFor(key));
		}

		// Copy the state of selectability of layers (click tool working or not)
		for (final String key : selectable.keySet()) {
			newMap.setSelectableFor(key, isSelectableFor(key));
		}

		newMap.setMaxExtend(getMaxExtend());

		// Copy the leftRight ratio of the legend/map divider
		newMap.setLeftRightRatio(getLeftRightRatio());

		newMap.setTitle(title.copyTo(new Translation()));
		newMap.setDesc(desc.copyTo(new Translation()));
		newMap.setKeywords(keywords.copyTo(new Translation()));

		newMap.setScaleVisible(scaleVisible);
		newMap.setScaleUnits(scaleUnits);

		/*** Copy stuff related to the hor. and vert. GridPanel ***/
		newMap.setGridPanelFormatter(gridPanelFormatter);
		newMap.setGridPanelCRS(gridPanelCRS);
		newMap.setGridPanelVisible(gridPanelVisible);

		/*** Copy the available additional Styles ***/
		for (final DpRef dpRef : getLayers()) {
			String layerID = dpRef.getTargetId();

			final List<String> availableStyles = getAdditionalStyles().get(
					layerID);
			if (availableStyles == null)
				continue;

			// copy the array
			final ArrayList<String> newAdditionalStyles = new ArrayList<String>();
			newAdditionalStyles.addAll(availableStyles);

			// put the array to the newMap
			newMap.getAdditionalStyles().put(layerID, newAdditionalStyles);

			// LOGGER.debug("newMap.setSelectedStyleID("+layerID+", getSelectedStyleID("+layerID+"));");
			newMap.setSelectedStyleID(layerID, getSelectedStyleID(layerID));
		}

		/** Copy available Charts ** */
		for (final String layerID : getAvailableCharts().keySet()) {

			final List<String> availableCharts = getAvailableCharts().get(
					layerID);
			if (availableCharts == null)
				continue;
			final ArrayList<String> newAvailableCharts = new ArrayList<String>();
			for (final String styleID : availableCharts) {
				newAvailableCharts.add(styleID);
				// LOGGER.debug("Copying an available ChartStyle " + styleID);
			}
			newMap.getAvailableCharts().put(layerID, newAvailableCharts);
		}

		newMap.resetMissingHTMLinfos();

		newMap.setPreviewMapExtendInGeopublisher(isPreviewMapExtendInGeopublisher());

		return newMap;
	}

	/**
	 * Removes all memory-intensive cached objects.
	 */
	public void uncache() {
		uncache(null);
	}

	/**
	 * @return The units the {@link ScalePane} is shown in.
	 */
	public ScaleUnits getScaleUnits() {
		return scaleUnits;
	}

	/**
	 * The units the {@link ScalePane} is shown in.
	 */
	public void setScaleUnits(ScaleUnits scaleUnits) {
		this.scaleUnits = scaleUnits;
	}

	/**
	 * If <code>true</code>, the max. map extend will be applied in the
	 * MapComposer. Otherwise it will will only be applied in the exported Atlas
	 */
	public boolean isPreviewMapExtendInGeopublisher() {
		return previewMapExtendInGeopublisher;
	}

	/**
	 * If <code>true</code>, the max. map extend will be applied in the
	 * MapComposer. Otherwise it will will only be applied in the exported Atlas
	 */
	public void setPreviewMapExtendInGeopublisher(
			boolean previewMapExtendInGeopublisher) {
		this.previewMapExtendInGeopublisher = previewMapExtendInGeopublisher;
	}

	/**
	 * Is the entry visible in this map?
	 */
	public boolean isVisible(DpEntry<? extends ChartStyle> dpe) {
		Boolean b = hidden.get(dpe.getId());
		if (b == null)
			return true;
		else
			return (!b);
	}

	public Object isVisibleInLegend(DpEntry<? extends ChartStyle> dpe) {
		Boolean b = hideInLegendMap.get(dpe.getId());
		if (b == null)
			return true;
		else
			return (!b);
	}

	/**
	 * @return A {@link List} of {@link String} with the names of all different
	 *         CRS used in the map.
	 */
	public List<String> getUsedCrs() {

		if (cacheUsedCrs != null
				|| (System.currentTimeMillis() - cacheUsedCrsLastCheckedTime) > 1000) {

			cacheUsedCrsLastCheckedTime = System.currentTimeMillis();

			CoordinateReferenceSystem mapCrs = getLayer0Crs();

			// Returns a List of all DIFFERENT CRS used in the map
			if (mapCrs != null) {

				cacheUsedCrs = new ArrayList<String>();

				// Name/Descriptor of the Map's CRS

				// The Map's CRS is always the first one in the list
				cacheUsedCrs.add(GeotoolsGUIUtil.getTitleForCRS(mapCrs));

				/**
				 * Now iterate over the layer's CRSs and check A) If they
				 * semantically differ from the Map's one, and B) check that no
				 * CRS-Name is in the list twice.
				 */
				for (DpRef dpr : getLayers()) {
					DpLayer dpl = (DpLayer) dpr.getTarget();
					CoordinateReferenceSystem layerCrs = dpl.getCrs();

					if (layerCrs != null) {
						if (!CRS.equalsIgnoreMetadata(mapCrs, layerCrs)) {

							String layerCrsName = GeotoolsGUIUtil
									.getTitleForCRS(layerCrs);

							if (layerCrsName != null
									&& !cacheUsedCrs.contains(layerCrsName)) {
								cacheUsedCrs.add(layerCrsName);
							}
						}
					}

				}
			} else
				cacheUsedCrs = null;
		}
		return cacheUsedCrs;
	}
}
