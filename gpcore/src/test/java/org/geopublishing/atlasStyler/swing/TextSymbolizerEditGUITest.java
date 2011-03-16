package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertEquals;

import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.rulesLists.TextRuleList;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.geotools.styling.StyledFS;
import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

public class TextSymbolizerEditGUITest extends TestingClass {
	private static StyledFS STYLED_FS;
	private static TextRuleList tr;
	private static AtlasStylerVector atlasStyler;

	@Before
	public void before() throws IOException {
		STYLED_FS = TestDatasetsVector.countryShp.getStyledFS();
		tr = new TextRuleList(STYLED_FS, true);
		// tr.addDefaultClass();
		atlasStyler = new AtlasStylerVector(STYLED_FS);

	}

	@Test
	public void testGetFontComboBox() throws IOException {
		TextSymbolizerEditGUI textSymbolizerEditGUI = new TextSymbolizerEditGUI(
				tr, atlasStyler, STYLED_FS.getFeatureCollection());
		JComboBox jComboBoxFont = textSymbolizerEditGUI.getJComboBoxFont();
		assertEquals("default number of fonts is 5", 5,
				jComboBoxFont.getItemCount());

	}

	@Test
	public void testGetFontComboBox2() throws IOException {
		List<Font> fonts = new ArrayList<Font>();
		// Font f = new JLabel().getFont().deriveFont(3);
		atlasStyler.setFonts(fonts);

		TextSymbolizerEditGUI textSymbolizerEditGUI = new TextSymbolizerEditGUI(
				tr, atlasStyler,
				TestDatasetsVector.countryShp.getFeatureCollection());
		JComboBox jComboBoxFont = textSymbolizerEditGUI.getJComboBoxFont();
		assertEquals("default number of fonts is 5", 5,
				jComboBoxFont.getItemCount());
	}

	@Test
	public void testTextSymbolizerEditGUIPolygon() throws Throwable {
		if (isInteractive())
			return;

		TextRuleList tRl = atlasStyler.getRlf().createTextRulesList(true);

		TextSymbolizerEditGUI tsGui = new TextSymbolizerEditGUI(tRl,
				atlasStyler, null);

		TestingUtil.testGui(tsGui);
	}

	@Test
	public void testTextSymbolizerEditGUILine() throws Throwable {
		if (!TestingUtil.isInteractive())
			return;

		STYLED_FS = TestDatasetsVector.lineBrokenQuix.getStyledFS();
		atlasStyler = new AtlasStylerVector(STYLED_FS);

		TextRuleList tRl = atlasStyler.getRlf().createTextRulesList(true);

		TextSymbolizerEditGUI tsGui = new TextSymbolizerEditGUI(tRl,
				atlasStyler, null);

		TestingUtil.testGui(tsGui, 1);
	}

}
