package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geotools.data.FeatureSource;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.TestingUtil;

public class ASUtilTest {

	@Test
	public void testGetDefaultNoDataSymbol() {
		ASUtil.getDefaultNoDataSymbol(GeometryForm.LINE);
		ASUtil.getDefaultNoDataSymbol(GeometryForm.LINE, Color.green);
		ASUtil.getDefaultNoDataSymbol(GeometryForm.LINE, Color.green, Color.black);
		
		ASUtil.getDefaultNoDataSymbol(GeometryForm.POINT);
		ASUtil.getDefaultNoDataSymbol(GeometryForm.POINT, Color.green);
		ASUtil.getDefaultNoDataSymbol(GeometryForm.POINT, Color.green, Color.black);
		
		ASUtil.getDefaultNoDataSymbol(GeometryForm.POLYGON);
		ASUtil.getDefaultNoDataSymbol(GeometryForm.POLYGON, Color.green);
		ASUtil.getDefaultNoDataSymbol(GeometryForm.POLYGON, Color.green, Color.black);
	}

	@Test
	public void testGetSymbolizerImage() throws Throwable
	{
		PolygonSymbolizer ps = StylingUtil.STYLE_BUILDER.createPolygonSymbolizer();
		FeatureSource<SimpleFeatureType, SimpleFeature> pfs = TestingUtil.TestDatasetsVector.kreise.getFeatureSource();
		BufferedImage symbolizerImage = ASUtil.getSymbolizerImage(ps, pfs.getSchema());
		TestingUtil.testGui(new JLabel(new ImageIcon(symbolizerImage)), 10);
	}

}
