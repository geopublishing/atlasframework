package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.regex.Matcher;

import org.geopublishing.atlasStyler.FreeMapSymbols;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;

public class SVGSelectorTest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSldAPachePattern() throws Throwable {

		String testLine = "<img src=\"/icons/image2.gif\" alt=\"[IMG]\"> <a href=\"shopping.svg\">shopping.svg</a>            11-Dec-2010 21:10  Royalty-free SVG vector map graphics that can be used an external graphics within SLD";

		Matcher matcher = SVGSelector.svgFilePattern.matcher(testLine);

		assertTrue(matcher.find());
	}

	@Test
	public void testSvgSelectorGui() throws Throwable {

		if (!TestingUtil.isInteractive())
			return;
		SVGSelector svgSelector = new SVGSelector(null, GeometryForm.POLYGON,
				null);
		svgSelector.changeURL(new URL(FreeMapSymbols.SVG_URL + "/" + "osm"));
		TestingUtil.testGui(svgSelector, 10);
	}
}
