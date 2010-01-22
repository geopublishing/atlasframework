package skrueger.atlas.internal;

import org.apache.log4j.Logger;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;

import schmitzm.geotools.feature.CQLFilterParser;

/**
 * A utility class with constants for parsing and storing AML.
 */
public class AMLUtil {
	private static Logger LOGGER = Logger.getLogger(AMLUtil.class);

	/**
	 * XML Schema name spaces of the AtlasMarkupLanguage (AML) and others
	 */
	public static final String AMLURI = "http://www.wikisquare.de/AtlasML";

	public static final String SLDURI = "http://www.opengis.net/sld";

	public static final String OGCURI = "http://www.opengis.net/ogc";

	public static final String GMLURI = "http://www.opengis.net/gml";

	public static final String ATT_majVersion = "majVersion";
	public static final String ATT_minVersion = "minVersion";
	public static final String ATT_buildVersion = "buildVersion";

	public static final String ATT_localname = "localname";

	public static final String ATT_namespace = "namespace";

	public static final String ATT_weight = "weight";

	public static final String ATT_functionX = "functionX";
	public static final String ATT_functionA = "functionA";

	/**
	 * Converts an old 'Martin' filter rule to a new CQL filter.
	 * @throws CQLException when the filter can not be
	 *             successfully converted.
	 */
	public static String upgradeMartinFilter2ECQL(String filterString) throws CQLException {
		
		String old = filterString;
		
		if (filterString.trim().equals(""))
			return Filter.INCLUDE.toString();
		
		filterString = filterString.replace("( \" \" )", "' '");
		filterString = filterString.replace("\"", "'");
		filterString = filterString.replace("$", "");
		filterString = filterString.replace("|", " OR ");
		filterString = filterString.replace("&", " AND ");
		filterString = filterString.replace("!=", " <> ");
		
		LOGGER.debug("old = \n"+old+" converted to \n"+filterString);
		
		Filter filter = new CQLFilterParser().parseFilter(filterString);
//		Filter cqlFilter = CQL.toFilter(filterString);
//		cqlFilter.evaluate(null);

		return filterString;
	}

}
