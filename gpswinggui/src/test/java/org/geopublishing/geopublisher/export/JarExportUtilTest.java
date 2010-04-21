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
package org.geopublishing.geopublisher.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GPTestingUtil;
import org.geopublishing.geopublisher.exceptions.AtlasExportException;
import org.junit.BeforeClass;
import org.junit.Test;

public class JarExportUtilTest{
	static private final Logger LOGGER = Logger
			.getLogger(JarExportUtilTest.class);

	final File atlasExportTesttDir = GPTestingUtil.getAtlasExportTesttDir();

	private static AtlasConfigEditable atlasConfig;

	@BeforeClass
	public  static void setUp() throws Exception {
		atlasConfig = GPTestingUtil.getAtlasConfigE(GPTestingUtil.Atlas.small);
	}
	

	@Test
	public void testCreateJarFromDpeUnsigned() throws AtlasExportException,
			IOException, InterruptedException, AtlasCancelException {

		JarExportUtil jarExportUtil = new JarExportUtil(atlasConfig,
				atlasExportTesttDir, true, true, false);

		// Expected first entry in the datapool 
		String expected = "pdf_02034337607_geopublisher_1.4_chart_creation_tutorial";
		
		File expoectedDpeJarFileLoaction = new File(jarExportUtil.getTempDir(),
				expected);

		expoectedDpeJarFileLoaction.delete();

		DpEntry dpEntry = (DpEntry) atlasConfig.getDataPool().values()
				.toArray()[0];
		assertNotNull(dpEntry);

		assertEquals("Der Test ist nicht korrekt, der erste eintrag im Datenpool is falsch", 
				expected,
				dpEntry.getId());

		File createJarFromDpe = jarExportUtil.createJarFromDpe(dpEntry);
		assertTrue("createJarFromDpe failed: After export the expected file "+expected+" doesn't exist!", createJarFromDpe.exists());

		expoectedDpeJarFileLoaction.delete();
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
		assertTrue(!exeFile.exists());

		jeu.createJSmooth(atlasDISKDir);

		assertTrue(exeFile.exists());

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

		String passwort = GPProps.get(GPProps.Keys.signingkeystorePassword);
		// LOGGER.info("Signer Passwort = " + passwort);
		assertNotNull(passwort);

		jeu.export(null);

		assertTrue("Datei autorun.inf exists in DISK folder", Arrays.asList(new File(atlasExportTesttDir, "DISK").list())
				.contains("autorun.inf"));

		assertTrue("Datei autorun.inf exists in DISK folder", Arrays.asList(new File(atlasExportTesttDir, "DISK").list())
				.contains("atlas.exe"));
		
		assertTrue("Datei autorun.inf may not exist in JWS folder",!Arrays.asList(new File(atlasExportTesttDir, "JWS").list())
				.contains("autorun.inf"));
	}

	@Test
	public void testJarExecutables() {
		try {
			JarExportUtil jarExportUtil = new JarExportUtil(atlasConfig,
					atlasExportTesttDir, true, true, false);
		} catch (Exception e) {
			fail();
		}
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
		

		assertTrue("createJarFromDpe didn't create an existing file?", createdJar.exists());
		assertEquals("Created JAR isn't where expected?", dpeJarFileExpected.getAbsolutePath(), createdJar.getAbsolutePath());
		dpeJarFileExpected.delete();
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
//		LOGGER.info("Signer Passwort = " + passwort);
		assertNotNull(passwort);

		jeu.export(null);

		assertTrue("Datei autorun.inf exists in DISK folder", Arrays.asList(new File(atlasExportTesttDir, "DISK").list())
				.contains("autorun.inf"));
		
		assertTrue("Datei autorun.inf may not exist in JWS folder",!Arrays.asList(new File(atlasExportTesttDir, "JWS").list())
				.contains("autorun.inf"));
	}

}
