package org.geopublishing.atlasViewer.swing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AtlasViewerGUITest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCheckTestModeArgument() {
		String[] args = new String[] { "-t", "/Desktop" };
		args = AtlasViewerGUI.checkTestModeArgument(args);
		assertTrue(AtlasViewerGUI.isTestMode());
		assertTrue(ArrayUtils.contains(args, "/Desktop"));
		assertFalse(ArrayUtils.contains(args, "-t"));
	}

}
