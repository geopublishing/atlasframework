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
