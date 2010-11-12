package org.geopublishing.atlasStyler.swing;

import static org.junit.Assert.assertEquals;

import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.TextRuleList;
import org.junit.Before;
import org.junit.Test;

import schmitzm.swing.TestingUtil.TestDatasetsVector;
import skrueger.geotools.StyledFS;

public class TextSymbolizerEditGUITest {
	private static StyledFS STYLED_FS;
	private static TextRuleList tr;
	private static AtlasStyler atlasStyler;

	@Before
	public void befire() throws IOException {
		STYLED_FS = TestDatasetsVector.countryShp.getStyledFS();
		tr = new TextRuleList(STYLED_FS, true);
//		tr.addDefaultClass();
		atlasStyler = new AtlasStyler(STYLED_FS);

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
		Font f = new JLabel().getFont().deriveFont(3);
		atlasStyler.setFonts(fonts);

		TextSymbolizerEditGUI textSymbolizerEditGUI = new TextSymbolizerEditGUI(
				tr, atlasStyler, TestDatasetsVector.countryShp.getFeatureCollection());
		JComboBox jComboBoxFont = textSymbolizerEditGUI.getJComboBoxFont();
		assertEquals("default number of fonts is 5", 5,
				jComboBoxFont.getItemCount());
	}

}
