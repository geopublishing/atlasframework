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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.exceptions.AtlasFatalException;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.atlasViewer.swing.HTMLInfoLoboBrowser;
import org.geopublishing.geopublisher.AMLExporter;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPProps;
import org.geopublishing.geopublisher.GPProps.Keys;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.LoggerResultProgressHandle;
import org.geopublishing.geopublisher.exceptions.AtlasExportException;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geotools.data.DataUtilities;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sun.security.tools.JarSigner;
import sun.tools.jar.Main;
import de.schmitzm.io.FilterUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.versionnumber.ReleaseUtil;

/**
 * This class exports an {@link AtlasConfigEditable} into a DISK and/or JWS
 * folder. The DISK folder can be burned to CDROM and will autostart the Atlas
 * under windows. <br>
 * The exported DISK directory also contains a <code>atlas.exe</code> to launch
 * the atlas if autostart is disabled.<br>
 * The JWS folder may be served by any www. Linking to the <code>.jnlp</code>
 * file will start the atlas using JavaWebStart
 */
public class JarExportUtil extends AbstractAtlasExporter {
	static final String AUTORUN_INF = "autorun.inf";
	static final String START_SCRIPT_LINUX = "start.sh";
	static final String START_SCRIPT_MAC = "start.command";

	final static private Logger LOGGER = Logger.getLogger(JarExportUtil.class);

	/** Subfolder in the export atlas directory for the JWS version of the atlas **/
	public static final String JWS = "JWS";
	/**
	 * Subfolder in the export atlas directory for the DISK version of the atlas
	 **/
	public static final String DISK = "DISK";
	/** Location of a JSMOOTH resource describing how to create the atlas.exe **/
	public static final String JSMOOTH_PROJEKT_RESOURCE = "/export/jsmooth/atlas.jsmooth";
	/** Location of a JSMOOTH resource describing how to create the atlas.exe **/
	public static final String JSMOOTH_SKEL_AD_RESOURCE1 = "/export/jsmooth/autodownload-wrapper/autodownload.exe";
	/** Location of a JSMOOTH resource describing how to create the atlas.exe **/
	public static final String JSMOOTH_SKEL_AD_RESOURCE2 = "/export/jsmooth/autodownload-wrapper/autodownload.skel";
	/** Location of a JSMOOTH resource describing how to create the atlas.exe **/
	public static final String JSMOOTH_SKEL_AD_RESOURCE3 = "/export/jsmooth/autodownload-wrapper/customdownload.skel";

	/**
	 * This boolean tells us, whether we download the JARs from the webserver
	 * (GP started via JWS) or if we are copying them from any local directory
	 * (Started from ZIP or using the JWS cache).
	 */
	boolean libsFromLocal = true;

	/**
	 * The name of the JAR file which contains general atlas resources like
	 * atlas.xml, HTML-pages, etc…
	 */
	public final static String ARJAR_FILENAME = "atlas_resources.jar";

	/**
	 * If <code>true</code>, unused DPEs are not exported
	 */
	private boolean filterUnused = true;

	/**
	 * The filename of the JNLP file which can be used to start the atlas from
	 * the Internet.
	 */
	public static final String JNLP_FILENAME = "atlasViewer.jnlp";

	private static final String LIB_DIR = ".";

	/**
	 * When exporting in DISK mode, the whole application is put into a
	 * DISK_SUB_DIR and only austart.inf, start.sh and atlas.exe reside in the
	 * main directory.<br/>
	 * Attention: This folder-name is also hard-coded into
	 * <code>atlas.jsmooth</code>!
	 **/
	public static final String DISK_SUB_DIR = "atlasdata/";

	private static final String postfixJar = ".jar";

	/**
	 * UGLY: During the export, the location of the required JARs is determined
	 * by looking at the classpath string. When running from with Eclipse or a
	 * Maven-Multimodule project, some projects are not in the classpath as
	 * jars, but rather do their relative paths appear in the classpath-string.
	 * This is a list of jar-names -> maven-groupIds to find the needed JARs in
	 * the local maven repository. This on the other hand expects you to
	 * "mvn install" the JARs there whenever important changes happened to them.
	 */
	// TODO UGLY!
	private static String GPVERSION = "2.0-SNAPSHOT";
	public static final String SCHMITZM_JARNAME1 = "schmitzm-core-2.8-SNAPSHOT.jar";
	public static final String SCHMITZM_JARNAME2 = "schmitzm-gt-2.8-SNAPSHOT.jar";
	public static final String SCHMITZM_JARNAME3 = "schmitzm-jfree-gt-2.8-SNAPSHOT.jar";
	public static final String SCHMITZM_JARNAME4 = "schmitzm-jfree-2.8-SNAPSHOT.jar";
	public static final String SCHMITZM_JARNAME5 = "schmitzm-db-2.8-SNAPSHOT.jar";
	public static final String GSRCJ_JARNAME = "gsrcj-0.5.jar";
	public static final String GPSYNC_JARNAME = "gpsync-" + GPVERSION + ".jar";
	/**
	 * Filename of the gpcore jar
	 */
	public static final String GPCORE_JARNAME = "gpcore-" + GPVERSION
			+ postfixJar;
	/**
	 * Filename of the gpcore jar
	 */
	public static final String AVSWINGGUI_JARNAME = "avswinggui-" + GPVERSION
			+ postfixJar;

	/**
	 * Filename of the ascore jar
	 */
	public static final String ASCORE_JARNAME = "ascore-" + GPVERSION
			+ postfixJar;

	/**
	 * List of JARs that are all created from the one geopublihing.org POM file
	 * and therefore are not part of the dependencies.
	 */
	final static List<String> BASEJARS = new ArrayList<String>(
			Arrays.asList(new String[] { SCHMITZM_JARNAME1, SCHMITZM_JARNAME2,
					SCHMITZM_JARNAME3, SCHMITZM_JARNAME4, SCHMITZM_JARNAME5,
					GSRCJ_JARNAME, ASCORE_JARNAME, GPSYNC_JARNAME,
					GPCORE_JARNAME, AVSWINGGUI_JARNAME
			// , GPNATIVES_JARNAME
			}));

	/**
	 * Mainclass for the exprted atlas.
	 * "org.geopublishing.atlasViewer.swing.AtlasViewerGUI"
	 **/
	public static final String MAIN_CLASS = AtlasViewerGUI.class.getName();

	/**
	 * Resource location of the .htaccess file that is exported as part of every
	 * JWS atlas
	 **/
	static final String HTACCESS_RES_LOCATION = "/export/htaccess";

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

			// ******************************************************************
			// start.sh for Linux (the
			// ******************************************************************
			final File startSHFile = new File(targetJar.getParentFile(),
					START_SCRIPT_LINUX);
			FileWriter fileWriter = new FileWriter(startSHFile);
			final Integer xmx = GPProps.getInt(GPProps.Keys.startJVMWithXmx,
					256);
			fileWriter.write("# JAVA "
					+ GPProps.get(Keys.MinimumJavaVersion, "1.6.0_18+")
					+ " or higher is required! \n");
			fileWriter
					.write("# This will start the atlas with a maximum of "
							+ xmx
							+ "Mb of memory. Increase this number if you have lots of memory.\n");
			fileWriter.write("java -Xmx" + xmx
					+ "m -Dfile.encoding=UTF-8 -Djava.library.path="
					+ DISK_SUB_DIR + LIB_DIR + " -jar " + DISK_SUB_DIR + "/"
					+ targetJar.getName() + "\n");
			fileWriter.close();

