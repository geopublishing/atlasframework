package org.geopublishing.atlasStyler.swing;

import java.awt.GraphicsEnvironment;

import org.junit.Test;


public class SelectPostgisLayerJDialogTest {

	public static final boolean INTERACTIVE = !GraphicsEnvironment.isHeadless();

	@Test
	public void testGui () {
		if (INTERACTIVE) {
			SelectPostgisLayerJDialog selectPostgisLayerJDialog = new SelectPostgisLayerJDialog(null);
			selectPostgisLayerJDialog.setVisible(true);
		}
	}

}
