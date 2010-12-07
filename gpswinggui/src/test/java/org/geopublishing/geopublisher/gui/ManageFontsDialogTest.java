package org.geopublishing.geopublisher.gui;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.Test;

import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;
public class ManageFontsDialogTest extends TestingClass {

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