			// ******************************************************************
			// Start.command for mac is a copy if start.sh
			// ******************************************************************
			final File startCOMMANDFile = new File(targetJar.getParentFile(),
					START_SCRIPT_MAC);
			FileUtils.copyFile(startSHFile, startCOMMANDFile);

		} catch (final IOException e) {
			throw new AtlasExportException("Error creating " + START_SCRIPT_MAC
					+ " or " + START_SCRIPT_LINUX, e);
		}

		try {
			// ******************************************************************
			// autorun.inf for windows [autorun]
			// ******************************************************************
			FileWriter fileWriter = new FileWriter(new File(
					targetJar.getParentFile(), AUTORUN_INF));
			fileWriter.write("[autorun]\n");
			fileWriter.write("icon=atlas.exe,1\n");
			fileWriter.write("open=atlas.exe\n");
			fileWriter.close();

		} catch (final IOException e) {
			throw new AtlasExportException("Error creating the " + AUTORUN_INF
					+ " file", e);
		}
	}

	/**
	 * Creates an index.html that allows to start the Atlas.
	 * 
	 * @param directory
	 *            The directory to create the index.html in
	 * 
	 * @return a {@link File} object to the created .html file.
	 */
	File createIndexHTML(final AtlasConfigEditable ace, final File directory)
			throws AtlasExportException {
		// **********************************************************************
		//
		// Creating the index.html
		//
		// **********************************************************************
		final File htmlFile = new File(directory, "index.html");

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(htmlFile);
			fileWriter
					.write("<?xml version='1.0' encoding='UTF-8'?><html><head><title>"
							+ ace.getTitle() + "</title></head><body>\n");

			/**
			 * Start des JNLP über das deplayJava.js
			 * https://jdk6.dev.java.net/deployment_advice
			 * .html#Deploying_Java_Web_Start_Applica
			 */
			String jnlpLocation = getJnlpBaseUrl();

			fileWriter.write("<h2>" + ace.getTitle() + "</h2>");
			fileWriter
					.write("Exported: "
							+ DateFormat.getInstance().format(new Date())
							+ "<br/>"
							+ "Created with <a href=\"http://en.geopublishing.org/Geopublisher\">Geopublisher</a> "
							+ ReleaseUtil.getVersionInfo(GeopublisherGUI.class)
							+ "<br/>");

			// ZIP
			if (zipDiskAfterExport && toDisk) {
				final String zipname = ace.getBaseName() + ".zip";
				fileWriter.write("Download offline version: ");
				fileWriter.write("<a href='" + zipname + "'> " + zipname
						+ "</a><br/>");
			}

			// JWS
			fileWriter.write("Start atlas via JWS:");
			String jsScript = GPProps.get(Keys.JWSStartScript).replaceAll(
					"__JNLPURL__", jnlpLocation + JNLP_FILENAME);
			jsScript = jsScript.replace("__MINJAVAVERSION__",
					GPProps.get(Keys.MinimumJavaVersion));
			fileWriter.write(jsScript);
			fileWriter.write("<br/>");

			// JWS commandline
			fileWriter.write("Start from commandline: javaws " + jnlpLocation
					+ JNLP_FILENAME);
			fileWriter.write("<br/>");

			fileWriter.write("</html></body>\n");
			fileWriter.close();
			return htmlFile;
		} catch (final IOException e) {
			throw new AtlasExportException("Creating index.html failed", e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					throw new AtlasExportException(
							"Creating index.html failed", e);
				}
			}
		}
	}

	protected static void createReadmeTXT(final AtlasConfigEditable ace,
			final File targetJar) throws IOException {

		// ******************************************************************
		// README.TXT
		// ******************************************************************
		final FileWriter fileWriter = new FileWriter(new File(
				targetJar.getParentFile(), "README.TXT"));
		fileWriter.write("JAVA " + GPProps.get(Keys.MinimumJavaVersion)
				+ " is required to run the atlas\n");
		fileWriter
				.write("For help and information go to http://www.geopublishing.org\n");
		fileWriter
				.write("The logfile is atlas.log in your user's folder for temporary files.\n");
		fileWriter
				.write("Something like: C:\\Dokumente und Einstellungen\\YourUsernamE\\Lokale Einstellungen\\Temp\n");

		fileWriter.close();

	}

	/** Shall the local JRE be copied to the DISK folder? **/
	private final Boolean copyJRE;

	private File targetDirJWS, targetDirDISK;

	public File getTargetDirJWS() {
		return targetDirJWS;
	}

	public void setTargetDirJWS(File targetDirJWS) {
		this.targetDirJWS = targetDirJWS;
	}

	/**
	 * The temp directory where jars are assembled. it is a random folder in the
	 * system temp directory.
	 */
	private final File tempDir = new File(IOUtil.getTempDir(),
			ATLAS_TEMP_FILE_EXPORTINSTANCE_ID
					+ GpCoreUtil.RANDOM.nextInt(19999) + 10000);

	/** Is export to DISK requested? **/
	private final Boolean toDisk;
	/** Is export to JWS requested? **/
	private final Boolean toJws;

	/**
	 * Initialized lazilly and caches where the JARs from the classpath come
	 * from
	 **/
	private HashMap<String, URL> jarUrlsFromClassPath = null;

	/** Automatically create a ZIP if eported to DISK **/
	private boolean zipDiskAfterExport = false;

	/**
	 * It set to a value not <code>null</code>, the jnlp base url stored in the
	 * {@link AtlasConfigEditable} is overwritten with this value. If
	 * <code>null</code>, the value from the <code>atlas.xml</code> is used.
	 */
	private URL overwriteJnlpUrl = null;

	/**
	 * Used for findFileinDirectory()
	 */
	private static boolean foundFile;
	private static String filePath;

	/**
	 * Initializes an {@link JarExportUtil} object to do the real work. The
	 * constructor already creates a temp dir that start with
	 * ATLAS_TEMP_FILE_EXPORTINSTANCE_ID.
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
			ResultProgressHandle progress, final File exportDirectory,
			final Boolean toDisk, final Boolean toJws, final Boolean copyJRE)
			throws IOException {
		super(ace_, progress);
		this.toDisk = toDisk;
		this.toJws = toJws;
		this.copyJRE = copyJRE;

		// Create temporary export folder
		deleteOldTempExportDirs();
		getTempDir().mkdirs();

		// Create final export folders
		if (toDisk) {
			targetDirDISK = new File(exportDirectory, DISK);
			// LOGGER.debug("Deleting old files...");
			try {
				FileUtils.deleteDirectory(targetDirDISK);
			} catch (IOException e) {
				throw new AtlasExportException(
						"The export "
								+ targetDirDISK
								+ " directory is write protected. Please choose another folder."); // i8n
			}
			targetDirDISK.mkdirs();
		}

		if (toJws) {

			if (targetDirJWS == null)
				targetDirJWS = new File(exportDirectory, JWS);
			// LOGGER.debug("Deleting old files...");
			try {
				FileUtils.deleteDirectory(targetDirJWS);
			} catch (IOException e) {
				throw new AtlasExportException(
						"The export "
								+ targetDirJWS
								+ " directory is write protected. Please choose another folder."); // i8n
			}
			targetDirJWS.mkdirs();
		}

	}

	/**
	 * Initializes an {@link JarExportUtil} object to do the real work. The
	 * constructor already creates a temp dir that start with
	 * ATLAS_TEMP_FILE_EXPORTINSTANCE_ID.
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
		this(ace_, new LoggerResultProgressHandle(), exportDirectory, toDisk,
				toJws, copyJRE);
	}

	private void addAtlasXMLToJar(final File targetJar,
			final AtlasConfigEditable ace) throws AtlasExportException {

		// if (1 == 1)
		// return;
		try {

			// Prepare a temporary atlas.xml
			File randomTempDir = new File(IOUtil.getTempDir(),
					ATLAS_TEMP_FILE_EXPORTINSTANCE_ID + "GPtempExport"
							+ System.currentTimeMillis());
			final File adDir = new File(randomTempDir,
					AtlasConfig.ATLASDATA_DIRNAME);
			FileUtils.deleteDirectory(adDir);
			adDir.mkdirs();
			final File exportAtlasXml = new File(adDir,
					AtlasConfig.ATLAS_XML_FILENAME);

			// Configure the AML exporter
			final AMLExporter amlExporter = new AMLExporter(ace);
			amlExporter.setExportMode(true);
			amlExporter.setAtlasXml(exportAtlasXml);

			amlExporter.saveAtlasConfigEditable();

			addToJar(targetJar, randomTempDir, AtlasConfig.ATLASDATA_DIRNAME
					+ "/" + AtlasConfig.ATLAS_XML_FILENAME);

			// Remove the created temporary atlas.xml, it has been copied into
			// the
			// jar
			if (!exportAtlasXml.delete()) {
				LOGGER.warn("could not delete temporary atlas.xml file at "
						+ adDir);
			}
		} catch (Exception e) {
			throw new AtlasExportException("addAtlasXMLToJar failed", e);
		}
	}

	/**
	 * Adds the what-String recursively to the target JAR Any manifest inside
	 * the target is deleted.. Call
	 * {@link #addManifest(File, AtlasConfigEditable)} after you are done with
	 * the jar If the jar doesn't exist, we create it. If the what resolves to a
	 * directory, we filter out any .svn or .cvs directories.
	 * 
	 * @param targetJar
	 *            Existing JAR to extend
	 * 
	 * @param baseDir
	 *            Directory with "ad" folder of the AtlasWorkingCopy
	 * 
	 * @param what
	 *            command line argument of what to put into the jar. All
	 *            '/'-chars will be replaces with the systemdependent
	 *            File.seperator
	 * @throws AtlasCancelException
	 * @throws Exception
	 *             So many things can fail ;-)
	 * 
	 * @see Thanks to http://www.jguru.com/faq/view.jsp?EID=68627
	 */
	public void addToJar(final File targetJar, final File baseDir,
			final String what) throws AtlasExportException, IOException,
			AtlasCancelException {

		final String jarName = targetJar.getAbsolutePath();

		/**
		 * Creating a JAR
		 */
		File testWhat = new File(baseDir, what);
		Collection<String> listRelFileNames = new ArrayList<String>();
		if (testWhat.isDirectory()) {
			final Collection<File> listFiles = FileUtils.listFiles(testWhat,
					FilterUtil.BlacklistedFilesFilter,
					FilterUtil.BlacklistedFoldersFilter);
			for (File f : listFiles) {
				if (IOUtil.getFileExt(f).equals(".html")) {
					String htmlString = IOUtil.readFileAsString(f);
					if (htmlString.contains("\\")) {
						GpCoreUtil
								.convertWindowsToLinuxSlashesInHtmlSrcTags(IOUtil
										.fileToURL(f));
					}
				}
				String relFileName = f.getAbsolutePath().substring(
						baseDir.getAbsolutePath().length() + 1);
				listRelFileNames.add(relFileName);
			}
		} else
			listRelFileNames.add(what);

		for (String what2 : listRelFileNames) {
			checkAbort();

			// We HAVE TO create a new JarTool instance of every calL!
			final Main jartool = new Main(System.out, System.err, "jar");

			if (!targetJar.exists()) {
				LOGGER.debug("creating new (without manifest)" + jarName);
				final boolean run = jartool.run(new String[] { "cf", jarName,
						"-C", baseDir.getAbsolutePath(), what2 });

				if (!run)
					throw new AtlasExportException("unable to create jar "
							+ targetJar + " with " + what2 + " from " + baseDir);
			} else {
				LOGGER.debug("updating " + jarName + ", adding " + what2
						+ " from " + baseDir);

				final boolean run = jartool.run(new String[] { "uf", jarName,
						"-C", baseDir.getAbsolutePath(), what2 });
				if (!run)
					throw new AtlasExportException("unable to update jar "
							+ targetJar + " with " + what2 + " from "
							+ baseDir.getAbsolutePath());
			}

		}

	}

	/**
	 * Copies and signs all required dependencies and native libs to the temp
	 * target directory.
	 * 
	 * @throws AtlasExportException
	 *             Export stopped with exception
	 * @throws AtlasCancelException
	 *             Export canceled by user
	 */
	protected void copyAndSignLibs(final File targetJar,
			final AtlasConfigEditable ace) throws AtlasExportException,
			AtlasCancelException {

		/************
		 * Copying the .jar files now!
		 */
		final File targetLibDir = new File(targetJar.getParentFile(), LIB_DIR);

		/**
		 * We do not export all libs found in a directory, but rather we only
		 * copy libraries mentioned in the .properties files of GP. The
		 * gp-natives.jar we add automatically.
		 */
		boolean packNotExistingErrorAlreadyShown = false;

		for (final String libName : getJarAndNativeLibNames()) {

			if (libName.equals("./postgresql-9.1-901.jdbc4.jar")) {
				System.out.println("bla");
			}
			checkAbort();

			File destination = new File(targetLibDir, libName);
			destination.getParentFile().mkdirs();

			// Source URLs
			URL fromURL = findJarUrl(libName);

			if (fromURL == null) {
				if (libName.endsWith("jar")) {
					throw new AtlasExportException("Library not found "
							+ libName);
				} else {
					// the native libs are not so important so far
					LOGGER.warn("Native lib not found: " + libName
							+ ". Not fatal... ");
					continue;
				}
			}

			final String msg = GpUtil.R("Export.progressMsg.copy_lib_to_",
					libName, destination.toString());

			LOGGER.debug(msg + " (URL:" + fromURL.toString() + ")");
			info(msg);

			try {
				FileUtils.copyURLToFile(fromURL, destination);

				if (toJws && libName.endsWith(".jar")
						&& !JNLPUtil.isJnlpServiceAvailable()) {
					// if they come from local, we might have to
					// sign them.
					jarSign(destination);
				}

				/*
				 * Copy the .pack.gz files only if we export to JWS. Do not
				 * export .pack.gz for gp-natives.jar, because the extra
				 * compression doesn't work for that kind of file.
				 */
				if (toJws && libName.endsWith(".jar")) {
					try {
						// URL fromURLPackGZ = DataUtilities.extendURL(fromURL,
						// libName + ".pack.gz");
						//
						// final File destinationPackGz = new File(targetLibDir,
						// libName + ".pack.gz");

						URL fromURLPackGZ = new URL(fromURL + ".pack.gz");

						final File destinationPackGz = new File(targetLibDir,
								libName + ".pack.gz");

						FileUtils.copyURLToFile(fromURLPackGZ,
								destinationPackGz);

					} catch (final Exception e) {

						if (packNotExistingErrorAlreadyShown == false) {
							LOGGER.warn(GpUtil.R("Export.Error.Pack200") + " "
									+ e.getMessage());
							// ExceptionDialog.show(null, new
							// AtlasException(
							// GpUtil.R("Export.Error.Pack200"), e));

						}
						// Do not show this warning again next time.
						packNotExistingErrorAlreadyShown = true;
					}
				} // toJws only

			} catch (final Exception e) {
				final String errorMsg = GpUtil.R(
						"Export.errorMsg.error_copy_lib_to_", libsFromLocal,
						libName, fromURL, destination);
				LOGGER.warn(errorMsg, e);
				throw new AtlasExportException(errorMsg, e);
			}
		}
	}

	/**
	 * Find an {@link URL} to the requested JAR.
	 * 
	 * @param libName
	 *            e.g. gpcore-1.5-SNAPSHOT.jar
	 */
	public URL findJarUrl(String libName) {

		URL url;

		// Try to find the file locally via getResource of gpcore.jar
		url = getJarUrlFileSystem(libName);
		if (url != null)
			return url;

		// Try to find the file online, where GP was started from (JWS only)
		url = getJarUrlFromJWS(libName);
		if (url != null)
			return url;

		// Maybe the file is a .dll inside gpnative.jar
		url = getJarUrlInsideGpNatives(libName);
		if (url != null)
			return url;

		// Maybe the file is a .dll inside java.library.path variable
		url = getNativeLibraryURL(libName);
		if (url != null)
			return url;

		// Fallback, last hope!
		url = getJarUrlFromClasspath(libName);
		if (url != null)
			return url;

		// Fallback, last hope when in Eclipse!
		url = getJarUrlFromMavenRepository(libName);
		if (url != null)
			return url;

		// url = getJarUrlViaClassLoaderDirectly(libName);
		// if (url != null)
		// return url;

		return null;
	}

	private URL getJarUrlViaClassLoaderDirectly(String libName) {
		URL url = GeopublisherGUI.class.getResource(libName);
		return url;
	}

	private URL getJarUrlInsideGpNatives(String libName) {
		URL url;
		try {
			url = new URL("jar:" + "!/" + libName);
			url.openStream().close();
		} catch (Exception e) {
			// LOGGER.info(e);
			return null;
		}
		return url;
	}

	/**
	 * @param libName
	 */
	private URL getJarUrlFromJWS(String libName) {

		try {
			// Will throw exception if not running under JWS
			final BasicService bs = (BasicService) ServiceManager
					.lookup("javax.jnlp.BasicService");

			String path = bs.getCodeBase().getFile();
			if (!path.endsWith("/"))
				path = path + "/";

			/**
			 * Normally the libs are found in the LIBDIR directory. The gp-jars
			 * are different!
			 */
			String fileAndPath;
			if (BASEJARS.contains(libName)) {
				fileAndPath = path + libName;
			} else {
				fileAndPath = path + LIB_DIR + "/" + libName;
			}

			URL testURL = new URL(bs.getCodeBase().getProtocol(), bs
					.getCodeBase().getHost(), bs.getCodeBase().getPort(),
					fileAndPath);

			testURL.openStream().close();

			libsFromLocal = false;

			return testURL;

		} catch (final javax.jnlp.UnavailableServiceException e) {
			/**
			 * The exception is harmless and means, that we have not been
			 * started via JWS.
			 */
			return null;
		} catch (MalformedURLException e) {
			LOGGER.error("While looking for " + libName + " in JWS mode: ", e);
			return null;

		} catch (IOException e) {
			LOGGER.error("While looking for " + libName + " in JWS mode: ", e);
			return null;

		}
	}

	/**
	 * Returns {@link URL} to the requested jar file. Searches for the file
	 * releative to the "gpcore...jar" file. Returns <code>null</code> if the
	 * file could not be found.
	 */
	private URL getJarUrlFileSystem(String jarName) {

		String classFileName = GeopublisherGUI.class.getSimpleName() + ".class";
		URL url = GeopublisherGUI.class.getResource(classFileName);

		// LOGGER.debug(classFileName + " found in " + url);

		if (url != null) {
			String stringUrl = url.toString();
			// LOGGER.debug(stringUrl);
			if (stringUrl.startsWith("jar:file:")) {
				stringUrl = stringUrl.substring(9, stringUrl.lastIndexOf("!"));
				LOGGER.debug(stringUrl);
				File gpCoreJarFile = new File(stringUrl);
				LOGGER.debug(gpCoreJarFile);
				if (gpCoreJarFile.exists()) {

					File try2 = new File(gpCoreJarFile.getParentFile(), LIB_DIR
							+ "/" + jarName);
					if (try2.exists())
						return DataUtilities.fileToURL(try2);

					File try1 = new File(gpCoreJarFile.getParentFile(), jarName);
					if (try1.exists())
						return DataUtilities.fileToURL(try1);

				}
			}
		}

		// Last try is to look in the working directory
		File file = new File(jarName);
		// LOGGER.debug(classFileName
		// + " didn't help! Fallback to working directory, "
		// + file.getAbsolutePath());

		if (file.exists())
			return DataUtilities.fileToURL(file);

		return null;
	}

	/**
	 * Returns {@link URL} to JAR in maven repository. Only used as a fall back,
	 * when exporting from within Eclipse IDE, where the needed jars do not
	 * exist. One has to execute "mvn install" in GP trunk before using this
	 * mehtod.<br/>
	 * <code>null</code> if not found in m2repo
	 * 
	 * 
	 * 
	 * @param jarName
	 *            name of searched jar, e.g. "colt-1.2.jar"
	 */
	private URL getJarUrlFromMavenRepository(final String jarName) {
		URL fromURL;

		// Actually the m2 repo can be at another location
		File m2repo = new File(System.getProperty("user.home")
				+ "/.m2/repository/");

		String path = null;
		if (jarName.contains(GPCORE_JARNAME)) {
			path = "org/geopublishing/geopublisher/gpcore/" + GPVERSION;
		}

		if (jarName.contains(AVSWINGGUI_JARNAME)) {
			path = "org/geopublishing/atlasViewer/avswinggui/" + GPVERSION;
		}

		if (jarName.contains(ASCORE_JARNAME)) {
			path = "org/geopublishing/atlasStyler/ascore/" + GPVERSION;
		}

		/**
		 * Look in the local maven repository for projects, that appear in tthe
		 * classpath-string as relative paths.
		 */
		if (jarName.contains(SCHMITZM_JARNAME1))
			path = "de/schmitzm/schmitzm-core/2.8-SNAPSHOT";
		if (jarName.contains(SCHMITZM_JARNAME2))
			path = "de/schmitzm/schmitzm-gt/2.8-SNAPSHOT";
		if (jarName.contains(SCHMITZM_JARNAME3))
			path = "de/schmitzm/schmitzm-jfree-gt/2.8-SNAPSHOT";
		if (jarName.contains(SCHMITZM_JARNAME4))
			path = "de/schmitzm/schmitzm-jfree/2.8-SNAPSHOT";
		if (jarName.contains(SCHMITZM_JARNAME5))
			path = "de/schmitzm/schmitzm-db/2.8-SNAPSHOT";
		if (jarName.contains(GSRCJ_JARNAME))
			path = "org/geopublishing/gsrcj/0.5";
		if (jarName.contains(GPSYNC_JARNAME))
			path = "org/geopublishing/gpsync/" + GPVERSION;

		/*
		 * ./gt-data-2.6.1.jar and other geotool jars
		 */
		Matcher matcher = Pattern.compile("(gt-.*)-(\\d\\.\\d.\\d)\\.jar")
				.matcher(jarName);
		if (matcher.find()) {
			path = "org/geotools/" + matcher.group(1) + "/" + matcher.group(2);
			if (matcher.group(1).contains("-jdbc-"))
				path = "org/geotools/jdbc/" + matcher.group(1) + "/"
						+ matcher.group(2);
		}
		if (path == null) {
			path = findFileInDirectory(m2repo, jarName.substring(2));
			if (path != null) {
				path = path.replace(m2repo.toString(), "");
				path = path.replace(jarName.substring(2), "");
			}
		}
		if (path == null)
			return null;

		File file = new File(m2repo, path + "/" + jarName);

		if (!file.exists()) {
			throw new AtlasExportException(
					"Can't find library: "
							+ jarName
							+ ". If you are running from source or IDE, try using 'mvn install' first. Otherwise please report this to the mailinglist. ");
		}
		fromURL = DataUtilities.fileToURL(file);

		return fromURL;
	}

	/**
	 * traverses the given parentDirectory recursively to find fileName and
	 * returns the filePath or null
	 * 
	 * @param parentDirectory
	 * @param fileName
	 * @return
	 */
	private static String findFileInDirectory(File parentDirectory,
			String fileName) {
		if (foundFile) {
			return filePath;
		}
		File[] files = parentDirectory.listFiles();
		for (File file : files) {
			if (file.getName().equals(fileName)) {
				foundFile = true;
				filePath = file.getAbsolutePath();
				break;
			}
			if (file.isDirectory()) {
				findFileInDirectory(file, fileName);
			}
		}
		return filePath;
	}

	private URL getNativeLibraryURL(String nativeName) {
		// Clean any path or ./ stuff
		nativeName = new File(nativeName).getName();

		String[] st = System.getProperty("java.library.path").split(":");
		for (String t : st) {
			// LOGGER.debug("looking in " + t + " for " + nativeName);
			File file = new File(t + "/" + nativeName);
			if (file.exists()) {
				return DataUtilities.fileToURL(file);
			}
		}

		return null;
	}

	/**
	 */
	private URL getJarUrlFromClasspath(String jarName) {
		if (jarUrlsFromClassPath == null) {
			jarUrlsFromClassPath = new HashMap<String, URL>();
			String[] st = System.getProperty("java.class.path").split(":");
			for (String t : st) {
				if (!t.endsWith("jar") && !t.endsWith("jar.pack.gz")
						&& !t.endsWith("zip")) {
					LOGGER.warn(t + " was not a valid entry on the classpath.");
					continue;
				}

				File file = new File(t);
				if (!file.exists()) {
					LOGGER.warn(t + " was not a valid entry on the classpath.");
					continue;
				}
				jarUrlsFromClassPath.put(file.getName(),
						DataUtilities.fileToURL(file));
			}

		}

		// Clean any path or ./ stuff
		jarName = new File(jarName).getName();

		URL url = jarUrlsFromClassPath.get(jarName);

		if (url == null) {
			// The requested jar was not found in the classpath. mmm...
			String curDir = System.getProperty("user.dir");
			// File baseFile =
			// DataUtilities.urlToFile(jarUrlsFromClassPath.get(JarExportUtil.GPCORE_JARNAME));

			File try1 = new File(curDir, jarName);
			// LOGGER.debug("try1 = " + try1);
			if (try1.exists())
				return DataUtilities.fileToURL(try1);

			File try2 = new File(curDir, LIB_DIR + "/" + jarName);
			// LOGGER.debug("try2 = " + try2);
			if (try2.exists())
				return DataUtilities.fileToURL(try2);
		}

		return url;
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
		if (SystemUtils.IS_OS_WINDOWS) {
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
			FileUtils.copyURLToFile(
					GpUtil.class.getResource(LICENSEHTML_RESOURCE_NAME),
					destLicense);
		} catch (final Exception e) {
			ExceptionDialog
					.show(null,
							new AtlasException(
									"Non-fatal error while copying the licence.txt:"
											+ e.getMessage()
											+ "\n   "
											+ GpUtil.class
													.getResource(LICENSEHTML_RESOURCE_NAME),
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
	private void copyUncompressedFiles() throws IOException {

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
	 * <ode>ARJAR_FILENAME</code> JAR.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 * @param dpe
	 *            The {@link DpEntry} to export.
	 * 
	 * @throws AtlasExportException
	 * @throws IOException
	 * @throws AtlasCancelException
	 */
	public File createJarFromDpe(final DpEntry<?> dpe)
			throws AtlasExportException, IOException, AtlasCancelException {

		checkAbort();

		info(dpe.getType().getLine1() + ": " + dpe.getTitle().toString());

		/**
		 * Setting up a new JAR file
		 */
		// Setting up da JAR File for this DatapoolEntry
		final File newJar = new File(getTempDir(), dpe.getId() + ".jar");
		LOGGER.debug("Exportig to JAR " + newJar.getName() + "    in "
				+ getTempDir());
		final BufferedOutputStream bo = new BufferedOutputStream(
				new FileOutputStream(newJar.getAbsolutePath()));
		try {

			final JarOutputStream jo = new JarOutputStream(bo);
			try {

				/**
				 * Adding all the files as JarEntrys
				 */
				final String absolutePath = ace.getAtlasDir().getAbsolutePath();
				final String relpath = "ad" + File.separator + "data"
						+ File.separator + dpe.getDataDirname();

				final Collection<File> listFiles = FileUtils.listFiles(
						new File(absolutePath, relpath),
						FilterUtil.BlacklistedFilesFilter,
						FilterUtil.BlacklistedFoldersFilter);
				for (final File intoJarSource : listFiles) {

					String act = intoJarSource.getPath();
					final BufferedInputStream bi = new BufferedInputStream(
							new FileInputStream(act));
					try {

						// act is now an absolute pathname. We need a relative
						// pathname
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

					} finally {
						bi.close();
					}
				}
			} finally {
				jo.close();
			}
		} finally {
			bo.close();
		}

		// Add a single index entry, that only contains its own contents
		// addJarIndex(newJar);

		if (toJws)
			jarSign(newJar);

		return newJar;
	}

	/**
	 * Creates an <code>{@link #JNLP_FILENAME}</code> file in the same folder,
	 * as the targetJar.
	 * 
	 * @param owner
	 *            GUI Owner
	 * 
	 * @param targetJar
	 *            Location of the {@link #ARJAR_FILENAME}
	 * @param Codebase
	 *            where the JWS will be running
	 * 
	 * @throws AtlasFatalException
	 */
	private void createJNLP(final AtlasConfigEditable ace, final File targetJar)
			throws AtlasExportException {

		String codebase = getJnlpBaseUrl();

		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			Document document = null;
			document = factory.newDocumentBuilder().newDocument();

			final Element jnlp = document.createElement("jnlp");

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
				FileUtils
						.copyURLToFile(splashscreenURL,
								new File(targetJar.getParentFile(),
										"splashscreen.png"));
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
			aResource.setAttribute("version",
					GPProps.get(Keys.MinimumJavaVersion, "1.6.0_18") + "+");
			aResource.setAttribute("href",
					"http://java.sun.com/products/autodl/j2se");
			aResource.setAttribute("java-vm-args",
					"-Xmx" + GPProps.getInt(GPProps.Keys.startJVMWithXmx, 256)
							+ "m" + " -Dfile.encoding=UTF-8");
			resources.appendChild(aResource);

			aResource = document.createElement("jar");
			aResource.setAttribute("href", targetJar.getName());
			aResource.setAttribute("part", "main");
			aResource.setAttribute("download", "eager");

			aResource.setAttribute("size", String.valueOf(targetJar.length()));

			resources.appendChild(aResource);

			for (final String libName : getJarLibNames()) {
				if (libName.trim().equals("")) {
					continue;
				}
				aResource = document.createElement("jar");

				String libNameChecked;
				if (libName.contains(GPCORE_JARNAME))
					aResource.setAttribute("main", "true");

				libNameChecked = LIB_DIR + "/" + libName;

				// Reference to other online jars
				libNameChecked = "http://www.geopublishing.org/gp-" + GPVERSION
						+ "/" + libNameChecked;

				// Cleaning the URL for better reuse
				libNameChecked = libNameChecked.replace("./", "");

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

			for (final DpEntry<?> dpe : filterUnused ? ace.getUsedDpes() : ace
					.getDataPool().values()) {

				resources.appendChild(document.createComment("Datapoolentry "
						+ dpe.getTitle().toString()));

				// Every part needs one package tag, so that JWS knows what to
				// expect there...
				aResource = document.createElement("package");
				aResource.setAttribute("part", dpe.getId());
				aResource.setAttribute("name",
						"ad.data." + dpe.getDataDirname() + ".*");
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
					for (final DpRef<?> ref : startUpMap.getLayers()) {
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
			applDesc.setAttribute("main-class", MAIN_CLASS);
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
	 * Returns the JNLP base url setup for the atlas, unless the
	 * "overwriteJnlpUrl" flag has been set.
	 */
	private String getJnlpBaseUrl() {
		String codebase = ace.getJnlpBaseUrl();
		if (overwriteJnlpUrl != null) {
			codebase = overwriteJnlpUrl.toString();
			if (!codebase.endsWith("/"))
				codebase += "/";
			LOGGER.info("Export JNLP base URL changed to " + codebase);
		}

		return codebase;
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
		FileUtils.copyURLToFile(
				GpUtil.class.getResource(JSMOOTH_SKEL_AD_RESOURCE1),
				jsmoothExeFile);
		final File jsmoothSkelFile = new File(jsmoothSkelDir,
				"autodownload.skel");
		FileUtils.copyURLToFile(
				GpUtil.class.getResource(JSMOOTH_SKEL_AD_RESOURCE2),
				jsmoothSkelFile);

		/**
		 * atlas.jsmooth is positioned in DISK/atlas.jsmooth
		 */
		final File destinationProjectFile = new File(atlasDir, "atlas.jsmooth");
		FileUtils.copyURLToFile(
				GpUtil.class.getResource(JSMOOTH_PROJEKT_RESOURCE),
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
				File iconFile = new File(atlasDir, "icon.gif")
						.getCanonicalFile();
				if (iconFile.exists())
					iconFile.delete();
				destinationProjectFile.delete();
			} catch (final IOException e) {
				LOGGER.warn(
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
	 * Exports the given {@link AtlasConfig} to two folders: One for
	 * CD/USB-STick, one to be run via JavaWebStart
	 * 
	 * @throws Exception
	 * 
	 */
	public void export() throws Exception {

		totalSteps = 10;

		/** One for every root-level file **/
		totalSteps += ace.getAtlasDir().listFiles(filterForRootLevelFiles).length;

		/**
		 * One for ever DPEntry
		 */
		totalSteps += ace.getUsedDpes().size();

		/**
		 * One for every Library and every Native libs
		 */
		totalSteps += getJarAndNativeLibNames().length;

		info(GpUtil.R("ExportDialog.processWindowTitle.Exporting"));

		// Try catch to always delete the temp folder
		try {

			final File targetJar = new File(getTempDir(), ARJAR_FILENAME);
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

				// Export only the HTML map info for the layers that are
				// referenced in the atlas
				for (Map m : ace.getUsedMaps()) {
					addToJar(targetJar, ace.getAtlasDir(), ace.getAd()
							.getName()
							+ "/"
							+ AtlasConfig.HTML_DIRNAME
							+ "/"
							+ m.getId());
				}

				// Export the about and popup screen
				ace.getAboutDir(); // calling this, guarantees, that the folder
				// will exist as if will be created if it
				// doesn't exist
				addToJar(targetJar, ace.getAtlasDir(), ace.getAd().getName()
						+ "/" + AtlasConfig.HTML_DIRNAME + "/"
						+ AtlasConfig.ABOUT_DIRNAME);

				addAtlasXMLToJar(targetJar, ace);

				// Export the additional atlas fonts, go though all fonts in
				// font dir and only export the ones that are readbale
				{
					File fontsDir = ace.getFontsDir();

					Collection<File> listFiles = FileUtils.listFiles(fontsDir,
							GpUtil.FontsFilesFilter,
							FilterUtil.BlacklistedFoldersFilter);
					for (File f : listFiles) {
						try {
							// Font createFont = Font.createFont(
							// Font.TRUETYPE_FONT, f);

							String relPath = f.getAbsolutePath().substring(
									fontsDir.getAbsolutePath().length() + 1);

							addToJar(targetJar, ace.getAtlasDir(),
									AtlasConfig.ATLASDATA_DIRNAME + "/"
											+ AtlasConfig.FONTS_DIRNAME + "/"
											+ relPath);

						} catch (Exception e) {
							LOGGER.warn("Not adding "
									+ f
									+ " to jar, because it can't be loaded correctly.");
						}
					}

				}

				addToJar(targetJar, ace.getAtlasDir(),
						AtlasConfig.ATLASDATA_DIRNAME + "/"
								+ AtlasConfig.IMAGES_DIRNAME);

				// Store the settings
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
					FileUtils
							.copyURLToFile(
									GpUtil.class
											.getResource(AtlasConfigEditable.SPLASHSCREEN_RESOURCE_NAME_FALLBACK),
									new File(
											ace.getAtlasDir(),
											AtlasConfig.SPLASHSCREEN_RESOURCE_NAME));
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
					iconURL = GpUtil.class
							.getResource(AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK);
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
						AtlasConfig.MAPLOGO_RESOURCE_NAME);
			} catch (final Exception e1) {
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
				LOGGER.info("Not exporting defaultcrs.prj beacuse it doesn't exist or is too small");
			}

			if (toDisk)
				copyAndSignLibs(targetJar, ace);

			// File[] listOfIndexJars = new File[] { targetJar };
			// Creating a JAR for every DpEntry
			LOGGER.debug("Creating a JAR for every DpEntry used");
			for (final DpEntry<?> dpe : filterUnused ? ace.getUsedDpes() : ace
					.getDataPool().values()) {
				createJarFromDpe(dpe);
			}

			checkAbort();

			/**
			 * now we add the correct manifest
			 */
			Main jartool = new Main(System.out, System.err, "jar");
			File manifestFile = getManifestFile();
			try {

				String[] args = new String[] { "ufm",
						targetJar.getAbsolutePath(),
						manifestFile.getAbsolutePath() };
				LOGGER.debug("Calling jartool with "
						+ LangUtil.stringConcatWithSep(" ", args));
				if (!jartool.run(args))
					throw new AtlasExportException(
							"unable to add correct manifest to " + targetJar);
			} finally {
				if (manifestFile != null)
					manifestFile.delete();
			}

			if (toJws) {
				LOGGER.debug("Signing " + ARJAR_FILENAME + "...");
				jarSign(targetJar);
			}

			/**
			 * The icon.gif is needed for DISK and JWS. (DISK will put it into
			 * the atlas.exe and delete it afterwards)
			 */
			URL iconURL = ace.getResource(AtlasConfig.JWSICON_RESOURCE_NAME);
			if (!GpCoreUtil.exists(iconURL)) {
				// LOGGER
				// .info("No user-defined icon provided. Using the default one.");
				iconURL = GpUtil.class
						.getResource(AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK);
			}
			FileUtils.copyURLToFile(iconURL, new File(
					targetJar.getParentFile(), "icon.gif"));

			/*
			 * Creating the JNLP...
			 */
			if (toJws) {
				LOGGER.debug("Creating JNLP...");
				createJNLP(ace, targetJar);

				// Create a generic index.html
				createIndexHTML(ace, targetJar.getParentFile());
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

			// All files "next to" atlas.gpa will be copied to the folders
			// without
			// putting them in a JAR. This can be usefull for example for PDF
			// files that should be referencable from within the atlas, but also
			// reside uncompressed on the CD root directory.
			copyUncompressedFiles();

			if (SystemUtils.IS_OS_LINUX)
				adjustRights();

			if (toDisk && zipDiskAfterExport) {
				zipDiskDir(targetDirDISK);
			}

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
			// } catch (Exception e) {
			// throw new
			// AtlasExportException("error during creation of atlas.exe", e);
		} finally {
			// Whatever happened, we have to delete the temp dir
			info(GpUtil.R("Export.Finally.Cleanup.Msg"));
			deleteOldTempExportDirs();
		}
	}

	/**
	 * @param targetDirDISK2
	 */
	public void zipDiskDir(File targetDirDISK2) {
		File zipFile = new File(targetDirDISK2.getParent(), ace.getBaseName()
				+ ".zip");

		info("Create " + zipFile.getName());
		try {
			IOUtil.zipDir(targetDirDISK2, zipFile);

		} catch (Exception e) {
			throw new AtlasExportException("Failed to create ZIP file "
					+ IOUtil.escapePath(zipFile), e);
		}
	}

	/**
	 * Adjusts the right sof the DISK and JWS folder. They have some strange
	 * defaults since OpenJDK in Lucid?!
	 */
	private void adjustRights() {

		if (!SystemUtils.IS_OS_LINUX)
			return;

		// In JWS set to execute, read and NOTwrite
		if (toJws) {
			targetDirJWS.setExecutable(true, false);
			targetDirJWS.setReadable(true, false);

			Iterator<File> iterateFiles = FileUtils.iterateFiles(targetDirJWS,
					FilterUtil.BlacklistedFoldersFilter,
					FilterUtil.BlacklistedFilesFilter);
			while (iterateFiles.hasNext()) {
				File next = iterateFiles.next();
				// next.setWritable(false, false);
				next.setExecutable(true, false);
				next.setReadable(true, false);
			}
		}

		// In DISK set to read and NOTwrite
		if (toDisk) {
			targetDirDISK.setExecutable(true, false);
			targetDirDISK.setReadable(true, false);

			Iterator<File> iterateFiles = FileUtils.iterateFiles(targetDirDISK,
					FilterUtil.BlacklistedFoldersFilter,
					FilterUtil.BlacklistedFilesFilter);
			while (iterateFiles.hasNext()) {
				File next = iterateFiles.next();
				next.setReadable(true, false);
			}

			// Linux .sh script must be +x
			new File(targetDirDISK, START_SCRIPT_LINUX).setExecutable(true,
					false);
			// MacOS .command script must be +x
			new File(targetDirDISK, START_SCRIPT_MAC)
					.setExecutable(true, false);

		}
	}

	/**
	 * @return A list of JAR-names and native lib name (.so etc) that
	 *         {@link AtlasViewerGUI} dependes on.
	 */
	private String[] getJarAndNativeLibNames() {

		String[] libs = getJarLibNames();

		// Native libs auch entpackt und einzeln in LIB ordner kopieren, wenn
		// für DISK exportiert wird.

		// Native Libs nicht mehr mitkopieren, kann komplett entfernt werden?
		if (toDisk) {
			libs = LangUtil.extendArray(libs, getNativeLibNames());
		}

		return libs;
	}

	/**
	 * @return List of .jar dependencies needed by the exported swing atlas.
	 */
	String[] getJarLibNames() {
		String[] libs;
		// Read maven2 generated list of dependencies
		Properties p = new Properties();
		String propertiesName = "/atlasdependencies.properties";
		try {
			// HTMLInfoLoboBrowser ist eine Klasse aus dem Modul avswinggui, wo
			// die Datei 'atlasdependencies.properties' durch mvn compile in
			// target/classes gelegt wird.
			URL resource = HTMLInfoLoboBrowser.class
					.getResource(propertiesName);
			p.load(resource.openStream());
			String atlasDependecies = p.getProperty("classpath");

			// remove any maven-made :./classes entries
			atlasDependecies = atlasDependecies.replaceAll("\\./classes", "")
					.replaceAll("::", ":");

			// Unnice, but remove all ant-related dependencies. They are only
			// part of the build provess:
			atlasDependecies = atlasDependecies.replaceAll("\\./ant-.*?\\.jar",
					"").replaceAll("::", ":");

			// Add the new libs to the existing list
			libs = atlasDependecies.split(":");
		} catch (IOException e) {
			throw new AtlasExportException(propertiesName + " not found!", e);
		}

		// If the .properties file does not contain a base jar, we add it.
		// Explanation: When
		// running from eclipse, the jars that are part of geopublisher project
		// are not included in the .properties file.
		{
			boolean[] baseJarsDefined = new boolean[BASEJARS.size()];

			for (String j : libs) {
				for (int i = 0; i < BASEJARS.size(); i++)
					if (j.contains(BASEJARS.get(i)))
						baseJarsDefined[i] = true;
			}

			for (int i = 0; i < BASEJARS.size(); i++)
				if (!baseJarsDefined[i])
					libs = LangUtil.extendArray(libs, BASEJARS.get(i));
		}

		// Von Leerstrings bereinigen
		String[] libs2 = new String[0];
		for (String s : libs) {
			s = s.trim();
			if (s.isEmpty())
				continue;
			libs2 = LangUtil.extendArray(libs2, s);
		}

		// Remove any tools.jar to save 12mb
		String[] libs3 = new String[0];
		for (String s : libs2) {
			if (s.startsWith("./tools"))
				continue;
			libs3 = LangUtil.extendArray(libs3, s);
		}

		return libs3;
	}

	private String[] getNativeLibNames() {
		final String nativeLibsLine = GPProps.get(Keys.NativeLibs);

		// LOGGER
		// .debug("These native dependencies have been set in the .properties file: "
		// + nativeLibsLine);

		String[] libs = nativeLibsLine.split(" ");

		// Von Leerstrings bereinigen
		String[] libs2 = new String[0];
		for (String s : libs) {
			s = s.trim();
			if (s.isEmpty())
				continue;
			libs2 = LangUtil.extendArray(libs2, s);
		}
		return libs2;
	}

	/**
	 * @return A {@link Manifest} for the av_resources.jar.
	 * 
	 */
	private Manifest getManifest() {
		final Manifest manifest = new Manifest();
		final Attributes mainAtts = manifest.getMainAttributes();

		mainAtts.put(Attributes.Name.MANIFEST_VERSION, "1.0");

		String classpathString = "";

		// ******************************************************************
		// Adding the DatapoolEntries
		// ******************************************************************
		for (final DpEntry<?> dpe : ace.getUsedDpes()) {
			classpathString += dpe.getId() + ".jar ";
		}

		classpathString += "gt-epsg-hsql-2.6-SNAPSHOT.jar" + " "
				+ AVSWINGGUI_JARNAME + " " + GPCORE_JARNAME + " "
				+ ASCORE_JARNAME + " " + SCHMITZM_JARNAME1 + " "
				+ SCHMITZM_JARNAME2 + " " + SCHMITZM_JARNAME3 + " "
				+ SCHMITZM_JARNAME4 + " " + SCHMITZM_JARNAME5;

		mainAtts.put(Attributes.Name.CLASS_PATH, classpathString);

		mainAtts.put(Attributes.Name.MAIN_CLASS, MAIN_CLASS);
		mainAtts.put(Attributes.Name.SPECIFICATION_TITLE, "Atlas Viewer "
				+ ReleaseUtil.getVersionInfo(GpCoreUtil.class));
		mainAtts.put(Attributes.Name.SPECIFICATION_VENDOR,
				"Stefan Alfons Tzeggai - www.wikisquare.de");
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
	private File getManifestFile() throws IOException, FileNotFoundException {
		final File manifestTempFile = File.createTempFile(
				ATLAS_TEMP_FILE_EXPORTINSTANCE_ID, ".manifest");
		getManifest().write(new FileOutputStream(manifestTempFile));
		return manifestTempFile;
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
	 *             http://java.sun.com/javase/6/docs/technotes/tools/windows/
	 *             jarsigner .html
	 *             http://java.sun.com/javase/6/docs/technotes/tools/windows
	 *             /keytool.html
	 */
	private void jarSign(final File jarFile) throws AtlasExportException {

		SwingUtil.checkNotOnEDT(); // i8n

		info("Signing JAR " + jarFile.getName());

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
		command.add(GPProps.get(Keys.sigp));
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
			LOGGER.debug("Signing " + jarFile + " OK.\n");
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
			 * Exclusively for DISK to real main folder
			 */
			FileUtils.moveFileToDirectory(new File(getTempDir(),
					START_SCRIPT_LINUX), targetDirDISK, true);
			FileUtils.moveFileToDirectory(new File(getTempDir(),
					START_SCRIPT_MAC), targetDirDISK, true);

			FileUtils.moveFileToDirectory(new File(getTempDir(), AUTORUN_INF),
					targetDirDISK, true);
			// Icon.gif is used (and deleted afterwards) by JSmooth
			FileUtils.copyFileToDirectory(new File(getTempDir(), "icon.gif"),
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
			new File(targetDirJWS, "index.html").delete(); // GpHoster puts a
															// "wait please"
															// file there
			FileUtils.moveFileToDirectory(new File(getTempDir(), "index.html"),
					targetDirJWS, true);
			FileUtils.moveFileToDirectory(
					new File(getTempDir(), JNLP_FILENAME), targetDirJWS, true);
			FileUtils.moveFileToDirectory(new File(getTempDir(), "icon.gif"),
					targetDirJWS, true);

			FileUtils.moveFileToDirectory(new File(getTempDir(),
					"splashscreen.png"), targetDirJWS, true);
		}

		/**
		 * For both
		 */
		final Collection<File> jars = FileUtils.listFiles(getTempDir(),
				new String[] { "jar", "jar.pack.gz", "so", "dll" }, true);
		for (final File jar : jars) {

			/**
			 * When a JAR comes from .../diffDir/a.jar, we have to copy it to
			 * folder "diffDir"
			 **/
			final String diffDir = jar.getAbsolutePath().substring(
					getTempDir().getAbsolutePath().length() + 1);

			/**
			 * Copy files to DISK/#DISK_SUB_DIR, but not libs that reside on
			 * www.geopublishing.org
			 */
			if (toDisk && !jar.getName().endsWith("pack.gz")) {

				final File targetSubDirDISK = new File(targetDirDISK,
						DISK_SUB_DIR + diffDir).getParentFile();

				targetSubDirDISK.mkdirs();

				FileUtils.copyFileToDirectory(jar, targetSubDirDISK);
			}

			/**
			 * Copy files to JWS Skipping over BASEJARS since they are
			 * referenced online now
			 */
			if (toJws
					&& !ArrayUtils.contains(getJarAndNativeLibNames(), "./"
							+ jar.getName())
					&& !BASEJARS.contains(jar.getName())) {
				final File targetSubDirJWS = new File(targetDirJWS, diffDir)
						.getParentFile();
				targetSubDirJWS.mkdirs();
				FileUtils.moveFileToDirectory(jar, targetSubDirJWS, true);
			}
		}

		if (toJws) {
			FileUtils.copyFileToDirectory(new File(getTempDir(), "README.TXT"),
					targetDirJWS);
			FileUtils.copyFileToDirectory(
					new File(getTempDir(), "license.html"), targetDirJWS);

			/**
			 * Copy the .htaccess from the deploy directory into the target
			 * directory
			 */
			{
				// command.add(ACProps.get(Keys.signingKeystore));
				final URL htaccessURL = JarExportUtil.class
						.getResource(HTACCESS_RES_LOCATION);
				FileUtils.copyURLToFile(htaccessURL, new File(targetDirJWS,
						".htaccess"));
			}
		}

		if (toDisk) {
			FileUtils
					.moveFileToDirectory(
							new File(getTempDir(), "license.html"),
							targetDirDISK, true);
			FileUtils.moveFileToDirectory(new File(getTempDir(), "README.TXT"),
					targetDirDISK, true);
		}

	}

	/**
	 * @return the temp directory where jars are assembled. it is a random
	 *         folder in the system temp directory: <br/>
	 *         <code>new File(IOUtil.getTempDir(), AVUtil.ATLAS_TEMP_FILE_EXPORTINSTANCE_ID
			+ "export" + AVUtil.RANDOM.nextInt(1999) + 1000);</code>
	 */
	public File getTempDir() {
		return tempDir;
	}

	/**
	 * If <code>true</code>, an exported atlas DISK folder will automatically be
	 * zipped with a nice filename.
	 */
	public boolean isZipDiskAfterExport() {
		return zipDiskAfterExport;
	}

	/**
	 * @param zipDiskAfterExport
	 *            If <code>true</code>, an exported atlas DISK folder will
	 *            automatically be zipped with a nice filename.
	 */
	public void setZipDiskAfterExport(boolean zipDiskAfterExport) {
		this.zipDiskAfterExport = zipDiskAfterExport;
	}

	/**
	 * It set to a value not <code>null</code>, the jnlp base url stored in the
	 * {@link AtlasConfigEditable} is overwritten with this value. If
	 * <code>null</code>, the value from the <code>atlas.xml</code> is used.
	 */
	public void setOverwriteJnlpBaseUrl(URL jwsUrl) {
		overwriteJnlpUrl = jwsUrl;
	}

	public boolean isFilterUnused() {
		return filterUnused;
	}

	public void setFilterUnused(boolean filterUnused) {
		this.filterUnused = filterUnused;
	}

}
