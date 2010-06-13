package org.geopublishing.atlasStyler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class FreeMapSymbolsTest {

	@Test
	public void testUrlsAreConnectable() throws MalformedURLException,
			IOException {
		new URL(FreeMapSymbols.LINE_URL).openStream().close();
		new URL(FreeMapSymbols.POINT_URL).openStream().close();
		new URL(FreeMapSymbols.POLYGON_URL).openStream().close();
	}

}
