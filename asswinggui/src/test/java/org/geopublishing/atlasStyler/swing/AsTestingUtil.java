package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertNotNull;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class AsTestingUtil {
	public final static String COUNTRY_SHP_RESNAME = "/data/shp countries/country.shp";
	
	public static boolean INTERACTIVE = !GraphicsEnvironment.isHeadless();

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

}
