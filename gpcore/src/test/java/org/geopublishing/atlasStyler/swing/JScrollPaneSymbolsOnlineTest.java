package org.geopublishing.atlasStyler.swing;

import org.junit.Test;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;
public class JScrollPaneSymbolsOnlineTest extends TestingClass {

	@Test
	public void testJScrollPaneSymbols() throws Throwable {

		if (hasGui()) {
			TestingUtil.testGui(
					new JScrollPaneSymbolsOnline(GeometryForm.POINT));

			TestingUtil.testGui(
					new JScrollPaneSymbolsOnline(GeometryForm.LINE));

			TestingUtil.testGui(new JScrollPaneSymbolsOnline(
					GeometryForm.POLYGON));
		}

	}

}
