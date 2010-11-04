package org.geopublishing.atlasViewer.swing.internal;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.junit.Test;

import schmitzm.io.IOUtil;
import skrueger.swing.formatter.MbDecimalFormatter;

public class AtlasStatusDialogTest {

	@Test
	/**
	2010-11-04 01:06:17,953 DEBUG internal.AtlasStatusDialog - progress http://atlas.geopublishing.org/atlases_testing/iida/raster_landsat00813704070.jar null 31469472 61469472 80
	 */
	public void formatProcessMessageJNLP() throws MalformedURLException {
		URL url = new URL(
				"http://atlas.geopublishing.org/atlases_testing/iida/raster_landsat00813704070.jar");

		int percentage = 40;
		long full = 61469472;
		Locale.setDefault(Locale.GERMAN);
		String formated = new MbDecimalFormatter().format(full);

		assertEquals("58,6Mb", formated);

		String fielname = IOUtil.getFilename(url);
		assertEquals("raster_landsat00813704070.jar", fielname);

		String shorter = AtlasStatusDialog.getShortFilename(url);
		assertEquals("raster_landsat", shorter);

	}

}
