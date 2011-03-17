package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.classification.FeatureClassification;
import org.junit.Test;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

public class FeatureClassificationGUITest extends TestingClass {

	@Test
	public void testShowGuiWithoutValueAttribute() throws Throwable {
		if (!hasGui())
			return;

		AtlasStylerVector asv = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.countryShp);
		FeatureClassification classifier = new FeatureClassification(
				asv.getStyledFeatures());
		ClassificationGUI gui = new FeatureClassificationGUI(null, classifier,
				asv, "junit test vector classification");

		TestingUtil.testGui(gui);
	}

	@Test
	public void testShowGui() throws Throwable {
		if (!hasGui())
			return;

		AtlasStylerVector asv = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.countryShp);
		FeatureClassification classifier = new FeatureClassification(
				asv.getStyledFeatures());
		classifier.setValue_field_name(FeatureUtil.getNumericalFieldNames(
				asv.getStyledFeatures().getSchema()).get(0));
		ClassificationGUI gui = new FeatureClassificationGUI(null, classifier,
				asv, "junit test vector classification");

		TestingUtil.testGui(gui);
	}
}
