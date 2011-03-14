package org.geopublishing.atlasStyler;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;

import de.schmitzm.geotools.styling.StyledRasterInterface;

public abstract class RasterRulesList extends AbstractRulesList {

	private RasterSymbolizer rs;
	private org.geotools.styling.ColorMap colorMap;

	private StyledRasterInterface styledRaster;

	public RasterRulesList(StyledRasterInterface styledRaster) {
		super(null);
		setStyledRaster(styledRaster);
	}

	@Override
	public String getAtlasMetaInfoForFTSName() {
		String metaInfoString = getType().toString();

		// metaInfoString = extendMetaInfoString(metaInfoString);
		//
		// metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_METHOD
		// + METAINFO_KVP_EQUALS_CHAR + getMethod();
		//
		// metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_PALTETTE
		// + METAINFO_KVP_EQUALS_CHAR + getBrewerPalette().getName();

		return metaInfoString;
	}

	@Override
	void parseMetaInfoString(String metaInfoString, FeatureTypeStyle fts) {
	}

	public void setStyledRaster(StyledRasterInterface styledRaster) {
		this.styledRaster = styledRaster;
	}

	public StyledRasterInterface getStyledRaster() {
		return styledRaster;
	}

	public void setRs(RasterSymbolizer rs) {
		this.rs = rs;
		if (rs != null)
			setColorMap(rs.getColorMap());
		else setColorMap(null);
	}

	public RasterSymbolizer getRs() {
		return rs;
	}

	public void setColorMap(org.geotools.styling.ColorMap colorMap) {
		this.colorMap = colorMap;
	}

	public org.geotools.styling.ColorMap getColorMap() {
		return colorMap;
	}

}
