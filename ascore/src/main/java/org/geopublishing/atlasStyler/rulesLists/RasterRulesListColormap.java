package org.geopublishing.atlasStyler.rulesLists;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStyler.LANGUAGE_MODE;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.PaletteType;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SelectedChannelType;
import org.opengis.filter.Filter;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;

/**
 * Abstrakte Oberklasser aller {@link RasterRulesList}s die auf {@link ColorMap}
 * s basieren. Diese RulesLists können immmer nur auf einem Band arbeiten. Bei
 * einem Mehrband-Bild <b>muss</b> das Feld <code>band</code> gesetzt sein.
 */
public abstract class RasterRulesListColormap extends RasterRulesList {

	/**
	 * Typ der Colormap, TYPE_RAMP = 1; TYPE_INTERVALS = 2; TYPE_VALUES = 3;
	 */
	protected final int cmt;

	private final ArrayList<Color> colors = new ArrayList<Color>();
	private final ArrayList<Translation> labels = new ArrayList<Translation>();
	private final ArrayList<Double> opacities = new ArrayList<Double>();

	/**
	 * Dieser 0-basierte Wert gibt an, welches Band von möglicherweise >1
	 * Bändern gestyled wird.
	 */
	private int band = 0;

	private BrewerPalette palette = ASUtil.getPalettes(new PaletteType(true,
			false), -1)[0];

	protected final ArrayList<Double> values = new ArrayList<Double>();

	public RasterRulesListColormap(RulesListType rlt,
			StyledRasterInterface<?> styledRaster, int colorMapType) {
		super(rlt, null);
		setStyledRaster(styledRaster);

		cmt = colorMapType;
		if (cmt < 1 || cmt > 3)
			throw new IllegalArgumentException(
					"ColorMapType has to be 1, 2 or 3!");
	}

	@Override
	public String extendMetaInfoString() {
		String metaInfoString = super.extendMetaInfoString();

		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_PALTETTE
				+ METAINFO_KVP_EQUALS_CHAR + getPalette().getName();

		if (getStyledRaster().getNodataValue() != null)
			metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_NODATA
					+ METAINFO_KVP_EQUALS_CHAR
					+ getStyledRaster().getNodataValue();

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
		int numClasses = -1;

		try {

			if (cmt == ColorMap.TYPE_VALUES)
				numClasses = getValues().size();
			else {
				if (getValues().size() < 1)
					numClasses = 0;
				else
					numClasses = getValues().size() - 1;
			}
		} finally {
			test(numClasses);
		}

		return numClasses;
	}

	/**
	 * Throws an exception as soon as the array sizes of values, colors and
	 * opacities are not in sync
	 */
	protected abstract void test(int numClasses);

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

	public BrewerPalette getPalette() {
		return palette;
	}

	@Override
	final public List<Rule> getRules() {

		RasterSymbolizer rs = StylingUtil.STYLE_BUILDER
				.createRasterSymbolizer();
		rs.setColorMap(getColorMap());

		if (band >= 0) {
			// Setze SelectedChannel auf das ausgewählte Band.
			rs.setChannelSelection(StylingUtil.STYLE_FACTORY
					.createChannelSelection(new SelectedChannelType[] { StylingUtil.STYLE_FACTORY
							.createSelectedChannelType(
									String.valueOf(band + 1),
									(ContrastEnhancement) null) }));
		}

		Rule rule = ASUtil.SB.createRule(rs);

		/** Saving the legend label */
		rule.setTitle("TITLE" + getType().getTitle());
		rule.setName("NAME" + getType().getTitle());

		rule.symbolizers().clear();

		rule.symbolizers().add(rs);
		if (rs.getColorMap().getColorMapEntries().length == 0) {
			// An empty colormap will result in a fullly black raster!?
			rs.getColorMap().addColorMapEntry(
					StylingUtil.createColorMapEntry(
							RulesListInterface.RULENAME_DONTIMPORT,
							Double.MIN_VALUE, Color.WHITE, 0.0));
		}

		Filter filter = FilterUtil.ALLWAYS_TRUE_FILTER;

		// The order is important! This is parsed the reverse way. The last
		// thing added to the filter equals the first level in the XML.
		filter = addAbstractRlSettings(filter);

		rule.setFilter(filter);

		ArrayList<Rule> rList = new ArrayList<Rule>();
		rList.add(rule);

		return rList;
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

			importColorMap(cm);

			// Analyse the filters...
			org.opengis.filter.Filter filter = rule.getFilter();
			filter = parseAbstractRlSettings(filter);

		} finally {
			popQuite();
		}
	}

	abstract public void importColorMap(ColorMap cm);

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

		if (!metaInfoString.startsWith(getType().toString())) {
			// When importing a no ATlasStyler ColorMap
			return;
		}

		metaInfoString = metaInfoString
				.substring(getType().toString().length());

		/***********************************************************************
		 * Parsing a list of Key-Value Pairs from the FeatureTypeStyleName
		 */
		String[] params = metaInfoString.split(METAINFO_SEPERATOR_CHAR);
		for (String p : params) {
			String[] kvp = p.split(METAINFO_KVP_EQUALS_CHAR);

			if (kvp[0].equalsIgnoreCase(KVP_NODATA.toString())) {

				// Try to understand NODATAVALUE as Double from String
				try {
					Double noDataValue = Double.valueOf(kvp[1]);

					//
					if (getStyledRaster().getNodataValue() == null) {
						getStyledRaster().setNodataValue(noDataValue);
					} else if (!getStyledRaster().getNodataValue().equals(
							noDataValue)) {
						LOGGER.info("StyledRaster has NODATA value '"
								+ getStyledRaster().getNodataValue()
								+ "'. SLD has '" + noDataValue
								+ "'. StyledRaster value is not changed!");
					}
				} catch (NumberFormatException e) {
					LOGGER.error("Failes to parse NODATA value", e);
				}
			}

			else

			if (kvp[0].equalsIgnoreCase(KVP_PALTETTE)) {
				String brewerPaletteName = kvp[1];

				BrewerPalette foundIt = null;

				for (BrewerPalette ppp : ASUtil.getPalettes(new PaletteType(
						true, false), getNumClasses())) {
					if (ppp.getName().equals(brewerPaletteName)) {
						foundIt = ppp;
						break;
					}
				}
				if (foundIt == null) {
					LOGGER.warn("Couldn't find the palette with the name '"
							+ brewerPaletteName + "'.");
				} else {
					setPalette(foundIt);
				}
			}

		}

	}

	/**
	 * Entfernt alle Einträge aus der Colormap
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

	public void reset() {
		getColors().clear();
		getLabels().clear();
		getValues().clear();
		getOpacities().clear();
	}

	public abstract void applyPalette(JComponent parentGui);

	/**
	 * Dieser 0-basierte Wert gibt an, welches Band von möglicherweise >1
	 * Bändern gestyled wird.
	 */
	public void setBand(int band) {
		System.out.println("band = " + band);
		this.band = band;
	}

	/**
	 * Dieser 0-basierte Wert gibt an, welches Band von möglicherweise >1
	 * Bändern gestyled wird.
	 */
	public int getBand() {
		return band;
	}

}
