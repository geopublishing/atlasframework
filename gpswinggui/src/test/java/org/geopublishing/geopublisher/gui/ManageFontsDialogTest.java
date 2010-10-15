package org.geopublishing.geopublisher.gui;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.Test;

import schmitzm.swing.TestingUtil;

public class ManageFontsDialogTest {

	@Test
	public void testJustOpenGUI() throws Throwable {
		if (TestingUtil.INTERACTIVE) {
			AtlasConfigEditable ace = GpTestingUtil
					.getAtlasConfigE(TestAtlas.small);
			ManageFontsDialog manageFontsDialog = new ManageFontsDialog(null,
					ace);
			TestingUtil.testGui(manageFontsDialog);
		}
	}

}
