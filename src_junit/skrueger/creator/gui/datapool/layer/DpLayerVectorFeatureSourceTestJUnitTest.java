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
package skrueger.creator.gui.datapool.layer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import schmitzm.io.IOUtil;
import skrueger.creator.AtlasConfigEditable;

/**
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public class DpLayerVectorFeatureSourceTestJUnitTest {

	private static URL resourceShape1;

	private static URL resourceShape2;

	@BeforeClass
	public static void setupResourceURLs() {
		resourceShape1 = DpLayerVectorFeatureSourceTestJUnitTest.class
				.getClassLoader().getResource(
						"data/esrishape 1/country.shp");
		assertNotNull(resourceShape1);

		resourceShape2 = DpLayerVectorFeatureSourceTestJUnitTest.class
				.getClassLoader().getResource(
						"data/esrishape2/One Country.SHP");
		assertNotNull(resourceShape2);

	}

	/**
	 * An {@link AtlasConfigEditable} that is being created before any test
	 */
	private AtlasConfigEditable ace;

	final File atlasTestDir = new File(IOUtil.getTempDir(), "junitTestAtlas");

	@Before
	public void setupAtlasConfigEditable() {

		// Creating the directory where the test Atlas resides
		System.out.println("Creating the test Atlas directory...");
		atlasTestDir.mkdirs();
		assertTrue(atlasTestDir.exists());
		assertTrue(atlasTestDir.canWrite());

		// Create a dummy ace
		ace = new AtlasConfigEditable();
		ace.setAtlasDir(atlasTestDir);
		assertTrue(ace.getAboutDir().exists());
		assertTrue(ace.getHtmlDir().exists());
		assertTrue(ace.getAd().exists());

		new File("/tmp/ad/data").mkdirs();
		assertTrue(new File(atlasTestDir, "ad/data").exists());
		assertTrue(new File(atlasTestDir, "ad/data").isDirectory());
		assertTrue(new File(atlasTestDir, "ad/data").canWrite());
	}

	@After
	public void cleanAtlasConfigEditable() throws IOException {
		System.out.println("Deleting the test Atlas directory...");
		FileUtils.deleteDirectory(atlasTestDir);
		assertTrue(!atlasTestDir.exists());
		ace = null;
	}
//
//	@Test
//	public void testAndCreateDpe_Shape1() throws Exception {
//		DpLayerVectorFeatureSourceTest tester = new DpLayerVectorFeatureSourceTest();
//
//		System.out.println("Testing if " + resourceShape1.getFile()
//				+ " (with a missing .prj file) can be identified and imported");
//		assertTrue(tester.test(null, resourceShape1));
//
//		DpLayerVectorFeatureSource dpe = (DpLayerVectorFeatureSource) tester
//				.create(ace, DataUtilities.urlToFile(resourceShape1), null, false);
//		assertNotNull(dpe);
//		// For resourceShape1 a new .prj should have been created.
//		assertTrue(DataUtilities.urlToFile( IOUtil.changeUrlExt(dpe.getUrl((Component)null), "prj")).exists());
//
//		dpe.dispose();
//		tester = null;
//	}
//
//	@Test
//	public void testAndCreateDpe_Shape2() throws Exception {
//		DpLayerVectorFeatureSourceTest tester = new DpLayerVectorFeatureSourceTest();
//
//		System.out
//				.println("Testing if "
//						+ resourceShape2.getFile()
//						+ " (dirty filenames, .PRJ instead of .prj) can be identified and imported.");
//		assertTrue(tester.test(null, resourceShape2));
//		// String s =
//		// "/home/alfonx/EigeneDateien/code/ws.atlas/AtlasCreator/bin/junit/data/esrishape2/One
//		// Country.SHP";
//		DpLayerVectorFeatureSource dpe = (DpLayerVectorFeatureSource) tester
//				.create(ace, new File(resourceShape2.toURI()), null, false);
//		assertNotNull(dpe);
//
//		// For resourceShape1 a new .prj should have been created.
//		final File prjFile = new File(IOUtil.changeUrlExt(dpe.getUrl((Component)null), "prj")
//				.toURI());
//
//		assertTrue(prjFile.exists());
//		assertTrue(prjFile.getName().equals("one_country.prj"));
//
//		dpe.dispose();
//		tester = null;
//	}
}
