package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.geopublishing.atlasStyler.AtlasStyler.LANGUAGE_MODE;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.FeatureTypeStyle;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.I18NUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.swing.SwingUtil;

public abstract class RasterRulesList extends AbstractRulesList {

	private final ArrayList<Double> values = new ArrayList<Double>();
	private final ArrayList<Translation> labels = new ArrayList<Translation>();
	private final ArrayList<Double> opacities = new ArrayList<Double>();
	private final ArrayList<Color> colors = new ArrayList<Color>();

	final int cmt;
	private BrewerPalette palette = null;

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

	public abstract RasterLegendData getRasterLegendData();

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
			importValuesLabelsQuantitiesColors(cme);
		}

	}

	protected void importValuesLabelsQuantitiesColors(ColorMapEntry cme) {
		final Double valueDouble = Double.valueOf(cme.getQuantity()
				.evaluate(null).toString());
		getValues().add(valueDouble);
		getOpacities().add(
				Double.valueOf(cme.getOpacity().evaluate(null).toString()));
		getColors().add(StylingUtil.getColorFromColorMapEntry(cme));

		Translation translation = styledRaster.getLegendMetaData() != null ? styledRaster
				.getLegendMetaData().get(valueDouble) : null;

		if (I18NUtil.isEmpty(translation)) {
			final String labelFromCM = cme.getLabel();
			if (labelFromCM != null && !labelFromCM.isEmpty())
				getLabels().add(new Translation(labelFromCM));
			else
				translation = new Translation("");

		}
		getLabels().add(translation);
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

	public void setPalette(BrewerPalette palette) {
		this.palette = palette;
	}

	public BrewerPalette getPalette() {
		return palette;
	}

	public void applyOpacity() {
		pushQuite();

		try {

			final Double op = getOpacity();

			for (int i = 0; i < getValues().size(); i++) {

				int idx = i;
				while (idx >= getPalette().getMaxColors()) {
					idx -= getPalette().getMaxColors();
				}

				getOpacities().set(i, op);
			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied an OPACITY to all ColorMapEntries ", this));
		}
	}

	private Double opacity;

	/**
	 * @param parentGui
	 *            is <code>null</code>, no warnings will be shown if the number
	 *            of classes if higher than the number of colors
	 */
	public void applyPalette(JComponent parentGui) {
		pushQuite();

		try {

			boolean warnedOnce = false;

			final Color[] colors = getPalette().getColors();

			for (int i = 0; i < getValues().size(); i++) {

				int idx = i;
				while (idx >= getPalette().getMaxColors()) {
					idx -= getPalette().getMaxColors();
					if ((parentGui != null) && (!warnedOnce)) {

						final String msg = AtlasStylerVector
								.R("UniqueValuesGUI.WarningDialog.more_classes_than_colors.msg",
										getPalette().getMaxColors(),
										getValues().size());
						JOptionPane.showMessageDialog(
								SwingUtil.getParentWindowComponent(parentGui),
								msg);
						warnedOnce = true;
					}
				}

				getColors().set(i, colors[idx]);
			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied a COLORPALETTE to all ColorMapEntries", this));
		}

	}

	public void setOpacity(Double opacity) {
		this.opacity = opacity;
	}

	/**
	 * A global Setting for all CMEs
	 * 
	 * @return
	 */
	public Double getOpacity() {
		return opacity;
	}

}
