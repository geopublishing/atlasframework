package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.TextRuleList;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.FeatureTypeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import skrueger.geotools.StyledFS;

public class TextSymbolizerEditGUITest {
	public final static String COUNTRY_SHP_RESNAME = "/data/shp countries/country.shp";
	private static FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon;
	private static TextRuleList tr;
	private static AtlasStyler atlasStyler;

	@BeforeClass
	public static void setup() throws IOException {
		URL shpURL = AtlasStyler.class.getResource("/data/smallshape/country.shp");
		assertNotNull(COUNTRY_SHP_RESNAME + " not found!", shpURL);
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("url", shpURL);
		DataStore dataStore = DataStoreFinder.getDataStore(params);
		featureSource_polygon = dataStore.getFeatureSource(dataStore
				.getTypeNames()[0]);
		StyledFS styledFeatures = new StyledFS(featureSource_polygon);
		tr = new TextRuleList(styledFeatures);
		tr.addDefaultClass();
		
		atlasStyler = new AtlasStyler(styledFeatures);
	}

	@AfterClass
	public static void after() {
		featureSource_polygon.getDataStore().dispose();
	}

	@Test
	public void testGetFontComboBox() throws IOException {
		TextSymbolizerEditGUI textSymbolizerEditGUI = new TextSymbolizerEditGUI(
				tr, atlasStyler, featureSource_polygon.getFeatures());
		JComboBox jComboBoxFont = textSymbolizerEditGUI.getJComboBoxFont();
		assertEquals("default number of fonts is 6", 6, jComboBoxFont
				.getItemCount());

	}

	@Test
	public void testGetFontComboBox2() throws IOException {
		List<Font> fonts = new ArrayList<Font>();
		Font f = new JLabel().getFont().deriveFont(3);
		atlasStyler.setFonts(fonts);

		TextSymbolizerEditGUI textSymbolizerEditGUI = new TextSymbolizerEditGUI(
				tr, atlasStyler, featureSource_polygon.getFeatures());
		JComboBox jComboBoxFont = textSymbolizerEditGUI.getJComboBoxFont();
		assertEquals("default number of fonts is 6", 6, jComboBoxFont
				.getItemCount());
	}

}
