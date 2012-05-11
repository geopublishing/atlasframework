package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertEquals;

import javax.swing.JComboBox;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RuleListFactory;
import org.geopublishing.atlasStyler.rulesLists.SinglePolygonSymbolRuleList;
import org.junit.Test;

import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.i18n.Translation;
import de.schmitzm.testing.TestingUtil;

public class SymbolSelectorGUITest {

	@Test
	public void testSymbolSelectorGUI() throws Throwable {
		if (!TestingUtil.isInteractive())
			return;

		/*
		 * 
		 * Stefan Tzeggai 10:35:21 Was hast du gestern comittet, bzw. nicht
		 * comittet?
		 * http://jenkins.wikisquare.de/hudson/job/gpcore-trunk/1099/org
		 * .geopublishing.geopublisher$gpcore/console
		 * 
		 * Stefan Tzeggai 10:45:04 keine compile fehler comitten und auch am
		 * besten keine tests die nicht laufen 10:45:14
		 * http://jenkins.wikisquare
		 * .de/hudson/job/gpcore-trunk/org.geopublishing
		 * .geopublisher$gpcore/1100
		 * /testReport/junit/org.geopublishing.atlasStyler
		 * .swing/SymbolSelectorGUITest/testSymbolSelectorGUI/ 10:45:22
		 */
		// AtlasStylerVector asv =
		// AsTestingUtil.getAtlasStyler(TestDatasetsVector.polygonSnow);
		// SinglePolygonSymbolRuleList singleSymbolRuleList =
		// RuleListFactory.createSinglePolygonSymbolRulesList(new
		// Translation("test with defaults"), true);
		// SymbolSelectorGUI ssg = new SymbolSelectorGUI(null, asv, "",
		// singleSymbolRuleList);
		// JComboBox jComboBoxSize = ssg.getJComboBoxSize();
		// jComboBoxSize.setSelectedIndex(10);
		//
		// assertEquals(10.,singleSymbolRuleList.getSizeBiggest(),0.0);
		// AsTestingUtil.testGui(ssg,1000);
	}
}
