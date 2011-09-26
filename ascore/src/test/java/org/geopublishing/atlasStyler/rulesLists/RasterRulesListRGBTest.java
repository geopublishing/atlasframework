package org.geopublishing.atlasStyler.rulesLists;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.filters.StringInputStream;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.style.ContrastMethod;
import org.xml.sax.SAXException;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.styling.StyledGridCoverageReader;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.testing.GTTestingUtil;
import de.schmitzm.testing.TestingClass;

public class RasterRulesListRGBTest extends TestingClass {

	private StyledGridCoverageReader styledRaster;

	@Before
	public void setup() throws IOException {
		styledRaster = GTTestingUtil.TestDatasetsRaster.geotiffRGBWithoutSLD
				.getStyled();
	}

	@Test
	@Ignore
	public void testRasterRGBImportExport1() {

		RasterRulesListRGB rl = new RasterRulesListRGB(styledRaster, true);

		rl.setChannelMethod(1, ContrastMethod.HISTOGRAM);
		rl.setChannelMethod(2, ContrastMethod.NORMALIZE);
		rl.setChannelMethod(3, ContrastMethod.NONE);
		rl.setGammaValue(1, 1.2);
		rl.setGammaValue(2, 0.1);
		rl.setGammaValue(3, 2.);
		rl.setRSGamma(1.1);

		assertEquals(1, rl.getChannel(1));
		assertEquals(2, rl.getChannel(2));
		assertEquals(3, rl.getChannel(3));
		assertEquals(1.2, Double.valueOf(rl.getGammaValue(1).toString()));
		assertEquals(0.1, Double.valueOf(rl.getGammaValue(2).toString()));
		assertEquals(2., Double.valueOf(rl.getGammaValue(3).toString()));
		assertEquals(1.1, Double.valueOf(rl.getRSGamma().toString()));

		FeatureTypeStyle fts = rl.getFTS();

		RasterRulesListRGB rl2 = new RasterRulesListRGB(styledRaster, true);
		rl2.importFts(fts);

		assertEquals(1, rl2.getChannel(1));
		assertEquals(ContrastMethod.HISTOGRAM, rl2.getChannelMethod(1));
		assertEquals(ContrastMethod.NORMALIZE, rl2.getChannelMethod(2));
		assertEquals(ContrastMethod.NONE, rl2.getChannelMethod(3));
		assertEquals(2, rl2.getChannel(2));
		assertEquals(3, rl2.getChannel(3));
		assertEquals(1.2, Double.valueOf(rl.getGammaValue(1).toString()));
		assertEquals(0.1, Double.valueOf(rl.getGammaValue(2).toString()));
		assertEquals(2., Double.valueOf(rl.getGammaValue(3).toString()));
		assertEquals(1.1, Double.valueOf(rl.getRSGamma().toString()));
	}

	@Test
	@Ignore
	public void testRasterRGBImportExport2() {

		RasterRulesListRGB rl = new RasterRulesListRGB(styledRaster, true);
		rl.setOpacity(0.6);
		rl.setEnabled(false);
		rl.setChannel(1, 3);
		rl.setChannel(2, 2);
		rl.setChannel(3, 1);
		rl.setMaxScaleDenominator(200);
		rl.setMinScaleDenominator(100);

		FeatureTypeStyle fts = rl.getFTS();

		RasterRulesListRGB rl2 = new RasterRulesListRGB(styledRaster, true);
		rl2.importFts(fts);

		assertEquals(.6, rl2.getOpacity(), 0.00000001);
		assertEquals(false, rl2.isEnabled());

		assertEquals(100, rl2.getMinScaleDenominator(), 0);
		assertEquals(200, rl2.getMaxScaleDenominator(), 0);

		assertEquals(3, rl2.getChannel(1));
		assertEquals(2, rl2.getChannel(2));
		assertEquals(1, rl2.getChannel(3));
	}

