package org.geopublishing.atlasViewer.swing;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.rulesLists.TextRuleList;
import org.geopublishing.atlasStyler.swing.StylerDialog;
import org.junit.Test;

import de.schmitzm.geotools.testing.GTTestingUtil;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

public class StylerDialogTest extends TestingClass {
	private static TextRuleList tr;

	@Test
	public void testStylerDialog() throws Throwable {

		AtlasStylerVector as = new AtlasStylerVector(
				GTTestingUtil.TestDatasetsVector.countryShp.getFeatureSource());

		if (!TestingUtil.isInteractive())
			return;

		StylerDialog stylerDialog = new StylerDialog(null, as, null);
		TestingUtil.testGui(stylerDialog);
	}

}
