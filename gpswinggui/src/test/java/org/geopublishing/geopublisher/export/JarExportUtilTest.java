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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GPTestingUtil;
import org.geopublishing.geopublisher.exceptions.AtlasExportException;
import org.junit.After;
import org.junit.Test;

public class JarExportUtilTest extends TestCase {
	static private final Logger LOGGER = Logger
			.getLogger(JarExportUtilTest.class);

	final File atlasExportTesttDir = GPTestingUtil.getAtlasExportTesttDir();

	private AtlasConfigEditable atlasConfig;

	@Override
	protected void setUp() throws Exception {
		atlasConfig = GPTestingUtil.getAtlasConfigE(GPTestingUtil.Atlas.small);
	}
	

	@Test
	public void testCreateJarFromDpeUnsigned() throws AtlasExportException,
			IOException, InterruptedException, AtlasCancelException {

		JarExportUtil jarExportUtil = new JarExportUtil(atlasConfig,
				atlasExportTesttDir, true, true, false);

		File dpeJarFile = new File(atlasExportTesttDir,
				"vector_landesgrenze_Benin01420640780.jar");

		dpeJarFile.delete();

		assertFalse(dpeJarFile.exists());

		DpEntry dpEntry = (DpEntry) atlasConfig.getDataPool().values()
				.toArray()[0];
		assertNotNull(dpEntry);

		assertEquals(
				"pdf_02034337607_geopublisher_1.4_chart_creation_tutorial",
				dpEntry.getId());

		jarExportUtil.createJarFromDpe(dpEntry);

		assertTrue(dpeJarFile.exists());
		long length = dpeJarFile.length();
		System.out.println(length);

		dpeJarFile.delete();
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

		File dpeJarFile = new File(atlasExportTesttDir,
				"vector_landesgrenze_Benin01420640780.jar");

		dpeJarFile.delete();
		assertFalse(dpeJarFile.exists());

		String passwort = GPProps.get(GPProps.Keys.signingkeystorePassword);
		assertNotNull(passwort);

		DpEntry dpEntry = (DpEntry) atlasConfig.getDataPool().values()
				.toArray()[0];
		assertNotNull(dpEntry);

		assertEquals(
				"pdf_02034337607_geopublisher_1.4_chart_creation_tutorial",
				dpEntry.getId());

		jarExportUtil.createJarFromDpe(dpEntry);

		assertTrue(dpeJarFile.exists());
		dpeJarFile.delete();
	}

	@Test
	public void testExportAtlasNoLibsNoSignNoGUI() throws Exception {
		assertNotNull(atlasExportTesttDir);

		JarExportUtil jeu = new JarExportUtil(atlasConfig, atlasExportTesttDir,
				false, false, false);

		LOGGER.debug("atlasExportTesttDir="
				+ atlasExportTesttDir.getAbsolutePath());

		FileUtils.deleteDirectory(atlasExportTesttDir);
		assertTrue(atlasExportTesttDir.mkdir());

		jeu.export(null);

		String[] files = atlasExportTesttDir.list();
		assertEquals(files[1], "JWS");
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
				true, false, false);

		String passwort = GPProps.get(GPProps.Keys.signingkeystorePassword);
//		LOGGER.info("Signer Passwort = " + passwort);
		assertNotNull(passwort);

		jeu.export(null);

		assertTrue("Datei autorun.inf exists in DISK folder", Arrays.asList(new File(atlasExportTesttDir, "DISK").list())
				.contains("autorun.inf"));
		
		assertTrue("Datei autorun.inf may not exist in JWS folder",!Arrays.asList(new File(atlasExportTesttDir, "JWS").list())
				.contains("autorun.inf"));
	}

	@After
	public void cleanAtlasConfig() {
		atlasConfig = null;
	}

}
