package org.geopublishing.atlasStyler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import schmitzm.junit.TestingClass;
public class OpenMapSymbolsTest extends TestingClass {

	@Test
	public void testUrlsAreConnectable() throws MalformedURLException,
			IOException {
		new URL(OpenMapSymbols.LINE_URL).openStream().close();
		new URL(OpenMapSymbols.POINT_URL).openStream().close();
		new URL(OpenMapSymbols.POLYGON_URL).openStream().close();
	}

}
