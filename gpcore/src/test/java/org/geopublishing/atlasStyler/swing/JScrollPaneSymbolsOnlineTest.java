package org.geopublishing.atlasStyler.swing;

import org.junit.Test;

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.swing.TestingUtil;

public class JScrollPaneSymbolsOnlineTest {

	@Test
	public void testJScrollPaneSymbols() throws Throwable {

		TestingUtil.testPanel(new JScrollPaneSymbolsOnline(GeometryForm.POINT));

		TestingUtil.testPanel(new JScrollPaneSymbolsOnline(GeometryForm.LINE));

		TestingUtil
				.testPanel(new JScrollPaneSymbolsOnline(GeometryForm.POLYGON));

	}

}
