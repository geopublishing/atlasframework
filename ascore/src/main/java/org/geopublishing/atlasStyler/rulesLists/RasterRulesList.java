package org.geopublishing.atlasStyler.rulesLists;

import org.geopublishing.atlasStyler.RuleChangedEvent;

import de.schmitzm.geotools.styling.StyledRasterInterface;

public abstract class RasterRulesList extends AbstractRulesList {

	protected Double opacity = null;

	private StyledRasterInterface<?> styledRaster;

	public RasterRulesList(RulesListType rlt,
			StyledRasterInterface<?> styledRaster) {
		super(rlt, null);
		setStyledRaster(styledRaster);
	}

	public abstract void applyOpacity();

	@Override
	public String extendMetaInfoString() {
		String metaInfoString = super.extendMetaInfoString();
		return metaInfoString;
	}

	/**
	 * A global setting
	 */
	public Double getOpacity() {
		if (opacity == null) return 1.;
		return opacity;
	}

	public StyledRasterInterface<?> getStyledRaster() {
		return styledRaster;
	}

	public void setOpacity(Double opacity) {
		this.opacity = opacity;
		fireEvents(new RuleChangedEvent("Opacity for whole RL changed", this));
	}

	public void setStyledRaster(StyledRasterInterface<?> styledRaster) {
		this.styledRaster = styledRaster;
	}

}
