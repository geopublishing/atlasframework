package org.geopublishing.atlasViewer.swing;

import org.junit.Test;

import de.schmitzm.testing.TestingClass;
public class IconsTest extends TestingClass {

	@Test
	/**
	 * Constructing the Icons class will fail if any icon is missing
	 */
	public void testConstruction() {
		new Icons();
	}
}
