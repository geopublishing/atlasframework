package org.geopublishing.geopublisher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.xml.transform.TransformerException;

import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesListRGB;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Style;
import org.junit.BeforeClass;
import org.junit.Test;

import de.schmitzm.geotools.styling.StyledGridCoverageReader;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.testing.GTTestingUtil;
import de.schmitzm.lang.LangUtil;

public class CopyPasteClipboardTest {

	private static AtlasConfigEditable ace;
	private static AtlasConfigEditable acr;

	@BeforeClass
	public static void setup() throws Exception {
		ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);
		acr = GpTestingUtil.getAtlasConfigE(TestAtlas.rasters);
		Style testStyle = StylingUtil.STYLE_FACTORY.createStyle();
		testStyle.featureTypeStyles().add(
				StylingUtil.STYLE_FACTORY.createFeatureTypeStyle());
	}

	@Test
	public void testCopyPaste() throws TransformerException {
		Set<DpLayer> layers = ace.getDataPool().getLayers();
		DpLayer dpl = layers.iterator().next();
		FeatureTypeStyle[] fts = dpl.getStyle().featureTypeStyles()
				.toArray(new FeatureTypeStyle[0]);
		String xmlString = StylingUtil.toXMLString(fts[0]);
		LangUtil.copyToClipboard(xmlString);
		String pastedXmlString = LangUtil.pasteFromClipboard();
		assertTrue(xmlString.equals(pastedXmlString));
	}

	@Test
	public void testCreateStyleFromPaste() throws TransformerException {
		Set<DpLayer> layers = ace.getDataPool().getLayers();
		DpLayer dpl = layers.iterator().next();

		Style oldStyle = dpl.getStyle();
		String xmlString = StylingUtil.toXMLString(oldStyle);
		LangUtil.copyToClipboard(xmlString);
		String pastedXmlString = LangUtil.pasteFromClipboard();
		Style[] loadedStyle = StylingUtil.loadSLD(pastedXmlString);
		assertFalse(StylingUtil.isStyleDifferent(oldStyle, loadedStyle[0]));
	}

	@Test
	public void testCreateRasterLayerFromPaste() throws Exception {
		StyledGridCoverageReader styledRaster = GTTestingUtil.TestDatasetsRaster.geotiffWithSld
				.getStyled();
		RasterRulesListRGB rl = new RasterRulesListRGB(styledRaster, true);
		AtlasStylerRaster asv = new AtlasStylerRaster(styledRaster);
		asv.addRulesList(rl);
		asv.importStyle(asv.getStyle()); // selfimport damit die Defaultdaten im
											// TestDatasetsRaster überschrieben
											// werden
		LangUtil.copyToClipboard(StylingUtil.toXMLString(asv.getStyle()));

		String pastedXml = LangUtil.pasteFromClipboard();

		Style[] pastedSLD = StylingUtil.loadSLD(pastedXml);
		StyledGridCoverageReader styledRaster2 = GTTestingUtil.TestDatasetsRaster.arcAscii
				.getStyled();
		AtlasStylerRaster asr = new AtlasStylerRaster(styledRaster2);
		asr.importStyle(pastedSLD[0]);
		assertFalse(StylingUtil
				.isStyleDifferent(asr.getStyle(), asv.getStyle()));
	}
}
