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
package skrueger.atlas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.awt.Rectangle;
import java.io.File;
import java.net.URL;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.epsg.ThreadedEpsgFactory;
import org.geotools.resources.CRSUtilities;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.FactoryException;

import skrueger.atlas.dp.AMLImport;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.Group;
import skrueger.atlas.http.Webserver;
import skrueger.atlas.map.MapPool;

public class AtlasConfigTest {

	protected static AtlasConfig atlasConfig;

	/**
	 * Import the test Atlas which serves as a test-base for all Atlas tests
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final URL atlasXmlURL = ClassLoader
				.getSystemResource("data/test Atlas 2/ad/atlas.xml");
		String TEST_ATLAS_WORKING_COPY1 = new File(atlasXmlURL.toURI())
				.getParentFile().getParent();

		AtlasViewer.setupResLoMan(new String[] { TEST_ATLAS_WORKING_COPY1 });

		atlasConfig = new AtlasConfig();

		// Caching EPSG once
		try {
			CRSUtilities.toWGS84String(DefaultGeographicCRS.WGS84,
					new Rectangle(0, 0, 1, 1));
			new ThreadedEpsgFactory().createDerivedCRS("");
		} catch (FactoryException e1) {
		}

		// Start a WebServer without user interaction
		new Webserver(false);

		AMLImport.parseAtlasConfig(null, atlasConfig, false);
	}
//
//	@AfterClass
//	public static void afterClass() {
//		Webserver.dispose();
//	}

	@Test
	public void testGetName() {
		System.out.println(atlasConfig.getTitle());
	}

	@Test
	public void testGetDataPool() {
		DataPool dp = atlasConfig.getDataPool();
		assertFalse(dp.isEmpty());
	}

	@Test
	public void testGetMapPool() {
		final MapPool mapPool = atlasConfig.getMapPool();
		assertFalse(mapPool.isEmpty());

	}

	@Test
	public void testGetFirstGroup() {
		Group firstGroup = atlasConfig.getFirstGroup();
		assertNotNull(firstGroup);

		assertEquals(atlasConfig, firstGroup.getAc());
	}
}
