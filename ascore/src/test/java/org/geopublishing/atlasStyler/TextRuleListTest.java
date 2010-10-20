package org.geopublishing.atlasStyler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.transform.TransformerException;

import org.geopublishing.atlasStyler.swing.AsTestingUtil;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.function.EnvFunction;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.TextSymbolizer;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.TestingUtil;
import schmitzm.swing.TestingUtil.TestDatasets;
import skrueger.geotools.StyledFS;

public class TextRuleListTest {

	@Test
	public void testParseAndRemoveEnabledDisabledFilters() throws IOException,
			CQLException {

		TextRuleList trl = new TextRuleList(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));

		System.out.println("\n now testing with only the default filter");
		trl.addDefaultClass();

		for (boolean allEnable : new boolean[] { false, true }) {
			for (boolean classEnable : new boolean[] { false, true }) {

				trl.setEnabled(allEnable);
				{
					Filter filter = trl.getClassFilter(0);
					trl.setClassEnabled(0, classEnable);

					Filter filterAdded = trl.addEnabledDisabledFilters(
							trl.getClassFilter(0), 0);
					Filter filterRemoved = trl
							.parseAndRemoveEnabledDisabledFilters(filterAdded,
									0);

					assertEquals(filter, filterRemoved);
					assertFalse(filterAdded.equals(filterRemoved));
				}
			}
		}

		for (Rule r : trl.getRules()) {
			System.out.println(r.getFilter());
		}

		System.out.println("\n now testing with one language specific filter");

		Filter filter = ECQL.toFilter("56<65");
		trl.addClass(StylingUtil.STYLE_BUILDER.createTextSymbolizer(),
				"testRuleName_DE", filter, true, "de", null, null);

		for (Rule r : trl.getRules()) {
			System.out.println(r.getFilter());
		}

		System.out.println("\n now testing with two language specific filters");

		trl.addClass(StylingUtil.STYLE_BUILDER.createTextSymbolizer(),
				"testRuleName_DE", filter, true, "ru", null, null);

