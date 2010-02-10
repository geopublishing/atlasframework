/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.datapool;

import java.awt.Color;
import java.nio.charset.Charset;

import junit.framework.TestCase;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.layer.DpLayerRasterPyramid;
import skrueger.atlas.dp.layer.DpLayerVector;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.TestingUtil;

public class EditDpEntryGUITest extends TestCase {

	DpLayerRasterPyramid pyr;
	private DpLayerVector dplv;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		AtlasConfigEditable atlasConfigE = TestingUtil.getAtlasConfigE();
		for (DpEntry dpe : atlasConfigE.getDataPool().values()) {
			if (dpe instanceof DpLayerRasterPyramid) {
				pyr = (DpLayerRasterPyramid) dpe;
			}
			if (dpe instanceof DpLayerVector) {
				dplv = (DpLayerVector) dpe;
			}
		}

	}

	public void testPyramidCancel() {

		EditDpEntryGUI editDpEntryGUI = new EditDpEntryGUI(null, pyr);
		editDpEntryGUI.setVisible(true);
		Color backupColor = pyr.getInputTransparentColor();
		pyr.setInputTransparentColor(Color.red);
		editDpEntryGUI.cancelClose();
		assertEquals(
				"Cancelling the EditDpEntryGUI didn't reset the transparent color of the pyr",
				backupColor, pyr.getInputTransparentColor());
	}

	public void testVectorCancel() {
		EditDpEntryGUI editDpEntryGUI = new EditDpEntryGUI(null, dplv);
		editDpEntryGUI.setVisible(true);
		Charset backupCharset = dplv.getCharset();
		dplv.setCharset(Charset.forName("UTF8"));
		editDpEntryGUI.cancelClose();
		assertEquals(
				"Cancelling the EditDpEntryGUI didn't reset the charset of the dplv",
				backupCharset, dplv.getCharset());
	}

	public void testDpeCancel() {
		EditDpEntryGUI editDpEntryGUI = new EditDpEntryGUI(null, dplv);
		editDpEntryGUI.setVisible(true);
		Boolean backupExportable = dplv.isExportable();
		dplv.setExportable(!backupExportable);
		editDpEntryGUI.cancelClose();
		assertEquals(
				"Cancelling the EditDpEntryGUI didn't reset the exportable-flag of dpe",
				backupExportable, dplv.isExportable());
	}

	public void testDpeOk() {
		if (!TestingUtil.INTERACTIVE)
			return;

		dplv.getTitle()
				.put(dplv.getAc().getLanguages().get(0), "sdasdasd{SDSD");
		EditDpEntryGUI editDpEntryGUI = new EditDpEntryGUI(null, dplv);
		editDpEntryGUI.setVisible(true);
		assertFalse(
				"The { char in the title's translation has not been vetoed!",
				editDpEntryGUI.okClose());
		assertTrue(
				"The { char in the title's translation has not been vetoed!",
				editDpEntryGUI.isVisible());
		editDpEntryGUI.dispose();
	}

	public void testSeeme() throws InterruptedException {
		if (!TestingUtil.INTERACTIVE)
			return;

		EditDpEntryGUI editDpEntryGUI = new EditDpEntryGUI(null, dplv);
		editDpEntryGUI.setVisible(true);

		while (editDpEntryGUI.isVisible()) {
			Thread.sleep(1000);
		}
	}

}
