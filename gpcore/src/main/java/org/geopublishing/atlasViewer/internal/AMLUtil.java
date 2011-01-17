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
package org.geopublishing.atlasViewer.internal;

import org.apache.log4j.Logger;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;

import schmitzm.geotools.gui.ScalePane;
import schmitzm.geotools.gui.ScalePanel;

/**
 * A utility class with constants for parsing and storing AML.
 */
public class AMLUtil {
	private static Logger LOGGER = Logger.getLogger(AMLUtil.class);

	/**
	 * XML Schema name spaces of the AtlasMarkupLanguage (AML) and others
	 */
	public static final String AMLURI = "http://www.wikisquare.de/AtlasML";
	public static final String AMLSCHEMALOCATION = "http://www.wikisquare.de/AtlasML.xsd";

	public static final String SLDURI = "http://www.opengis.net/sld";

	public static final String OGCURI = "http://www.opengis.net/ogc";

	public static final String GMLURI = "http://www.opengis.net/gml";

	/** Tag used in AMLURI name-space **/
	public static final String TAG_nodataValue = "nodataValue";
	/** Tag used in AMLURI name-space **/
	public static final String TAG_attributeMetadata = "dataAttribute";

	/** Attribute used in AMLURI name-space **/
	public static final String ATT_majVersion = "majVersion";
	/** Attribute used in AMLURI name-space **/
	public static final String ATT_minVersion = "minVersion";
	/** Attribute used in AMLURI name-space **/
	public static final String ATT_buildVersion = "buildVersion";
	/** Attribute used in AMLURI name-space **/
	public static final String ATT_localname = "localname";
	/** Attribute used in AMLURI name-space **/
	public static final String ATT_namespace = "namespace";
	/** Attribute used in AMLURI name-space **/
	public static final String ATT_weight = "weight";
	/** Attribute used in AMLURI name-space **/
	public static final String ATT_functionX = "functionX";
	/** Attribute used in AMLURI name-space **/
	public static final String ATT_functionA = "functionA";
	/** Attribute used in AMLURI name-space **/
	public static final String ATT_PREVIEW_MAX_MAPEXTEND_IN_GP = "previewMaxMapExtendInGP";

	/**
	 * Attribute used in AML map description to describe the units that shall be
	 * used in the {@link ScalePanel}
	 **/
	public static final String ATT_MAP_SCALE_UNITS = "scaleUnits";

	/**
	 * Attribute used in AML map description to describe whether the
	 * {@link ScalePane} shall be shown in the map
	 **/
	public static final String ATT_MAP_SCALE_VISIBLE = "scaleVisible";

	/**
	 * Tag used to list font filenames that are part of an atlas
	 * 
	 * @since 1.5
	 */
	public static final String TAG_FONTS = "fonts";

	/**
	 * Tag to describe one font inside the TAG_FONTS tag
	 * 
	 * @since 1.5
	 */
	public static final String TAG_FONT = "font";

	/**
	 * Attribute to describe a font filename
	 * 
	 * @since 1.5
	 */
	public static final String ATT_FONT_FILENAME = "filename";

	/**
	 * If the atlas is exported to the web, this the URL that it is supposed to
	 * run at. E.g. http://www.geopublishing.org/atlases/myatlas/
	 * 
	 * @since 1.6
	 */
	public static final String ATT_jnlpBaseUrl = "jnlpBaseUrl";

	/**
	 * Where to position the map logo.
	 * 
	 * @since 1.7
	 */
	public static final String ATT_maplogoPosition = "mapLogoPosition";

	/**
	 * Converts an old 'Martin' filter rule to a new CQL filter.
	 * 
	 * @throws CQLException
	 *             when the filter can not be successfully converted.
	 */
	public static String upgradeMartinFilter2ECQL(String filterString)
			throws CQLException {

		String old = filterString;

		if (filterString.trim().equals(""))
			return Filter.INCLUDE.toString();

		filterString = filterString.replace("( \" \" )", "' '");
		filterString = filterString.replace("\"", "'");
		filterString = filterString.replace("$", "");
		filterString = filterString.replace("|", " OR ");
		filterString = filterString.replace("&", " AND ");
		filterString = filterString.replace("!=", " <> ");

		LOGGER.debug("old = \n" + old + " converted to \n" + filterString);

		// Filter filter = new CQLFilterParser().parseFilter(filterString);
		// Filter cqlFilter = CQL.toFilter(filterString);
		// cqlFilter.evaluate(null);

		return filterString;
	}

}
