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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.charabia.jsmoothgen.application.ExeCompiler;
import net.charabia.jsmoothgen.application.JSmoothModelBean;
import net.charabia.jsmoothgen.application.JSmoothModelPersistency;
import net.charabia.jsmoothgen.skeleton.SkeletonBean;
import net.charabia.jsmoothgen.skeleton.SkeletonList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AVUtil.OSfamiliy;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.exceptions.AtlasFatalException;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.geopublisher.AMLExporter;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.GPProps.Keys;
import org.geopublishing.geopublisher.exceptions.AtlasExportException;
import org.geotools.data.DataUtilities;
import org.jfree.util.Log;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import schmitzm.io.FileInputStream;
import schmitzm.io.IOUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.lang.LangUtil;
import schmitzm.swing.ExceptionDialog;
import sun.security.tools.JarSigner;
import sun.tools.jar.Main;

/**
 * This class exports an {@link AtlasConfigEditable} to a folder. This folder
 * can be burned to CDROM and will autostart the Atlas under windows. <br>
 * The export directory also contains a <code>start.bat</code> and a
 * <code>start.sh</code> to launch the atlas if autostart is disabled.<br>
 * The folder may also be served by a webserver. Linking to the
 * <code>.jnlp</code> file will start the atlas using JavaWebStart
 */
public class JarExportUtil {

	/**
	 * The name of the JAR file which contains general atlas resources like
	 * HTML-pages, atlas.xml, etc…
	 */
	private final static String ARJAR_FILENAME = "atlas_resources.jar";

	/**
	 * Files next to atlas.gpa will be copied to the folders without putting
	 * them in a JAR. This can be useful for example for PDF files that should
	 * be referencable from within the atlas, but also reside uncompressed on
	 * the CD root directory.
	 */
	private static final FilenameFilter filterForRootLevelFiles = new FilenameFilter() {

		@Override
		public boolean accept(final File dir, final String name) {

			// This is a list of files expected in ad that shall not be copied
			if (name.equalsIgnoreCase(AtlasConfigEditable.ATLAS_GPA_FILENAME))
				return false;
			if (name.equalsIgnoreCase(AVProps.PROPERTIESFILE_RESOURCE_NAME))
				return false;
			if (name.equalsIgnoreCase(AtlasConfig.ATLASDATA_DIRNAME))
				return false;

			return true;
		}
	};

	/**
	 * The filename of the JNLP file which can be used to start the atlas from
	 * the Internet.
	 */
	public static final String JNLP_FILENAME = "atlasViewer.jnlp";

	final static private Logger LOGGER = Logger.getLogger(JarExportUtil.class);

	private static final String LIBDIR = "."; // "lib";

	/**
	 * Create the files needed to enable autorun features for as many OSs as
	 * possible
	 * 
	 * @param ace
	 * @param targetJar
	 * @throws AtlasExportException
	 */
	private static void createAuxiliaryDISKFiles(final AtlasConfigEditable ace,
			final File targetJar) throws AtlasExportException {
		try {

			final File startSHFile = new File(targetJar.getParentFile(),
					"start.sh");
			// ******************************************************************
			// start.sh for Linux (the
			// ******************************************************************
			FileWriter fileWriter = new FileWriter(startSHFile);
			final Integer xmx = GPProps.getInt(GPProps.Keys.startJVMWithXmx,
					256);
			fileWriter.write("# JAVA "
					+ GPProps.get(Keys.MinimumJavaVersion, "1.6.0_14")
					+ " or higher is required! \n");
			fileWriter
					.write("# This will start the Atlas with a maximum of "
							+ xmx
							+ "Mb of memory. Increase this number if have more memory to spend.\n");
			fileWriter.write("java -Xmx" + xmx
					+ "m -Dfile.encoding=UTF-8 -Djava.library.path=" + LIBDIR
					+ "/natives -jar " + targetJar.getName() + "\n");
			fileWriter.close();
			startSHFile.setExecutable(true, false);

			// ******************************************************************
			// start.bat for Windows
			// ******************************************************************
			fileWriter = new FileWriter(new File(targetJar.getParentFile(),
					"start.bat"));
			fileWriter.write("@echo off\r\n");
			fileWriter.write("atlas.exe\r\n");
			fileWriter.close();

			/******************************************************************
			 * // autorun.inf for windows [autorun] OPEN=SETUP.EXE /AUTORUN
			 * ICON=SETUP.EXE,1
			 * 
			 * shell\configure=&Konfigurieren...
			 * shell\configure\command=SETUP.EXE
			 * 
			 * shell\install=&Installieren... shell\install\command=SETUP.EXE //
			 ******************************************************************/
			fileWriter = new FileWriter(new File(targetJar.getParentFile(),
					"autorun.inf"));
			fileWriter.write("[autorun]\n");
			fileWriter.write("icon=atlas.exe,1\n");
			fileWriter.write("open=atlas.exe\n");
			fileWriter.close();

		} catch (final IOException e) {
			throw new AtlasExportException(
					"Error creating the autorun.inf file", e);
		}
	}

	/**
	 * Creates an index.html that allows to start the Atlas
	 * 
	 * @param targetJar
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * @param ace
	 * @throws AtlasExportException
	 */
	private static void createIndexHTML(final AtlasConfigEditable ace,
			final File targetJar) throws AtlasExportException {
		// **********************************************************************
		// 
		// Creating the index.html
		//
		// **********************************************************************

		try {
			final File indexFile = new File(targetJar.getParent(), "index.html");
			final FileWriter fileWriter = new FileWriter(indexFile);
			fileWriter
					.write("<?xml version='1.0' encoding='UTF-8'?><html><body>\n");
			final Date date = new Date();
			fileWriter.write("<h2>atlas build time: " + date.toString()
					+ "</h2>\n");

			/**
			 * Start des JNLP über das deplayJava.js
			 * https://jdk6.dev.java.net/deployment_advice
			 * .html#Deploying_Java_Web_Start_Applica
			 */
			String jnlpLocation = GPProps.get(GPProps.Keys.jnlpURL,
					"http://localhost/atlas");
			if (!jnlpLocation.endsWith("/")) {
				jnlpLocation += "/";
			}

			fileWriter
					.write("<h2>"
							+ ace.getTitle()
							+ "</h2>"
							+ "Created with <a href=\"http://www.geopublishing.org/Geopublisher\">Geopublisher</a>!"
							+ "<br>Us can use the following button to start the atlas:<center>");

			String jsScript = GPProps.get(Keys.JWSStartScript).replaceAll(
					"__JNLPURL__", jnlpLocation + JNLP_FILENAME);
			jsScript = jsScript.replace("__MINJAVAVERSION__", GPProps.get(
					Keys.MinimumJavaVersion, "1.6.0_14"));
			fileWriter.write(jsScript);

			fileWriter.write("</center></html></body>\n");
			fileWriter.close();
		} catch (final FileNotFoundException e) {
			throw new AtlasExportException("Creating index.html failed", e);
		} catch (final IOException e) {
			throw new AtlasExportException("Creating index.html failed", e);
		}

	}

	protected static void createReadmeTXT(final AtlasConfigEditable ace,
			final File targetJar) throws IOException {

		// ******************************************************************
		// README.TXT
		// ******************************************************************
		final FileWriter fileWriter = new FileWriter(new File(targetJar
				.getParentFile(), "README.TXT"));
		fileWriter.write("JAVA "
				+ GPProps.get(Keys.MinimumJavaVersion, "1.6.0_14")
				+ " is required to run the atlas\n");
		fileWriter
				.write("For help and information go to http://www.geopublishing.org\n");
		fileWriter
				.write("The logfile is atlas.log in your user's folder for temporary files.\n");
		fileWriter
				.write("Something like: C:\\Dokumente und Einstellungen\\YourUsernamE\\Lokale Einstellungen\\Temp\n");

		fileWriter.close();

	}

	private volatile boolean aborted = false;

	private final AtlasConfigEditable ace;

