package org.geopublishing.atlasViewer.swing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;
public class AVSwingUtilTest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateLocalCopyFromURLRecoversIfTheFileIsDeleted()
			throws IOException {

		if (!TestingUtil.INTERACTIVE)
			return;
		URL url = new URL("http://www.wikisquare.de/favicon.ico");
		assertNotNull(url);
		File file = AVSwingUtil
				.createLocalCopyFromURL(null, url, "test", "tif");
		assertTrue(file.exists());
		file.delete();
		assertFalse(file.exists());
		file = AVSwingUtil.createLocalCopyFromURL(null, url, "test", "tif");
		assertTrue(file.exists());
	}

}
