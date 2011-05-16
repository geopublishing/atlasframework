package org.geopublishing.geopublisher.export;


import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GpHosterServerSettingsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParsePropertiesString() {
		GpHosterServerSettings gpHosterServerSettings = new GpHosterServerSettings();
		String s = "a|b|c|d|e|f";
		String[] split = s.split(Pattern.quote("|"));
		assertEquals(6, split.length);

		s = "a|b|c|d||f";
		split = s.split(Pattern.quote("|"));
		assertEquals(6, split.length);

		s = "a|b|c|d|e| ";
		split = s.split(Pattern.quote("|"));
		assertEquals(6, split.length);

		s = "a|b|c|d|| ";
		split = s.split(Pattern.quote("|"));
		assertEquals(6, split.length);

		s = "a|b|c|||";
		split = s.split(Pattern.quote("|"));
		assertEquals(6, split.length);
	}

	@Test
	public void testParsePropertiesString2() {
		String s = "notitle|asd|localhost|notitle|asda|asd|";
		String[] split = s.split(Pattern.quote("|"));
		assertEquals(6, split.length);

		s = "no title|http://localhost:8080/gp-hoster-jsf/|localhost|no title|asda|| ";
		split = s.split(Pattern.quote("|"));
		assertEquals(6, split.length);

	}

}