	/** Shall the local JRE be copied to the DISK folder? **/
	private final Boolean copyJRE;

	/** Internal counting for the percentage bar **/
	private int currSteps = 0;

	private ResultProgressHandle progress;

	private File targetDirJWS, targetDirDISK;

	private File tempDir = new File(IOUtil.getTempDir(), AVUtil.ATLAS_TEMP_FILE_ID
			+ "export" + AVUtil.RANDOM.nextInt(1999) + 1000);

	/** Is export to DISK requested? **/
	private final Boolean toDisk;
	/** Is export to JWS requested? **/
	private final Boolean toJws;

	/** Internal counting for the percentage bar **/
	private int totalSteps = 1;

	private List<DpEntry<? extends ChartStyle>> unusedDpes;

	/**
	 * Initialized lazilly and caches where the JARs from the classpath come
	 * from
	 **/
	private HashMap<String, URL> jarUrlsFromClassPath = null;

	/**
	 * Initializes an {@link JarExportUtil} object to do the real work.
	 * 
	 * @param ace
	 *            Instance of {@link AtlasConfigEditable} to export.
	 * @param exportDirectory
	 *            A {@link File} denoting the main export folder where DISK and
	 *            JWS will be created.
	 * @param toDisk
	 *            export the DISK version?
	 * @param toJws
	 *            export the JWS version?
	 * @param copyJRE
	 *            If {@link #toDisk} is <code>true</code>, this controls whether
	 *            the locally installed JRE should be copied to DISK/jre. May be
	 *            <code>null</code>.
	 * @throws IOException 
	 */
	public JarExportUtil(final AtlasConfigEditable ace_,
			final File exportDirectory, final Boolean toDisk,
			final Boolean toJws, final Boolean copyJRE) throws IOException {
		ace = ace_;
		this.toDisk = toDisk;
		this.toJws = toJws;
		this.copyJRE = copyJRE;
		
		// Create temporary export forlder
		info("Removing old files...");
		if (toDisk) {
			FileUtils.deleteDirectory(targetDirDISK);
		}
		if (toJws) {
			FileUtils.deleteDirectory(targetDirJWS);
		}

		deleteOldTempExportDirs();
		tempDir.mkdirs();
		
		
		// Create final export folders
		if (toDisk) {
			targetDirDISK = new File(exportDirectory, "DISK");
			targetDirDISK.mkdirs();
		}

		if (toJws) {
			targetDirJWS = new File(exportDirectory, "JWS");
			targetDirJWS.mkdirs();
		}

	}

	/**
	 * Can be called from external to abort the export process
	 */
	public void abort() {
		aborted = true;
	}

	private void addAtlasXMLToJar(final File targetJar,
			final AtlasConfigEditable ace) throws Exception {

		// Prepare a temporary atlas.xml
		final File adDir = new File(IOUtil.getTempDir(), "ad");
		FileUtils.deleteDirectory(adDir);
		adDir.mkdirs();
		final File exportAtlasXml = new File(adDir,
				AtlasConfig.ATLAS_XML_FILENAME);

		// Configure the AML exporter
		final AMLExporter amlExporter = new AMLExporter(ace);
		amlExporter.setExportMode(true);
		amlExporter.setAtlasXml(exportAtlasXml);

		amlExporter.saveAtlasConfigEditable();

		addToJar(targetJar, IOUtil.getTempDir(), "ad/"
				+ AtlasConfig.ATLAS_XML_FILENAME);
	}

	/**
	 * Adds/Updates an METAINF/INDEX.LIST entry to the given file.
	 * 
	 * @throws AtlasExportException
	 * @throws IOException
	 */
	public void addJarIndex(final File jarFile) throws AtlasExportException,
			IOException {

		LOGGER.debug("adding index to " + jarFile.getAbsolutePath());
		/**
		 * Adding an index
		 */
		final Main jartool = new Main(System.out, System.err, "jar");
		final boolean run = jartool.run(new String[] { "i",
				jarFile.getAbsolutePath() });
		if (!run) {
			LOGGER.warn("unable to create index for jar " + jarFile);
			// throw new AtlasExportException("unable to create index for jar "
			// + jarFile);
		}
	}

	/**
	 * Adds the what-String recursively to the target JAR Any manifest inside
	 * the target is deleted.. Call
	 * {@link #addManifest(File, AtlasConfigEditable)} after you are done with
	 * the jar If the jar doesn't exist, we create it.
	 * 
	 * @param targetJar
	 *            Existing JAR to extend
	 * 
	 * @param relDir
	 *            Directory with "ad" folder of the AtlasWorkingCopy
	 * 
	 * @param what
	 *            command line argument of what to put into the jar. All
	 *            '/'-chars will be replaces with the systemdependent
	 *            File.seperator
	 * @throws IOException
	 * @throws AtlasExportException
	 * @throws IOException
	 * @throws Exception
	 *             So many things can fail ;-)
	 * 
	 * @see Thanks to http://www.jguru.com/faq/view.jsp?EID=68627
	 */
	public void addToJar(final File targetJar, final File relDir,
			final String what) throws AtlasExportException, IOException {

		final String jarName = targetJar.getAbsolutePath();

		/**
		 * Creating a JAR
		 */
		final Main jartool = new Main(System.out, System.err, "jar");

		if (!targetJar.exists()) {
			final String manifestName = getManifestFile(true).getAbsolutePath();
			LOGGER.debug("creating new (with manifest:)" + jarName);
			final boolean run = jartool.run(new String[] { "cfm", jarName,
					manifestName, "-C", relDir.getAbsolutePath(), what });
			if (!run)
				throw new AtlasExportException("unable to create jar "
						+ targetJar + " with " + what);
		} else {
			LOGGER.debug("updating " + jarName + ", adding " + what);

			final boolean run = jartool.run(new String[] { "uf", jarName, "-C",
					relDir.getAbsolutePath(), what });
			if (!run)
				throw new AtlasExportException("unable to update jar "
						+ targetJar + " with " + what);
		}

	}

	/**
	 * Monitors the isRunning method of {@link #progress}. If it has been
	 * canceled throws an {@link AtlasExportCancelledException}.
	 * 
	 * @throws AtlasExportCancelledException
	 */
	private void checkAbort() throws AtlasCancelException {
		// if (progress != null && !progress.isRunning())
		if (aborted)
			throw new AtlasCancelException();
	}

