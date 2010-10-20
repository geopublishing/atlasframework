package org.geopublishing.atlasViewer.swing;

import org.junit.Test;

public class IconsTest {

	@Test
	/**
	 * Constructing the Icons class will fail if any icon is missing
	 */
	public void testConstruction() {
		new Icons();
	}
}
