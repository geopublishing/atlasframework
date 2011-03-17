package org.geopublishing.atlasStyler.rulesLists;

import hep.aida.bin.QuantileBin1D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStyler.LANGUAGE_MODE;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.PaletteType;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.opengis.filter.Filter;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.I18NUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.SwingUtil;

public abstract class RasterRulesList extends AbstractRulesList {

	protected final int cmt;
	private final ArrayList<Color> colors = new ArrayList<Color>();
	private final ArrayList<Translation> labels = new ArrayList<Translation>();
	private final ArrayList<Double> opacities = new ArrayList<Double>();

	private Double opacity = 1.;
	private BrewerPalette palette = ASUtil.getPalettes(new PaletteType(true,
			false), -1)[0];

	private StyledRasterInterface<?> styledRaster;

	protected final ArrayList<Double> values = new ArrayList<Double>();

	public RasterRulesList(RulesListType rlt,
			StyledRasterInterface<?> styledRaster, int colorMapType) {
		super(rlt, null);
		setStyledRaster(styledRaster);

		cmt = colorMapType;
		if (cmt < 1 || cmt > 3)
			throw new IllegalArgumentException(
					"ColorMapType has to be 1, 2 or 3!");
	}

	public void applyOpacity() {
		pushQuite();

		try {

			final Double op = getOpacity();
			if (op == null)
				return;

			for (int i = 0; i < getValues().size(); i++) {

				if (i >= getOpacities().size())
					getOpacities().add(op);

				if (getOpacities().get(i) != 0)
					setOpacity(i, op);
			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied an OPACITY to all ColorMapEntries ", this));
		}
	}

	/**
	 * 1 value = 1 class
	 * 
	 * @param parentGui
	 *            is <code>null</code>, no warnings will be shown if the number
	 *            of classes if higher than the number of colors
	 */
	public void applyPalette(JComponent parentGui) {
		pushQuite();

		getColors().clear();

		try {

			if (getValues().size() == 0)
				return;

			if (getNumClassesVisible() > getPalette().getMaxColors()
					&& parentGui != null) {

				final String msg = ASUtil
						.R("UniqueValuesGUI.WarningDialog.more_classes_than_colors.msg",
								getPalette().getMaxColors(),
								getNumClassesVisible());
				JOptionPane.showMessageDialog(
						SwingUtil.getParentWindowComponent(parentGui), msg);
			}

			final Color[] colors = getPalette().getColors(
					getValues().size() % getPalette().getMaxColors());

			int idx = 0;
			for (int i = 0; i < getValues().size(); i++) {

				if (getOpacities().get(i) == 0.)
					continue;

				if (i >= getColors().size())
					getColors().add(colors[idx]);
				else
					getColors().set(i, colors[idx]);

				idx++;
				idx = idx % getPalette().getMaxColors();

			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied a COLORPALETTE to all ColorMapEntries", this));
		}

	}

	@Override
	public String extendMetaInfoString() {
		String metaInfoString = super.extendMetaInfoString();

		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_PALTETTE
				+ METAINFO_KVP_EQUALS_CHAR + getPalette().getName();

		return metaInfoString;
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

			String labelString = "";

			final Translation label = getLabels().get(i);
			if (label != null) {
				if (AtlasStyler.getLanguageMode() == LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
					labelString = label.toOneLine();
				} else
					labelString = label.toString();
			}

			ColorMapEntry cme = StylingUtil.createColorMapEntry(labelString,
					getValues().get(i), getColors().get(i),
					getOpacities().get(i));

			cm.addColorMapEntry(cme);
		}

		return cm;
	}

	public ArrayList<Color> getColors() {
		return colors;
	}

	public ArrayList<Translation> getLabels() {
		return labels;
	}

	/**
	 * Attention: "Classes" means logical classes. When using ColorMap.RAMPS or
	 * COLOMAP.INTERVAL it is values - 1
	 */
	public int getNumClasses() {
		if (cmt == ColorMap.TYPE_VALUES)
			return getValues().size();
		else {
			if (getValues().size() < 1)
				return 0;
			return getValues().size() - 1;
		}
	}

	/**
	 * Returns the number of visible (opacity > 0) classes.
	 */
	public int getNumClassesVisible() {
		int visible = 0;
		for (Double o : getOpacities()) {
			if (o > 0.)
				visible++;
		}
		return visible;
	}

	/**
	 * Opacities for classes
	 */
	public ArrayList<Double> getOpacities() {
		return opacities;
	}

	/**
	 * A global Setting for all CMEs
	 * 
	 * @return
	 */
	public Double getOpacity() {
		return opacity;
	}

	public BrewerPalette getPalette() {
		return palette;
	}

	public abstract RasterLegendData getRasterLegendData();

	@Override
	final public List<Rule> getRules() {

		RasterSymbolizer rs = StylingUtil.STYLE_BUILDER
				.createRasterSymbolizer();
		rs.setColorMap(getColorMap());

		Rule rule = ASUtil.SB.createRule(rs);

		/** Saving the legend label */
		rule.setTitle("TITLE" + getType().getTitle());
		rule.setName("NAME" + getType().getTitle());

		// addFilters(rule);

		rule.symbolizers().clear();

		rule.symbolizers().add(rs);

		Filter filter = FilterUtil.ALLWAYS_TRUE_FILTER;

		// The order is important! This is parsed the reverse way. The last
		// thing added to the filter equals the first level in the XML.
		filter = addAbstractRlSettings(filter);

		rule.setFilter(filter);

		ArrayList<Rule> rList = new ArrayList<Rule>();
		rList.add(rule);

		return rList;
	}

	public StyledRasterInterface<?> getStyledRaster() {
		return styledRaster;
	}

	public ArrayList<Double> getValues() {
		return values;
	}

	@Override
	public void importRules(List<Rule> rules) {
		pushQuite();

		if (rules.size() > 1) {
			LOGGER.warn("Importing a " + this.getClass().getSimpleName()
					+ " with " + rules.size() + " rules");
		}

		Rule rule = rules.get(0);

		// TODO Parse metainfostring?!

		try {
			RasterSymbolizer rs = (RasterSymbolizer) rule.symbolizers().get(0);
			ColorMap cm = rs.getColorMap();

			importValuesLabelsQuantitiesColors(cm);

			// Analyse the filters...
			Filter filter = rule.getFilter();
			filter = parseAbstractRlSettings(filter);

		} finally {
			popQuite();
		}
	}

	/**
	 * Attenetion: Ignores the CM type!
	 */
	protected void importValuesLabelsQuantitiesColors(ColorMap cm) {
		QuantileBin1D ops = new QuantileBin1D(1.);

		int count = 0;
		for (ColorMapEntry cme : cm.getColorMapEntries()) {
			importValuesLabelsQuantitiesColors(cme);

			if (cmt == ColorMap.TYPE_VALUES || count > 0) {
				// Add the last added opacity to the statistics
				ops.add(getOpacities().get(getOpacities().size() - 1));
			} else {
				// Skip the first if n+1=values = n classes
			}

			count++;
		}

		if (ops.median() >= 0 && ops.median() < 1.)
			setOpacity(ops.median());
		else
			setOpacity(1.);
	}

	protected void importValuesLabelsQuantitiesColors(ColorMapEntry cme) {
		final Double valueDouble = Double.valueOf(cme.getQuantity()
				.evaluate(null).toString());

		Translation translation = styledRaster.getLegendMetaData() != null ? styledRaster
				.getLegendMetaData().get(valueDouble) : null;

		if (I18NUtil.isEmpty(translation)) {
			final String labelFromCM = cme.getLabel();
			if (labelFromCM != null && !labelFromCM.isEmpty())
				getLabels().add(new Translation(labelFromCM));
			else
				translation = new Translation("");

		}

		add(valueDouble,
				Double.valueOf(cme.getOpacity().evaluate(null).toString()),
				StylingUtil.getColorFromColorMapEntry(cme), translation);
	}

	public void add(Double value, Double opacity, Color color, Translation label) {
		getValues().add(value);
		if (opacity == null)
			opacity = 1.;
		getOpacities().add(opacity);
		getColors().add(color);
		getLabels().add(label);
	}

	public void set(int idx, Double value, Double opacity, Color color,
			Translation label) {
		getValues().set(idx, value);
		getOpacities().set(idx, opacity);
		getColors().set(idx, color);
		getLabels().set(idx, label);
	}

	public void setOrAdd(int idx, Double value, Double opacity, Color color,
			Translation label) {
		if (idx >= getValues().size())
			add(value, opacity, color, label);
		else
			set(idx, value, opacity, color, label);
	}

	@Override
	public void parseMetaInfoString(String metaInfoString, FeatureTypeStyle fts) {

		metaInfoString = metaInfoString
				.substring(getType().toString().length());

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

	public void removeIdx(int index) {
		getValues().remove(index);
		getOpacities().remove(index);
		getLabels().remove(index);
		getColors().remove(index);

		fireEvents(new RuleChangedEvent("Index " + index + " removed", this));
	}

	public void setOpacity(Double opacity) {
		this.opacity = opacity;
	}

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

	public void setPalette(BrewerPalette palette) {
		this.palette = palette;
	}

	public void setStyledRaster(StyledRasterInterface<?> styledRaster) {
		this.styledRaster = styledRaster;
	}

}
