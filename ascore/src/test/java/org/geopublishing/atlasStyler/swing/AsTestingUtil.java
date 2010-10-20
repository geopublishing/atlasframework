package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.styling.StylingUtil;
import skrueger.geotools.StyledFS;
import skrueger.geotools.io.GeoImportUtilURL;

public class AsTestingUtil {
	public final static String COUNTRY_SHP_RESNAME = "/data/shp countries/country.shp";

	public static FeatureSource<SimpleFeatureType, SimpleFeature> getPolygonsFeatureSource()
			throws IOException {
		final URL shpURL = AtlasStyler.class
				.getResource("/data/smallshape/country.shp");
		assertNotNull(COUNTRY_SHP_RESNAME + " not found!", shpURL);
		final Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", shpURL);
		final DataStore dataStore = DataStoreFinder.getDataStore(params);
		return dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
	}

	public enum TestSld {
		textRulesDefaultLocalizedPre16("/oldLocalizedDefaultRule_pre16.sld"), textRulesPre15(
				"/oldTextRuleClasses_Pre15.sld");

		private final String resLoc;

		TestSld(String resLoc) {
			this.resLoc = resLoc;
		}

		public Style getStyle() {
			return StylingUtil.loadSLD(getUrl())[0];
		}

		public String getResLoc() {
			return resLoc;
		}

		public URL getUrl() {
			return AsTestingUtil.class.getResource(resLoc);
		}

	}

}
