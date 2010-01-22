/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
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