	/**
	 * Without calling this function, only av_resources.jar and auxialliary
	 * files (jnlp, start.bat...) are created. The while lib directory is still
	 * missing. The JARs in the lib directory have to be signed if they shall be
	 * usable for JWS. This function copies them to the target directory. The
	 * source is either the local filesystem or the webserver where GP has been
	 * started from.
	 * 
	 * @throws AtlasExportException
	 * @throws AtlasCancelException
	 */
	protected void copyAndSignLibs(final File targetJar,
			final AtlasConfigEditable ace) throws AtlasExportException,
			AtlasCancelException {

		// if (progressWindow != null && progressWindow.isCanceled())
		// throw new AtlasExportCancelledException();

		/**
		 * Now we have to copy some native .dll and .so file as well:
		 */

		// The folder to copy to might not exist yet
		final File targetNativeDir = new File(targetJar.getParentFile(), ""
				+ LIBDIR + "/natives");
		targetNativeDir.mkdirs();

		/**
		 * We do not export all files found in a directory, but rather we only
		 * copy libraries mentioned in the geopublisher.properties files of GP
		 */

		// TODO i would guess, that we only copy them as .so and .ddl files, if
		// we export to DISK. FOr JWS we have a gp-natives.jar !?
		if (toDisk) {

			for (final String nat : getNatives()) {

				checkAbort();

				// if (progressWindow != null && progressWindow.isCanceled())
				// throw new AtlasExportCancelledException();

				if (nat.trim().equals("")) {
					continue;
				}

				final File destination = new File(targetNativeDir, nat);
				destination.getParentFile().mkdirs();

				URL fromURL = null;

				// This boolean tells us, if we download the JARs from the
				// webserver (GP started via JWS) or if we are copying them
				// from
				// a local directory.
				boolean fromLocal;
				try {

					// TODO check if it also works in JWS: fromURL =
					// getNativeLibraryURL(nat);

					final BasicService bs = (BasicService) ServiceManager
							.lookup("javax.jnlp.BasicService");

					String path = bs.getCodeBase().getFile();
					if (!path.endsWith("/")) {
						path = path + "/";
					}

					/**
					 * Normaly the natives are found in the ""+LIBDIR+"/natives"
					 * directory.
					 */
					String fileAndPath;
					fileAndPath = path + LIBDIR + "/natives/" + nat;

					fromURL = new URL(bs.getCodeBase().getProtocol(), bs
							.getCodeBase().getHost(), bs.getCodeBase()
							.getPort(), fileAndPath);

					fromURL.openConnection();

					fromLocal = false;
				} catch (final Exception e) {
					/**
					 * The exception means, that we have not been started via
					 * JWS. So we just copy this file from the local lib
					 * directory.
					 */
					fromLocal = true;
					// fromURL = new File(LIBDIR + "/natives/" + nat).toURI()
					// .toURL();

					fromURL = getNativeLibraryURL(nat);
					
				}
				if (fromURL == null) {
					System.err.println(nat+" not found in "+System.getProperty("java.library.path"));
					continue;
				}

				final String msg = GpUtil.R("Export.progressMsg.copy_lib_to_",
						nat, destination.toString());
				LOGGER.debug(msg + " (URL:" + fromURL.toString() + ")");
				info(msg);

				try {
					FileUtils.copyURLToFile(fromURL, destination);
				} catch (final Exception e) {
					final String errorMsg = GpUtil.R(
							"Export.errorMsg.error_copy_lib_to_", fromLocal,
							nat, fromURL, destination);
					LOGGER.warn(errorMsg, e);
					throw new AtlasExportException(errorMsg, e);
				}

			}
		} // toDisk only

		/************
		 * Exporting the .jar files now!
		 */
		final File targetLibDir = new File(targetJar.getParentFile(), LIBDIR);

		/**
		 * We do not export all libs found in a directory, but rather we only
		 * copy libraries mentioned in the .properties files of GP. The
		 * gp-natives.jar we add automatically.
		 */

		boolean packNotExistingErrorAlreadyShown = false;

		for (final String lib : getLibraries()) {

			checkAbort();

			// if (progressWindow != null && progressWindow.isCanceled())
			// throw new AtlasExportCancelledException();

			if (lib.trim().equals("")) {
				continue;
			}

			try {
				File destination = new File(targetLibDir, lib);
				destination.getParentFile().mkdirs();
				File destinationPackGz = new File(targetLibDir, lib
						+ ".pack.gz");

				URL fromURL = null;
				URL fromURLPackGZ = null;

				// This boolean tells us, if we download the JARs from the
				// webserver (GP started via JWS) or if we are copying them from
				// a local directory.
				boolean libsFromLocal;
				try {
					final BasicService bs = (BasicService) ServiceManager
							.lookup("javax.jnlp.BasicService");

					String path = bs.getCodeBase().getFile();
					if (!path.endsWith("/")) {
						path = path + "/";
					}

					/**
					 * Normally the libs are found in the LIBDIR directory. The
					 * "../gpcore.jar" is different!
					 */
					String fileAndPath;
					if (lib.equals("../gpcore.jar")) {
						fileAndPath = path + "gpcore.jar";
						destination = new File(targetLibDir.getParentFile(),
								"gpcore.jar");
						destinationPackGz = new File(targetLibDir
								.getParentFile(), "gpcore.jar.pack.gz");
					} else {
						fileAndPath = path + LIBDIR + "/" + lib;
					}

					fromURL = new URL(bs.getCodeBase().getProtocol(), bs
							.getCodeBase().getHost(), bs.getCodeBase()
							.getPort(), fileAndPath);
					fromURLPackGZ = new URL(bs.getCodeBase().getProtocol(), bs
							.getCodeBase().getHost(), bs.getCodeBase()
							.getPort(), fileAndPath + ".pack.gz");

					libsFromLocal = false;
				} catch (final javax.jnlp.UnavailableServiceException e) {

					/**
					 * The exception means, that we have not been started via
					 * JWS. So we just copy this file from the local lib
					 * directory.
					 */
					libsFromLocal = true;
					if (lib.equals("../gpcore.jar")) {
						fromURL = DataUtilities.fileToURL(new File(
								"./gpcore.jar"));
						fromURLPackGZ = new File("./gpcore.jar.pack.gz")
								.toURI().toURL();
						destinationPackGz = new File(targetLibDir
								.getParentFile(), "gpcore.jar.pack.gz");
					} else {

						fromURL = getJarUrl(lib);
						if (fromURL == null)
							System.out.println("null");
						fromURLPackGZ = fromURL != null ? DataUtilities
								.changeUrlExt(fromURL, "jar.pack.gz") : null;

						// fromURL = DataUtilities.fileToURL(new File(LIBDIR +
						// "/" + lib));
						// fromURLPackGZ = DataUtilities.fileToURL(new
						// File(LIBDIR + "/" + lib
						// + ".pack.gz"));
					}
				}
				if (fromURL == null) {
					System.err.println(lib+" not found in "+System.getProperty("java.class.path"));
					continue;
				}

				final String msg = GpUtil.R("Export.progressMsg.copy_lib_to_",
						lib, destination.toString());

				LOGGER.debug(msg + " (URL:" + fromURL.toString() + ")");
				info(msg);

				try {
					FileUtils.copyURLToFile(fromURL, destination);

					/*
					 * Copy the .pack.gz files only if we export to JWS. Do not
					 * export .pack.gz for gp-natives.jar, because the extra
					 * compression doesn't work for that kind of file.
					 */
					if (toJws && !lib.equals("gp-natives.jar")) {
						try {
							FileUtils.copyURLToFile(fromURLPackGZ,
									destinationPackGz);
						} catch (final Exception e) {
							if (packNotExistingErrorAlreadyShown == false) {
//								ExceptionDialog.show(null, new AtlasException(
//										GpUtil.R("Export.Error.Pack200"), e));
							}
							// Do not show this warning again next time.
							packNotExistingErrorAlreadyShown = true;
						}
					} // toJws only

				} catch (final Exception e) {
					final String errorMsg = GpUtil.R(
							"Export.errorMsg.error_copy_lib_to_",
							libsFromLocal, lib, fromURL, destination);
					LOGGER.warn(errorMsg, e);
					throw new AtlasExportException(errorMsg, e);
				}

			} catch (final MalformedURLException e) {
				final String errorMsg = "MalformedURLException during export of "
						+ lib;
				LOGGER.warn(errorMsg, e);
				throw new AtlasExportException(errorMsg, e);
			}

		}

	}

	private URL getNativeLibraryURL(String nativeName) {
		// Clean any path or ./ stuff
		nativeName = new File(nativeName).getName();

		String[] st = System.getProperty("java.library.path").split(":");
		for (String t : st) {
			System.err.println("looking in " + t + " for " + nativeName);
			File file = new File(t + "/" + nativeName);
			if (file.exists()) {
				return DataUtilities.fileToURL(file);
			}
		}

		return null;
	}

	/**
	 */
	private URL getJarUrl(String jarName) {
		if (jarUrlsFromClassPath == null) {
			jarUrlsFromClassPath = new HashMap<String, URL>();
			String[] st = System.getProperty("java.class.path").split(":");
			for (String t : st) {
				if (!t.endsWith("jar") && !t.endsWith("pack.gz")
						&& !t.endsWith("zip")) {
					LOGGER.warn(t + " was not a valid entry on the classpath.");
					continue;
				}

				File file = new File(t);
				if (!file.exists()) {
					LOGGER.warn(t + " was not a valid entry on the classpath.");
					continue;
				}
				jarUrlsFromClassPath.put(file.getName(), DataUtilities
						.fileToURL(file));
			}

		}

		// Clean any path or ./ stuff
		jarName = new File(jarName).getName();

		return jarUrlsFromClassPath.get(jarName);
	}

