package org.geopublishing.atlasViewer.swing;

import java.io.IOException;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.TextRuleList;
import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geopublishing.atlasStyler.swing.StylerDialog;
import org.geotools.data.FeatureSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import skrueger.geotools.StyledFS;

public class StylerDialogTest {
	private static TextRuleList tr;
//	private static AtlasStyler atlasStyler;
	private static FeatureSource<SimpleFeatureType, SimpleFeature> featureSource_polygon;

	@BeforeClass
	public static void setup() throws IOException {
		featureSource_polygon = AsTestingUtil.getPolygonsFeatureSource();
		StyledFS styledFeatures = new StyledFS(featureSource_polygon);
		tr = new TextRuleList(styledFeatures);
		tr.addDefaultClass();
//		atlasStyler = new AtlasStyler(styledFeatures);
	}

	@AfterClass
	public static void after() {
		featureSource_polygon.getDataStore().dispose();
	}

	@Test
	public void testStylerDialog() {
		AtlasStyler as = new AtlasStyler(featureSource_polygon);

		if (AsTestingUtil.INTERACTIVE) {
			StylerDialog stylerDialog = new StylerDialog(null, as);
			stylerDialog.setVisible(true);
			while (stylerDialog.isVisible())
				;
		}

	}

}
