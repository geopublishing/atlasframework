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

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.AMLImport;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geotools.feature.FeatureCollection;
import org.geotools.gce.imagepyramid.ImagePyramidFormat;
import org.geotools.gce.imagepyramid.ImagePyramidReader;
import org.geotools.resources.coverage.FeatureUtilities;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

import schmitzm.geotools.grid.GridUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.lang.LangUtil;
import skrueger.RasterLegendData;
import skrueger.geotools.StyledLayerUtil;
import skrueger.geotools.StyledRasterPyramidInterface;
import skrueger.geotools.ZoomRestrictableGridInterface;
import skrueger.i8n.Translation;

/**
 * This is a raster {@link DpEntry} that is backed by an image pyramid. The
 * "main file" is a .properties file as described here:
 * http://javadoc.geotools.fr
 * /2.3/org/geotools/gce/imagepyramid/ImagePyramidReader.html
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public class DpLayerRasterPyramid
		extends
		DpLayer<FeatureCollection<SimpleFeatureType, org.opengis.feature.simple.SimpleFeature>, ChartStyle>
		implements StyledRasterPyramidInterface, ZoomRestrictableGridInterface {

	static private final Logger LOGGER = Logger
			.getLogger(DpLayerRasterPyramid.class);

	private RasterLegendData legendMetaData;

	@Override
	public Style getStyle() {
		super.getStyle();
		if (StyledLayerUtil.isStyleable(this) && super.getStyle() == null)
			setStyle(GridUtil.createDefaultStyle());
		return super.getStyle();
	}

	private FeatureCollection<SimpleFeatureType, SimpleFeature> wrappedReader;

	/**
	 * A color that is interpreted as transparent in the source images. May be
	 * null. This can also be set in the AtlasML *
	 */
	private Color inputTransparentColor = null;

	/**
	 * Default constructor, typically used by {@link AMLImport}
	 * 
	 * @param ac
	 */
	public DpLayerRasterPyramid(AtlasConfig ac) {
		super(ac);
		setType(DpEntryType.RASTER_PYRAMID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.atlas.datapool.layer.DpLayer#getGeoObject()
	 */
	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getGeoObject() {
		try {

			if (wrappedReader == null) {

				GeneralParameterValue[] params = new GeneralParameterValue[] {};

				if (getInputTransparentColor() != null) {
					{
						ParameterValue<Color> inputTransarent = ImagePyramidFormat.INPUT_TRANSPARENT_COLOR
								.createValue();
						inputTransarent.setValue(getInputTransparentColor());
						params = LangUtil.extendArray(params, inputTransarent);
					}

					{
						ParameterValue<double[]> backgroundValues = ImagePyramidFormat.BACKGROUND_VALUES
								.createValue();
						backgroundValues
								.setValue(new double[] { 0., 0., 0., 0. });
						params = LangUtil.extendArray(params, backgroundValues);
					}

					{
						ParameterValue<Color> outputTransp = ImagePyramidFormat.OUTPUT_TRANSPARENT_COLOR
								.createValue();
						outputTransp.setValue(new Color(0, true));
						params = LangUtil.extendArray(params, outputTransp);
					}

				}

				final ImagePyramidReader gridCoverageReader = new ImagePyramidReader(
						getUrl());
				wrappedReader = FeatureUtilities.wrapGridCoverageReader(
						gridCoverageReader, params);

				//
				// reader = new ImagePyramidReader(getUrl(), hints) {
				// @Override
				// public Format getFormat() {
				// return new ImagePyramidFormat() {
				//
				// @Override
				// protected void setInfo() {
				// super.setInfo();
				//
				// /** Control the transparency of the output coverage. */
				// final DefaultParameterDescriptor INPUT_TRANSPARENT_COLOR =
				// new DefaultParameterDescriptor(
				// "InputTransparentColor", Color.class, null,
				// getInputTransparentColor());
				//
				// // reading parameters
				// readParameters = new ParameterGroup(
				// new DefaultParameterDescriptorGroup(mInfo,
				// new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D,
				// INPUT_TRANSPARENT_COLOR,
				// INPUT_IMAGE_THRESHOLD_VALUE ,OUTPUT_TRANSPARENT_COLOR}));
				//
				//
				// }
				// };
				// }
				// };
				//
				// ParameterValueGroup readParameters =
				// reader.getFormat().getReadParameters();
				//
				// readParameters.parameter("InputTransparentColor").setValue(getInputTransparentColor());
				// readParameters.parameter("OutputTransparentColor").setValue(getInputTransparentColor());
				//
				// readParameters = reader.getFormat().getReadParameters();

				// TODO
				// reader.setInputTransparentColor(getInputTransparentColor());

				crs = gridCoverageReader.getCrs();

				// Create an Envelope that contains all information of the
				// raster
				Envelope e = gridCoverageReader.getOriginalEnvelope();
				envelope = new com.vividsolutions.jts.geom.Envelope(e
						.getUpperCorner().getOrdinate(0), // X1
						e.getLowerCorner().getOrdinate(0), // X2
						e.getUpperCorner().getOrdinate(1), // Y1
						e.getLowerCorner().getOrdinate(1) // Y2
				);
			}
			return wrappedReader;

		} catch (Exception e) {
			throw new RuntimeException(
					"Exception while accessing the GeoObject", e);
		}
	};

	/**
	 * Define a color from the source images that will be interpretated as
	 * transparent
	 * 
	 * @param inputTransparentColor
	 *            may be null for NoValue Color
	 */
	public void setInputTransparentColor(Color inputTransparentColor) {
		this.inputTransparentColor = inputTransparentColor;
	}

	public Color getInputTransparentColor() {
		return inputTransparentColor;
	}

	@Override
	public void dispose() {
		if (isDisposed())
			return;
		uncache();
	}

	/**
	 * Help the GC by uncaching
	 */
	@Override
	public void uncache() {
		if (wrappedReader != null) {
			// reader.dispose();
			wrappedReader = null;
		}
	}

	/**
	 * Returns the maximum allowed resolution for this RasterPyramid.
	 */
	@Override
	public Double getMaxResolution() {

		// TODO This feature has been disabled because it's not in trunk.
		// final double[] highestRes = getGeoObject().getHighestRes();
		// if (highestRes != null)
		// return highestRes[0];
		return null;
	}

	/**
	 * Returns the minimum allowed resolution for this RasterPyramid.
	 */
	@Override
	public Double getMinResolution() {
		return null;
	}

	/**
	 * PyramidLayers are far to complex to be exportable right now...
	 */
	@Override
	public Boolean isExportable() {
		return false;
	}

	/**
	 * This method returns the value/{@link Translation} pairs that will be
	 * shown in the Legend
	 */
	@Override
	public RasterLegendData getLegendMetaData() {
		if (legendMetaData == null) {
			legendMetaData = new RasterLegendData(false);
		}
		return legendMetaData;
	}

	public void setLegendMetaData(RasterLegendData legendMetaData) {
		this.legendMetaData = legendMetaData;
	}

	@Override
	public DpLayer<FeatureCollection<SimpleFeatureType, SimpleFeature>, ChartStyle> copy() {
		DpLayerRasterPyramid copy = new DpLayerRasterPyramid(ac);
		return copyTo(copy);
	}

	@Override
	public DpLayer<FeatureCollection<SimpleFeatureType, SimpleFeature>, ChartStyle> copyTo(
			DpLayer<FeatureCollection<SimpleFeatureType, SimpleFeature>, ChartStyle> target) {
		DpLayerRasterPyramid copy = (DpLayerRasterPyramid) super.copyTo(target);

		copy.setInputTransparentColor(getInputTransparentColor());
		copy.setLegendMetaData(getLegendMetaData()); // TODO Copyable! copy it!

		return copy;
	}

	@Override
	public void exportWithGUI(Component owner) throws IOException {
		// TODO Auto-generated method stub
		LOGGER.warn("not implemented"); // TODO
	}

}
