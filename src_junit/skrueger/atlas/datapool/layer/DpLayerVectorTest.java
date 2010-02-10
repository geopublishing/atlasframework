/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.datapool.layer;

import javax.swing.JFrame;

import junit.framework.TestCase;

import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.junit.Ignore;
import org.junit.Test;

import schmitzm.geotools.feature.FeatureUtil;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.TestingUtil;
import skrueger.geotools.XMapPane;

public class DpLayerVectorTest extends TestCase {

	@Test
	@Ignore
	public void testGetGeoObjectFeatureSource() throws Exception {

		AtlasConfigEditable atlasConfig = TestingUtil.getAtlasConfigE();

		final DpLayerVectorFeatureSource dpLayerVectorFSfromDS = (DpLayerVectorFeatureSource) atlasConfig
				.getDataPool().get("vector_well_morocco_00714208980");
		assertNotNull(dpLayerVectorFSfromDS);

		MapContext context = new DefaultMapContext(dpLayerVectorFSfromDS
				.getCrs());

		final XMapPane mapPane = new XMapPane(context, null);
		context
				.addLayer(new DefaultMapLayer(dpLayerVectorFSfromDS
						.getGeoObject(), FeatureUtil
						.createDefaultStyle(dpLayerVectorFSfromDS
								.getDefaultGeometry())));

		if (TestingUtil.INTERACTIVE) {
			JFrame frame = new JFrame();
			mapPane.setSize(500, 500);
			mapPane.setMapArea(dpLayerVectorFSfromDS.getEnvelope());

			frame.add(mapPane);
			frame.pack();
			frame.setSize(500, 500);
			frame.setVisible(true);

			assertTrue(
					"The envelpe of the JMappane contains the envelope of the dplvfs",
					mapPane.getMaxExtend().contains(
							dpLayerVectorFSfromDS.getEnvelope()));
		}

		Thread.sleep(4000);
	}

}
