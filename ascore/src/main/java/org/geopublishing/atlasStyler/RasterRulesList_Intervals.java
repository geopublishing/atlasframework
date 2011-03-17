package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.geopublishing.atlasStyler.AtlasStyler.LANGUAGE_MODE;
import org.geopublishing.atlasStyler.classification.CLASSIFICATION_METHOD;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.PaletteType;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.FeatureTypeStyle;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.swing.SwingUtil;

public class RasterRulesList_Intervals extends RasterRulesList {

	/**
	 * n+1 values = n classes
	 * 
	 * @param parentGui
	 *            is <code>null</code>, no warnings will be shown if the number
	 *            of classes if higher than the number of colors
	 */
	@Override
	public void applyPalette(JComponent parentGui) {
		pushQuite();

		getColors().clear();

		try {

			if (getValues().size() < 2)
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
					Math.min(getPalette().getMaxColors(),
							getValues().size() - 1));

			int idx = 0;
			for (int i = 1; i < getValues().size(); i++) {

				Color colorToSet;
				if (getOpacities().get(i-1) == 0.) {
					colorToSet = Color.WHITE;
				} else
				colorToSet = colors[idx];

				if (i >= getColors().size())
					getColors().add(colorToSet);
				else
					getColors().set(i, colorToSet);

				idx++;
				idx = idx % getPalette().getMaxColors();

			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied a COLORPALETTE to all ColorMapEntries", this));
		}

	}

