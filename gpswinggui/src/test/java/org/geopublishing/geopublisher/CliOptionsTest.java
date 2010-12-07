package org.geopublishing.geopublisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.geopublisher.CliOptions.Errors;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geopublishing.geopublisher.export.JarExportUtil;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.io.IOUtil;
import schmitzm.lang.LangUtil;
import schmitzm.swing.TestingUtil;

public class CliOptionsTest {
	// private final ByteArrayOutputStream outContent = new
	// ByteArrayOutputStream();
	// private final ByteArrayOutputStream errContent = new
	// ByteArrayOutputStream();
	CliOptions cliOptions;

	@Before
	public void setUpStreams() {
		// System.setOut(new PrintStream(outContent));
		// System.setErr(new PrintStream(errContent));
		cliOptions = new CliOptions();
	}

	@After
	public void cleanUpStreams() {
		// System.setOut(null);
		// System.setErr(null);
	}

	//
	// @Test
	// @Ignore
	// // bis das data dir auf das atlas dir gelinkt ist!
	// public void testConfigureGs() throws IOException {
	// if (!GsTestingUtil.isAvailable())
	// return;
	// TestAtlas small = TestAtlas.small;
	//
	// String gsUser = GsTestingUtil.getUsername();
	// String gsUrl = GsTestingUtil.getUrl();
	// String gsPwd = GsTestingUtil.getPassword();
	//
	// GsRest gsRest = new GsRest(gsUrl, gsUser, gsPwd);
	// AtlasConfigEditable ace = small.getAce();
	// boolean deleteWorkspace = gsRest.deleteWorkspace(ace.getBaseName(),
	// true);
	//
	// String dsNameExpected = FilenameUtils.removeExtension(ace.getDataPool()
	// .get(1).getFilename());
	// List<String> layersUsingDatastore = gsRest.getLayersUsingDataStore(
	// ace.getBaseName(), dsNameExpected);
	// assertEquals(0, layersUsingDatastore.size());
	//
	// int rv = CliOptions.performArgs(new String[] { "-a",
	// small.getFile().toString(), "-gs", gsUrl, "-gsu", gsUser,
	// "-gsp", gsPwd });
	//
	// assertEquals(0, rv);
	//
	// layersUsingDatastore = gsRest.getLayersUsingDatastore(
	// ace.getBaseName(), dsNameExpected);
	// assertEquals(1, layersUsingDatastore.size());
	// }

	@Test
	public void testsLoadWithGUIandSaveAndExit() throws Throwable {

		if (TestingUtil.INTERACTIVE) {

			// Loads an atlas into the GUI and closes GP after a few seconds

			int performArgs = CliOptions.performArgs(new String[] { "-a",
					TestAtlas.small.getFile().toString(), "-s" });
			assertEquals(0, performArgs);
			assertTrue(!GeopublisherGUI.isInstanciated());
		}
	}

