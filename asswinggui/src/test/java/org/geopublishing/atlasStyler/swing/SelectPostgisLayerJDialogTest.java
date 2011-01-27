package org.geopublishing.atlasStyler.swing;

import org.junit.Test;

import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;
public class SelectPostgisLayerJDialogTest extends TestingClass {

	@Test
	public void testGui() throws Throwable {
		if (isInteractive())
			return;

		SelectPostgisLayerJDialog selectPostgisLayerJDialog = new SelectPostgisLayerJDialog(
				null);
		TestingUtil.testGui(selectPostgisLayerJDialog);
		System.out.println(selectPostgisLayerJDialog.getPassword());
	}

}
