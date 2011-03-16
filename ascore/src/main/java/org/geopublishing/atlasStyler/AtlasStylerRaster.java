package org.geopublishing.atlasStyler;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList;
import org.geopublishing.atlasStyler.rulesLists.RulesListInterface;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledGridCoverageReaderInterface;
import de.schmitzm.geotools.styling.StyledLayerInterface;

public class AtlasStylerRaster extends AtlasStyler {

	private final static Logger LOGGER = Logger
			.getLogger(AtlasStylerRaster.class);

	private StyledGridCoverageReaderInterface styledRaster;

	private RasterLegendData backupRasterLegend;

	public AtlasStylerRaster(StyledGridCoverageReaderInterface styledRaster,
			Style loadStyle, MapLayer mapLayer, HashMap<String, Object> params,
			Boolean withDefaults) {
		super(mapLayer, params, withDefaults);

		this.setStyledRaster(styledRaster);

		this.rlf = new RuleListFactory(styledRaster);

		if (loadStyle != null) {
			importStyle(loadStyle, styledRaster.getLegendMetaData());
		} else {
			if (styledRaster.getStyle() != null) {
				importStyle(styledRaster.getStyle(),
						styledRaster.getLegendMetaData());
			} else {

				if (withDefaults != null && withDefaults == true) {
					final SingleRuleList<? extends Symbolizer> defaultRl = rlf
							.createSingleRulesList(
									getRuleTitleFor(styledRaster), true);
					LOGGER.debug("Added default rulelist: " + defaultRl);
					addRulesList(defaultRl);
				}
			}
		}

	}

	@Override
	public AbstractRulesList copyRulesList(RulesListInterface rl) {
		return null;
	}

	@Override
	public Style sanitize(Style style) {
		return style;
	}

	public RasterLegendData getLegendMetaData() {

		if (getRuleLists().size() == 0)
			return new RasterLegendData(false);

		RasterRulesList rrl = (RasterRulesList) getRuleLists().get(0);

		// TODO What if there is more than one RUlesList!?

		return rrl.getRasterLegendData();
	}

	@Override
	public void cancel() {
		super.cancel();

		// Apply the RasterLegendData if it is a raster
		if (this != null) {
			backupRasterLegend.copyTo(getStyledRaster().getLegendMetaData());
		}

		for (final StyleChangeListener l : listeners) {
			// LOGGER.debug("fires a StyleChangedEvent... ");
			l.changed(new StyleChangedEvent(backupStyle));
		}

	}

	public void importStyle(Style importStyle, RasterLegendData rasterLegendData) {
		// Backup
		if (backupRasterLegend == null) {
			backupRasterLegend = new RasterLegendData(true);
			if (rasterLegendData != null)
				rasterLegendData.copyTo(backupRasterLegend);
		}

		super.importStyle(importStyle);
	}

	@Override
	StyleChangedEvent getStyleChangeEvent() {
		return new RasterStyleChangedEvent(getStyle(), getLegendMetaData());
	}

	public void setStyledRaster(StyledGridCoverageReaderInterface styledRaster) {
		this.styledRaster = styledRaster;
	}

	public StyledGridCoverageReaderInterface getStyledRaster() {
		return styledRaster;
	}

	@Override
	public StyledLayerInterface<?> getStyledInterface() {
		return styledRaster;
	}
}
