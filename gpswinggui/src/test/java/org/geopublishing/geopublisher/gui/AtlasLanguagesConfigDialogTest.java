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
	}

	@Test
	public void testLanguageSelectionDialog() throws Throwable {
		AtlasLanguagesConfigDialog languageSelectionDialog = new AtlasLanguagesConfigDialog(
				null, orig);
		TestingUtil.testGui(languageSelectionDialog);
	}

}
