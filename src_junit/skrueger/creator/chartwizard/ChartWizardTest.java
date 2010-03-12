/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.creator.chartwizard;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import schmitzm.jfree.feature.style.FeatureChartStyle;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.exceptions.AtlasException;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.GPDialogManager;
import skrueger.creator.TestingUtil;
import skrueger.creator.gui.DesignAtlasChartJDialog;
import skrueger.geotools.AttributeMetadataMap;

public class ChartWizardTest extends TestCase {

	private AtlasConfigEditable ace;

	protected void setUp() throws Exception {
		super.setUp();
		ace = TestingUtil.getAtlasConfigE();
	}

	/**
	 * If there is any chart defined in the given Atlas, it will be opened.
	 * 
	 * @throws InterruptedException
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws TransformException 
	 * @throws FactoryException 
	 * @throws AtlasException 
	 */
	public void testOpenChartEditorAndWait() throws InterruptedException, AtlasException, FactoryException, TransformException, SAXException, IOException, ParserConfigurationException {

		if (!TestingUtil.INTERACTIVE)
			return;

		System.out
				.println("Opening all Charts of the Atlas one by one without");

		final AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();
		for (DpEntry dpe : ace.getDataPool().values()) {
			if (dpe instanceof DpLayerVectorFeatureSource) {
				DpLayerVectorFeatureSource dplvfs = (DpLayerVectorFeatureSource) dpe;

				for (FeatureChartStyle cs : dplvfs.getCharts()) {
					DesignAtlasChartJDialog dialog = GPDialogManager.dm_DesignCharts
							.getInstanceFor(cs, null, cs, null, dplvfs, ace);
					assertTrue(dialog.isVisible());
					while (dialog.isVisible()) {
						Thread.sleep(100);
					}
				}
			}
		}
	}

	/**
	 * Open a CHartWizard for the Cities layer of the IIDA
	 */
	public void testWizard() {

		if (!TestingUtil.INTERACTIVE)
			return;

		DpLayerVectorFeatureSource cities = TestingUtil.getCities(ace);

		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = cities
				.getGeoObject();
		AttributeMetadataMap attributeMetaDataMap = cities
				.getAttributeMetaDataMap();
		FeatureChartStyle newChart = ChartWizard.showWizard(featureSource,
				attributeMetaDataMap);

		// JFreeChart chart =
		// newChart.applyToFeatureCollection(featureSource.getFeatures());
		if (newChart != null) {
			DesignAtlasChartJDialog first = GPDialogManager.dm_DesignCharts
					.getInstanceFor(newChart, null, newChart, null, cities, ace);

			assertTrue("The dialog must be visible automatically ", first
					.isVisible());

			DesignAtlasChartJDialog second = GPDialogManager.dm_DesignCharts
					.getInstanceFor(newChart, null, newChart, null, cities, ace);
			assertTrue("The dialog must be visible automatically ", second
					.isVisible());

			assertEquals("The DialogManager returns the same instance", first,
					second);
		}
	}
}
