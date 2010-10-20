package org.geopublishing.atlasStyler.swing;

import org.junit.Test;

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.swing.TestingUtil;

public class JScrollPaneSymbolsOnlineTest {

	@Test
	public void testJScrollPaneSymbols() throws Throwable {

		TestingUtil.testGui(new JScrollPaneSymbolsOnline(GeometryForm.POINT));

		TestingUtil.testGui(new JScrollPaneSymbolsOnline(GeometryForm.LINE));

		TestingUtil
				.testGui(new JScrollPaneSymbolsOnline(GeometryForm.POLYGON));

	}

}
