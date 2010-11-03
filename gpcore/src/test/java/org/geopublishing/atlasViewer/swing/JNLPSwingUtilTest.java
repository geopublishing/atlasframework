package org.geopublishing.atlasViewer.swing;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JNLPSwingUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoadPart() throws IOException {
		JNLPSwingUtil.loadPartAndCreateDialogForIt("a");
	}

}
