package org.geopublishing.atlasViewer.swing;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.TextRuleList;
import org.geopublishing.atlasStyler.swing.StylerDialog;
import org.junit.Test;

import schmitzm.junit.TestingClass;
import schmitzm.swing.TestingUtil;

public class StylerDialogTest extends TestingClass {
	private static TextRuleList tr;

	@Test
	public void testStylerDialog() throws Throwable {

		AtlasStyler as = new AtlasStyler(
				TestingUtil.TestDatasetsVector.countryShp.getFeatureSource());

		if (!TestingUtil.isInteractive())
			return;

		StylerDialog stylerDialog = new StylerDialog(null, as, null);
		TestingUtil.testGui(stylerDialog);
	}

}