	/**
	 * TextSymbolizer2 specific properties saved and laoded again must fit
	 */
	@Test
	@Ignore
	public void testRasterSymbolizer_inAndOut() throws TransformerException,
			SAXException, IOException {
		StyleBuilder sb = new StyleBuilder();

		RasterSymbolizer rs = StylingUtil.STYLE_FACTORY
				.createRasterSymbolizer();

		// Create a Graphic with two recognizable values
		rs.setOpacity(FilterUtil.FILTER_FAC2.literal(0.7));
		ContrastEnhancement ceRs = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		ceRs.setGammaValue(FilterUtil.FILTER_FAC2.literal(0.4));
		// ceRs.setMethod(ContrastMethod.HISTOGRAM);
		ceRs.setType(FilterUtil.FILTER_FAC2.literal("HISTOGRAM"));
		// ceRs.setHistogram();
		rs.setContrastEnhancement(ceRs);

		ContrastEnhancement ceRed = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		// ceRed.setMethod(ContrastMethod.HISTOGRAM);
		// ceRed.setHistogram();
		ceRed.setType(FilterUtil.FILTER_FAC2.literal("HISTOGRAM"));
		ceRed.setGammaValue(FilterUtil.FILTER_FAC2.literal(0.1));
		ContrastEnhancement ceGreen = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		// ceGreen.setMethod(ContrastMethod.NORMALIZE);
		// ceGreen.setNormalize();
		ceGreen.setType(FilterUtil.FILTER_FAC2.literal("NORMALIZE"));
		ceGreen.setGammaValue(FilterUtil.FILTER_FAC2.literal(0.2));
		ContrastEnhancement ceBlue = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		// ceBlue.setMethod(ContrastMethod.NONE);
		ceBlue.setType(FilterUtil.FILTER_FAC2.literal("NONE"));
		ceBlue.setGammaValue(FilterUtil.FILTER_FAC2.literal(0.3));

		SelectedChannelType redT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType("1", ceRed);
		SelectedChannelType greenT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType("2", ceGreen);
		SelectedChannelType blueT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType("3", ceBlue);

		ChannelSelection cs = StylingUtil.STYLE_FACTORY.channelSelection(redT,
				greenT, blueT);
		rs.setChannelSelection(cs);
		// A first check of the XML
		// assertXpathEvaluatesTo("1", "count(/sld:TextSymbolizer/sld:Graphic)",
		// doc);
		// assertXpathEvaluatesTo("1", "count(/sld:TextSymbolizer/sld:Snippet)",
		// doc);
		// assertXpathEvaluatesTo("1",
		// "count(/sld:TextSymbolizer/sld:OtherText)", doc);
		// assertXpathEvaluatesTo("1",
		// "count(/sld:TextSymbolizer/sld:FeatureDescription)", doc);

		// Transform and reimport and compare
		String xml = StylingUtil.sldToString(sb.createStyle(rs));

		Style importedStyle = StylingUtil.loadSLD(new StringInputStream(xml))[0];

		RasterSymbolizer copy = (RasterSymbolizer) importedStyle
				.featureTypeStyles().get(0).rules().get(0).symbolizers().get(0);

		// compare it
		assertEquals(
				"Opacity of RasterSymbolizer has not been correctly ex- and reimported",
				rs.getOpacity(), copy.getOpacity());
		assertEquals("1",
				copy.getChannelSelection().getRGBChannels()[0].getChannelName());
		assertEquals("2",
				copy.getChannelSelection().getRGBChannels()[1].getChannelName());
		assertEquals("3",
				copy.getChannelSelection().getRGBChannels()[2].getChannelName());

		assertEquals(ContrastMethod.HISTOGRAM, copy.getChannelSelection()
				.getRGBChannels()[0].getContrastEnhancement().getMethod());
		assertEquals(ContrastMethod.NORMALIZE, copy.getChannelSelection()
				.getRGBChannels()[1].getContrastEnhancement().getMethod());
		assertEquals(null, copy.getChannelSelection().getRGBChannels()[2]
				.getContrastEnhancement().getMethod());

		assertEquals("0.1", copy.getChannelSelection().getRGBChannels()[0]
				.getContrastEnhancement().getGammaValue().toString());
		assertEquals("0.2", copy.getChannelSelection().getRGBChannels()[1]
				.getContrastEnhancement().getGammaValue().toString());
		assertEquals("0.3", copy.getChannelSelection().getRGBChannels()[2]
				.getContrastEnhancement().getGammaValue().toString());

		assertEquals(ContrastMethod.HISTOGRAM, copy.getContrastEnhancement()
				.getMethod());
		assertEquals("0.4", copy.getContrastEnhancement().getGammaValue()
				.toString());
	}

