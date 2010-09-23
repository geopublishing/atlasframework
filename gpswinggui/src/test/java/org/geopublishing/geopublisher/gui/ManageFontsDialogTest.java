package org.geopublishing.geopublisher.gui;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPTestingUtil;
import org.geopublishing.geopublisher.GPTestingUtil.Atlas;
import org.junit.Test;

import schmitzm.swing.TestingUtil;


public class ManageFontsDialogTest {
	
	@Test
	public void testJustOpenGUI() throws Throwable {
		if (!GPTestingUtil.INTERACTIVE) return;
		
		AtlasConfigEditable ace = GPTestingUtil.getAtlasConfigE(Atlas.small);
		ManageFontsDialog manageFontsDialog = new ManageFontsDialog(null, ace);
		TestingUtil.testGui(manageFontsDialog);
	}

}
