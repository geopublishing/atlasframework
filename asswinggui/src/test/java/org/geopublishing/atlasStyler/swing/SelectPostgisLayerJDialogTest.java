package org.geopublishing.atlasStyler.swing;

import org.junit.Test;

import schmitzm.swing.TestingUtil;

public class SelectPostgisLayerJDialogTest {

	@Test
	public void testGui() throws Throwable {
		SelectPostgisLayerJDialog selectPostgisLayerJDialog = new SelectPostgisLayerJDialog(
				null);
		TestingUtil.testGui(selectPostgisLayerJDialog);
	}

}