	/**
	 * Together with {@link #extendMetaInfoString()} this allows loading and
	 * saving the RL
	 */
	@Override
	public void parseMetaInfoString(String metaInfoString, FeatureTypeStyle fts) {

		metaInfoString = metaInfoString
				.substring(getType().toString().length());

		/***********************************************************************
		 * Parsing a list of Key-Value Pairs from the FeatureTypeStyleName
		 */
		String[] params = metaInfoString.split(METAINFO_SEPERATOR_CHAR);
		for (String p : params) {
			String[] kvp = p.split(METAINFO_KVP_EQUALS_CHAR);

			if (kvp[0].equalsIgnoreCase(KVP_METHOD.toString())) {

				// We had a typo error in AtlasStyler 1.1 - to correctly import
				// old styled, we have to correct it here:
				if (kvp[1].equals("QANTILES"))
					kvp[1] = "QUANTILES";

				setMethod(CLASSIFICATION_METHOD.valueOf(kvp[1]));

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

	public RasterRulesList_Intervals(StyledRasterInterface<?> styledRaster,
			boolean withDefaults) {
		super(RulesListType.RASTER_COLORMAP_INTERVALS, styledRaster,
				ColorMap.TYPE_INTERVALS);

		if (withDefaults) {
			// TODO automatically min to max?
		}

	}

	/**
	 * Defines the number of digits shown in interval description (rule title);
	 * Default is 3
	 */
	private int classDigits = 2;

	public final DecimalFormat classDigitsDecimalFormat = new DecimalFormat(
			SwingUtil.getNumberFormatPattern(classDigits));

	public final DecimalFormat classDigitsIntegerFormat = new DecimalFormat(
			SwingUtil.getNumberFormatPattern(0));

	/**
	 * This initializes {@link #numClasses} also. Colors are set to
	 * <code>null</code>, so they will be chosen from the palette again. It
	 * rests the rule titles.
	 * 
	 * @param classLimits
	 *            Classlimits or the Classes to set.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void setValues(ArrayList<Double> classLimits) {
		setValues(classLimits, false);
	}

	public void setMethod(CLASSIFICATION_METHOD method) {
		this.method = method;
	}

	public CLASSIFICATION_METHOD getMethod() {
		return method;
	}

	private CLASSIFICATION_METHOD method = CLASSIFICATION_METHOD.DEFAULT_METHOD;

	/**
	 * Assembles the Colormap from the values, lables, colors and opacities.
	 */
	@Override
	public ColorMap getColorMap() {

		test(-1);

		ColorMap cm = StylingUtil.STYLE_FACTORY.createColorMap();
		cm.setType(cmt);

		for (int i = 0; i < getValues().size(); i++) {

			if (i == 0) {
				ColorMapEntry cme = StylingUtil.createColorMapEntry("",
						getValues().get(i), Color.WHITE, 0.);
				cm.addColorMapEntry(cme);
			} else {
				String labelString = "";

				final Translation label = getLabels().get(i - 1);
				if (label != null) {
					if (AtlasStyler.getLanguageMode() == LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
						labelString = label.toOneLine();
					} else
						labelString = label.toString();
				}
				ColorMapEntry cme = StylingUtil.createColorMapEntry(
						labelString, getValues().get(i),
						getColors().get(i - 1), getOpacities().get(i - 1));

				cm.addColorMapEntry(cme);

			}

		}

		return cm;
	}

	/**
	 * This initializes {@link #numClasses} also. Colors are set to
	 * <code>null</code>, so they will be chosen from the palette again.
	 * 
	 * @param classLimits
	 *            Classlimits or the Classes to set.
	 * @param classDigits
	 *            number of digits shown in the rule title
	 * @param resetRuleTitles
	 *            if <code>true</code> the rule titles will be reset to default
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public void setValues(ArrayList<Double> classLimits, boolean resetRuleTitles) {

		getValues().clear();
		getValues().addAll(classLimits);

		getOpacities().clear();
		applyOpacity();

		if (classLimits.size() < 1) {
			LOGGER.error("numClasses == " + classLimits.size()
					+ " bei setClassLimits!?");
			return;
		}

		/***********************************************************************
		 * Create default Rule Titles..
		 */
		if (resetRuleTitles)
			getLabels().clear();

		// This loop will only be executed if there are at least 2 breaks
		for (int i = 0; i < classLimits.size() - 1; i++) {
			Double lower = values.get(i);
			Double upper = values.get(i + 1);

			String stringTitle = createDefaultClassLabelFor(lower, upper,
					!(i < classLimits.size() - 1 - 1), "", getFormatter());

			// If we do not reset the ruleTiles, we only put a default where no
			// other value exists
			if (!resetRuleTitles) {
				if (getLabels().get(i) != null) {
					getLabels().set(i, new Translation(stringTitle));
				}
			} else {
				getLabels().add(i, new Translation(stringTitle));
			}

		}

		// Special case
		if (classLimits.size() == 1 && resetRuleTitles) {
			getLabels().set(0, new Translation(classLimits.get(0).toString()));
		}
		
		if (getLabels().size() != classLimits .size()-1) {
			throw new RuntimeException("Labels not set correctly");
		}

		updateColorsClassesChanged();

		fireEvents(new RuleChangedEvent("Set class limits", this));
	}

	@Override
	public String extendMetaInfoString() {
		String metaInfoString = super.extendMetaInfoString();

		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_METHOD
				+ METAINFO_KVP_EQUALS_CHAR + getMethod();

		return metaInfoString;
	}

	/**
	 * Sets the number of digits shown in the rule description. Values less then
	 * 0 are treat as 0.
	 */
	public void setClassDigits(int classDigits) {
		this.classDigits = Math.max(0, classDigits);
		this.classDigitsDecimalFormat.applyPattern(SwingUtil
				.getNumberFormatPattern(classDigits));
	}

	protected void updateColorsClassesChanged() {
		if (getColors() != null) {
			// The user might have manually adapted the colors, so we try to
			// keep them where possible.
			if (getColors().size() == getValues().size() - 1) {
				return;
			} else {
				applyPalette(null);
			}
		}
	}

	@Override
	public RasterLegendData getRasterLegendData() {
		RasterLegendData rld = new RasterLegendData(true);

		for (int i = 1; i < getNumClasses(); i++) {
			// if (getShowInLegends().get(i))
			rld.put(getValues().get(i), getLabels().get(i));
		}

		return rld;
	}

	public DecimalFormat getFormatter() {
		return classDigitsDecimalFormat;
	}

	@Override
	public void applyOpacity() {
		pushQuite();

		try {

			final Double op = getOpacity();
			if (op == null)
				return;

			for (int i = 0; i < getValues().size() - 1; i++) {

				if (i >= getOpacities().size())
					getOpacities().add(op);

				if (getOpacities().get(i) != 0)
					setOpacity(i, op);
			}

			// Remove extra opacities
			while (getOpacities().size() > getValues().size() - 1) {
				getOpacities().remove(getOpacities().size() - 1);
			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied an OPACITY to all ColorMapEntries ", this));
		}
	}

	/**
	 * Throws an exception as soon as the array sizes of values, colors and
	 * opacities are not in sync
	 */
	@Override
	protected void test(int classesExpected) {
		int valSize = getValues().size();

		if (classesExpected == -1)
			classesExpected = valSize - 1;
		if (classesExpected < 0)
			classesExpected = 0;

		int opSize = getOpacities().size();
		int colSize = getColors().size();
		int labelSize = getLabels().size();
		String error = "expectedClasses=" + classesExpected + "  valSize="
				+ valSize + " opSize=" + opSize + " colSize=" + colSize
				+ " labelSize=" + labelSize;
		if (opSize != classesExpected
				|| (valSize != classesExpected + 1 && (classesExpected != 0 && valSize != 0))
				|| colSize != classesExpected || labelSize != classesExpected)
			throw new RuntimeException(error);
	}
}
