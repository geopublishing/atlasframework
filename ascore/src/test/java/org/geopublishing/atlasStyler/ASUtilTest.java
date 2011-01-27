package org.geopublishing.atlasStyler;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.geotools.data.FeatureSource;
import org.geotools.styling.PolygonSymbolizer;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.testing.GTTestingUtil;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;
public class ASUtilTest extends TestingClass {

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
		FeatureSource<SimpleFeatureType, SimpleFeature> pfs = GTTestingUtil.TestDatasetsVector.kreise.getFeatureSource();
		BufferedImage symbolizerImage = ASUtil.getSymbolizerImage(ps, pfs.getSchema());
		TestingUtil.testGui(new JLabel(new ImageIcon(symbolizerImage)));
	}

}
