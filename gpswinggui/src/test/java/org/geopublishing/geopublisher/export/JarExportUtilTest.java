/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.exceptions.AtlasExportException;
import org.geotools.data.DataUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.io.IOUtil;
import schmitzm.swing.TestingUtil;

public class JarExportUtilTest {
	static private final Logger LOGGER = Logger
			.getLogger(JarExportUtilTest.class);

	File atlasExportTesttDir;

	private static AtlasConfigEditable atlasConfig;

	@Before
	public void setUp() throws Exception {
		atlasExportTesttDir = GpTestingUtil.createAtlasExportTesttDir();
		atlasConfig = GpTestingUtil
				.getAtlasConfigE(GpTestingUtil.TestAtlas.small);
	}

	@After
	public void tearDown() throws Exception {
		atlasConfig.dispose();
		FileUtils.deleteDirectory(atlasExportTesttDir);
	}

	@Test
	public void testCleanUrl() {
		String libNameChecked = "http://www.geopublishing.org/gp/././gt-xsd-core-2.6.5.jar";
		libNameChecked = libNameChecked.replace("./", "");
		assertEquals("http://www.geopublishing.org/gp/gt-xsd-core-2.6.5.jar",
				libNameChecked);
	}

	@Test
	public void testExportAtlasLibsNoSignNoGUI() throws Exception {
		assertNotNull(atlasExportTesttDir);
		LOGGER.debug("atlasExportTesttDir="
				+ atlasExportTesttDir.getAbsolutePath());
		FileUtils.deleteDirectory(atlasExportTesttDir);
		// GuiAndTools.deleteDir(atlasExportTesttDir);
		assertTrue(atlasExportTesttDir.mkdir());

		JarExportUtil jeu = new JarExportUtil(atlasConfig, atlasExportTesttDir,
				true, true, false);

		String passwort = GPProps.get(GPProps.Keys.signingkeystorePassword);
		// LOGGER.info("Signer Passwort = " + passwort);
		assertNotNull(passwort);

		jeu.export(null);

		assertTrue("File autorun.inf exists in DISK folder",
				Arrays.asList(new File(atlasExportTesttDir, "DISK").list())
						.contains("autorun.inf"));

		assertTrue("File autorun.inf may not exist in JWS folder",
				!Arrays.asList(new File(atlasExportTesttDir, "JWS").list())
						.contains("autorun.inf"));

		{
			List<String> listOfFilesInAtlasDataDir = Arrays.asList(new File(
					atlasExportTesttDir, "DISK/" + JarExportUtil.DISK_SUB_DIR)
					.list());
			for (String s : listOfFilesInAtlasDataDir) {
				if (s.startsWith("tools-"))
					fail("File " + s
							+ " should not be part of the exported atlas!");
			}
		}
	}