	/**
	 * Copies the JRE we are running on into the DISK/jre folder. On Linux this
	 * will be skipped and a message will be shown instead.
	 * 
	 * @param diskDir
	 *            The DISK export folder.
	 * @throws IOException
	 */
	private void copyJRE(final File diskDir) throws IOException {
		if (AVUtil.getOSType() == OSfamiliy.windows) {
			info("Copying JRE"); // i8n
			FileUtils.copyDirectory(new File(System.getProperty("java.home")),
					new File(targetDirDISK, "jre"));
		}
	}

	private void copyLicenseHtml(final AtlasConfigEditable ace,
			final File targetJar) {
		// Copy the license.html to the main directory
		try {

			final File destLicense = new File(targetJar.getParentFile(),
					"license.html");
			FileUtils.copyURLToFile(GpUtil.class.getResource(
					AtlasConfig.LICENSEHTML_RESOURCE_NAME), destLicense);
		} catch (final Exception e) {
			ExceptionDialog
					.show(
							null,
							new AtlasException(
									"Non-fatal error while copying the licence.txt:"
											+ e.getMessage()
											+ "\n   "
											+ GpUtil.class
													.getResource(AtlasConfig.LICENSEHTML_RESOURCE_NAME),
									e));
		}
	}

	/**
	 * Files next to atlas.gpa will be copied to the folders without putting
	 * them in a JAR. This can be usefull for exmaple for PDF files that should
	 * be referencable from within the atlas, but also reside uncompressed on
	 * the CD root directory.
	 * 
	 * @throws IOException
	 */
	private void copyUncompressFiles() throws IOException {

		final File[] filesToCopy = ace.getAtlasDir().listFiles(
				filterForRootLevelFiles);

		for (final File f : filesToCopy) {
			if (f.isDirectory()) {
				continue;
			}
			info("copy root-level file " + f.getName());
			if (toDisk) {
				FileUtils.copyFileToDirectory(f, targetDirDISK);
			}
			if (toJws) {
				FileUtils.copyFileToDirectory(f, targetDirJWS);
			}
		}

	}

	/**
	 * Creates a JAR-file that contains all files of one {@link DpEntry}. The
	 * JAR will be automatically signed if {@link #signJARs} is
	 * <code>true</code>. The JARs are not indexed, as indexing doesn't make
	 * sense for a single jar.
	 * <p>
	 * Exception: The info HTML pages attached to {@link Map}s are stored in the
	 * <ode>AVJAR_FILENAME</code> JAR.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 * @param dpe
	 *            The {@link DpEntry} to export.
	 * 
	 * @throws AtlasExportException
	 * @throws IOException
	 * @throws AtlasCancelException
	 */
	public File createJarFromDpe(final DpEntry dpe)
			throws AtlasExportException, IOException, AtlasCancelException {

		checkAbort();

		info(dpe.getType().getLine1() + ": " + dpe.getTitle().toString());
	
		/**
		 * Setting up a new JAR file
		 */
		// Setting up da JAR File for this DatapoolEntry
		final File newJar = new File(tempDir, dpe.getId() + ".jar");
		LOGGER.debug("Exportig to JAR " + newJar.getName() + "    in "
				+ tempDir);
		final BufferedOutputStream bo = new BufferedOutputStream(
				new FileOutputStream(newJar.getAbsolutePath()));
		final JarOutputStream jo = new JarOutputStream(bo);

		/**
		 * Adding all the files as JarEntrys
		 */
		final String absolutePath = ace.getAtlasDir().getAbsolutePath();
		final String relpath = "ad" + File.separator + "data" + File.separator
				+ dpe.getDataDirname();

		final Collection<File> listFiles = FileUtils.listFiles(new File(
				absolutePath, relpath), GpUtil.BlacklistesFilesFilter,
				GpUtil.BlacklistedFoldersFilter);
		for (final File intoJarSource : listFiles) {

			String act = intoJarSource.getPath();
			final BufferedInputStream bi = new BufferedInputStream(
					new FileInputStream(act));
			// act is now an absolute pathname. We need a relative pathname
			// starting with ad/....
			act = act.substring(absolutePath.length() + 1);
			act = act.replace("\\", "/");
			final JarEntry je = new JarEntry(act);
			jo.putNextEntry(je);

			// Copy the data byte by byte...
			final byte[] buf = new byte[1024];
			int anz;
			while ((anz = bi.read(buf)) != -1) {
				jo.write(buf, 0, anz);
			}
			bi.close();
		}
		jo.close();
		bo.close();

		jarSign(newJar);

		return newJar;
	}

