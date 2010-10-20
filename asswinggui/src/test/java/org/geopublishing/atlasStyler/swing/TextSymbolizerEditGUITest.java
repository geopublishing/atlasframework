package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertEquals;

import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.TextRuleList;
import org.geotools.data.FeatureSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import skrueger.geotools.StyledFS;

public class TextSymbolizerEditGUITest {
	private static TextRuleList tr;
	private static AtlasStyler atlasStyler;
	private static FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon;

	@BeforeClass
	public static void setup() throws IOException {
		featureSource_polygon = AsTestingUtil.getPolygonsFeatureSource();
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
		assertEquals("default number of fonts is 5", 5, jComboBoxFont
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
		assertEquals("default number of fonts is 5", 5, jComboBoxFont
				.getItemCount());
	}

}
