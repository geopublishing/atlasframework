package org.geopublishing.geopublisher.gui;

import static org.junit.Assert.assertEquals;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.Test;

import de.schmitzm.testing.TestingUtil;

public class EditAtlasParamsDialogInteractiveTest {

	@Test
	public void testEditAtlasParamsDialog() throws Throwable {
		final AtlasConfigEditable ace = TestAtlas.small.getAce();
		String bn = ace.getBaseName();
		EditAtlasParamsDialog editAtlasParamsDialog = new EditAtlasParamsDialog(
				null, ace);

		TestingUtil.testGui(editAtlasParamsDialog, 120);

		assertEquals(bn, ace.getBaseName());
	}

}
