/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.sld;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.geotools.styling.FeatureTypeStyle;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsBetween;

import schmitzm.swing.SwingUtil;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.Translation;
import skrueger.sld.AtlasStyler.LANGUAGE_MODE;

abstract public class QuantitiesRuleList<NUMBERTYPE extends Number> extends
		FeatureRuleList {
	private  static final Logger LOGGER = Logger
			.getLogger(QuantitiesRuleList.class);

	/** KEY-name for the KVPs in the meta information * */
	private static final String KVP_NORMALIZATION_FIELD = "NORM";

	/** KEY-name for the KVPs in the meta information * */
	private static final String KVP_VALUE_FIELD = "VALUE";

	/**
	 * The {@link String} name of the attribute which contains the quantity
	 * values
	 */
	private String value_field_name;

	/**
	 * The {@link String} name of the attribute used to normalize the quantity
	 * attribute
	 */
	private String normalizer_field_name;

	// /** The number of classes * */
	// private int numClasses = 5;

	private Color[] colors = null;

	public int getNumClasses() {
		return classLimits.size() - 1;
	}

	/** Caches the limits* */
	private TreeSet<NUMBERTYPE> classLimits = new TreeSet<NUMBERTYPE>();

	//
	// public void setNumClasses(int numClasses) {
	// if (this.numClasses != numClasses) {
	// LOGGER.debug("setting numclasses to " + numClasses
	// + " and colors to null");
	// setColors(null);
	// this.numClasses = numClasses;
	// } else {
	// LOGGER.debug("IGNORING setting numclasses to " + numClasses
	// + " and colors to null");
	// }
	// }

	// ms-01.sn
	/**
	 * Defines the number of digits shown in interval description (rule title);
	 * Default is 3
	 */
	private int classDigits = 3;
	private DecimalFormat classDigitsFormat = new DecimalFormat(SwingUtil
			.getNumberFormatPattern(classDigits));

	/**
	 * Returns the number of digits shown in the rule description.
	 */
	public int getClassDigits() {
		return this.classDigits;
	}

	/**
	 * Sets the number of digits shown in the rule description. Values less then
	 * 0 are treat as 0.
	 */
	public void setClassDigits(int classDigits) {
		this.classDigits = Math.max(0, classDigits);
		this.classDigitsFormat.applyPattern(SwingUtil
				.getNumberFormatPattern(classDigits));
	}

	// ms-01.en

	public TreeSet<NUMBERTYPE> getClassLimits() {
		return classLimits;
	}

	/**
	 * @return the classLimits as an {@link ArrayList}.
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public ArrayList<NUMBERTYPE> getClassLimitsAsArrayList() {
		return new ArrayList<NUMBERTYPE>(classLimits);
	}

	/**
	 * This initializes {@link #numClasses} also. Colors are set to
	 * <code>null</code>, so they will be chosen from the palette again. It
	 * rests the rule titles.
	 * 
	 * @param classLimits
	 *            Classlimits or the Classes to set.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void setClassLimits(TreeSet<NUMBERTYPE> classLimits) {
		setClassLimits(classLimits, false);
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public void setClassLimits(TreeSet<NUMBERTYPE> classLimits,
			boolean resetRuleTitles) {

		this.classLimits = classLimits;
//
		if (classLimits.size() < 2) {
			LOGGER.error("numClasses < 1 bei setClassLimits. NOT accepting it");
			// numClasses = 0;
			return;
		}

//		 setNumClasses(classLimits.size() - 1);
//		
//		 if (numClasses < 0) {
//		 LOGGER.error("numClasses < 0! bei setClassLimits");
//		 }

		/***********************************************************************
		 * Create default Rule Titles..
		 */
		if (resetRuleTitles)
			getRuleTitles().clear();

		for (int i = 0; i < classLimits.size() - 1; i++) {
			NUMBERTYPE lower = getClassLimitsAsArrayList().get(i);
			NUMBERTYPE upper = getClassLimitsAsArrayList().get(i + 1);

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

		// Setting the colors to null we lead to new colors beeing created the
		// next time getColors() is called.
		setColors(null);

		fireEvents(new RuleChangedEvent("Set class limits", this));
	}

	/**
	 * Creates a default Rules Label for that class. It checks and honours the
	 * {@link LANGUAGE_MODE} setting.
	 * 
	 * @param lowber
	 *            Lower (included) class break
	 * @param upper
	 *            Upper (excluded) class break
	 * @param isLast
	 *            If <code>true</code>, the upper class break is also included.
	 * @return something like
	 */
	public String createDefaultClassLabelFor(Number lowber, Number upper,
			boolean isLast) {

		String limitsLabel = "[" + classDigitsFormat.format(lowber) + " - "
				+ classDigitsFormat.format(upper);
		limitsLabel += isLast ? "]" : "[";

		String stringTitle;
		/**
		 * Create a default title
		 */
		if (AtlasStyler.getLanguageMode() == AtlasStyler.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
			stringTitle = new Translation(AtlasStyler.getLanguages(),
					limitsLabel).toOneLine();
		} else {
			stringTitle = limitsLabel;
		}

		return stringTitle;
	}

	public QuantitiesRuleList(StyledFeaturesInterface<?> styledFeatures) {
		super(styledFeatures);
		Collection<String> numericalFieldNames = ASUtil.getNumericalFieldNames(
				getStyledFeatures().getSchema(), false);
		if (numericalFieldNames.size() > 0)
			value_field_name = numericalFieldNames.toArray(new String[] {})[0];
	}

	/**
	 * @return the normalizer_field_name
	 */
	public String getNormalizer_field_name() {
		return normalizer_field_name;
	}

	/**
	 * @param normalizer_field_name
	 *            the normalizer_field_name to set
	 */
	final public void setNormalizer_field_name(String normalizer_field_name) {
		this.normalizer_field_name = normalizer_field_name;
	}

	/**
	 * @return the value_field_name
	 */
	final public String getValue_field_name() {
		return value_field_name;
	}

	/**
	 * @param value_field_name
	 *            the value_field_name to set
	 */
	public void setValue_field_name(String value_field_name) {
		this.value_field_name = value_field_name;
	}

	/**
	 * @param filter
	 *            A {@link Filter}
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 * @return <code>null</code> if it is not a "BetweenFilter"
	 */
	public static double[] interpretBetweenFilter(Filter filter) {
		if (filter instanceof PropertyIsBetween) {
			PropertyIsBetween betweenFilter = (PropertyIsBetween) filter;
			double lower = Double.parseDouble(betweenFilter.getLowerBoundary()
					.toString());
			double upper = Double.parseDouble(betweenFilter.getUpperBoundary()
					.toString());
			return new double[] { lower, upper };
		}
		return null;
	}

	private HashMap<Integer, String> ruleTitles = new HashMap<Integer, String>();

	public HashMap<Integer, String> getRuleTitles() {
		return ruleTitles;
	}

	public void setRuleTitles(HashMap<Integer, String> ruleTitles) {
		this.ruleTitles = ruleTitles;
		fireEvents(new RuleChangedEvent("setRuleTitles", this));
	}

	/**
	 * The children of this class define most metainfo. Some metainformation is
	 * the same for all children and is added here.
	 * 
	 * @param metaInfoString
	 *            The metaInfoString starting with a getType()-result and some
	 *            KVPs
	 * 
	 * @return an expanded {@link String} with more KVPs
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	protected String extendMetaInfoString(String metaInfoString) {
		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_VALUE_FIELD
				+ METAINFO_KVP_EQUALS_CHAR + getValue_field_name();
		metaInfoString += METAINFO_SEPERATOR_CHAR + KVP_NORMALIZATION_FIELD
				+ METAINFO_KVP_EQUALS_CHAR + getNormalizer_field_name();
		return metaInfoString;
	}

	/**
	 * The children of this class parse most metainfo. Some metainformation is
	 * the same for all children and is parsed here. This method must be called
	 * from all children.
	 * 
	 * @param metaInfoString
	 *            The metaInfoString cleared from with a getType()-result and
	 *            some KVPs
	 * 
	 * @return an expanded {@link String} with more KVPs
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	protected void parseMetaInfoString(String metaInfoString) {

		/***********************************************************************
		 * Parsing a list of Key-Value Pairs from the FeatureTypeStyleName
		 */
		String[] kvp = new String[] { "uninitialized", "uninit" };
		String value = "nothging yet";
		try {

			String[] params = metaInfoString.split(METAINFO_SEPERATOR_CHAR);
			for (String p : params) {
				kvp = p.split(METAINFO_KVP_EQUALS_CHAR);

				if (kvp[0].equalsIgnoreCase(KVP_NORMALIZATION_FIELD.toString())) {
					value = kvp[1];
					if (value.equalsIgnoreCase("null"))
						setNormalizer_field_name(null);
					else
						setNormalizer_field_name(value);
				}

				if (kvp[0].equalsIgnoreCase(KVP_VALUE_FIELD.toString())) {
					value = kvp[1];
					if (value.equalsIgnoreCase("null"))
						setValue_field_name(null);
					else
						setValue_field_name(kvp[1]);
				}
			}

		} catch (RuntimeException e) {
			LOGGER.error("KVP=" + kvp[0] + kvp[1]);
			LOGGER.error("VALUE=" + value);

			throw (e);
		}
	}

	/***************************************************************************
	 * ABSTRACT METHODS BEGIN HERE
	 * 
	 * @return
	 **************************************************************************/

	abstract public void parseMetaInfoString(String metaInfoString,
			FeatureTypeStyle fts);

	/**
	 * @param colors
	 */
	public void setColors(Color[] colors) {
		this.colors = colors;
	}

	public Color[] getColors() {
		return colors;
	}

}
