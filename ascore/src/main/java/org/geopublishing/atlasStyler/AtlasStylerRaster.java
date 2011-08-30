package org.geopublishing.atlasStyler;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesListColormap;
import org.geopublishing.atlasStyler.rulesLists.RulesListInterface;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StyledGridCoverageReader;
import de.schmitzm.geotools.styling.StyledGridCoverageReaderInterface;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StyledLayerUtil;

public class AtlasStylerRaster extends AtlasStyler {

	private final static Logger LOGGER = Logger.getLogger(AtlasStylerRaster.class);

//	private RasterLegendData backupRasterLegend;

	private StyledGridCoverageReaderInterface styledRaster;

	/**
	 * Wenn <code>-1</code>, und die Anzahl der verfügbaren Band >= 3, dann werden 3 Band zusammen in einem
	 * RBG-Fehlfarbenbild angezeigt. Wenn der Wert >= 0 ist, dann wird ein Style nur für das eine Band erstellt. Der
	 * Style enhält dann eine Channel-Selektion Anweisung.
	 */
	private int band = 0;

	/**
	 * Create an {@link AtlasStylerVector} object for any {@link StyledFeaturesInterface}
	 */
	public AtlasStylerRaster(AbstractGridCoverage2DReader reader) {
		this(new StyledGridCoverageReader(reader), null, null, null, null);
	}

	/**
	 * Create an {@link AtlasStylerVector} object for any {@link StyledFeaturesInterface}
	 */
	public AtlasStylerRaster(StyledGridCoverageReaderInterface styledRaster) {
		this(styledRaster, null, null, null, null);
	}

	public AtlasStylerRaster(StyledGridCoverageReaderInterface styledRaster, Style loadStyle, MapLayer mapLayer,
			HashMap<String, Object> params, Boolean withDefaults) {
		super(mapLayer, params, withDefaults);

		this.setStyledRaster(styledRaster);

		this.rlf = new RuleListFactory(styledRaster);

		if (loadStyle != null) {
			importStyle(loadStyle);
		} else {
			if (styledRaster.getStyle() != null) {
				importStyle(styledRaster.getStyle());
			} else {

				if (withDefaults != null && withDefaults == true) {
					final SingleRuleList<? extends Symbolizer> defaultRl = rlf.createSingleRulesList(
							getRuleTitleFor(styledRaster), true);
					LOGGER.debug("Added default rulelist: " + defaultRl);
					addRulesList(defaultRl);
				}
			}
		}

	}

	@Override
	public void cancel() {
		super.cancel();

//		// Apply the RasterLegendData if it is a raster
//		if (this != null) {
//			backupRasterLegend.copyTo(getStyledRaster().getLegendMetaData());
//		}

		for (final StyleChangeListener l : listeners) {
			// LOGGER.debug("fires a StyleChangedEvent... ");
			l.changed(new StyleChangedEvent(backupStyle));
		}

	}

	@Override
	public AbstractRulesList copyRulesList(RulesListInterface rl) {
		return null;
	}

	@Override
	StyleChangedEvent getStyleChangeEvent() {
		RasterLegendData rlm = new RasterLegendData(false);
		try {
			rlm = StyledLayerUtil.generateRasterLegendData(getStyle(), false, null);
		} catch (Exception e) {
			// Happens if there are no classes, and hence no entries in the
			// colormap.
		}
		return new RasterStyleChangedEvent(getStyle());
	}

	@Override
	public StyledLayerInterface<?> getStyledInterface() {
		return styledRaster;
	}

	public StyledGridCoverageReaderInterface getStyledRaster() {
		return styledRaster;
	}

//	public void importStyle(Style importStyle, RasterLegendData rasterLegendData) {
		// Backup
//		if (backupRasterLegend == null) {
//			backupRasterLegend = new RasterLegendData(true);
//			if (rasterLegendData != null)
//				rasterLegendData.copyTo(backupRasterLegend);
//		}

//		super.importStyle(importStyle);
//	}

	@Override
	public Style sanitize(Style style) {
		return style;
	}

	public void setStyledRaster(StyledGridCoverageReaderInterface styledRaster) {
		this.styledRaster = styledRaster;
	}

	public void setBand(int band) {
		this.band = band;
		for (AbstractRulesList rl : getRuleLists()) {
			if (rl instanceof RasterRulesListColormap)
			((RasterRulesListColormap)rl).setBand(band);
		}
	}

	/**
	 * -1 ist die Auswahl für RGB
	 */
	public int getBand() {
		return band;
	}
}
