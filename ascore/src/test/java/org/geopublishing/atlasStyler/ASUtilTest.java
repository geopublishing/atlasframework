package org.geopublishing.atlasStyler;

import java.awt.Color;

import org.junit.Test;

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;

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

}