		for (Rule r : trl.getRules()) {
			System.out.println(r.getFilter());
		}
	}

	@Test
	public void testLanguageDefaultFilters() throws IOException, CQLException {

		TextRuleList trl = new TextRuleList(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));

		trl.setEnabled(true);
		trl.addDefaultClass();
		trl.addDefaultClass("de");

		for (Rule r : trl.getRules()) {
			System.out.println(r.getFilter());
		}

		System.out.println("Now added a third non-lang specific class");
		Filter filter = ECQL.toFilter("56<65");
		trl.addClass(StylingUtil.STYLE_BUILDER.createTextSymbolizer(),
				"testRuleName", filter, true, null, null, null);
		for (Rule r : trl.getRules()) {
			System.out.println(r.getFilter());
		}

		System.out.println("Now added a fourth lang specific class");
		filter = ECQL.toFilter("33<33");
		trl.addClass(StylingUtil.STYLE_BUILDER.createTextSymbolizer(),
				"testRuleName_DE", filter, true, "de", null, null);
		for (Rule r : trl.getRules()) {
			System.out.println(r.getFilter());
		}
	}

	@Test
	public void testLanguageFilterEnvFunction() throws IOException {
		Filter filter = TextRuleList.classLanguageFilter("de");
		System.out.println(filter);
		assertEquals("[ de = env([LANG], [XX]) ]", filter.toString());

		FeatureCollection<SimpleFeatureType, SimpleFeature> testFeatures = TestDatasets.arabicInHeader
				.getFeatureCollection();
		Iterator<SimpleFeature> it = testFeatures.iterator();
		try {
			SimpleFeature f = it.next();

			EnvFunction.setLocalValue("LANG", "de");
			assertTrue(filter.evaluate(f));

			EnvFunction.setLocalValue("LANG", "en");
			assertFalse(filter.evaluate(f));
		} finally {
			testFeatures.close(it);
		}
	}

	@Test
	public void testLanguageDefaultClasses() throws IOException {
		TextRuleList trl = new TextRuleList(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));

		trl.setEnabled(true);
		trl.addDefaultClass();
		trl.addDefaultClass("de");
		trl.addDefaultClass("ar");

		FeatureCollection<SimpleFeatureType, SimpleFeature> testFeatures = TestDatasets.arabicInHeader
				.getFeatureCollection();
		Iterator<SimpleFeature> it = testFeatures.iterator();
		try {
			SimpleFeature f = it.next();

			assertEquals(3, trl.getRules().size());

			Filter filter = trl.getRules().get(0).getFilter();
			Filter filterDe = trl.getRules().get(1).getFilter();
			Filter filterAr = trl.getRules().get(2).getFilter();

			EnvFunction.setLocalValue("LANG", "de");
			assertFalse(filter.evaluate(f));
			assertTrue(filterDe.evaluate(f));
			assertFalse(filterAr.evaluate(f));

			EnvFunction.setLocalValue("LANG", "en");
			assertTrue(filter.evaluate(f));
			assertFalse(filterDe.evaluate(f));
			assertFalse(filterAr.evaluate(f));

			EnvFunction.setLocalValue("LANG", "ar");
			assertFalse(filter.evaluate(f));
			assertFalse(filterDe.evaluate(f));
			assertTrue(filterAr.evaluate(f));
		} finally {
			testFeatures.close(it);
		}
	}

	/**
	 * One default class, a default class for german, and a german spezial rule
	 * 
	 * @throws TransformerException
	 */
	@Test
	public void testLanguageClasses() throws IOException, CQLException,
			TransformerException {
		TextRuleList trl = new TextRuleList(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));

		trl.setEnabled(true);
		trl.addDefaultClass();
		trl.addDefaultClass("de");
		trl.addClass(StylingUtil.STYLE_BUILDER.createTextSymbolizer(),
				"testRuleName_DE", ECQL.toFilter("SURFACE=13591"), true, "de",
				null, null);

		FeatureCollection<SimpleFeatureType, SimpleFeature> testFeatures = trl
				.getStyledFeatures().getFeatureCollection();
		Iterator<SimpleFeature> it = testFeatures.iterator();
		try {
			SimpleFeature f = it.next();

			assertEquals(13591, f.getAttribute("SURFACE"));

			assertEquals(3, trl.getRules().size());

			Filter filter = trl.getRules().get(0).getFilter();
			Filter filterDe = trl.getRules().get(1).getFilter();
			Filter filterDeSpez = trl.getRules().get(2).getFilter();

			EnvFunction.setLocalValue("LANG", "en");
			assertTrue(filter.evaluate(f));
			assertFalse(filterDe.evaluate(f));
			assertFalse(filterDeSpez.evaluate(f));

			EnvFunction.setLocalValue("LANG", "de");
			assertFalse(filter.evaluate(f));
			assertFalse(filterDe.evaluate(f));
			assertTrue(filterDeSpez.evaluate(f));

			// Next Feature, which doesn't fit the special rule
			f = it.next();
			assertEquals(74119, f.getAttribute("SURFACE"));
			EnvFunction.setLocalValue("LANG", "de");
			assertFalse(filter.evaluate(f));
			assertTrue(filterDe.evaluate(f));
			assertFalse(filterDeSpez.evaluate(f));

		} finally {
			testFeatures.close(it);
		}

		// and Parse
		TextRuleList trl2 = new TextRuleList(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));
		trl2.importRules(trl.getRules());

		FeatureTypeStyle fts2 = StylingUtil.STYLE_BUILDER
				.createFeatureTypeStyle("test",
						trl2.getRules().toArray(new Rule[0]));

		// and Parse
		TextRuleList trl3 = new TextRuleList(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));
		trl3.importRules(trl2.getRules());

		FeatureTypeStyle fts3 = StylingUtil.STYLE_BUILDER
				.createFeatureTypeStyle("test",
						trl3.getRules().toArray(new Rule[0]));

		assertEquals(
				"A TextRuleList is not the same after subsequent transforms to rules and re-importing it.",
				StylingUtil.toXMLString(fts2), StylingUtil.toXMLString(fts3));

		FeatureTypeStyle fts1 = StylingUtil.STYLE_BUILDER
				.createFeatureTypeStyle("test",
						trl.getRules().toArray(new Rule[0]));
		assertEquals(
				"A TextRuleList is not the same after transforming it to rules as importing it again.",
				StylingUtil.toXMLString(fts1), StylingUtil.toXMLString(fts2));

	}

	/**
	 * One default class, a default class for german, and a german spezial rule
	 * 
	 * @throws TransformerException
	 */
	@Test
	public void testGetRuleAndReimport() throws IOException, CQLException,
			TransformerException {
		TextRuleList trl = new TextRuleList(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));

		trl.setEnabled(true);
		trl.addDefaultClass();
		trl.addDefaultClass("fr");
		trl.addClass(StylingUtil.STYLE_BUILDER.createTextSymbolizer(),
				"testRuleName_DE", ECQL.toFilter("SURFACE=13591"), true, "de",
				null, null);
		trl.addClass(StylingUtil.STYLE_BUILDER.createTextSymbolizer(),
				"testRuleName_DE", ECQL.toFilter("SURFACE=13591"), true, null,
				null, null);

		System.out.println("after creation  "
				+ trl.getRules().get(1).getFilter());

		// and Parse
		TextRuleList trl2 = new TextRuleList(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));
		trl2.importRules(trl.getRules());

		System.out.println("after reimport  "
				+ trl2.getRules().get(1).getFilter());

		FeatureTypeStyle fts2 = StylingUtil.STYLE_BUILDER
				.createFeatureTypeStyle("test",
						trl2.getRules().toArray(new Rule[0]));

		FeatureTypeStyle fts1 = StylingUtil.STYLE_BUILDER
				.createFeatureTypeStyle("test",
						trl.getRules().toArray(new Rule[0]));
		assertEquals(
				"A TextRuleList is not the same after transforming it to rules and importing it again.",
				StylingUtil.toXMLString(fts1), StylingUtil.toXMLString(fts2));
	}

	@Test
	public void testExistsClass() throws IOException, CQLException {
		TextRuleList trl = new TextRuleList(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));
		trl.setEnabled(true);
		trl.addDefaultClass();
		trl.addDefaultClass("de");

		trl.addClass(StylingUtil.STYLE_BUILDER.createTextSymbolizer(),
				"testRuleName_DE", ECQL.toFilter("SURFACE>2000"), true, "de",
				null, null);

		assertFalse(trl.existsClass(ECQL.toFilter("1=5"), "de"));
		assertFalse(trl.existsClass(ECQL.toFilter("1=5"), null));
		assertFalse(trl.existsClass(ECQL.toFilter("1=5"), null));
		assertFalse(trl.existsClass(ECQL.toFilter("SURFACE>2000"), null));
		assertTrue(trl.existsClass(ECQL.toFilter("SURFACE>2000"), "de"));

		assertTrue(trl
				.existsClass(TextRuleList.DEFAULT_FILTER_ALL_OTHERS, null));
	}

	@Test
	/**
	 * Test whether textRules created with version prior to 1.5 are still correctly parsed
	 */
	public void testOldTextRuleParsedCorrectly() throws IOException {
		org.geotools.styling.Style style = AsTestingUtil.TestSld.textRulesPre15
				.getStyle();

		AtlasStyler as = new AtlasStyler(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));

		as.importStyle(style);

		TextRuleList textRulesList = as.getTextRulesList();

		assertEquals(1, textRulesList.countClasses());
		assertFalse(textRulesList.isEnabled());
		assertTrue(textRulesList.isClassEnabled(0));

		assertNull(textRulesList.getClassLang(0));
		assertEquals(TextRuleList.DEFAULT_FILTER_ALL_OTHERS,
				textRulesList.getClassFilter(0));
		assertEquals(TextRuleList.DEFAULT_CLASS_RULENAME,
				textRulesList.getRuleName(0));
	}

	@Test
	/**
	 * Test whether textRules created with version prior to 1.5 are still correctly parsed
	 */
	public void testOldTextRuleDefaultLocalizedParsedCorrectly()
			throws IOException {
		org.geotools.styling.Style style = AsTestingUtil.TestSld.textRulesDefaultLocalizedPre16
				.getStyle();

		AtlasStyler as = new AtlasStyler(new StyledFS(
				TestingUtil.getTestFeatureSource(TestDatasets.arabicInHeader)));

		as.importStyle(style);

		TextRuleList textRulesList = as.getTextRulesList();

		assertEquals(
				"3 testrules are expected since we one sime default and one language specific default with two label attributes",
				3, as.getStyle().featureTypeStyles().get(1).rules().size());

		assertEquals(2, textRulesList.countClasses());
		assertEquals(0, textRulesList.getDefaultLanguages().size());

		// The first rule has two label properties:
		{
			TextSymbolizer cSymb0 = textRulesList.getClassSymbolizer(0);
			assertEquals("CNTRY_NAME",
					StylingUtil.getFirstPropertyName(null, cSymb0).toString());
			assertEquals("SURFACE",
					StylingUtil.getSecondPropertyName(null, cSymb0).toString());
		}

		// The second rule has only one label property
		{
			TextSymbolizer cSymb1 = textRulesList.getClassSymbolizer(1);
			assertEquals("SURFACE",
					StylingUtil.getFirstPropertyName(null, cSymb1).toString());
			assertEquals(null, StylingUtil.getSecondPropertyName(null, cSymb1));
		}

		assertTrue(textRulesList.isEnabled());
		assertTrue(textRulesList.isClassEnabled(0));

		assertEquals("DEFAULT", textRulesList.getRuleName(0));
		assertEquals("missing data", textRulesList.getRuleName(1));

		assertNull(textRulesList.getClassLang(0));
		assertEquals(TextRuleList.DEFAULT_FILTER_ALL_OTHERS,
				textRulesList.getClassFilter(0));
		assertEquals(TextRuleList.DEFAULT_CLASS_RULENAME,
				textRulesList.getRuleName(0));
	}
}
