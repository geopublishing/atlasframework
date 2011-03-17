/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AtlasStyler.LANGUAGE_MODE;
import org.geopublishing.atlasStyler.classification.CLASSIFICATION_METHOD;
import org.geopublishing.atlasStyler.rulesLists.FeatureRuleList;
import org.geotools.styling.FeatureTypeStyle;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.swing.SwingUtil;

abstract public class QuantitiesRuleList extends FeatureRuleList {

	private static final Logger LOGGER = Logger
			.getLogger(QuantitiesRuleList.class);

	/**
	 * Defines the number of digits shown in interval description (rule title);
	 * Default is 3
	 */
	private int classDigits = 2;

	public final DecimalFormat classDigitsDecimalFormat = new DecimalFormat(
			SwingUtil.getNumberFormatPattern(classDigits));

	public final DecimalFormat classDigitsIntegerFormat = new DecimalFormat(
			SwingUtil.getNumberFormatPattern(0));

	/** Caches the limits* */
	private TreeSet<Double> classLimits = new TreeSet<Double>();

	private Color[] colors = null;

	private CLASSIFICATION_METHOD method = CLASSIFICATION_METHOD.DEFAULT_METHOD;

	private HashMap<Integer, String> ruleTitles = new HashMap<Integer, String>();

	public QuantitiesRuleList(RulesListType rulesListType,
			StyledFeaturesInterface<?> styledFeatures,
			GeometryForm geometryForm, boolean withDefaults) {

		super(rulesListType, styledFeatures, geometryForm, withDefaults);
	}

	/**
	 * Creates a default Rules Label for that class. It checks and honors the
	 * {@link LANGUAGE_MODE} setting.
	 * 
	 * @param lower
	 *            Lower (included) class break
	 * @param upper
	 *            Upper (excluded) class break
	 * @param isLast
	 *            If <code>true</code>, the upper class break is also included.
	 * @return something like
	 */
	public String createDefaultClassLabelFor(Number lower, Number upper,
			boolean isLast) {

		return createDefaultClassLabelFor(
				lower,
				upper,
				isLast,
				getStyledFeatures().getAttributeMetaDataMap()
						.get(getValue_field_name()).getUnit(), getFormatter());
	}

	/**
	 * Returns the number of digits shown in the rule description.
	 */
	public int getClassDigits() {
		return this.classDigits;
	}

	public TreeSet<Double> getClassLimits() {
		return classLimits;
	}

	/**
	 * @return the classLimits as an {@link ArrayList}.
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public ArrayList<Double> getClassLimitsAsArrayList() {
		return new ArrayList<Double>(classLimits);
	}

	public Color[] getColors() {
		return colors;
	}

	public CLASSIFICATION_METHOD getMethod() {
		return method;
	}

	public int getNumClasses() {
		if (classLimits.size() == 1) {
			// Special case
			return 1;
		}
		return classLimits.size() - 1;
	}

	public HashMap<Integer, String> getRuleTitles() {
		return ruleTitles;
	}

	/***************************************************************************
	 * ABSTRACT METHODS BEGIN HERE
	 * 
	 * @return
	 **************************************************************************/

	@Override
	abstract public void parseMetaInfoString(String metaInfoString,
			FeatureTypeStyle fts);

	/**
	 * Sets the number of digits shown in the rule description. Values less then
	 * 0 are treat as 0.
	 */
	public void setClassDigits(int classDigits) {
		this.classDigits = Math.max(0, classDigits);
		this.classDigitsDecimalFormat.applyPattern(SwingUtil
				.getNumberFormatPattern(classDigits));
	}

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
	public void setClassLimits(TreeSet<Double> classLimits) {
		setClassLimits(classLimits, false);
	}

	/**
	 * @return a {@link DecimalFormat} appropriate to render samples of the
	 *         selected value attribute.
	 */
	public DecimalFormat getFormatter() {
		return classDigitsDecimalFormat;
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
	public void setClassLimits(TreeSet<Double> classLimits,
			boolean resetRuleTitles) {

		this.classLimits = classLimits;

		if (classLimits.size() < 1) {
			LOGGER.error("numClasses == " + classLimits.size()
					+ " bei setClassLimits!?");
			return;
		}

		/***********************************************************************
		 * Create default Rule Titles..
		 */
		if (resetRuleTitles)
			getRuleTitles().clear();

		// This loop will only be executed if there are at least 2 breaks
		for (int i = 0; i < classLimits.size() - 1; i++) {
			Double lower = getClassLimitsAsArrayList().get(i);
			Double upper = getClassLimitsAsArrayList().get(i + 1);

			String stringTitle = createDefaultClassLabelFor(lower, upper,
					!(i < classLimits.size() - 1 - 1));

			// If we do not reset the ruleTiles, we only put a default where no
			// other value exists
			if (!resetRuleTitles) {
				if (!getRuleTitles().containsKey(i)) {
					getRuleTitles().put(i, stringTitle);
				}
			} else {
				getRuleTitles().put(i, stringTitle);
			}

		}

		// Special case
		if (classLimits.size() == 1 && resetRuleTitles) {
			getRuleTitles().put(0, classLimits.first().toString());
		}

		// Setting the colors to null we lead to new colors being created the
		// next time getColors() is called.
		updateColorsClassesChanged();

		fireEvents(new RuleChangedEvent("Set class limits", this));
	}

	public void setColors(Color[] colors) {
		this.colors = colors;
	}

	public void setMethod(CLASSIFICATION_METHOD method) {
		this.method = method;
	}

	public void setRuleTitles(HashMap<Integer, String> ruleTitles) {
		this.ruleTitles = ruleTitles;
		fireEvents(new RuleChangedEvent("setRuleTitles", this));
	}

	protected void updateColorsClassesChanged() {
		if (getColors() != null) {
			// The user might have manually adapted the colors, so we try to
			// keep them where possible.
			if (getColors().length == getNumClasses()) {
				return;
			} else {
				setColors(null);
			}
		}
	}

}
