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

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.GsRest;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasCancelException;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSourceShapefile;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.exceptions.AtlasExportException;
import org.netbeans.spi.wizard.ResultProgressHandle;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import skrueger.geotools.io.GsServerSettings;

/**
 * This class exports an {@link AtlasConfigEditable} into a DISK and/or JWS
 * folder. The DISK folder can be burned to CDROM and will autostart the Atlas
 * under windows. <br>
 * The exported DISK directory also contains a <code>start.bat</code> and a
 * <code>atlas.exe</code> to launch the atlas if autostart is disabled.<br>
 * The JWS folder may be served by any www. Linking to the <code>.jnlp</code>
 * file will start the atlas using JavaWebStart
 */
public class AtlasGeoserverExporter extends AbstractAtlasExporter {
	final static private Logger LOGGER = Logger
			.getLogger(AtlasGeoserverExporter.class);
	private final File tempDir = new File(IOUtil.getTempDir(),
			ATLAS_TEMP_FILE_EXPORTINSTANCE_ID + AVUtil.RANDOM.nextInt(19999)
					+ 10000);

	private final GsServerSettings settings;
	private final GsRest gsRest;
	private String namespace;

	public AtlasGeoserverExporter(final AtlasConfigEditable ace,
			GsServerSettings settings) throws IOException {
		super(ace);
		this.settings = settings;

		gsRest = new GsRest(settings.getUrl(), settings.getUsername(),
				settings.getPassword());
		namespace = "http://www.geopublishing.org/" + ace.getBaseName();
	}

	@Override
	public void export(ResultProgressHandle progress) throws Exception {
		String wsName = ace.getBaseName();

		gsRest.deleteWorkspace(wsName);

		gsRest.createWorkspace(wsName);

		for (DpEntry dpe : ace.getUsedDpes()) {
			if (dpe instanceof DpLayerVectorFeatureSourceShapefile) {
				DpLayerVectorFeatureSourceShapefile dpshp = (DpLayerVectorFeatureSourceShapefile) dpe;

				String relPath = "file:data/myatlas/" + dpe.getDataDirname()
						+ "/" + dpe.getFilename();

				String dsName = dpshp.getFilename();
				gsRest.createDatastoreShapefile(wsName, dsName, namespace,
						relPath, dpe.getCharset().toString());

				String ftName = dsName;
				gsRest.createFeatureType(wsName, dsName, ftName);

				String stylename = wsName + "_" + dsName;

				String sldString = StylingUtil.sldToString(dpshp.getStyle());
				gsRest.createSld(stylename, sldString);
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
	public void exportOld(final ResultProgressHandle progress) throws Exception {

		this.progress = progress;

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
		// totalSteps += getJarAndNativeLibNames().length;

		info(GpUtil.R("ExportDialog.processWindowTitle.Exporting"));

		// Try catch to always delete the temp folder
		try {

			// Export the additional atlas fonts, go though all fonts in
			// font dir and only export the ones that are readbale
			{
				File fontsDir = ace.getFontsDir();

				Collection<File> listFiles = FileUtils.listFiles(fontsDir,
						GpUtil.FontsFilesFilter,
						GpUtil.BlacklistedFoldersFilter);
				for (File f : listFiles) {
					try {
						Font createFont = Font
								.createFont(Font.TRUETYPE_FONT, f);

						String relPath = f.getAbsolutePath().substring(
								fontsDir.getAbsolutePath().length() + 1);

						// addToJar(targetJar, ace.getAtlasDir(),
						// AtlasConfig.ATLASDATA_DIRNAME + "/"
						// + AtlasConfig.FONTS_DIRNAME + "/"
						// + relPath);

					} catch (Exception e) {
						LOGGER.warn("Not adding "
								+ f
								+ " to jar, because it can't be loaded correctly.");
					}
				}

			}
			// Store the settings
			ace.getProperties().save(
					new File(ace.getAtlasDir(),
							AVProps.PROPERTIESFILE_RESOURCE_NAME));
			// addToJar(targetJar, ace.getAtlasDir(),
			// AVProps.PROPERTIESFILE_RESOURCE_NAME);

			/**
			 * Look for a user-defined splashscreen. If it doesn't exist, ask
			 * the user if he wants to use the default one.
			 */
			if (ace.getResource(AtlasConfig.SPLASHSCREEN_RESOURCE_NAME) == null) {
				FileUtils.copyURLToFile(GpUtil.class
						.getResource(SPLASHSCREEN_RESOURCE_NAME_FALLBACK),
						new File(ace.getAtlasDir(),
								AtlasConfig.SPLASHSCREEN_RESOURCE_NAME));
				// addToJar(targetJar, ace.getAtlasDir(),
				// AtlasConfig.SPLASHSCREEN_RESOURCE_NAME);
			} else {
				// addToJar(targetJar, ace.getAtlasDir(),
				// AtlasConfig.SPLASHSCREEN_RESOURCE_NAME);
			}

			/**
			 * Look for a user-defined icon.gif. If not available, copy the
			 * default icon to where we expect the user icon and then add it to
			 * the jar.
			 */
			URL iconURL = ace.getResource(AtlasConfig.JWSICON_RESOURCE_NAME);
			if (iconURL == null) {
				iconURL = GpUtil.class
						.getResource(AtlasConfig.JWSICON_RESOURCE_NAME_FALLBACK);
				FileUtils.copyURLToFile(iconURL, new File(ace.getAtlasDir(),
						AtlasConfig.JWSICON_RESOURCE_NAME));
			}
			// addToJar(targetJar, ace.getAtlasDir(),
			// AtlasConfig.JWSICON_RESOURCE_NAME);

			// addToJar(targetJar, ace.getAtlasDir(),
			// AtlasConfig.MAPICON_RESOURCE_NAME);

			/*
			 * Exporting the defaultcrs.prj if it exists
			 */
			final File defaultCrsFile = new File(ace.getAd(),
					AtlasConfig.DEFAULTCRS_FILENAME);
			if (defaultCrsFile.exists() && defaultCrsFile.length() > 2) {
				// addToJar(targetJar, ace.getAtlasDir(), "ad/"
				// + AtlasConfig.DEFAULTCRS_FILENAME);
			} else {
				LOGGER.info("Not exporting defaultcrs.prj beacuse it doesn't exist or is too small");
			}

			// File[] listOfIndexJars = new File[] { targetJar };
			// Creating a JAR for every DpEntry
			LOGGER.debug("Creating a JAR for every DpEntry used");
			for (final DpEntry<?> dpe : ace.getUsedDpes()) {

				// createJarFromDpe(dpe);
			}

			checkAbort();

			/**
			 * The icon.gif is needed for DISK and JWS. (DISK will put it into
			 * the atlas.exe and delete it afterwards)
			 */
			// FileUtils.copyURLToFile(iconURL, new File(
			// targetJar.getParentFile(), "icon.gif"));
		} catch (final AtlasCancelException cancel) {
			// // In that case we delete the target directory.
			info(GpUtil.R("Export.Cancelled.Msg"));
			throw cancel;
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
			IOUtil.zipDir(targetDirDISK2, zipFile, targetDirDISK2);

		} catch (Exception e) {
			throw new AtlasExportException("Failed to create ZIP file "
					+ IOUtil.escapePath(zipFile), e);
		}
	}

}
