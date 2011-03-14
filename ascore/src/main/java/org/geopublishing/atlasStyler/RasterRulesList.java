package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.util.ArrayList;

import org.geopublishing.atlasStyler.AtlasStyler.LANGUAGE_MODE;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.FeatureTypeStyle;

import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.Translation;

public abstract class RasterRulesList extends AbstractRulesList {

	private final ArrayList<Double> values = new ArrayList<Double>();
	private final ArrayList<Translation> labels = new ArrayList<Translation>();
	private final ArrayList<Double> opacities = new ArrayList<Double>();
	private final ArrayList<Color> colors = new ArrayList<Color>();

	final int cmt;

	private StyledRasterInterface styledRaster;

	public RasterRulesList(StyledRasterInterface styledRaster, int colorMapType) {
		super(null);
		setStyledRaster(styledRaster);

		cmt = colorMapType;
		if (cmt < 1 || cmt > 3)
			throw new IllegalArgumentException(
					"ColorMapType has to be 1, 2 or 3!");
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

	public int getNumClasses() {
		// return getColorMap().getColorMapEntries().length;
		return getValues().size();
	}

	/**
	 * Assembles the Colormap from the values, lables, colors and opacities
	 * 
	 * @return
	 */
	public ColorMap getColorMap() {

		ColorMap cm = StylingUtil.STYLE_FACTORY.createColorMap();
		cm.setType(cmt);

		for (int i = 0; i < getValues().size(); i++) {

			final Translation label = getLabels().get(i);
			String labelString = label.toString();
			if (AtlasStyler.getLanguageMode() == LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
				labelString = label.toOneLine();
			}

			ColorMapEntry cme = StylingUtil.createColorMapEntry(labelString,
					getValues().get(i), getColors().get(i),
					getOpacities().get(i));

			cm.addColorMapEntry(cme);
		}

		return cm;
	}

	/**
	 * Attenetion: Ignores the CM type!
	 */
	protected void importValuesLabelsQuantitiesColors(ColorMap cm) {
		for (ColorMapEntry cme : cm.getColorMapEntries()) {
			getValues()
					.add(Double.valueOf(cme.getQuantity().evaluate(null)
							.toString()));
			getOpacities().add(
					Double.valueOf(cme.getOpacity().evaluate(null).toString()));
			getColors().add(StylingUtil.getColorFromColorMapEntry(cme));

			getLabels().add(new Translation(cme.getLabel()));
		}

	}

	public ArrayList<Double> getOpacities() {
		return opacities;
	}

	public ArrayList<Color> getColors() {
		return colors;
	}

	public ArrayList<Translation> getLabels() {
		return labels;
	}

	public ArrayList<Double> getValues() {
		return values;
	}

	public void removeIdx(int index) {
		getValues().remove(index);
		getOpacities().remove(index);
		getLabels().remove(index);
		getColors().remove(index);

		fireEvents(new RuleChangedEvent("Index " + index + " removed", this));
	}

	/**
	 * Ent
	 */
	public void removeAll() {
		getValues().clear();
		getOpacities().clear();
		getLabels().clear();
		getColors().clear();

		fireEvents(new RuleChangedEvent("Removed all entries", this));
	}

}