	@Test
	public void testsLoadWithGUI() throws Throwable {

		if (TestingUtil.INTERACTIVE) {

			// Loads an atlas into the GUI and closes GP after a few seconds

			int performArgs = CliOptions.performArgs(new String[] { "-a "
					+ TestAtlas.small.getFile() });
			assertEquals(-1, performArgs);

			LangUtil.sleepExceptionless(5000);

			assertTrue(GeopublisherGUI.isInstanciated());

			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					GeopublisherGUI.getInstance().closeAtlas(false);
					GeopublisherGUI.getInstance().getJFrame().dispose();
				}
			});
		}
	}

	@Test
	public void testExportFails() throws Throwable {

		assertEquals("No atlas, no export",
				Errors.EXPORTDIR_NOTEMPTYNOFORCE.getErrCode(),
				CliOptions.performArgs(new String[] { "-e /tmp" }));

		assertEquals(
				"Not empty directory should not allow export without -f",
				Errors.EXPORTDIR_NOTEMPTYNOFORCE.getErrCode(),
				CliOptions.performArgs(new String[] { "-e", "/tmp", "--atlas",
						TestAtlas.small.getFile().toString() }));
	}

	@Test
	public void testExportWithForce() throws Throwable {

		File expDir = GpTestingUtil.createAtlasExportTesttDir();
		new File(expDir, "sometestfile").createNewFile();

		assertEquals(
				Errors.EXPORTDIR_NOTEMPTYNOFORCE.getErrCode(),
				CliOptions.performArgs(new String[] { "-e", expDir.toString(),
						"--atlas", TestAtlas.small.getFile().toString() }));

		assertEquals(
				0,
				CliOptions.performArgs(new String[] { "-f", "-e",
						expDir.toString(), "--atlas",
						TestAtlas.small.getFile().toString() }));

	}

	@Test
	public void testExport() throws Throwable {

		File expDir = GpTestingUtil.createAtlasExportTesttDir();

		assertEquals(
				0,
				CliOptions.performArgs(new String[] { "-e", expDir.toString(),
						"--atlas", TestAtlas.small.getFile().toString() }));

		assertTrue(ArrayUtils.contains(expDir.list(), JarExportUtil.DISK));
		assertTrue(ArrayUtils.contains(expDir.list(), JarExportUtil.JWS));
	}

	@Test
	public void testExportDISKonlyZipped() throws Throwable {

		File expDir = GpTestingUtil.createAtlasExportTesttDir();

		assertEquals(0, CliOptions.performArgs(new String[] {
				"--" + CliOptions.ZIPDISK, "-e", expDir.toString(), "-d",
				"--atlas", TestAtlas.small.getFile().toString() }));

		assertTrue(ArrayUtils.contains(expDir.list(),
				AtlasConfig.DEFAULTBASENAME + ".zip"));
		assertTrue(ArrayUtils.contains(expDir.list(), JarExportUtil.DISK));
		assertFalse(ArrayUtils.contains(expDir.list(), JarExportUtil.JWS));
	}

	@Test
	public void testExportJWSonly() throws Throwable {

		File expDir = GpTestingUtil.createAtlasExportTesttDir();

		assertEquals(0, CliOptions.performArgs(new String[] { "-e",
				expDir.toString(), "-j", "--atlas",
				TestAtlas.small.getFile().toString() }));

		assertFalse(ArrayUtils.contains(expDir.list(), JarExportUtil.DISK));
		assertTrue(ArrayUtils.contains(expDir.list(), JarExportUtil.JWS));
	}

	@Test
	public void testExportJWSonlyWithUrl() throws Throwable {

		File expDir = GpTestingUtil.createAtlasExportTesttDir();

		assertEquals(
				0,
				CliOptions.performArgs(new String[] { "-e", expDir.toString(),
						"-j", "--atlas", TestAtlas.small.getFile().toString(),
						"-u", "http://atlas/atlas/" }));

		assertFalse(ArrayUtils.contains(expDir.list(), JarExportUtil.DISK));
		assertTrue(ArrayUtils.contains(expDir.list(), JarExportUtil.JWS));

		String readFileAsString = IOUtil.readFileAsString(new File(new File(
				expDir, JarExportUtil.JWS), JarExportUtil.JNLP_FILENAME));
		assertTrue(readFileAsString.contains("http://atlas/atlas/"));
		assertTrue(readFileAsString.contains("http://atlas/atlas/"
				+ JarExportUtil.JNLP_FILENAME));
	}

	@Test
	public void testCommandLineExecution() throws Throwable {

		assertEquals("Missing value for a", Errors.PARSEEXCEPTION.getErrCode(),
				CliOptions.performArgs(new String[] { "-a" }));

		assertEquals("Illegal value for atlas",
				Errors.AWCPARAM_ILLEGAL.getErrCode(),
				CliOptions.performArgs(new String[] { "-a asdf342fds" }));
	}

	@Test
	public void testPrintHelp() {
		new CliOptions().printHelp();
	}

	@Test
	public void testLoggerChange() {

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(org.apache.log4j.Level.WARN);

		rootLogger.warn("rootLogger");
		rootLogger.debug("rootLogger");

		Logger l = Logger.getLogger(CliOptionsTest.class);
		l.warn("l");
		l.debug("l");

	}

}
