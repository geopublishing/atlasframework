package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasStyler.RuleListFactory;
import org.geopublishing.atlasStyler.TextRuleList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.geotools.testing.GTTestingUtil;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

public class TextLabelingClassLanguageSelectorDialogTest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTextLabelingClassLanguageSelectorDialog() throws Throwable {

		if (!hasGui())
			return;

		TextRuleList textRulesList = new RuleListFactory(
				GTTestingUtil.TestDatasetsVector.countryShp.getStyledFS())
				.createTextRulesList(true);

		TextLabelingClassLanguageSelectorDialog d = new TextLabelingClassLanguageSelectorDialog(
				null, textRulesList);

		// d.setModal(true);
		d.setVisible(true);
		d.pack();
		TestingUtil.testGui(d, 100);
	}

}