	/**
	 * Check that all ant related dependencies have been removed
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetJarLibNames() throws IOException {
		assertNotNull(atlasExportTesttDir);
		LOGGER.debug("atlasExportTesttDir="
				+ atlasExportTesttDir.getAbsolutePath());
		FileUtils.deleteDirectory(atlasExportTesttDir);
		// GuiAndTools.deleteDir(atlasExportTesttDir);
		assertTrue(atlasExportTesttDir.mkdir());

		JarExportUtil jeu = new JarExportUtil(atlasConfig, atlasExportTesttDir,
				true, true, false);

		String[] jarLibNames = jeu.getJarLibNames();

		assertTrue("Number of dependencies should be greater than 20",
				jarLibNames.length > 20);

		for (String dep : jarLibNames) {
			assertFalse(dep + " is an unwanted dependency", dep.toLowerCase()
					.contains("ant"));
		}
	}

	@Test
	public void testFindNativeDllURL() throws IOException {
		JarExportUtil jarExportUtil = new JarExportUtil(atlasConfig,
				IOUtil.getTempDir(), true, true, false);
		URL soDllUrl = jarExportUtil.findJarUrl("gdal14.dll");

		assertNotNull(soDllUrl);
	}

	@Test
	public void testResourceLocationsCorrect() {

		assertNotNull(GpUtil.class
				.getResource(JarExportUtil.HTACCESS_RES_LOCATION));

		assertNotNull(GpUtil.class
				.getResource(JarExportUtil.LICENSEHTML_RESOURCE_NAME));

		assertNotNull(GpUtil.class
				.getResource(JarExportUtil.SPLASHSCREEN_RESOURCE_NAME_FALLBACK));

		assertNotNull(GpUtil.class
				.getResource(JarExportUtil.JSMOOTH_SKEL_AD_RESOURCE1));

		assertNotNull(GpUtil.class
				.getResource(JarExportUtil.JSMOOTH_SKEL_AD_RESOURCE2));

		assertNotNull(GpUtil.class
				.getResource(JarExportUtil.JSMOOTH_SKEL_AD_RESOURCE3));

		// "/" needed here
		assertNotNull(GpUtil.class
				.getResource(AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK));

	}

	@Test
	public void testCreateJarFromDpeUnsigned() throws AtlasExportException,
			IOException, InterruptedException, AtlasCancelException {

		JarExportUtil jarExportUtil = new JarExportUtil(atlasConfig,
				atlasExportTesttDir, true, true, false);

		assertExitsTempFilesCount(
				jarExportUtil.ATLAS_TEMP_FILE_EXPORTINSTANCE_ID, 1);

		// Expected first entry in the datapool
		String expected = "pdf_02034337607_geopublisher_1.4_chart_creation_tutorial";

		File expectedDpeJarFileLoaction = new File(jarExportUtil.getTempDir(),
				expected);

		expectedDpeJarFileLoaction.delete();

		DpEntry dpEntry = (DpEntry) atlasConfig.getDataPool().values()
				.toArray()[0];
		assertNotNull(dpEntry);

		assertEquals(
				"Der Test ist nicht korrekt, der erste eintrag im Datenpool is falsch",
				expected, dpEntry.getId());

		File createJarFromDpe = jarExportUtil.createJarFromDpe(dpEntry);
		assertTrue("createJarFromDpe failed: After export the expected file "
				+ expected + " doesn't exist!", createJarFromDpe.exists());

		expectedDpeJarFileLoaction.delete();

		// Temp dir still exists, because this is just the export of one layer.
		assertExitsTempFilesCount(
				jarExportUtil.ATLAS_TEMP_FILE_EXPORTINSTANCE_ID, 1);

		AVUtil.cleanupTempDir(jarExportUtil.ATLAS_TEMP_FILE_EXPORTINSTANCE_ID,
				null);

		// Temp dir still exists, because this is just the export of one layer.
		assertExitsTempFilesCount(
				jarExportUtil.ATLAS_TEMP_FILE_EXPORTINSTANCE_ID, 0);

	}

	private void assertExitsTempFilesCount(final String tempPrefix, int count) {
		// Assert no files with this prefix exist
		String[] list = IOUtil.getTempDir().list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(tempPrefix);
			}
		});
		assertEquals(count, list.length);
	}

	@Test
	public void testExportAtlasLibsSignNoGUI() throws Exception {

		assertNotNull(atlasExportTesttDir);
		LOGGER.debug("atlasExportTesttDir="
				+ atlasExportTesttDir.getAbsolutePath());
		FileUtils.deleteDirectory(atlasExportTesttDir);
		assertTrue(atlasExportTesttDir.mkdir());

		JarExportUtil jeu = new JarExportUtil(atlasConfig, atlasExportTesttDir,
				true, true, false);

		// Temp dir created
		assertExitsTempFilesCount(jeu.ATLAS_TEMP_FILE_EXPORTINSTANCE_ID, 1);

		String passwort = GPProps.get(GPProps.Keys.signingkeystorePassword);
		// LOGGER.info("Signer Passwort = " + passwort);
		assertNotNull(passwort);

		jeu.export(null);

		// Temp dir created
		assertExitsTempFilesCount(jeu.ATLAS_TEMP_FILE_EXPORTINSTANCE_ID, 0);

		assertTrue("Datei autorun.inf exists in DISK folder",
				Arrays.asList(new File(atlasExportTesttDir, "DISK").list())
						.contains("autorun.inf"));

		assertTrue("Datei start.sh exists in DISK exists ", new File(
				atlasExportTesttDir, "DISK/start.sh").exists());
		assertTrue("Datei start.sh exists in DISK is executable", new File(
				atlasExportTesttDir, "DISK/start.sh").canExecute());

		assertTrue("Datei autorun.inf exists in DISK folder",
				Arrays.asList(new File(atlasExportTesttDir, "DISK").list())
						.contains("atlas.exe"));

		assertTrue("Datei autorun.inf may not exist in JWS folder",
				!Arrays.asList(new File(atlasExportTesttDir, "JWS").list())
						.contains("autorun.inf"));

		assertTrue(new File(atlasExportTesttDir, "JWS/"
				+ JarExportUtil.JNLP_FILENAME).exists());

		File fileArDiskJar = new File(atlasExportTesttDir, "DISK/"
				+ JarExportUtil.DISK_SUB_DIR + JarExportUtil.ARJAR_FILENAME);
		assertTrue(fileArDiskJar.exists());
		assertTrue(new File(atlasExportTesttDir, "JWS/"
				+ JarExportUtil.ARJAR_FILENAME).exists());

		assertTrue(fileArDiskJar.exists());
		assertTrue(new File(atlasExportTesttDir, "JWS/"
				+ JarExportUtil.ARJAR_FILENAME).exists());

		File fileGpCoreDiskJar = new File(atlasExportTesttDir, "DISK/"
				+ JarExportUtil.DISK_SUB_DIR + JarExportUtil.GPCORE_JARNAME);
		assertTrue(fileGpCoreDiskJar.exists());
		assertTrue(new File(atlasExportTesttDir, "JWS/"
				+ JarExportUtil.GPCORE_JARNAME).exists());

		// asswinggui is not needed for exported atlases
		// assertTrue(new File(atlasExportTesttDir, "DISK/"
		// + JarExportUtil.DISK_SUB_DIR + JarExportUtil.ASSWINGGUI_JARNAME)
		// .exists());
		//
		// assertTrue(new File(atlasExportTesttDir, "JWS/"
		// + JarExportUtil.ASSWINGGUI_JARNAME).exists());

		assertTrue(new File(atlasExportTesttDir, "DISK/"
				+ JarExportUtil.DISK_SUB_DIR + JarExportUtil.SCHMITZM_JARNAME)
				.exists());
		assertFalse(
				"The libs are not exported anymore, but rahter referenced online",
				new File(atlasExportTesttDir, "JWS/"
						+ JarExportUtil.SCHMITZM_JARNAME).exists());

		File file = new File(atlasExportTesttDir, "JWS/"
				+ JarExportUtil.ARJAR_FILENAME);
		assertTrue("The resources '" + file.getAbsolutePath()
				+ "' must exist in the JWS folder", file.exists());

		file = new File(atlasExportTesttDir, "DISK/"
				+ JarExportUtil.DISK_SUB_DIR + JarExportUtil.ARJAR_FILENAME);
		assertTrue("The resources '" + file.getAbsolutePath()
				+ "' must exist in the DISK folder", file.exists());

		// Test start atlas..
		if (TestingUtil.INTERACTIVE && SystemUtils.IS_OS_LINUX) {
			String[] cmd = { "/usr/bin/java", "-jar",
					fileArDiskJar.getAbsolutePath() };

			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			String line;
			while ((line = input.readLine()) != null) {
				System.err.println(line);
			}
			input.close();

			assertEquals("Test atlas didn't start or didn't exit normally.", 0,
					p.waitFor());
		}

	}

	@Test
	public void testJarExecutables() throws IOException {
		new JarExportUtil(atlasConfig, atlasExportTesttDir, true, true, false);
	}

	@Test
	public void testCreateJarFromDpeSigned() throws AtlasExportException,
			IOException, InterruptedException, AtlasCancelException {
		JarExportUtil jarExportUtil = new JarExportUtil(atlasConfig,
				atlasExportTesttDir, false, true, false);

		File dpeJarFileExpected = new File(jarExportUtil.getTempDir(),
				"pdf_02034337607_geopublisher_1.4_chart_creation_tutorial.jar");

		dpeJarFileExpected.delete();
		assertFalse(dpeJarFileExpected.exists());

		String passwort = GPProps.get(GPProps.Keys.signingkeystorePassword);
		assertNotNull(passwort);

		DpEntry dpEntry = (DpEntry) atlasConfig.getDataPool().values()
				.toArray()[0];
		assertNotNull(dpEntry);

		File createdJar = jarExportUtil.createJarFromDpe(dpEntry);

		assertTrue("createJarFromDpe didn't create an existing file?",
				createdJar.exists());
		assertEquals("Created JAR isn't where expected?",
				dpeJarFileExpected.getAbsolutePath(),
				createdJar.getAbsolutePath());
		dpeJarFileExpected.delete();
	}

	@Test
	public void testJSmooth() throws Exception {

		assertNotNull(atlasExportTesttDir);

		JarExportUtil jeu = new JarExportUtil(atlasConfig, atlasExportTesttDir,
				true, true, true);

		LOGGER.debug("atlasExportTesttDir="
				+ atlasExportTesttDir.getAbsolutePath());

		FileUtils.deleteDirectory(atlasExportTesttDir);
		assertTrue(atlasExportTesttDir.mkdir());

		jeu.export(null);

		String[] files = atlasExportTesttDir.list();
		assertTrue((files[0].equals("JWS") && files[1].equals("DISK"))
				|| (files[1].equals("JWS") && files[0].equals("DISK")));

		File atlasDISKDir = new File(atlasExportTesttDir, "DISK");
		File exeFile = new File(atlasDISKDir, "atlas.exe");

		exeFile.delete();
		assertFalse(exeFile + " must not exist before it's creation",
				exeFile.exists());

		jeu.createJSmooth(atlasDISKDir);

		assertTrue(exeFile + " must exist after creation", exeFile.exists());
	}

	@Test
	public void testCreateIndexHTML() throws IOException {
		AtlasConfigEditable ace = TestAtlas.small.getAce();
		File html = JarExportUtil.createIndexHTML(ace,
				TestingUtil.getNewTempDir());

		assertTrue(html.exists());

		System.out.println(html.getAbsolutePath() + ":");
		System.out.println(IOUtil.readFileAsString(html));

		if (TestingUtil.INTERACTIVE) {
			AVSwingUtil.lauchHTMLviewer(null, DataUtilities.fileToURL(html));
		} else {
		}

	}

}
