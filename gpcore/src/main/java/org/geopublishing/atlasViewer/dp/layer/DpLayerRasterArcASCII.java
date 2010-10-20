/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.dp.layer;

import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageReader;
import it.geosolutions.imageio.plugins.arcgrid.spi.AsciiGridsImageReaderSpi;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geopublishing.atlasViewer.swing.AVDialogManager;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.internal.AtlasExportTask;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.styling.Style;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.geotools.grid.GridUtil;
import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.geotools.io.GeoImportUtil.ARCASCII_POSTFIXES;
import schmitzm.geotools.io.GeoImportUtil.WORLD_POSTFIXES;
import schmitzm.io.IOUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.RasterLegendData;
import skrueger.geotools.StyledRasterInterface;
import skrueger.geotools.ZoomRestrictableGridInterface;
import skrueger.i8n.Translation;

import com.vividsolutions.jts.geom.Envelope;

public class DpLayerRasterArcASCII extends DpLayer<ArcGridReader, ChartStyle>
		implements StyledRasterInterface<ArcGridReader>,
		ZoomRestrictableGridInterface {

	static final private Logger LOGGER = Logger
			.getLogger(DpLayerRasterArcASCII.class);

	/**
	 * caches the {@link GridCoverage2D} Can be un-cached by calling uncache()
	 */
	protected ArcGridReader gc;

	private RasterLegendData legendMetaData;

	/**
	 * Creates an empty {@link DpLayerRasterArcASCII}.
	 * 
	 * @param ac
	 *            {@link AtlasConfig}
	 */
	public DpLayerRasterArcASCII(AtlasConfig ac) {
		super(ac);
		setType(DpEntryType.RASTER);
	}

	/**
	 * Exports the raster file and all related files. Only failure on the main
	 * file produces an {@link IOException}
	 */
	@Override
	public void exportWithGUI(Component owner) throws IOException {

		AtlasExportTask exportTask = new AtlasExportTask(owner, getTitle()
				.toString()) {

			@Override
			protected Boolean doInBackground() throws Exception {

				setPrefix("Exporting ");

				try {
					// waitDialog.setVisible(false);
					exportDir = AVSwingUtil.selectExportDir(owner,
							getAtlasConfig());
					// waitDialog.setVisible(true);

					if (exportDir == null) {
						// The fodler selection was cancelled.
						return false;
					}

					URL url = AVSwingUtil.getUrl(DpLayerRasterArcASCII.this,
							owner);
					final File file = new File(exportDir, getFilename());

					// ****************************************************************************
					// Copy main file and possibly throw an Exception
					// ****************************************************************************
					publish(file.getAbsolutePath());
					FileUtils.copyURLToFile(AVSwingUtil.getUrl(
							DpLayerRasterArcASCII.this, owner), file);

					// Try to copy pending world files...
					for (WORLD_POSTFIXES pf : GeoImportUtil.WORLD_POSTFIXES
							.values()) {
						final File changeFileExt = IOUtil.changeFileExt(file,
								pf.toString());
						publish(changeFileExt.getAbsolutePath());
						AtlasConfig.exportURLtoFileNoEx(
								IOUtil.changeUrlExt(url, pf.toString()),
								changeFileExt);
					}

					final File changeFileExt = IOUtil
							.changeFileExt(file, "prj");
					publish(changeFileExt.getAbsolutePath());
					AtlasConfig.exportURLtoFileNoEx(
							IOUtil.changeUrlExt(url, "prj"), changeFileExt);
					AtlasConfig.exportURLtoFileNoEx(
							IOUtil.changeUrlExt(url, "sld"),
							IOUtil.changeFileExt(file, "sld"));
					publish("done");
					success = true;
				} catch (Exception e) {
					done();
				}
				return success;
			}

		};
		exportTask.execute();
	}

	/**
	 * This method is caching the geotools object, and can be uncached by
	 * calling uncache()
	 */
	@Override
	public ArcGridReader getGeoObject() {

		try {

			if (gc == null) {

				/**
				 * Can we define transparent colors here?
				 */
				GeneralParameterValue[] readParams = null;

				URL url = getUrl();

				final String filename = getFilename().toLowerCase();

				// ****************************************************************************
				// Check if the ending suggests a Arc/Info ASCII Grid
				// ****************************************************************************
				for (ARCASCII_POSTFIXES ending : GeoImportUtil.ARCASCII_POSTFIXES
						.values()) {
					if (filename.endsWith(ending.toString())) {
						// MS-01.sc
						// gc = GeoImportUtil.GridFromArcInfoASCII(url);
						// gc = new ArcGridReader(url);

						final AsciiGridsImageReader reader = (AsciiGridsImageReader) new AsciiGridsImageReaderSpi()
								.createReaderInstance();
						reader.setInput(url);

						// MS-01.ec
						setType(DpEntryType.RASTER_ARCASCII);
					}
				}

				if (gc == null)
					throw (new IllegalArgumentException(
							"File doesn't seem to be a GeoTIFF nor a GIF"));

				// Create an Envelope that contains all information of the
				// raster
				// MS-01.sc
				// Envelope e = gc.getEnvelope();
				// envelope = new com.vividsolutions.jts.geom.Envelope(e
				// .getUpperCorner().getOrdinate(0), // X1
				// e.getLowerCorner().getOrdinate(0), // X2
				// e.getUpperCorner().getOrdinate(1), // Y1dddd
				// e.getLowerCorner().getOrdinate(1) // Y2
				// );

				envelope = new Envelope(gc.getOriginalEnvelope().getMinimum(0),
						gc.getOriginalEnvelope().getMaximum(0), gc
								.getOriginalEnvelope().getMinimum(1), gc
								.getOriginalEnvelope().getMaximum(1));

				crs = gc.getCrs();
			}

			return gc;

		} catch (Exception e) {
			throw new RuntimeException(
					"Exception while accessing the GeoObject", e);
		}
	}

	/**
	 * Returns the cached {@link Style} for this Layer. Tries to load the
	 * {@link Style} from a file with the same URL but the ending
	 * <code>.sld</code>. If it doesn't exist, returns a default RasterStyle.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	public Style getStyle() {
		// if (StyledLayerUtil.isStyleable(this) && super.getStyle() == null) {
		if (super.getStyle() == null) {
			setStyle(GridUtil.createDefaultStyle());
		}
		return super.getStyle();
	}

	/**
	 * Clears all memory-intensive cache objects
	 */
	@Override
	public void uncache() {
		LOGGER.debug("unchaching " + getId() + " aka " + getTitle());
		super.uncache();

		/** Close any open attribute table for this layer */
		AVDialogManager.dm_AtlasRasterStyler.disposeInstanceFor(this);

		if (gc != null) {
			gc.dispose();
			gc = null;
		}

		envelope = null;
	}

	/**
	 * This method returns the value/{@link Translation} pairs that will be
	 * shown in the Legend
	 */
	public RasterLegendData getLegendMetaData() {
		if (legendMetaData == null) {
			legendMetaData = new RasterLegendData(false);
		}
		return legendMetaData;
	}

	public void dispose() {
		if (isDisposed())
			return;
		disposed = true;
		uncache();
	}

	/**
	 * Calculates the width's resolution in it's
	 * {@link CoordinateReferenceSystem}:
	 * 
	 * @return width in CRS units divided by pixel width
	 */
	public Double getMaxResolution() {
		// MS-01.sc
		// try {
		// double pixelwidth = getGeoObject().getGridGeometry().getGridRange()
		// .getHigh().getCoordinateValues()[0];
		// double crswidth = getGeoObject().getEnvelope().getUpperCorner()
		// .getDirectPosition().getCoordinate()[0]
		// - getGeoObject().getEnvelope().getLowerCorner()
		// .getDirectPosition().getCoordinate()[0];
		// // LOGGER.debug("resolution of " + getTitle().toString() + " = "
		// // + crswidth / pixelwidth);
		// return crswidth / pixelwidth;
		// } catch (Exception e) {
		// LOGGER.error(e);
		//
		// return 0.;
		// }
		LOGGER.warn("DpLayerRaster.getMaxResolution() not yet implemented for AbstractGridCoverage2DReader!");
		return null;
		// MS-01.ec
	}

	@Override
	public Double getMinResolution() {
		return null;
	}

	public void setLegendMetaData(RasterLegendData legendMetaData) {
		this.legendMetaData = legendMetaData;
	}

	public DpLayerRasterArcASCII copy() {
		DpLayerRasterArcASCII copy = new DpLayerRasterArcASCII(ac);
		return (DpLayerRasterArcASCII) copyTo(copy);
	}

	@Override
	public DpLayer<ArcGridReader, ChartStyle> copyTo(
			DpLayer<ArcGridReader, ChartStyle> target) {

		DpLayerRasterArcASCII copy = (DpLayerRasterArcASCII) super
				.copyTo(target);

		copy.setLegendMetaData(getLegendMetaData()); // TODO should be copied!

		return copy;
	}

}
