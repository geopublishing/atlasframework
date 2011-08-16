package org.geopublishing.atlasStyler.rulesLists;

import org.geopublishing.atlasStyler.RuleChangedEvent;

import de.schmitzm.geotools.styling.StyledRasterInterface;

public abstract class RasterRulesList extends AbstractRulesList {

	private Double opacity = 1.;
	
	private StyledRasterInterface<?> styledRaster;

	public RasterRulesList(RulesListType rlt, StyledRasterInterface<?> styledRaster) {
		super(rlt, null);
		setStyledRaster(styledRaster);
	}

	public abstract void applyOpacity();

	@Override
	public String extendMetaInfoString() {
		String metaInfoString = super.extendMetaInfoString();

//		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_PALTETTE + METAINFO_KVP_EQUALS_CHAR + getPalette().getName();
//
//		if (getStyledRaster().getNodataValue() != null)
//			metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_NODATA + METAINFO_KVP_EQUALS_CHAR
//					+ getStyledRaster().getNodataValue();

		return metaInfoString;
	}
   

	/**
	 * A global setting 
	 */
	public Double getOpacity() {
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
