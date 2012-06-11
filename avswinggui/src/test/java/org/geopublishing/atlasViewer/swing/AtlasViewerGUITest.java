package org.geopublishing.atlasViewer.swing;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

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
		assertTrue(Arrays.asList(args).contains("/Desktop"));
		assertTrue(Arrays.asList(args).contains("-t"));
	}

}
