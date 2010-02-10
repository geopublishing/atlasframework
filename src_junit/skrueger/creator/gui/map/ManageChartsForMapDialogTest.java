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
package skrueger.creator.gui.map;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.dp.DpRef;
import skrueger.atlas.dp.layer.DpLayer;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.exceptions.AtlasException;
import skrueger.atlas.gui.map.AtlasMapLegend;
import skrueger.atlas.map.Map;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.GPDialogManager;
import skrueger.creator.TestingUtil;

public class ManageChartsForMapDialogTest extends TestCase {

	// TODO move to a JUnit test
	public void testShowGUI() throws InterruptedException, AtlasException, FactoryException,  SAXException, IOException, ParserConfigurationException, TransformException {
		AtlasConfigEditable atlasConfigE = 
			TestingUtil.getAtlasConfigE();

		DesignMapView atlasMapView = new DesignMapView(null, atlasConfigE);

		Map map = atlasConfigE.getMapPool().get(0);
		atlasMapView.setMap(map);
		atlasMapView.initialize();

		AtlasMapLegend legend = atlasMapView.getLegend();

		DpLayerVectorFeatureSource dpv = null;
		for (DpRef<DpLayer<?, ? extends ChartStyle>> dpr : map.getLayers()) {
			if (dpr.getTarget() instanceof DpLayerVectorFeatureSource) {
				dpv = (DpLayerVectorFeatureSource) dpr.getTarget();
				break;
			}
		}

		if (dpv == null)
			return;

		ManageChartsForMapDialog instanceFor = GPDialogManager.dm_ManageCharts
				.getInstanceFor(dpv, null, dpv, legend);

		instanceFor.setModal(true);

		while (instanceFor.isVisible()) {
			Thread.sleep(1000);
		}

		System.exit(0);

	}
}
