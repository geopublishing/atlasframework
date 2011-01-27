package org.geopublishing.atlasStyler.swing;

import org.geotools.styling.LineSymbolizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

public class LineSymbolEditGUITest extends TestingClass {

	LineSymbolizer ps;

	@Before
	public void setUp() throws Exception {
		ps = StylingUtil.STYLE_BUILDER.createLineSymbolizer();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLineSymbolEditGUI() throws Throwable {
		if (!TestingUtil.isInteractive())
			return;

		// ps.setStroke(null);
		LineSymbolEditGUI lineSymbolEditGUI = new LineSymbolEditGUI(ps);
		// assertFalse(lineSymbolEditGUI.getJCheckBoxFillGraphic().isSelected());
		// lineSymbolEditGUI.getJCheckBoxFillGraphic().doClick();
		// assertTrue(lineSymbolEditGUI.getJCheckBoxFillGraphic().isSelected());

		TestingUtil.testGui(lineSymbolEditGUI, 20);

		// // Mark createMark = StylingUtil.STYLE_BUILDER.createMark("circle");
		// // Graphic circleGraphic =
		// StylingUtil.STYLE_BUILDER.createGraphic(null,
		// // createMark, null););
		// String s1 = StylingUtil.toXMLString(ps);
		//
		// // Open a new GUI
		// lineSymbolEditGUI = new LineSymbolEditGUI(ps);
		// assertTrue(lineSymbolEditGUI.getJCheckBoxFillGraphic().isSelected());
		// TestingUtil.testGui(lineSymbolEditGUI, 10);
		// String s2 = StylingUtil.toXMLString(ps);
		//
		// assertEquals(s1, s2);

	}

}
