package org.geopublishing.atlasViewer.dp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.swing.swingworker.AtlasStatusDialogInterface;
import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;

public class DpEntryTest extends TestingClass {

    @Test
    public void testGetQuality() throws AtlasException, FactoryException, TransformException,
	    SAXException, IOException, ParserConfigurationException {

	AtlasConfigEditable atlasConfig = GpTestingUtil
		.getAtlasConfigE(GpTestingUtil.TestAtlas.small);

	DataPool dataPool = atlasConfig.getDataPool();
	int dpeWithChartCount = 0;
	for (DpEntry<? extends ChartStyle> dpe : dataPool.values()) {
	    if (dpe.getCharts().size() > 0) {
		dpeWithChartCount++;
		Double quality = dpe.getQuality();
		assertTrue("quality has to be between 0 and 1", quality >= 0.);
		assertTrue("quality has to be between 0 and 1", quality <= 1.);

		dpe.getCharts().clear();
		Double quality2 = dpe.getQuality();
		assertTrue(quality != quality2);
	    }
	}
	assertEquals("The chart demo atlas contains one layer with charts ", 1, dpeWithChartCount);
	atlasConfig.deleteAtlas();
    }

}
