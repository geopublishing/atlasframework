package org.geopublishing.atlasStyler;

import hep.aida.bin.QuantileBin1D;
import hep.aida.bin.StaticBin1D;

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
import de.schmitzm.lang.LangUtil;
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
		QuantileBin1D ops = new QuantileBin1D(1.);
		for (ColorMapEntry cme : cm.getColorMapEntries()) {
			importValuesLabelsQuantitiesColors(cme);
			ops.add(getOpacities().get(getNumClasses() - 1));
		}
		setOpacity(ops.median());
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
				if (getOpacities().get(i) == 0)
					continue;
				setOpacity(i, op);
			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied an OPACITY to all ColorMapEntries ", this));
		}
	}

	private Double opacity;

	public void setOpacity(int rowIndex, Double newOp) {
		if (newOp == getOpacities().get(rowIndex))
			return;
		if (newOp > 1.)
			newOp = 1.;
		if (newOp < 0.)
			newOp = 0.;
		getOpacities().set(rowIndex, LangUtil.round(newOp, 2));
		fireEvents(new RuleChangedEvent("Opacity changed", this));
	}

	/**
	 * @param parentGui
	 *            is <code>null</code>, no warnings will be shown if the number
	 *            of classes if higher than the number of colors
	 */
	public void applyPalette(JComponent parentGui) {
		pushQuite();

		try {

			final Color[] colors = getPalette().getColors();

			if (getNumClassesVisible() > getPalette().getMaxColors()
					&& parentGui != null) {

				final String msg = ASUtil
						.R("UniqueValuesGUI.WarningDialog.more_classes_than_colors.msg",
								getPalette().getMaxColors(),
								getNumClassesVisible());
				JOptionPane.showMessageDialog(
						SwingUtil.getParentWindowComponent(parentGui), msg);
			}

			int idx = 0;
			for (int i = 0; i < getValues().size(); i++) {

				if (getOpacities().get(i) == 0)
					continue;

				getColors().set(i, colors[idx]);

				idx++;
				idx = idx % getPalette().getMaxColors();

			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied a COLORPALETTE to all ColorMapEntries", this));
		}

	}

	public int getNumClassesVisible() {
		int visible = 0;
		for (Double o : getOpacities()) {
			if (o > 0.)
				visible++;
		}
		return visible;
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
