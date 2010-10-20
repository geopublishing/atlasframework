package org.geopublishing.geopublisher.gui;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import schmitzm.swing.TestingUtil;

public class AtlasLanguagesConfigDialogTest {

	private ArrayList<String> orig;

	@Before
	public void setUp() throws Exception {
		orig = new ArrayList<String>();
		orig.add("en");
		orig.add("fr");
		orig.add("kj");
	}

	@Test
	public void testLanguageSelectionDialog() throws Throwable {
		if (TestingUtil.INTERACTIVE) {
			AtlasLanguagesConfigDialog languageSelectionDialog = new AtlasLanguagesConfigDialog(
					null, orig);
			TestingUtil.testGui(languageSelectionDialog, 40);
		}
	}

}
