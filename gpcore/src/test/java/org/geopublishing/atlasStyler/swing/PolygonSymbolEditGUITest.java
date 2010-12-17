package org.geopublishing.atlasStyler.swing;

import org.geotools.styling.PolygonSymbolizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;

public class PolygonSymbolEditGUITest extends TestingClass {

	PolygonSymbolizer ps;

	@Before
	public void setUp() throws Exception {
		ps = StylingUtil.STYLE_BUILDER.createPolygonSymbolizer();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPolygonSymbolEditGUI() throws Throwable {
		if (!TestingUtil.isInteractive())
			return;

		ps.setFill(null);
		ps.setStroke(null);
		PolygonSymbolEditGUI polygonSymbolEditGUI = new PolygonSymbolEditGUI(ps);
		// assertFalse(polygonSymbolEditGUI.getJCheckBoxFillGraphic().isSelected());
		// polygonSymbolEditGUI.getJCheckBoxFillGraphic().doClick();
		// assertTrue(polygonSymbolEditGUI.getJCheckBoxFillGraphic().isSelected());

		TestingUtil.testGui(polygonSymbolEditGUI, 20);

		// // Mark createMark = StylingUtil.STYLE_BUILDER.createMark("circle");
		// // Graphic circleGraphic =
		// StylingUtil.STYLE_BUILDER.createGraphic(null,
		// // createMark, null););
		// String s1 = StylingUtil.toXMLString(ps);
		//
		// // Open a new GUI
		// polygonSymbolEditGUI = new PolygonSymbolEditGUI(ps);
		// assertTrue(polygonSymbolEditGUI.getJCheckBoxFillGraphic().isSelected());
		// TestingUtil.testGui(polygonSymbolEditGUI, 10);
		// String s2 = StylingUtil.toXMLString(ps);
		//
		// assertEquals(s1, s2);

	}

}
