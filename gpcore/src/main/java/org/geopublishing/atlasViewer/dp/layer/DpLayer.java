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
package org.geopublishing.atlasViewer.dp.layer;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.exceptions.AtlasFatalException;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.jfree.ui.Layer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.geotools.StyledLayerInterface;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This abstract {@link Class} unites all methods and fields common to all
 * {@link DpLayer}s
 */
public abstract class DpLayer<E, CHART_STYLE_IMPL extends ChartStyle> extends
		DpEntry<CHART_STYLE_IMPL> implements StyledLayerInterface<E> {
	static final private Logger LOGGER = Logger.getLogger(DpLayer.class);

	/**
	 * Can the AtlasStyler for this layer be opened from within the AV's
	 * legends. Default value is <code>false</code>.
	 **/
	private boolean stylerInLegend = true;

	/**
	 * Can the attribute table for this layer be opened from within the AV's
	 * legends. Default value is <code>false</code>.
	 **/
	private boolean tableInLegend = true;

	/**
	 * May the features for this layer be filtered from within the AV? Default
	 * value is <code>false</code>.
	 **/
	private boolean filterInLegend = true;

	/**
	 * A list of (optional) additional {@link LayerStyle}s available for this
	 * {@link DpLayer}.
	 */
	private List<LayerStyle> layerStyles = new ArrayList<LayerStyle>();

	/** Caching the maximum envelope of this {@link DpLayer} */
	protected Envelope envelope = null;

	/** Caching the {@link Style} for this {@link DpLayer} */
	private Style style;

	/** Will be cached with every call to {@link #getGeoObject()} */
	protected CoordinateReferenceSystem crs;

	/**
	 * Caches the result of searching for HTML pages for a while. This is for
	 * the QM tool-tips.
	 */
	private volatile ArrayList<String> missingHTMLLanguages = null;

	public DpLayer(AtlasConfig ac) {
		super(ac);
	}

	/**
	 * Is supposed to fill envelope and crs
	 * 
	 * @return An object for GeoTools to play with
	 */
	@Override
	public abstract E getGeoObject();

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.atlas.datapool.DatapoolEntry#isLayer()
	 */
	@Override
	public final boolean isLayer() {
		return true;
	}

	/**
	 * Returns the {@link Envelope} that contains all layer information.
	 * Envelope supposed to be set by {@link #getGeoObject()}
	 */
	@Override
	public final Envelope getEnvelope() {
		if (envelope == null && !isBroken())
			getGeoObject();
		return envelope;
	}

	/**
	 * Set the {@link Style} to use for this {@link MapLayer} Set style to null
	 * if you want the {@link Style} to reload from file on next call to
	 * {@link #getStyle()}
	 * 
	 */
	@Override
	public void setStyle(Style style) {
		this.style = style;
	}

	/**
	 * Returns the default {@link Style} for this {@link DpLayer}. Tries to load
	 * a {@link Style} from a file with the same {@link URL} as the main file,
	 * but with ending <code>.sld</code>
	 */
	@Override
	public Style getStyle() {
		if (style == null) {
			// First try to load a .sld file from the same location
			Style[] styles = null;

			URL changeUrlExt = IOUtil.changeUrlExt(getUrl(), "sld");
			try {
				styles = StylingUtil.loadSLD(changeUrlExt);
			} catch (Exception e) {
				LOGGER.error("Loading the Style from " + changeUrlExt
						+ ". Using default Style.", e);

				style = ASUtil.createDefaultStyle(DpLayer.this);
				return style;
			}

			if (styles == null || styles.length == 0 || styles[0] == null) {
				LOGGER.warn("SLD file doesn't contain a style!");

				style = ASUtil.createDefaultStyle(DpLayer.this);
			} else
				style = styles[0];

			// Correcting any wrongly upper/lowercased attribute names
			if (this instanceof DpLayerVectorFeatureSource) {
				style = StylingUtil.correctPropertyNames(style,
						((DpLayerVectorFeatureSource) this).getSchema());
			} else {
				// RasterSymbolizers may have a wrong geometry property name
				// which would be corrected here
				style = StylingUtil.correctPropertyNames(style, null);
			}
		}
		return style;
	}

	/**
	 * Returnes human readable {@link String} of the CRS natively used by this
	 * {@link DpLayer}
	 * 
	 * If crs == null, it will call {@link #getGeoObject()}
	 * 
	 * @throws AtlasFatalException
	 * @throws IllegalArgumentException
	 * 
	 */
	@Override
	public String getCRSString() {

		CoordinateReferenceSystem crs = getCrs();

		if (crs == null) {
			return "CRS?"; // i8n
		}

		try {
			String code = crs.getName().getCode();
			if (code.equals("unnamed")) {
				// System.out.println("sd");
			}
			return code;
		} catch (Exception e) {
			return crs.toString();
		}

	}

	/**
	 * If crs == null, it will first try to parse a .prj file. If it doesn't
	 * exist, it calls {@link #getGeoObject()} and hopes that the crs will be
	 * set there
	 * 
	 * This is overwritten by DpLayerFeatureSource, because e.g. a WFS doens't
	 * not have a .prj file
	 * 
	 * @return null if not working
	 * @throws AtlasFatalException
	 * @throws IllegalArgumentException
	 * 
	 */
	@Override
	public CoordinateReferenceSystem getCrs() {
		if (crs == null && !isBroken()) {
			try {
				crs = GeoImportUtil.readProjectionFile(getProjectionFileURL());
			} catch (Exception e) {
				try {
					getGeoObject();
				} catch (Exception cantGetGeoEx) {
					setBrokenException(cantGetGeoEx);
					return null;
				}
			}
		}

		if (crs == null) {
			crs = GeoImportUtil.getDefaultCRS();
		}

		return crs;
	}

	/**
	 * Convenience method to get the URL to the .prj file (which doesn't have to
	 * exists)
	 * 
	 * @throws AtlasFatalException
	 * @throws IllegalArgumentException
	 */
	public final URL getProjectionFileURL() throws IllegalArgumentException,
			AtlasFatalException {
		return IOUtil.changeUrlExt(getUrl(), "prj");
	}

	/**
	 * Returns the {@link URL} to a (HTML) file that provides more information
	 * about this layer. If no HTML file is associated with this
	 * {@link StyledLayerInterface} <code>null</code> will be returned. This
	 * 
	 * @return <code>null</code> or a {@link URL}
	 * 
	 * @see StyledLayerInterface which defines this as abstract
	 */
	@Override
	public URL getInfoURL() {
		return getInfoURL(Translation.getActiveLang());
	}

	/**
	 * Returns the {@link URL} to a (HTML) file that provides more information
	 * about this layer. If no HTML file is associated with this
	 * {@link StyledLayerInterface} <code>null</code> will be returned.
	 * 
	 * @return <code>null</code> or a {@link URL}
	 */
	private URL getInfoURL(String lang) {
		try {
			URL infoURL;
			String url = getUrl().toString();
			infoURL = new URL(url.substring(0, url.lastIndexOf('.')) + "_"
					+ lang + ".html");

			// Try to access the URL...
			InputStream openStream = infoURL.openStream();
			openStream.close();
			return infoURL;
		} catch (Exception e) {
			// No info available..
		}

		// LOGGER.info("****+ Info URL returned = "+infoURL);
		return null;

	}

	public List<LayerStyle> getLayerStyles() {
		return layerStyles;
	}

	/**
	 * Adds a LayerStyle/Additional Style/view for his Layer. This method checks
	 * if the document exists. Otherwise it's omitted.
	 * 
	 * @param newLayerStyle
	 *            the {@link LayerStyle} to add for this {@link Layer}
	 */
	public void addLayerStyle(LayerStyle newLayerStyle) {
		try {
			URL testUrl = IOUtil.extendURL(IOUtil.getParentUrl(getUrl()),
					newLayerStyle.getFilename());
			testUrl.openStream();
			layerStyles.add(newLayerStyle);
		} catch (Exception e) {
			LOGGER.warn(
					"Not adding additional style"
							+ newLayerStyle.toString()
							+ " because the URL can't be openend.\n   LayerID = "
							+ getId(), e);
			return;
		}

	}

	/**
	 * Convinience method for accessing a {@link LayerStyle} via its ID
	 * {@link String}
	 * 
	 * @param styleID
	 *            When <code>null</code> will return <code>null</code>
	 * 
	 * @return <code>null</code> if no {@link LayerStyle} with given ID can be
	 *         found.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public LayerStyle getLayerStyleByID(String styleID) {
		for (LayerStyle ls : getLayerStyles()) {
			if (ls.getFilename().equals(styleID))
				return ls;
		}
		return null;
	}

	/**
	 * @return A value between 0 and 1 which describes how good much meta data
	 *         has been provided. 1 is great!
	 */
	@Override
	public Double getQuality() {
		if (isBroken())
			return 0.;

		Double result;

		Double simpleQM = super.getQuality();
		
		// TODO NPE when reloading atlas!
		
		int countLanguages = getAtlasConfig().getLanguages().size();
		int existing = countLanguages - getMissingHTMLLanguages().size();
		double htmlQM = (double) existing / (double) countLanguages;

		double lsQM = getQualityLayerStyles();

		result = (simpleQM * 7. + htmlQM * 3. + lsQM * 3) / 13.;

		return result;
	}

	/**
	 * @return the meta data quality of any additional styles. 1. (best) if no
	 *         additional Styles exist.
	 */
	public double getQualityLayerStyles() {
		double lsQM;
		if (getLayerStyles().size() > 0) {
			lsQM = 0.;
			final List<String> languages = getAtlasConfig().getLanguages();
			for (final LayerStyle ls : getLayerStyles()) {
				lsQM += (I8NUtil.qmTranslation(languages, ls.getTitle()) * 4. + I8NUtil
						.qmTranslation(languages, ls.getDesc()) * 2.) / 6.;
			}
			lsQM = lsQM / getLayerStyles().size();
		} else
			lsQM = 1.;

		return lsQM;
	}

	/**
	 * @return A {@link List<String>} of language codes that can't be found as
	 *         HTML info files.
	 */
	public List<String> getMissingHTMLLanguages() {

		if (missingHTMLLanguages == null) {

			missingHTMLLanguages = new ArrayList<String>();
			synchronized (missingHTMLLanguages) {

				for (String l : getAtlasConfig().getLanguages()) {
					if (getInfoURL(l) == null) {
						missingHTMLLanguages.add(l);
					}
				}
			}
		}

		return missingHTMLLanguages;
	}

	/**
	 * Removes cached values
	 */
	@Override
	public void uncache() {
		super.uncache();

		// style = null;

		crs = null;
		envelope = null;
		missingHTMLLanguages = null;
	}

	/**
	 * When in AtlasViewer the user is changing a Style, and then switches to a
	 * new map, the style changes will be forgotten by calling this method.<br/>
	 * When running in GeopublisherGUI as a preview, we do not want to uncache a
	 * style, as it would result in reloading Styled from disk.
	 */
	public void resetStyleChanges() {
		if (AtlasViewerGUI.isRunning()
				&& AtlasViewerGUI.getInstance().isPreviewMode()) {
			// Do not uncache Styles when running in preview mode.
		} else
			style = null;
	}

	/**
	 * Define, whether the style for this layer can be edited with AS from
	 * within the AV's legends. Default value is <code>false</code>.
	 */
	public void setTableInLegend(boolean tableInLegend) {
		this.tableInLegend = tableInLegend;

	}

	/**
	 * Define, whether the attribute table for this layer can be opened from
	 * within the AV's legends. Default value is <code>false</code>.
	 */
	public void setStylerInLegend(boolean stylerInLegend) {
		this.stylerInLegend = stylerInLegend;
	}

	/**
	 * @return whether the style for this layer can be edited with AS from
	 *         within the AV's legends. Default value is <code>false</code>.
	 */
	public boolean isStylerInLegend() {
		return stylerInLegend;
	}

	/**
	 * @return whether the attribute table for this layer can be opened from
	 *         within the AV's legends. Default value is <code>false</code>.
	 */
	public boolean isTableVisibleInLegend() {
		return tableInLegend;
	}

	/**
	 * Defines whether the features for this layer may be filtered from withing
	 * the AV. Default value is <code>false</code>.
	 */
	public void setFilterInLegend(boolean filterInLegend) {
		this.filterInLegend = filterInLegend;
	}

	/**
	 * @return whether the features for this layer may be filtered from withing
	 *         the AV. Default value is <code>false</code>.
	 */
	public boolean isFilterInLegend() {
		return filterInLegend;
	}

	abstract public DpLayer<E, CHART_STYLE_IMPL> copy();

	public DpLayer<E, CHART_STYLE_IMPL> copyTo(
			DpLayer<E, CHART_STYLE_IMPL> target) {
		target.setAtlasConfig(getAtlasConfig());
		target.setId(getId());
		if (getBrokenException() != null)
			target.setBrokenException(getBrokenException());

		target.setCharset(getCharset());

		target.setDataDirname(getDataDirname());
		target.setDesc(getDesc().copy());
		target.setExportable(isExportable());
		target.setFilename(getFilename());
		target.setImageIcon(getImageIcon());
		target.setKeywords(getKeywords().copy());
		target.setTitle(getTitle().copy());
		target.setType(getType());

		target.setFilterInLegend(isFilterInLegend());
		target.setStylerInLegend(isStylerInLegend());
		target.setTableInLegend(isTableVisibleInLegend());

		DuplicatingStyleVisitor dsv = new DuplicatingStyleVisitor();
		dsv.visit(getStyle());
		target.setStyle((Style) dsv.getCopy());

		return target;
	}

	public E getGeoObject(AtlasStatusDialogInterface statusDialog) {
		LOGGER.warn("called not fully implemented getGeoObject(AtlasStatusDialog statusDialog) in abstract DpLayer");
		return getGeoObject();
	}
}