	@Test
	@Ignore
	public void testRasterSymbolizer_inAndOut2() throws TransformerException,
			SAXException, IOException {
		StyleBuilder sb = new StyleBuilder();

		RasterSymbolizer rs = StylingUtil.STYLE_FACTORY
				.createRasterSymbolizer();

		// Create a Graphic with two recognizable values
		rs.setOpacity(FilterUtil.FILTER_FAC2.literal(0.7));
		ContrastEnhancement ceRs = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		ceRs.setGammaValue(FilterUtil.FILTER_FAC2.literal(0.4));
		// ceRs.setMethod(ContrastMethod.HISTOGRAM);
		ceRs.setHistogram();
		rs.setContrastEnhancement(ceRs);

		ContrastEnhancement ceRed = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		ceRed.setType(FilterUtil.FILTER_FAC2.literal("HISTOGRAM"));
		ceRed.setGammaValue(FilterUtil.FILTER_FAC2.literal(0.1));
		ceRed.getType();
		ContrastEnhancement ceGreen = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		ceGreen.setType(FilterUtil.FILTER_FAC2.literal("Normalize"));
		ceGreen.setGammaValue(FilterUtil.FILTER_FAC2.literal(0.2));
		ContrastEnhancement ceBlue = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		ceBlue.setType(FilterUtil.FILTER_FAC2.literal("None"));
		ceBlue.setGammaValue(FilterUtil.FILTER_FAC2.literal(0.3));

		SelectedChannelType redT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType("1", ceRed);
		SelectedChannelType greenT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType("2", ceGreen);
		SelectedChannelType blueT = StylingUtil.STYLE_FACTORY
				.createSelectedChannelType("3", ceBlue);

		ChannelSelection cs = StylingUtil.STYLE_FACTORY.channelSelection(redT,
				greenT, blueT);
		rs.setChannelSelection(cs);

		rs.getChannelSelection().getRGBChannels()[0].getContrastEnhancement()
				.getMethod();
		// A first check of the XML

		DuplicatingStyleVisitor dsv = StylingUtil.DUPLICATINGSTYLEVISITOR;
		dsv.visit(rs);

		RasterSymbolizer copy = (RasterSymbolizer) dsv.getCopy();

		// compare it
		assertEquals(
				"Opacity of RasterSymbolizer has not been correctly ex- and reimported",
				rs.getOpacity(), copy.getOpacity());
		assertEquals("1",
				copy.getChannelSelection().getRGBChannels()[0].getChannelName());
		assertEquals("2",
				copy.getChannelSelection().getRGBChannels()[1].getChannelName());
		assertEquals("3",
				copy.getChannelSelection().getRGBChannels()[2].getChannelName());
		assertEquals("0.1", copy.getChannelSelection().getRGBChannels()[0]
				.getContrastEnhancement().getGammaValue().toString());
		assertEquals("0.2", copy.getChannelSelection().getRGBChannels()[1]
				.getContrastEnhancement().getGammaValue().toString());
		assertEquals("0.3", copy.getChannelSelection().getRGBChannels()[2]
				.getContrastEnhancement().getGammaValue().toString());
		assertEquals("0.4", copy.getContrastEnhancement().getGammaValue()
				.toString());
		assertEquals(ContrastMethod.HISTOGRAM, rs.getContrastEnhancement()
				.getMethod());

		assertEquals(ContrastMethod.HISTOGRAM, copy.getChannelSelection()
				.getRGBChannels()[0].getContrastEnhancement().getMethod());
		assertEquals(ContrastMethod.NORMALIZE, copy.getChannelSelection()
				.getRGBChannels()[1].getContrastEnhancement().getMethod());
		assertEquals(null, copy.getChannelSelection().getRGBChannels()[2]
				.getContrastEnhancement().getMethod());

	}

	@Test
	@Ignore
	public void testRasterSymbolizer_ceHandling() throws TransformerException {
		StyleBuilder sb = new StyleBuilder();

		RasterSymbolizer rs = StylingUtil.STYLE_FACTORY
				.createRasterSymbolizer();
		ContrastEnhancement ceRs = StylingUtil.STYLE_FACTORY
				.createContrastEnhancement();
		ceRs.setGammaValue(FilterUtil.FILTER_FAC2.literal(0.4));
		rs.setContrastEnhancement(ceRs);
//		DuplicatingStyleVisitor dsv = StylingUtil.DUPLICATINGSTYLEVISITOR;
//		dsv.visit(rs);
//
//		RasterSymbolizer copy = (RasterSymbolizer) dsv.getCopy();

		String xml = StylingUtil.sldToString(sb.createStyle(rs));

		Style importedStyle = StylingUtil.loadSLD(new StringInputStream(xml))[0];

		RasterSymbolizer copy = (RasterSymbolizer) importedStyle
				.featureTypeStyles().get(0).rules().get(0).symbolizers().get(0);
		
		assertEquals("0.4", copy.getContrastEnhancement().getGammaValue()
				.toString());
	}
}
