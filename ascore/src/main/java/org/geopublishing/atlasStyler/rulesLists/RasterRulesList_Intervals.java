package org.geopublishing.atlasStyler.rulesLists;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStyler.LANGUAGE_MODE;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.classification.CLASSIFICATION_METHOD;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.FeatureTypeStyle;

import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.swing.SwingUtil;

public class RasterRulesList_Intervals extends RasterRulesListColormap {

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
				if (getOpacities().get(i - 1) == 0.) {
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

		super.parseMetaInfoString(metaInfoString, fts);

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
		if (getValues().size() < 2)
			return cm;

		int lastidx = getValues().size() - 1;
		for (int i = 0; i < lastidx; i++) {

			String labelString = "";

			final Translation label = getLabels().get(i);
			if (label != null) {
				if (AtlasStyler.getLanguageMode() == LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
					labelString = label.toOneLine();
				} else
					labelString = label.toString();
			}

			Color color;
			Double opacity;
			if (i == 0) {
				color = Color.WHITE;
				opacity = 0.;
			} else {
				color = getColors().get(i - 1);
				opacity = getOpacities().get(i - 1);
			}

			ColorMapEntry cme = StylingUtil.createColorMapEntry(labelString,
					getValues().get(i), color, opacity);

			cm.addColorMapEntry(cme);

		}

		// The pre-last CME
		ColorMapEntry cme = StylingUtil.createColorMapEntry("", getValues()
				.get(lastidx), getColors().get(lastidx - 1), getOpacities()
				.get(lastidx - 1));
		cm.addColorMapEntry(cme);

		// The last CME is only a repetition of the pre-last CME to make the
		// upper limit of the last class INCLUSIVE
		cm.addColorMapEntry(cme);

		return cm;
	}

	@Override
	public void importColorMap(ColorMap cm) {
		reset();

		/**
		 * Because the upper Limits are EXCLUSIVE, the last value is repeated by
		 * AtlasStylerRaster. So the last CME can be ignored, since it is
		 * expected to contain the pre-last's value again.
		 */
		int countCME = cm.getColorMapEntries().length - 1;
		int lastCME = countCME - 1;

		ArrayList<ColorMapEntry> cmes = new ArrayList<ColorMapEntry>();
		for (ColorMapEntry cme : cm.getColorMapEntries())
			cmes.add(cme);

		for (int i = 0; i < countCME; i++) {

			// Wert wird immer importiert
			ColorMapEntry cme = cmes.get(i);
			getValues().add(Double.valueOf(cme.getQuantity().toString()));

			if (i < lastCME) {
				// Bis auf dem letzten wird das label importiert
				getLabels().add(new Translation(cme.getLabel()));
			}

			if (i > 0) {
				getColors().add(StylingUtil.getColorFromColorMapEntry(cme));
				getOpacities().add(
						Double.valueOf(cme.getOpacity().evaluate(null)
								.toString()));
			}

		}

	}

	/**
	 * INTERVAL!
	 */
	protected void importValuesLabelsQuantitiesColors(ColorMapEntry cme,
			boolean full) {
		final Double valueDouble = Double.valueOf(cme.getQuantity()
				.evaluate(null).toString());

		if (!full) {
			getValues().add(valueDouble);
			return;
		}

		// Translation translation = styledRaster.getLegendMetaData() != null ?
		// styledRaster
		// .getLegendMetaData().get(valueDouble) : null;
		//
		// if (I18NUtil.isEmpty(translation)) {
		final String labelFromCM = cme.getLabel();
		Translation translation;
		if (labelFromCM != null && !labelFromCM.isEmpty())
			translation = new Translation(labelFromCM);
		else
			translation = new Translation("");
		// }

		if (translation.toString().startsWith(
				RulesListInterface.RULENAME_DONTIMPORT))
			return;

		add(valueDouble,
				Double.valueOf(cme.getOpacity().evaluate(null).toString()),
				StylingUtil.getColorFromColorMapEntry(cme), translation);
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

		// Wenn mehr OPs als Classen, dann entfernen
		while (getOpacities().size() > classLimits.size() - 1) {
			getOpacities().remove(getOpacities().size() - 1);
		}
		// Wenn weniger OPs als Classen, dann hinzufügen
		while (getOpacities().size() < classLimits.size() - 1) {
			getOpacities().add(getOpacity());
		}

		// Wenn mehr Labels als Classen, dann entfernen
		while (getLabels().size() > classLimits.size() - 1) {
			getLabels().remove(getLabels().size() - 1);
		}

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
				if (getLabels().get(i) == null || getLabels().get(i).isEmpty()) {
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

		if (getLabels().size() != classLimits.size() - 1) {
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
