package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertEquals;

import javax.swing.JComboBox;

import org.apache.commons.httpclient.util.LangUtils;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RuleListFactory;
import org.geopublishing.atlasStyler.rulesLists.SinglePolygonSymbolRuleList;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.junit.Test;
import org.opengis.style.Fill;

import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.testing.TestingUtil;

public class SymbolSelectorGUITest {

	@Test
	public void testSymbolSelectorGUI() throws Throwable {
		if (!TestingUtil.isInteractive())
			return;

		AtlasStylerVector asv = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.polygonSnow);
		SinglePolygonSymbolRuleList singleSymbolRuleList = RuleListFactory
				.createSinglePolygonSymbolRulesList(new Translation(
						"test with defaults"), true);
		asv.addRulesList(singleSymbolRuleList);
		PolygonSymbolizer ps = (PolygonSymbolizer) singleSymbolRuleList.getSymbolizers().get(0);
		ps.setFill(StylingUtil.STYLE_BUILDER.createFill());
		ps.getFill().setGraphicFill(StylingUtil.STYLE_BUILDER.createGraphic());
		ps.getFill().getGraphicFill().setSize(StylingUtil.ff.literal(5.0));
		assertEquals(5., singleSymbolRuleList.getSizeBiggest(), 0.0);
		SymbolSelectorGUI ssg = new SymbolSelectorGUI(null, asv, "",
				singleSymbolRuleList);
//		System.out.println(StylingUtil.sldToString(asv.getStyle()));
		JComboBox jComboBoxSize = ssg.getJComboBoxSize();
		jComboBoxSize.setSelectedIndex(10);
		System.out.println(StylingUtil.sldToString(asv.getStyle()));
		assertEquals(10., singleSymbolRuleList.getSizeBiggest(), 0.0);
//		AsTestingUtil.testGui(ssg, 1000);
	}
}