	/**
	 * Creates an <code>atlasViewer.jnlp</code> file in the same folder, as the
	 * targetJar.
	 * 
	 * @param owner
	 *            GUI Owner
	 * 
	 * @param targetJar
	 * @param Codebase
	 *            where the JWS will be running
	 * 
	 * @throws AtlasFatalException
	 */
	private void createJNLP(final AtlasConfigEditable ace,
			final File targetJar, String codebase) throws AtlasExportException {

		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			Document document = null;
			document = factory.newDocumentBuilder().newDocument();

			final Element jnlp = document.createElement("jnlp");

			if (!codebase.endsWith("/")) {
				codebase += "/";
			}
			jnlp.setAttribute("codebase", codebase);

			jnlp.setAttribute("href", codebase + JNLP_FILENAME);

			final List<String> languages = ace.getLanguages();
			languages.add(""); // Adding a pseudo Language... (has to be
			// removed again after the loop)

			for (String langID : languages) {
				final Element info = document.createElement("information");

				// should result in locale="de", or in no locale attribute, when
				// the "" langID is in the loop
				if (!langID.equals("")) {
					info.setAttribute("locale", langID.toLowerCase());
				} else {
					// Creating the default infromation tag.
					// If english exists, we use it as default
					// otherwise we use the first language
					if (languages.contains("en")) {
						langID = "en";
					} else {
						langID = languages.get(0);
					}
				}

				final Element title = document.createElement("title");
				final String titleText = ace.getTitle().get(langID);
				if (titleText == null)
					throw new AtlasExportException(
							"Title field has to be set in " + langID);
				title.appendChild(document.createTextNode(titleText));
				info.appendChild(title);

				final Element desc = document.createElement("description");

				final String descText = ace.getDesc().get(langID);
				if (descText == null)
					throw new AtlasExportException(
							"Desc field has to be set in " + langID);
				desc.appendChild(document.createTextNode(descText));
				info.appendChild(desc);

				// Allow offline
				final Element offline = document
						.createElement("offline-allowed");
				info.appendChild(offline);

				final Element vendor = document.createElement("vendor");
				final String vendorText = ace.getCreator().get(langID);
				if (vendorText == null)
					throw new AtlasExportException(
							"Vendor field has to be set in " + langID);
				vendor.appendChild(document.createTextNode(vendorText));
				info.appendChild(vendor);

				// **************************************************************
				// Create icon tags for icon and splashscreen
				// **************************************************************
				final Element icon = document.createElement("icon");
				icon.setAttribute("href", "icon.gif");
				info.appendChild(icon);

				final Element slpash = document.createElement("icon");
				slpash.setAttribute("kind", "splash");
				slpash.setAttribute("href", "splashscreen.png");
				info.appendChild(slpash);

				final Element shortcut = document.createElement("shortcut");
				shortcut.setAttribute("online", "false");
				shortcut.appendChild(document.createElement("desktop"));
				final Element menu = document.createElement("menu");
				menu.setAttribute("submenu", titleText == null ? ace.getTitle()
						.toString() : titleText);
				shortcut.appendChild(menu);
				info.appendChild(shortcut);

				jnlp.appendChild(info);
			}

			languages.remove("");
			// ResourceLoaderManager resLoMan = AtlasConfig.getResLoMan();
			// Removing the pseudo / aka default Language...

			// ******************************************************************
			// copy the license.html, icon and splashscreen to the target root
			// folder
			// ******************************************************************

			/**
			 * If no splashscreen.png exists in ad, do nothing. If a
			 * splashscreen exists, we have to copy it to the root folder for
			 * the JNLP to us it.
			 */
			final URL splashscreenURL = ace
					.getResource(AtlasConfig.SPLASHSCREEN_RESOURCE_NAME);
			if (splashscreenURL != null) {
				FileUtils.copyURLToFile(splashscreenURL, new File(targetJar
						.getParentFile(), "splashscreen.png"));
			}

			// ******************************************************************
			// security tag
			// ******************************************************************
			final Element security = document.createElement("security");
			final Element allPerm = document.createElement("all-permissions");
			security.appendChild(allPerm);
			jnlp.appendChild(security);

			// ******************************************************************
			// <update check="always" policy="always"/>
			// ******************************************************************
			final Element update = document.createElement("update");
			update.setAttribute("check", "timeout");
			update.setAttribute("policy", "prompt-update");
			jnlp.appendChild(update);

			// Ressources
			final Element resources = document.createElement("resources");

			// ******************************************************************
			// Set the property to allow pack200 compression for the .jar files
			// See
			// http://java.sun.com/javase/downloads/ea/6u10/
			// newJavaSystemProperties.jsp
			// <property name="jnlp.packEnabled" value="true"/>
			// ******************************************************************

			final Element propertyPack200Element = document
					.createElement("property");
			propertyPack200Element.setAttribute("name", "jnlp.packEnabled");
			propertyPack200Element.setAttribute("value", "true");
			resources.appendChild(propertyPack200Element);

			Element aResource; // will be reused
			// ******************************************************************
			// The version and parameters to start the AtlasViwer with
			// The amount of max HEAP is read from the .properties
			// ******************************************************************
			aResource = document.createElement("java");
			aResource.setAttribute("version", GPProps.get(
					Keys.MinimumJavaVersion, "1.6.0_14")
					+ "+");
			aResource.setAttribute("href",
					"http://java.sun.com/products/autodl/j2se");
			aResource.setAttribute("java-vm-args", "-Xmx"
					+ GPProps.getInt(GPProps.Keys.startJVMWithXmx, 256) + "m"
					+ " -Dfile.encoding=UTF-8");
			resources.appendChild(aResource);

			aResource = document.createElement("jar");
			aResource.setAttribute("href", targetJar.getName());
			aResource.setAttribute("part", "main");
			aResource.setAttribute("download", "eager");

			aResource.setAttribute("size", String.valueOf(targetJar.length()));

			resources.appendChild(aResource);

			for (final String libName : getLibraries()) {
				if (libName.trim().equals("")) {
					continue;
				}
				aResource = document.createElement("jar");

				String libNameChecked;
				if (libName.equals("../gpcore.jar")) {
					libNameChecked = "gpcore.jar";
					aResource.setAttribute("main", "true");
				} else {
					libNameChecked = LIBDIR + "/" + libName;
				}

				aResource.setAttribute("href", libNameChecked);
				aResource.setAttribute("part", "main");
				aResource.setAttribute("download", "eager");

				resources.appendChild(aResource);
			}

			// PARTS from the Datapool!
			/**
			 * <jar href="vector_admin_boundary_benin_utm476999133.jar"
			 * part="vector_admin_boundary_benin_utm476999133" download="lazy"
			 * size="9674000"/> <package
			 * name="atlas.vector_admin_boundary_benin_utm476999133.*"
			 * part="vector_admin_boundary_benin_utm476999133"
			 * recursive="true"/>
			 * 
			 * <jar href="raster_luclass187299855.jar"
			 * part="raster_luclass187299855" download="lazy" size="22000000"/>
			 * <package name="atlas.raster_luclass187299855.*"
			 * part="raster_luclass187299855" recursive="true"/>
			 */

			/*******************************************************************
			 * Creating many little JARs for every DatapoolEntry (that is used)
			 * 
			 * Normally all resources are downloaded lazy, BUT if they are part
			 * of the default/startUp map they will be downloaded directly
			 */
			final String startMapID = ace.getMapPool().getStartMapID();
			if (ace.getMapPool().size() == 0)
				throw new IllegalArgumentException(
						"This atlas contains no map!");

			Map startUpMap;
			if (startMapID == null) {
				/**
				 * If not default/startup map has been selected, use the first
				 * map that has been created.
				 */
				startUpMap = ace.getMapPool().get(0);
			} else {
				startUpMap = ace.getMapPool().get(startMapID);
			}

			for (final DpEntry dpe : ace.getDataPool().values()) {
				if (getUnusedDpes().contains(dpe)) {
					continue;
				}

				resources.appendChild(document.createComment("Datapoolentry "
						+ dpe.getTitle().toString()));

				// Every part needs one package tag, so that JWS knows what to
				// expect there...
				aResource = document.createElement("package");
				aResource.setAttribute("part", dpe.getId());
				aResource.setAttribute("name", "ad.data."
						+ dpe.getDataDirname() + ".*");
				// aResource.setAttribute("name", "skrueger.atlas.*");
				aResource.setAttribute("recursive", "true");
				resources.appendChild(aResource);

				aResource = document.createElement("jar");
				aResource.setAttribute("href", dpe.getId() + ".jar");
				aResource.setAttribute("part", dpe.getId());

				/**
				 * Determine is this Dpe is part of the "first map".
				 */
				boolean partIsPartOfFirstMap = false;

				if (startUpMap != null && startUpMap.getLayers().size() > 0) {
					for (final DpRef ref : startUpMap.getLayers()) {
						final String targetId = ref.getTargetId();
						if (dpe.getId().equals(targetId)) {
							// if (targetId != null &&
							// targetId.equals(dpe.getId())) {
							partIsPartOfFirstMap = true;
							break;
						}
					}
				}

				aResource.setAttribute("download",
						partIsPartOfFirstMap ? "eager" : "lazy");

				resources.appendChild(aResource);
			}

			jnlp.appendChild(resources);

			// <application-desc main-class="skrueger.atlas.AtlasViewer"/>
			final Element applDesc = document.createElement("application-desc");
			applDesc.setAttribute("main-class", "skrueger.atlas.AtlasViewer");
			jnlp.appendChild(applDesc);

			// XML Exporting beginns here

			// Prepare the output file
			final File jnlpFile = new File(targetJar.getParent(), JNLP_FILENAME);
			jnlpFile.delete();
			jnlpFile.createNewFile();

			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					new FileOutputStream(jnlpFile), "utf-8");
			try {
				final Result result = new StreamResult(outputStreamWriter);

				// with indenting to make it more human-readable
				final TransformerFactory tf = TransformerFactory.newInstance();

				final Transformer xformer = tf.newTransformer();
				xformer.setOutputProperty(OutputKeys.INDENT, "yes");
				xformer.setOutputProperty(
						"{http://xml.apache.org/xalan}indent-amount", "2");

				// Write the DOM document to the file
				final Source source = new DOMSource(jnlp);
				xformer.transform(source, result);
			} finally {
				outputStreamWriter.flush();
				outputStreamWriter.close();
			}

		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new AtlasExportException("Creating " + JNLP_FILENAME
					+ " JNLP file failed!\n" + e.getMessage());
		}

	}

	/**
	 * Call JSmooth to create an .exe file
	 * 
	 * @param targetDirDISK2
	 *            The folder where the exported disk version of the atlas
	 *            exists.
	 * @throws Exception
	 */
	public void createJSmooth(final File atlasDir) throws Exception {
		info("Creating JSmooth atlas.exe"); // i8n

		/**
		 * Copy JSmooth skeleton stull to DISK/autodownload-wrapper/*
		 */
		final File jsmoothSkelDir = new File(atlasDir, "autodownload-wrapper");
		jsmoothSkelDir.mkdirs();

		final File jsmoothExeFile = new File(jsmoothSkelDir, "autodownload.exe");
		FileUtils.copyURLToFile(GpUtil.class.getResource(AtlasConfig.JSMOOTH_SKEL_AD_RESOURCE1),
				jsmoothExeFile);
		final File jsmoothSkelFile = new File(jsmoothSkelDir,
				"autodownload.skel");
		FileUtils.copyURLToFile(GpUtil.class
				.getResource(AtlasConfig.JSMOOTH_SKEL_AD_RESOURCE2),
				jsmoothSkelFile);

		/**
		 * atlas.jsmooth is positioned in DISK/atlas.jsmooth
		 */
		final File destinationProjectFile = new File(atlasDir, "atlas.jsmooth");
		FileUtils.copyURLToFile(GpUtil.class
				.getResource(AtlasConfig.JSMOOTH_PROJEKT_RESOURCE),
				destinationProjectFile);
		try {

			/**
			 * Start the creation of .EXE
			 */

			final JSmoothModelBean model = JSmoothModelPersistency
					.load(destinationProjectFile);
			final File basedir = destinationProjectFile.getParentFile();
			final File skelbase = basedir;

			final SkeletonList skelList = new SkeletonList(skelbase);

			final File out = new File(basedir, model.getExecutableName());

			final SkeletonBean skel = skelList.getSkeleton(model
					.getSkeletonName());
			final File skelroot = skelList.getDirectory(skel);

			final ExeCompiler compiler = new ExeCompiler();
			compiler.compile(skelroot, skel, basedir, model, out);

		} finally {

			System.gc();
			try {
				/**
				 * Cleanup JSmooth stuff. We saw some strange
				 * "can't delete file" exceptions on WINDOWS, so we use the
				 * canonical name and retry if it fails.
				 * http://jira.codehaus.org/secure/attachment/27455/
				 * MCLEAN-file-management.patch
				 */
				if (!jsmoothExeFile.getCanonicalFile().delete()) {
					jsmoothExeFile.getCanonicalFile().delete();
				}
				if (!jsmoothSkelFile.getCanonicalFile().delete()) {
					jsmoothSkelFile.getCanonicalFile().delete();
				}
				if (!jsmoothSkelDir.getCanonicalFile().delete()) {
					jsmoothSkelDir.getCanonicalFile().delete();
				}
				new File(atlasDir, "icon.gif").getCanonicalFile().delete();
				destinationProjectFile.delete();
			} catch (final IOException e) {
				LOGGER
						.warn(
								"Error during cleanup. Some unused files might be left in you DISK directory.",
								e);
				throw new AtlasExportException(
						// i8n
						"Error during cleanup. Some unused files might be left in you DISK directory.",
						e);
			}
		}

	}

	/**
	 * This approach could be problematic if there are more than one exports
	 * running.
	 */
	public void deleteOldTempExportDirs() {

		// Create a temporary directory to put stuff into before we split it
		// into two directories

		/**
		 * Delete any old/parallel export directories
		 */
		final IOFileFilter oldDirs = FileFilterUtils
				.makeDirectoryOnly(FileFilterUtils
						.prefixFileFilter(AVUtil.ATLAS_TEMP_FILE_ID + "export"));
		final String[] list = IOUtil.getTempDir().list(oldDirs);
		for (final String deleteOldTempDirName : list) {
			try {
				FileUtils.deleteDirectory(new File(IOUtil.getTempDir(),
						deleteOldTempDirName));
			} catch (final Exception e) {
				ExceptionDialog.show(null, e);
			}
		}
	}

	/**
	 * Exports the given {@link AtlasConfig} to two folders: One for
	 * CD/USB-STick, one to be run via JavaWebStart
	 * 
	 * @throws Exception
	 * 
	 */
	public void export(final ResultProgressHandle progress) throws Exception {

		this.progress = progress;

		totalSteps = 10;

		/** One for every root-level file **/
		totalSteps += ace.getAtlasDir().listFiles(filterForRootLevelFiles).length;

		/**
		 * One for ever DPEntry
		 */
		totalSteps += ace.getDataPool().size() - getUnusedDpes().size();

		/**
		 * One for every Library and every Native libs
		 */
		totalSteps += getLibraries().length;

		if (toDisk) {
			/** JSmooth */
			totalSteps++;
			totalSteps += getNatives().length;
		}

		if (toJws) {
			/** JNLP creation */
			totalSteps++;
		}



		info(GpUtil.R("ExportDialog.processWindowTitle.Exporting"));


		// Try catch to always delete the temp folder
		try {

			final File targetJar = new File(tempDir, ARJAR_FILENAME);
			LOGGER.debug("Export of " + ace.getTitle() + " to " + targetJar);

			// **********************************************************************
			// Adding important stuff to the av_resources.jar
			// atlas.xml
			// html folder
			// av.properties
			// splashscreen.png
			//
			// NOT AtlasML.xsd - it's provided from gpcore.jar via the internal
			// webserver
			//
			// NOT av_log4j.xml - it's provided from gpcore.jar via the internal
			// webserver
			// **********************************************************************
			try {
				info("Creating " + ARJAR_FILENAME); // 1st call to info

				addToJar(targetJar, ace.getAtlasDir(), ace.getAd().getName()
						+ "/" + AtlasConfig.HTML_DIRNAME);

				// addToJar(targetJar, ace.getAtlasDir(), ace.getAd().getName()
				// + "/" + AtlasConfig.ATLAS_XML_FILENAME);
				addAtlasXMLToJar(targetJar, ace);

				addToJar(targetJar, ace.getAtlasDir(), ace.getAd().getName()
						+ "/" + ace.getImagesDir().getName());

				ace.getProperties().save(
						new File(ace.getAtlasDir(),
								AVProps.PROPERTIESFILE_RESOURCE_NAME));
				addToJar(targetJar, ace.getAtlasDir(),
						AVProps.PROPERTIESFILE_RESOURCE_NAME);

				/**
				 * Look for a user-defined splashscreen. If it doesn't exist,
				 * ask the user if he wants to use the default one.
				 */
				if (ace.getResource(AtlasConfig.SPLASHSCREEN_RESOURCE_NAME) == null) {
					// final int useDefaultSplashConfirmDialog = JOptionPane
					// .showConfirmDialog(
					// null,
					// AtlasCreator
					// .R("Export.NoSplashscreenExists_DoYouWantToUseTheDefault"),
					// null, JOptionPane.YES_NO_OPTION);
					// if (useDefaultSplashConfirmDialog ==
					// JOptionPane.YES_OPTION) {
					FileUtils
							.copyURLToFile(
									GpUtil.class
											.getClassLoader()
											.getResource(
													AtlasConfig.SPLASHSCREEN_RESOURCE_NAME_FALLBACK),
									new File(
											ace.getAtlasDir(),
											AtlasConfig.SPLASHSCREEN_RESOURCE_NAME));
					// JOptionPane
					// .showMessageDialog(
					// null,
					// AtlasCreator
					// .R(
					// "Export.NoSplashscreenExists_TheDefaultHasBeenCopied",
					// new File(
					// ace.getAtlasDir(),
					// AtlasConfig.SPLASHSCREEN_RESOURCE_NAME)
					// .getAbsolutePath()));
					addToJar(targetJar, ace.getAtlasDir(),
							AtlasConfig.SPLASHSCREEN_RESOURCE_NAME);
					// }
				} else {
					addToJar(targetJar, ace.getAtlasDir(),
							AtlasConfig.SPLASHSCREEN_RESOURCE_NAME);
				}

				info("Creating " + ARJAR_FILENAME); // 4th call to info

				/**
				 * Look for a user-defined icon.gif. If not available, copy the
				 * default icon to where we expect the user icon and then add it
				 * to the jar.
				 */
				URL iconURL = ace
						.getResource(AtlasConfig.JWSICON_RESOURCE_NAME);
				if (iconURL == null) {
					iconURL = GpUtil.class.getClassLoader().getResource(
							AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK);
					FileUtils.copyURLToFile(iconURL, new File(
							ace.getAtlasDir(),
							AtlasConfig.JWSICON_RESOURCE_NAME));
				}
				addToJar(targetJar, ace.getAtlasDir(),
						AtlasConfig.JWSICON_RESOURCE_NAME);

			} catch (final IOException e) {
				throw new AtlasExportException("can't add basic data to "
						+ targetJar.getAbsolutePath(), e);
			}

			// **********************************************************************
			// Adding optional stuff to the av_resource.jar
			// map_logo.png
			// **********************************************************************
			try {
				LOGGER.debug("Adding optional stuff to " + targetJar.getName());
				addToJar(targetJar, ace.getAtlasDir(),
						AtlasConfig.MAPICON_RESOURCE_NAME);
			} catch (final Exception e1) {
				final String msg = GpUtil.R("Export.NoMapiconExists_NoProblem",
						ace.getAtlasDir() + AtlasConfig.MAPICON_RESOURCE_NAME);
				JOptionPane.showMessageDialog(null, msg);
			}

			/*
			 * Exporting the defaultcrs.prj if it exists
			 */
			final File defaultCrsFile = new File(ace.getAd(),
					AtlasConfig.DEFAULTCRS_FILENAME);
			if (defaultCrsFile.exists() && defaultCrsFile.length() > 2) {
				addToJar(targetJar, ace.getAtlasDir(), "ad/"
						+ AtlasConfig.DEFAULTCRS_FILENAME);
			} else {
				LOGGER
						.info("No exporting defaultcrs.prj beacuse it doesn't exist or is too small");
			}

			copyAndSignLibs(targetJar, ace);

			String[] listOfDataJarNames = new String[] {};
			// Creating a JAR for every DpEntry
			LOGGER.debug("Creating a JAR for every DpEntry used");
			for (final DpEntry dpe : ace.getDataPool().values()) {
				if (getUnusedDpes().contains(dpe)) {
					continue;
				}
				final File newJar = createJarFromDpe(dpe);
				listOfDataJarNames = LangUtil.extendArray(listOfDataJarNames,
						newJar.getAbsolutePath());
			}

			checkAbort();

			//
			// /**
			// * Before the atlas_resources.jar is signed,
			// * <ul>
			// * <li>create an index</li>
			// * <li>add classpath elemnt to manifest</li>
			// */
			//
			// /**
			// * now we create the index
			// */
			//
			// Main jartool = new Main(System.out, System.err, "jar");
			// String[] args = new String[] { "i", targetJar.getAbsolutePath()
			// };
			// args = LangUtil.extendArray(args, listOfDataJarNames);
			// LOGGER.debug("Calling jartool with " + args);
			// if (!jartool.run(args))
			// throw new AtlasExportException("unable to add index to "
			// + targetJar);
			// /**
			// * now we add the correct manifest
			// */
			// jartool = new Main(System.out, System.err, "jar");
			// args = new String[] { "ufm", targetJar.getAbsolutePath(),
			// getManifestFile(true).getAbsolutePath() };
			// LOGGER.debug("Calling jartool with " + args);
			// if (!jartool.run(args))
			// throw new AtlasExportException(
			// "unable to add correct manifest to " + targetJar);

			LOGGER.debug("Finally signing the ataslResouce JAR...");
			jarSign(targetJar);

			/**
			 * The icon.gif is needed for DISK and JWS. (DISK will put it into
			 * the atlas.exe and delete it afterwards)
			 */
			URL iconURL = ace.getResource(AtlasConfig.JWSICON_RESOURCE_NAME);
			if (!AVUtil.exists(iconURL)) {
				// LOGGER
				// .info("No user-defined icon provided. Using the default one.");
				iconURL = GpUtil.class.getClassLoader().getResource(
						AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK);
			}
			FileUtils.copyURLToFile(iconURL, new File(
					targetJar.getParentFile(), "icon.gif"));

			/*
			 * Creating the JNLP...
			 */
			if (toJws) {
				LOGGER.debug("Creating JNLP...");
				createJNLP(ace, targetJar, GPProps.get(GPProps.Keys.jnlpURL,
						"http://localhost/atlas"));

				createIndexHTML(ace, targetJar);
			}

			/*
			 * Creating start scripts etc...
			 */
			if (toDisk) {
				createAuxiliaryDISKFiles(ace, targetJar);
			}

			createReadmeTXT(ace, targetJar);
			copyLicenseHtml(ace, targetJar);

			splitTempDir();

			if (toDisk) {
				createJSmooth(targetDirDISK);
			}

			if (toDisk && copyJRE) {
				copyJRE(targetDirDISK);
			}

			// Files next to atlas.gpa will be copied to the folders without
			// putting them in a JAR. This can be usefull for exmaple for PDF
			// files that should be referencable from within the atlas, but also
			// reside uncompressed on the CD root directory.
			copyUncompressFiles();

		} catch (final AtlasCancelException cancel) {
			// // In that case we delete the target directory.
			info(GpUtil.R("Export.Cancelled.Msg"));
			if (toDisk) {
				FileUtils.deleteDirectory(targetDirDISK);
			}
			if (toJws) {
				FileUtils.deleteDirectory(targetDirJWS);
			}
			throw cancel;
		} finally {
			// Whatever happened, we have to delete the temp dir

			info(GpUtil.R("Export.Finally.Cleanup.Msg"));

			deleteOldTempExportDirs();
		}
	}

	private String[] getLibraries() {
		// final String libsLine = "gp-natives.jar "
		// + GPProps.get(Keys.ClassPathLibs);
		// LOGGER
		// .debug("These dependencies have been set in the .properties file (gp-natives.jar has been added automatically): "
		// + libsLine);
		// return libsLine.split(" ");

		// String pathToManifest =
		// AtlasViewerGUI.class.getPackage().toString().replace(".", "/") +
		// "/META-INF/MANIFEST.MF";
		// try {
		//			
		// Manifest manifest = new Manifest(new
		// URL(pathToManifest).openStream());
		// System.out.println(manifest);
		// Attributes attributes = manifest.getAttributes("Classpath");
		// String[] libs = attributes.toString().split(" ");
		// return libs;
		// } catch (MalformedURLException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// return new String[] {};

		Properties p = new Properties();
		String proeprtiesName = "/atlasdependencies.properties";
		try {
			p.load(GpUtil.class.getResource(proeprtiesName).openStream());
			String atlasDependecies = p.getProperty("classpath");

			// remove maven made :.classes entries
			atlasDependecies = atlasDependecies.replaceAll("\\./classes", "")
					.replaceAll("::", ":");

			return atlasDependecies.split(":");
		} catch (IOException e) {
			throw new AtlasExportException(proeprtiesName + " not found!", e);
		}
	}

	/**
	 * @return A {@link Manifest} for the av_resources.jar.
	 * 
	 */
	private Manifest getManifest(final boolean withCP) {
		final Manifest manifest = new Manifest();
		final Attributes mainAtts = manifest.getMainAttributes();

		mainAtts.put(Attributes.Name.MANIFEST_VERSION, "1.0");

		if (withCP) {
			String classpathString = "gpcore.jar ";

			// ******************************************************************
			// Adding the DatapoolEntries
			// ******************************************************************
			for (final DpEntry dpe : ace.getDataPool().values()) {
				if (getUnusedDpes().contains(dpe)) {
					continue;
				}
				classpathString += dpe.getId() + ".jar ";
			}
			mainAtts.put(Attributes.Name.CLASS_PATH, classpathString);
		}

		mainAtts.put(Attributes.Name.MAIN_CLASS, "skrueger.atlas.AtlasViewer");
		mainAtts.put(Attributes.Name.SPECIFICATION_TITLE, "Atlas Viewer");
		mainAtts.put(Attributes.Name.SPECIFICATION_VENDOR,
				"Stefan Alfons Krüger - www.wikisquare.de");
		mainAtts.put(Attributes.Name.IMPLEMENTATION_TITLE, ace.getTitle()
				.toString());
		mainAtts.put(new Name("SplashScreen-Image"),
				AtlasConfig.SPLASHSCREEN_RESOURCE_NAME);

		return manifest;
	}

	/**
	 * 
	 * @return A {@link Manifest} for the av_resources.jar in form of a temporry
	 *         {@link File}.
	 */
	private File getManifestFile(final boolean withCP) throws IOException,
			FileNotFoundException {
		final File manifestTempFile = File.createTempFile(
				AVUtil.ATLAS_TEMP_FILE_ID, ".manifest");
		getManifest(withCP).write(new FileOutputStream(manifestTempFile));
		return manifestTempFile;
	}

	//
	// private File getIndexListFile() throws IOException {
	// final File indexTempFile = File.createTempFile(
	// AVUtil.ATLAS_TEMP_FILE_ID, ".manifest");
	//
	// FileWriter indexwriter = new FileWriter(indexTempFile);
	//
	// indexwriter.write("JarIndex-Version: 1.0\n\n");
	//
	// // ******************************************************************
	// // Adding the DatapoolEntries
	// // ******************************************************************
	// for (final DpEntry dpe : ace.getDataPool().values()) {
	// if (unusedDpes.contains(dpe))
	// continue;
	//
	// // name of the JAR:
	// indexwriter.write(dpe.getId() + ".jar\n");
	//
	// // The main packacge/folder contained:
	// indexwriter.write(ace.getAd().getName() + "/"
	// + ace.getDataDir().getName() + "/" + dpe.getId() + "\n");
	// indexwriter.write("\n");
	// }
	//
	// return indexTempFile;
	// }

	private String[] getNatives() {
		final String nativeLibsLine = GPProps.get(Keys.NativeLibs);
		LOGGER
				.debug("These native dependencies have been set in the .properties file: "
						+ nativeLibsLine);
		return nativeLibsLine.split(" ");
	}

	public List<DpEntry<? extends ChartStyle>> getUnusedDpes() {
		if (unusedDpes == null) {
			unusedDpes = ace.listNotReferencedInGroupTreeNorInAnyMap();

		}
		return unusedDpes;
	}

	/**
	 * Promote export progress to GUI.
	 */
	protected void info(final String msg) {
		if (progress != null) {

			if (currSteps < totalSteps) {
				currSteps++;
			} else {
				currSteps++;
				totalSteps = currSteps + 3;
				LOGGER.warn("overcounting " + currSteps + " to " + totalSteps);
			}
			progress.setProgress(msg, currSteps, totalSteps);
		} else {
			LOGGER.info(msg);
		}
	}

	/**
	 * Signs the Jar. keys and everything else is defined in the properties
	 * files
	 * 
	 * @param jarFile
	 *            The existing JAR file to be signed. It may not be changed
	 *            afterwards. PAK200 compression has to happen before.
	 * 
	 * @throws AtlasExportException
	 * 
	 *             http://java.sun.com/javase/6/docs/technotes/tools/windows/jarsigner
	 *             .html
	 *             http://java.sun.com/javase/6/docs/technotes/tools/windows
	 *             /keytool.html
	 */
	private void jarSign(final File jarFile) throws AtlasExportException {
		// if (progressWindow != null && progressWindow.isCanceled())
		// throw new AtlasExportCancelledException();

		AVUtil.checkThatWeAreNotOnEDT();

		// info("Signing JAR " + targetJar.getName());

		final List<String> command = new ArrayList<String>();

		command.add("-keystore");
		final String keyStoreName = "/export/export.keystore";
		final URL keyStoreURL = JarExportUtil.class.getResource(keyStoreName);

		if (keyStoreURL == null)
			throw new AtlasExportException(
					"The keystore for signing couldn't be found at "
							+ keyStoreName); 

		command.add(keyStoreURL.toString());
		command.add("-storepass");
		command.add(GPProps.get(Keys.signingkeystorePassword));
		command.add(jarFile.getAbsolutePath());
		command.add(GPProps.get(Keys.signingAlias));

		final String[] args = command.toArray(new String[command.size()]);

		// For debugging, we convert command to a string
		String signCmdLine = "";
		for (final String s : command) {
			signCmdLine += " " + s;
		}

		try {
			JarSigner.main(args);
			Log.debug("Signing " + jarFile + " OK.\n");
		} catch (final Throwable e) {
			LOGGER.error(e);
			throw new AtlasExportException("Exception while signing\n "
					+ jarFile + " with\n '" + signCmdLine + "'", e);
		}
	}

	/**
	 * The export first exports everything into one temp folder. Now we have to
	 * split it into two folders: ONe for JWS, one for DISK
	 * 
	 * @throws IOException
	 */
	private void splitTempDir() throws IOException {
		info("Moving files"); // i8n

		if (toDisk) {

			/**
			 * Exclusively for DISK
			 */
			FileUtils.moveFileToDirectory(new File(tempDir, "start.bat"),
					targetDirDISK, true);
			FileUtils.moveFileToDirectory(new File(tempDir, "start.sh"),
					targetDirDISK, true);
			FileUtils.moveFileToDirectory(new File(tempDir, "autorun.inf"),
					targetDirDISK, true);
			// Icon.gif is used (and deleted afterwards) by JSmooth
			FileUtils.copyFileToDirectory(new File(tempDir, "icon.gif"),
					targetDirDISK, true);

			/**
			 * Creates an empty jre directory.
			 */
			new File(targetDirDISK, "jre").mkdir();
		}

		if (toJws) {

			/**
			 * Exclusively for JWS
			 */
			FileUtils.moveFileToDirectory(new File(tempDir, "index.html"),
					targetDirJWS, true);
			FileUtils.moveFileToDirectory(new File(tempDir, JNLP_FILENAME),
					targetDirJWS, true);
			FileUtils.moveFileToDirectory(new File(tempDir, "icon.gif"),
					targetDirJWS, true);
			FileUtils.moveFileToDirectory(
					new File(tempDir, "splashscreen.png"), targetDirJWS, true);
		}

		/**
		 * For both
		 */
		final Collection<File> jars = FileUtils.listFiles(tempDir,
				new String[] { "jar", "jar.pack.gz", "so", "dll" }, true);
		for (final File jar : jars) {

			final String diffDir = jar.getAbsolutePath().substring(
					tempDir.getAbsolutePath().length() + 1);

			/**
			 * Copy files to DISK
			 */
			if (toDisk && !jar.getName().endsWith("pack.gz")) {
				final File targetSubDirDISK = new File(targetDirDISK, diffDir)
						.getParentFile();
				targetSubDirDISK.mkdirs();
				FileUtils.copyFileToDirectory(jar, targetSubDirDISK);
			}

			/**
			 * Copy files to JWS
			 */
			if (toJws) {
				final File targetSubDirJWS = new File(targetDirJWS, diffDir)
						.getParentFile();
				targetSubDirJWS.mkdirs();
				FileUtils.moveFileToDirectory(jar, targetSubDirJWS, true);
			}
		}

		if (toJws) {
			FileUtils.copyFileToDirectory(new File(tempDir, "README.TXT"),
					targetDirJWS);
			FileUtils.copyFileToDirectory(new File(tempDir, "license.html"),
					targetDirJWS);

			/**
			 * Copy the .htaccess from the deploy directory into the target
			 * directory
			 */
			{
				// command.add(ACProps.get(Keys.signingKeystore));
				final URL htaccessURL = JarExportUtil.class.getResource("/export/htaccess");
				FileUtils.copyURLToFile(htaccessURL, new File(targetDirJWS,
						".htaccess"));
			}
		}

		if (toDisk) {
			FileUtils.moveFileToDirectory(new File(tempDir, "license.html"),
					targetDirDISK, true);
			FileUtils.moveFileToDirectory(new File(tempDir, "README.TXT"),
					targetDirDISK, true);
		}

	}

}
