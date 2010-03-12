/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
///*******************************************************************************
// * Copyright (c) 2009 Stefan A. Krüger.
// * 
// * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
// * http://www.geopublishing.org
// * 
// * AtlasViewer is part of the Geopublishing Framework hosted at:
// * http://wald.intevation.org/projects/atlas-framework/
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public License
// * as published by the Free Software Foundation; either version 3
// * of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License (license.txt)
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// * or try this link: http://www.gnu.org/licenses/lgpl.html
// * 
// * Contributors:
// *     Stefan A. Krüger - initial API and implementation
// ******************************************************************************/
///*
// * Geotools 2 - OpenSource mapping toolkit
// * (C) 2006, Geotools Project Managment Committee (PMC)
// *
// *    This library is free software; you can redistribute it and/or
// *    modify it under the terms of the GNU Lesser General Public
// *    License as published by the Free Software Foundation; either
// *    version 2.1 of the License, or (at your option) any later version.
// *
// *    This library is distributed in the hope that it will be useful,
// *    but WITHOUT ANY WARRANTY; without even the implied warranty of
// *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// *    Lesser General Public License for more details.
// *
// *    You should have received a copy of the GNU Lesser General Public
// *    License along with this library; if not, write to the Free Software
// *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// */
//package skrueger.atlas.gui.map;
//
//import java.awt.Color;
//import java.awt.Rectangle;
//import java.awt.RenderingHints;
//import java.awt.Transparency;
//import java.awt.geom.AffineTransform;
//import java.awt.geom.Area;
//import java.awt.geom.Point2D;
//import java.awt.image.BufferedImage;
//import java.awt.image.ColorModel;
//import java.awt.image.DataBuffer;
//import java.awt.image.IndexColorModel;
//import java.awt.image.renderable.ParameterBlock;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.UnsupportedEncodingException;
//import java.lang.ref.SoftReference;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Properties;
//
//import javax.imageio.ImageIO;
//import javax.imageio.ImageReadParam;
//import javax.imageio.stream.ImageInputStream;
//import javax.media.jai.ImageLayout;
//import javax.media.jai.JAI;
//import javax.media.jai.ParameterBlockJAI;
//import javax.media.jai.PlanarImage;
//import javax.media.jai.ROI;
//import javax.media.jai.operator.AWTImageDescriptor;
//import javax.media.jai.operator.MosaicDescriptor;
//
//import org.apache.log4j.Logger;
//import org.geotools.coverage.grid.GridCoverage2D;
//import org.geotools.coverage.grid.GridGeometry2D;
//import org.geotools.coverage.grid.io.AbstractGridFormat;
//import org.geotools.data.AbstractDataStore;
//import org.geotools.data.DataSourceException;
//import org.geotools.data.FeatureSource;
//import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.factory.FactoryRegistryException;
//import org.geotools.factory.Hints;
//import org.opengis.feature.simple.SimpleFeature;
//import org.geotools.gce.imagemosaic.ImageMosaicFormat;
//import org.geotools.gce.imagemosaic.ImageMosaicReader;
//import org.geotools.gce.imagemosaic.MemorySpatialIndex;
//import org.geotools.geometry.GeneralEnvelope;
//import org.geotools.geometry.jts.ReferencedEnvelope;
//import org.geotools.image.ImageWorker;
//import org.geotools.parameter.Parameter;
//import org.geotools.referencing.CRS;
//import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
//import org.geotools.resources.image.ImageUtilities;
//import org.geotools.util.SoftValueHashMap;
//import org.opengis.coverage.grid.Format;
//import org.opengis.coverage.grid.GridCoverage;
//import org.opengis.parameter.GeneralParameterValue;
//import org.opengis.referencing.FactoryException;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;
//import org.opengis.referencing.datum.PixelInCell;
//import org.opengis.referencing.operation.MathTransform;
//import org.opengis.referencing.operation.NoninvertibleTransformException;
//import org.opengis.referencing.operation.TransformException;
//
//import schmitzm.io.IOUtil;
//import schmitzm.swing.SwingUtil;
//import skrueger.atlas.AVProps;
//import skrueger.atlas.gui.PyramidEvaluationJFrame;
//
//import com.vividsolutions.jts.geom.Envelope;
//
///**
// * This reader is repsonsible for providing access to mosaic of georeferenced
// * images. Citing JAI documentation:
// * 
// * The "Mosaic" operation creates a mosaic of two or more source images. This
// * operation could be used for example to assemble a set of overlapping
// * geospatially rectified images into a contiguous image. It could also be used
// * to create a montage of photographs such as a panorama.
// * 
// * All source images are assumed to have been geometrically mapped into a common
// * coordinate space. The origin (minX, minY) of each image is therefore taken to
// * represent the location of the respective image in the common coordinate
// * system of the sour ce images. This coordinate space will also be that of the
// * destination image.
// * 
// * All source images must have the same data type and sample size for all bands
// * and have the same number of bands as color components. The destination will
// * have the same data type, sample size, and number of bands and color
// * components as the sources.
// * 
// * TODO This class is extended/changed by Stefan Krueger. The documentation is
// * not recent
// * 
// * @author Simone Giannecchini
// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
// *         Kr&uuml;ger</a>#
// * @since 2.3.atlas
// * 
// */
//public final class AtlasImageMosaicReader extends
//		org.geotools.coverage.grid.io.AbstractGridCoverage2DReader {
//
//	/** Logger. */
//	private final static Logger LOGGER = Logger
//			.getLogger(AtlasImageMosaicReader.class);
//
//	/**
//	 * The source {@link URL} pointing to the index shapefile for this
//	 * {@link AtlasImageMosaicReader}.
//	 */
//	private final URL sourceURL;
//
//	/** {@link AbstractDataStore} pointd to the index shapefile. */
//	private final AbstractDataStore tileIndexStore;
//
//	/** {@link SoftReference} to the index holding the tiles' envelopes. */
//	private final SoftReference<MemorySpatialIndex> index;
//
//	/**
//	 * This is a cache for the tiles. Its static, so ALL Mosaics use the same
//	 * chache
//	 */
//	static final SoftValueHashMap tilesCache = new SoftValueHashMap(15);
//
//	/**
//	 * The typename of the chems inside the {@link ShapefileDataStore} that
//	 * contains the index for this {@link AtlasImageMosaicReader}.
//	 */
//	private final String typeName;
//
//	/** {@link FeatureSource} for the shape index. */
//	private final FeatureSource featureSource;
//
//	/**
//	 * This {@link BufferedImage} is cached in memory since it is used whenever
//	 * I need to build up a fake mosaic.
//	 */
//	private final BufferedImage unavailableImage;
//
//	private boolean expandMe;
//
//	private Color inputTransparentColor;
//
//	private Color outputTransparentColor;
//
//	private double inputImageThreshold;
//
//	/**
//	 * Max number of tiles that this plugin will load.
//	 * 
//	 * If this number is exceeded, i.e. we request an area which is too large
//	 * instead of getting stuck ith opening thousands of files I give you back a
//	 * fake coverage.
//	 * 
//	 * The parameter is backed by parameter
//	 */
//	public static int MAX_TILES;
//
//	/**
//	 * Constructor.
//	 * 
//	 * @param source
//	 *            The source object.
//	 * @throws IOException
//	 * @throws UnsupportedEncodingException
//	 * 
//	 */
//	public AtlasImageMosaicReader(final Object source, final Hints uHints)
//			throws IOException {
//
//		MAX_TILES = AVProps.getInt(AVProps.Keys.maxMosaicTiles, 300);
//
//		// /////////////////////////////////////////////////////////////////////
//		// 
//		// Forcing longitude first since the geotiff specification seems to
//		// assume that we have first longitude then latitude.
//		//
//		// /////////////////////////////////////////////////////////////////////
//		if (uHints != null) {
//			// prevent the use from reordering axes
//			this.hints.add(uHints);
//		}
//		if (source == null) {
//
//			final IOException ex = new IOException(
//					"ImageMosaicReader:No source set to read this coverage.");
//			LOGGER.warn(ex.getLocalizedMessage(), ex);
//			throw new DataSourceException(ex);
//		}
//		this.source = source;
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Check source
//		//
//		// /////////////////////////////////////////////////////////////////////
//		if (source instanceof URL)
//			this.sourceURL = (URL) source;
//		else
//			throw new IllegalArgumentException("This plugin accepts only URL");
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Load tiles informations, especially the bounds, which will be
//		// reused
//		//
//		// /////////////////////////////////////////////////////////////////////
//		tileIndexStore = new ShapefileDataStore(this.sourceURL);
//		// LOGGER.debug("Connected mosaic reader to its data store "+
//		// sourceURL.toString());
//		final String[] typeNames = tileIndexStore.getTypeNames();
//		if (typeNames.length <= 0)
//			throw new IllegalArgumentException(
//					"Problems when opening the index, no typenames for the schema are defined");
//
//		typeName = typeNames[0];
//		featureSource = tileIndexStore.getFeatureSource(typeName);
//
//		// //
//		//
//		// Load all the features inside the index
//		//
//		// //
//		// LOGGER.debug("About to create index");
//		index = new SoftReference<MemorySpatialIndex>(new MemorySpatialIndex(
//				featureSource.getFeatures()));
//		// LOGGER.debug("Created index");
//		// //
//		//
//		// get the crs if able to
//		//
//		// //
//		final Object tempCRS = this.hints
//				.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);
//		if (tempCRS != null) {
//			this.crs = (CoordinateReferenceSystem) tempCRS;
//			LOGGER.warn(new StringBuffer(
//					"Using forced coordinate reference system ").append(
//					crs.toWKT()).toString());
//		} else {
//			final CoordinateReferenceSystem tempcrs = featureSource.getSchema()
//					.getDefaultGeometry().getCoordinateSystem();
//			if (tempcrs == null) {
//				// use the default crs
//				crs = AbstractGridFormat.getDefaultCRS();
//				LOGGER
//						.warn(new StringBuffer(
//								"Unable to find a CRS for this coverage, using a default one: ")
//								.append(crs.toWKT()).toString());
//			} else
//				crs = tempcrs;
//		}
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Load properties file with information about levels and envelope
//		//
//		// /////////////////////////////////////////////////////////////////////
//		// property file
//		loadProperties();
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Get Transparency defaults
//		//
//		// /////////////////////////////////////////////////////////////////////
//		inputTransparentColor = (Color) ImageMosaicFormat.INPUT_TRANSPARENT_COLOR
//				.getDefaultValue();
//		outputTransparentColor = (Color) ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR
//				.getDefaultValue();
//		inputImageThreshold = ((Double) ImageMosaicFormat.INPUT_IMAGE_THRESHOLD_VALUE
//				.getDefaultValue()).doubleValue();
//
//		// load the unavailaible pattern
//		unavailableImage = ImageIO.read(ImageMosaicReader.class
//				.getResource("unav.png"));
//	}
//
//	/**
//	 * Loads the properties file that contains useful information about this
//	 * coverage.
//	 * 
//	 * @throws UnsupportedEncodingException
//	 * @throws IOException
//	 * @throws FileNotFoundException
//	 */
//	private void loadProperties() throws UnsupportedEncodingException,
//			IOException {
//
//		final Properties properties = new Properties();
//		final InputStream openStream = changeUrlExt(sourceURL, "properties")
//				.openStream();
//		properties.load(openStream);
//
//		// If a transparency color is given use it, null equals "no
//		// transparency"
//		String transparentColorString = properties.getProperty(
//				"TransparentColor", null);
//		if (transparentColorString != null)
//			try {
//				inputTransparentColor = SwingUtil
//						.parseColor(transparentColorString);
//			} catch (Exception e1) {
//				LOGGER
//						.warn("The TransparencyColor given in the .properties for this layer can't be interpretated as a color. Using NaN / null as the transparent color.");
//			}
//
//		// load the envelope
//		final String envelope = properties.getProperty("Envelope2D");
//		String[] pairs = envelope.split(" ");
//		final double cornersV[][] = new double[2][2];
//		String pair[];
//		for (int i = 0; i < 2; i++) {
//			pair = pairs[i].split(",");
//			cornersV[i][0] = Double.parseDouble(pair[0]);
//			cornersV[i][1] = Double.parseDouble(pair[1]);
//		}
//		this.originalEnvelope = new GeneralEnvelope(cornersV[0], cornersV[1]);
//		this.originalEnvelope.setCoordinateReferenceSystem(crs);
//
//		// resolutions levels
//		numOverviews = Integer.parseInt(properties.getProperty("LevelsNum")) - 1;
//		final String levels = properties.getProperty("Levels");
//		pairs = levels.split(" ");
//		overViewResolutions = numOverviews > 1 ? new double[numOverviews][2]
//				: null;
//		pair = pairs[0].split(",");
//		highestRes = new double[2];
//		highestRes[0] = Double.parseDouble(pair[0]);
//		highestRes[1] = Double.parseDouble(pair[1]);
//
//		// LOGGER.debug(new StringBuffer("Highest res ").append(highestRes[0])
//		// .append(" ").append(highestRes[1]).toString());
//
//		for (int i = 1; i < numOverviews + 1; i++) {
//			pair = pairs[i].split(",");
//			overViewResolutions[i - 1][0] = Double.parseDouble(pair[0]);
//			overViewResolutions[i - 1][1] = Double.parseDouble(pair[1]);
//		}
//
//		// name
//		coverageName = properties.getProperty("Name");
//
//		// need a color expansion?
//		// this is a newly added property we have to be ready to the case where
//		// we do not find it.
//		try {
//			expandMe = properties.getProperty("ExpandToRGB").equalsIgnoreCase(
//					"true");
//		} catch (final Exception e) {
//			expandMe = false;
//		}
//
//		// original gridrange (estimated)
//		originalGridRange = new GeneralGridRange(
//				new Rectangle((int) Math.round(originalEnvelope.getLength(0)
//						/ highestRes[0]), (int) Math.round(originalEnvelope
//						.getLength(1)
//						/ highestRes[1])));
//
//		// Gracefully try to close the stream
//		try {
//			openStream.close();
//		} catch (Exception e) {
//		}
//
//	}
//
//	/**
//	 * Constructor.
//	 * 
//	 * @param source
//	 *            The source object.
//	 * @throws IOException
//	 * @throws UnsupportedEncodingException
//	 * 
//	 */
//	public AtlasImageMosaicReader(final Object source) throws IOException {
//		this(source, null);
//
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
//	 */
//	public Format getFormat() {
//		return new ImageMosaicFormat();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter
//	 * .GeneralParameterValue[])
//	 */
//	public GridCoverage read(final GeneralParameterValue[] params)
//			throws IOException {
//
//		// LOGGER.debug("Reading mosaic from " + sourceURL.toString());
//		// LOGGER.debug(new StringBuffer("Highest res ").append(highestRes[0])
//		// .append(" ").append(highestRes[1]).toString());
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Checking params
//		//
//		// /////////////////////////////////////////////////////////////////////
//		GeneralEnvelope requestedEnvelope = null;
//		Rectangle dim = null;
//		boolean blend = false;
//		boolean skipEmptyAreas = false;
//		if (params != null) {
//			for (int i = 0; i < params.length; i++) {
//				final Parameter param = (Parameter) params[i];
//				if (param.getDescriptor().getName().getCode().equals(
//						org.geotools.coverage.grid.io.AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
//								.toString())) {
//					final GridGeometry2D gg = (GridGeometry2D) param.getValue();
//					requestedEnvelope = (GeneralEnvelope) gg.getEnvelope();
//					dim = gg.getGridRange2D().getBounds();
//				} else if (param.getDescriptor().getName().getCode().equals(
//						ImageMosaicFormat.INPUT_TRANSPARENT_COLOR.getName()
//								.toString())) {
//					inputTransparentColor = (Color) param.getValue();
//
//				} else if (param.getDescriptor().getName().getCode().equals(
//						ImageMosaicFormat.INPUT_IMAGE_THRESHOLD_VALUE.getName()
//								.toString())) {
//					inputImageThreshold = ((Double) param.getValue())
//							.doubleValue();
//
//				} else if (param.getDescriptor().getName().getCode().equals(
//						ImageMosaicFormat.FADING.getName().toString())) {
//					blend = ((Boolean) param.getValue()).booleanValue();
//
//				} else if (param.getDescriptor().getName().getCode().equals(
//						ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR.getName()
//								.toString())) {
//					outputTransparentColor = (Color) param.getValue();
//
//					// Upgrade zu 2.4.2
//					// } else if
//					// (param.getDescriptor().getName().getCode().equals(
//					// ImageMosaicFormat.DONT_CREATE_EMPTY_TILES.getName()
//					// .toString())) {
//					// skipEmptyAreas = ((Boolean) param.getValue())
//					// .booleanValue();
//
//				}
//			}
//		}
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Loading tiles trying to optimize as much as possible
//		//
//		// /////////////////////////////////////////////////////////////////////
//
//		return loadTiles(requestedEnvelope, inputTransparentColor,
//				outputTransparentColor, inputImageThreshold, dim, blend,
//				skipEmptyAreas);
//	}
//
//	/**
//	 * Loading the tiles which overlap with the requested envelope with control
//	 * over the <code>inputImageThresholdValue</code>, the fading effect between
//	 * different images, abd the <code>transparentColor</code> for the input
//	 * images.
//	 * 
//	 * @param requestedOriginalEnvelope
//	 *            bounds the tiles that we will load. Tile outside ths
//	 *            {@link GeneralEnvelope} won't even be considered.
//	 * 
//	 * 
//	 * @param transparentColor
//	 *            should be used to control transparency on input images.
//	 * @param outputTransparentColor
//	 * @param inputImageThresholdValue
//	 *            should be used to create ROIs on the input images
//	 * @param pixelDimension
//	 *            is the dimension in pixels of the requested coverage.
//	 * @param fading
//	 *            tells to ask for {@link MosaicDescriptor#MOSAIC_TYPE_BLEND}
//	 *            instead of the classic
//	 *            {@link MosaicDescriptor#MOSAIC_TYPE_OVERLAY}.
//	 * @param skipEmptyAreas
//	 *            tells this reader to avoid creating an image if no backed by
//	 *            any data.
//	 * @return a {@link GridCoverage2D} matching as close as possible the
//	 *         requested {@link GeneralEnvelope} and <code>pixelDimension</code>
//	 *         , or null in case nothing existed in the requested area.
//	 * @throws IOException
//	 */
//	private GridCoverage loadTiles(GeneralEnvelope requestedOriginalEnvelope,
//			final Color transparentColor, final Color outputTransparentColor,
//			final double inputImageThresholdValue,
//			final Rectangle pixelDimension, final boolean fading,
//			final boolean skipEmptyAreas) throws IOException {
//
//		/*
//		 * LOGGER.debug(new StringBuffer( "Creating mosaic to comply with
//		 * envelope ").append( requestedOriginalEnvelope != null ?
//		 * requestedOriginalEnvelope .toString() : null).append(
//		 * "\nrequestedOriginalEnvelope.crs ").append(
//		 * requestedOriginalEnvelope.getCoordinateReferenceSystem()
//		 * .toWKT()).append("\ncrs ").append(crs.toWKT()).append( " dim
//		 * ").append( pixelDimension == null ? " null" :
//		 * pixelDimension.toString()) .toString());
//		 */
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Check if we have something to load by intersecting the requested
//		// envelope with the bounds of the data set.
//		//
//		// If the requested envelope is not in the same crs of the data set crs
//		// we have to perform a conversion towards the latter crs before
//		// intersecting anything.
//		//
//		// /////////////////////////////////////////////////////////////////////
//		GeneralEnvelope intersectionEnvelope = null;
//		if (requestedOriginalEnvelope != null) {
//			if (!CRS.equalsIgnoreMetadata(requestedOriginalEnvelope
//					.getCoordinateReferenceSystem(), this.crs)) {
//				try {
//					// transforming the envelope back to the dataset crs in
//					// order to interact with the original envelope for this
//					// mosaic.
//					final MathTransform transform = operationFactory
//							.createOperation(
//									requestedOriginalEnvelope
//											.getCoordinateReferenceSystem(),
//									crs).getMathTransform();
//					if (!transform.isIdentity()) {
//						requestedOriginalEnvelope = CRS.transform(transform,
//								requestedOriginalEnvelope);
//						requestedOriginalEnvelope
//								.setCoordinateReferenceSystem(this.crs);
//
//						LOGGER
//								.debug(new StringBuffer("Reprojected envelope ")
//										.append(
//												requestedOriginalEnvelope
//														.toString()).append(
//												" crs ").append(crs.toWKT())
//										.toString());
//					}
//				} catch (final TransformException e) {
//					throw new DataSourceException(
//							"Unable to create a coverage for this source", e);
//				} catch (final FactoryException e) {
//					throw new DataSourceException(
//							"Unable to create a coverage for this source", e);
//				}
//			}
//			if (!requestedOriginalEnvelope.intersects(this.originalEnvelope,
//					true)) {
//				LOGGER
//						.warn("The requested envelope does not intersect the envelope of this mosaic, we will return a null coverage.");
//				return null;
//			}
//			intersectionEnvelope = new GeneralEnvelope(
//					requestedOriginalEnvelope);
//			// intersect the requested area with the bounds of this layer
//			intersectionEnvelope.intersect(originalEnvelope);
//
//		} else {
//			requestedOriginalEnvelope = new GeneralEnvelope(originalEnvelope);
//			intersectionEnvelope = requestedOriginalEnvelope;
//
//		}
//		requestedOriginalEnvelope.setCoordinateReferenceSystem(this.crs);
//		intersectionEnvelope.setCoordinateReferenceSystem(this.crs);
//		// ok we got something to return, let's load records from the index
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Prepare the filter for loading the needed layers
//		//
//		// /////////////////////////////////////////////////////////////////////
//		final ReferencedEnvelope intersectionJTSEnvelope = new ReferencedEnvelope(
//				intersectionEnvelope.getMinimum(0), intersectionEnvelope
//						.getMaximum(0), intersectionEnvelope.getMinimum(1),
//				intersectionEnvelope.getMaximum(1), crs);
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Load feaures from the index
//		// In case there are no features under the requested bbox which is legal
//		// in case the mosaic is not a real sqare, we return a fake mosaic.
//		//
//		// /////////////////////////////////////////////////////////////////////
//		// LOGGER.debug("loading tile for envelope "
//		// + intersectionJTSEnvelope.toString());
//		final List features = getFeaturesFromIndex(intersectionJTSEnvelope);
//		if (features == null) {
//			return fakeMosaic(requestedOriginalEnvelope, pixelDimension);
//		}
//		// do we have any feature to load?
//		final Iterator it = features.iterator();
//		if (!it.hasNext()) {
//			// //
//			//
//			// If requested to do so, I do not generate a fake mosaic for an
//			// area where there is no data but I return null.
//			//
//			// This is requested byt the process of creating a pyramid which
//			// builds a reduced resolution mosaic from the another mosaic. IF
//			// the original mosaic has big holes it might happen that we
//			// generate a lot of useless fake mosaics!
//			//
//			// ///
//			if (skipEmptyAreas)
//				return null;
//			return fakeMosaic(requestedOriginalEnvelope, pixelDimension);
//		}
//		final int size = features.size();
//		if (size > MAX_TILES) {
//			LOGGER
//					.warn(new StringBuffer("We can load at most ")
//							.append(MAX_TILES)
//							.append(" tiles while there were requested ")
//							.append(features.size())
//							.append(
//									"\nI am going to print out a fake coverage, sorry about it!")
//							.toString());
//			return fakeMosaic(intersectionEnvelope, pixelDimension);
//		}
//
//		// LOGGER.debug("We have " + size + " tiles to load");
//
//		PyramidEvaluationJFrame.getInstance().setTilesNeeded(
//				String.valueOf(size));
//		try {
//			return loadRequestedTiles(requestedOriginalEnvelope,
//					intersectionEnvelope, transparentColor,
//					outputTransparentColor, intersectionJTSEnvelope, features,
//					it, inputImageThresholdValue, pixelDimension, size, fading);
//		} catch (final DataSourceException e) {
//			LOGGER.error(e.getLocalizedMessage(), e);
//		} catch (final TransformException e) {
//			LOGGER.error(e.getLocalizedMessage(), e);
//		}
//		return null;
//
//	}
//
//	/**
//	 * Builds up a fake mosaic that consists of a transparent image with a red
//	 * cross.
//	 * 
//	 * <p>
//	 * The purpose of the fake mosaic
//	 * 
//	 * @param requestedEnvelope
//	 *            is the envelope requested by the user.
//	 * @param dim
//	 *            indicates the requested dimension for this
//	 *            {@link GridCoverage2D} in terms of pixels.
//	 * @param features
//	 *            is the number of features touching the requsted envelope.
//	 * @return a {@link GridCoverage2D}.
//	 */
//	private GridCoverage fakeMosaic(final GeneralEnvelope requestedEnvelope,
//			final Rectangle dim) {
//
//		return coverageFactory.create(coverageName, AWTImageDescriptor.create(
//				unavailableImage, ImageUtilities.NOCACHE_HINT),
//				requestedEnvelope);
//	}
//
//	/**
//	 * This method loads the tiles which overlap the requested
//	 * {@link GeneralEnvelope} using the provided values for alpha and input
//	 * ROI.
//	 * 
//	 * @param requestedOriginalEnvelope
//	 * @param intersectionEnvelope
//	 * @param transparentColor
//	 * @param outputTransparentColor
//	 * @param requestedJTSEnvelope
//	 * @param features
//	 * @param it
//	 * @param inputImageThresholdValue
//	 * @param dim
//	 * @param numImages
//	 * @param blend
//	 * @return
//	 * @throws DataSourceException
//	 * @throws TransformException
//	 */
//	private GridCoverage loadRequestedTiles(
//			final GeneralEnvelope requestedOriginalEnvelope,
//			final GeneralEnvelope intersectionEnvelope, Color transparentColor,
//			final Color outputTransparentColor,
//			final Envelope requestedJTSEnvelope, final List features,
//			final Iterator it, final double inputImageThresholdValue,
//			final Rectangle dim, final int numImages, final boolean blend)
//			throws DataSourceException, TransformException {
//
//		try {
//			// if we get here we have something to load
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// prepare the params for executing a mosaic operation.
//			//
//			// /////////////////////////////////////////////////////////////////////
//			final ParameterBlockJAI pbjMosaic = new ParameterBlockJAI("Mosaic");
//			pbjMosaic.setParameter("mosaicType",
//					MosaicDescriptor.MOSAIC_TYPE_OVERLAY);
//
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// compute the requested resolution given the requested envelope and
//			// dimension.
//			//
//			// /////////////////////////////////////////////////////////////////////
//			final ImageReadParam readP = new ImageReadParam();
//			final Integer imageChoice;
//			if (dim != null)
//				imageChoice = setReadParams(readP, requestedOriginalEnvelope,
//						dim);
//			else
//				imageChoice = new Integer(0);
//
//			// LOGGER.debug(new StringBuffer("Loading level ").append(
//			// imageChoice.toString())
//			// .append(" with subsampling factors ").append(
//			// readP.getSourceXSubsampling()).append(" ").append(
//			// readP.getSourceYSubsampling()).toString());
//
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// Resolution.
//			//
//			// I am implicitly assuming that all the images have the same
//			// resolution. In principle this is not required but in practice
//			// having different resolution would surely bring to having small
//			// displacements in the final mosaic which we do not wnat to happen.
//			//
//			// /////////////////////////////////////////////////////////////////////
//			final double[] res;
//			if (imageChoice.intValue() == 0) {
//				res = new double[highestRes.length];
//				res[0] = highestRes[0];
//				res[1] = highestRes[1];
//			} else {
//				final double temp[] = overViewResolutions[imageChoice
//						.intValue() - 1];
//				res = new double[temp.length];
//				res[0] = temp[0];
//				res[1] = temp[1];
//
//			}
//			// adjusting the resolution for the source subsampling
//			res[0] *= readP.getSourceXSubsampling();
//			res[1] *= readP.getSourceYSubsampling();
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// Envelope of the loaded dataset and upper left corner of this
//			// envelope.
//			//
//			// Ths envelope corresponds to the union of the envelopes of all the
//			// tiles that intersect the area that was request by the user. It is
//			// crucial to understand that this geographic area can be, and it
//			// usually is, bigger then the requested one. This involves doing a
//			// crop operation at the end of the mosaic creation.
//			//
//			// /////////////////////////////////////////////////////////////////////
//			// final Envelope loadedDataSetBound =
//			// getLoadedDataSetBoud(features);
//			// final Point2D ULC = new Point2D.Double(
//			// loadedDataSetBound.getMinX(), loadedDataSetBound.getMaxY());
//			//			
//			final com.vividsolutions.jts.geom.Envelope loadedDataSetBound = getLoadedDataSetBoud(features);
//			final Point2D ULC = new Point2D.Double(
//					loadedDataSetBound.getMinX(), loadedDataSetBound.getMaxY());
//
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// CORE LOOP
//			//
//			// Loop over the single features and load the images which
//			// intersect the requested envelope. Once all of them have been
//			// loaded, next step is to create the mosaic and then
//			// crop it as requested.
//			//
//			// /////////////////////////////////////////////////////////////////////
//			// final File tempFile = new File(this.sourceURL.getFile());
//			// final String parentLocation = tempFile.getParent();
//			SimpleFeature feature;
//			String location;
//			Envelope bound;
//			PlanarImage loadedImage;
//			// File imageFile;
//			final ROI[] rois = new ROI[numImages];
//			final PlanarImage[] alphaChannels = new PlanarImage[numImages];
//			final Area finalLayout = new Area();
//
//			// reusable parameters
//			boolean alphaIn = false;
//			boolean doTransparentColor = false;
//			boolean doInputImageThreshold = false;
//			int[] alphaIndex = null;
//			int i = 0;
//			final Boolean readMetadata = Boolean.FALSE;
//			final Boolean readThumbnails = Boolean.FALSE;
//			final Boolean verifyInput = Boolean.FALSE;
//			ParameterBlock pbjImageRead;
//			ColorModel model;
//
//			do {
//				// /////////////////////////////////////////////////////////////////////
//				//
//				// Get location and envelope of the image to load.
//				//
//				// /////////////////////////////////////////////////////////////////////
//				feature = (SimpleFeature) it.next();
//				location = (String) feature.getAttribute("location");
//				bound = feature.getBounds();
//
//				// /////////////////////////////////////////////////////////////////////
//				//
//				// Load a tile from URL as requested.
//				//
//				// /////////////////////////////////////////////////////////////////////
//
//				final String key = location + imageChoice + res[0] + res[1]
//						+ sourceURL;
//				// LOGGER.debug("About to read image number from URL" + i
//				// + " key = " + key);
//				synchronized (tilesCache) {
//					// LOGGER.debug("Trying to use the tilesCache...");
//					loadedImage = (PlanarImage) tilesCache.get(key);
//					if (loadedImage != null) {
//						// LOGGER.debug("HIT, size = "+tilesCache.size());
//					} else {
//						// LOGGER.debug("sourceURL = " + sourceURL);
//
//						URL parentSourceURL = IOUtil.getParentUrl(sourceURL);
//
//						// LOGGER.debug("parentSourceURL = " + parentSourceURL);
//
//						// LOGGER.debug("location = " + location);
//
//						final URL imageURL = extendURL(parentSourceURL,
//								location);
//
//						// LOGGER.debug("imageURL = " + imageURL);
//
//						pbjImageRead = new ParameterBlock();
//
//						ImageInputStream imageInputStream = ImageIO
//								.createImageInputStream(imageURL.openStream());
//
//						// LOGGER.debug("imageInputStream = " + imageURL);
//
//						pbjImageRead.add(imageInputStream);
//						pbjImageRead.add(imageChoice);
//						pbjImageRead.add(readMetadata);
//						pbjImageRead.add(readThumbnails);
//						pbjImageRead.add(verifyInput);
//						pbjImageRead.add(null);
//						pbjImageRead.add(null);
//						pbjImageRead.add(readP);
//						pbjImageRead.add(null);
//						loadedImage = JAI.create("ImageRead", pbjImageRead);
//
//						// LOGGER.debug("MISS, size = "+tilesCache.size());
//						tilesCache.put(key, loadedImage);
//					}
//				}
//
//				// /////////////////////////////////////////////////////////////
//				//
//				// Input alpha, ROI and transparent color management.
//				//
//				// Once I get the first image Ican acquire all the information I
//				// need in order to decide which actions to while and after
//				// loading the images.
//				//
//				// Specifically, I have to check if the loaded image have
//				// transparency, because if we do a ROI and/or we have a
//				// transparent color to set we have to remove it.
//				//
//				// /////////////////////////////////////////////////////////////
//				if (i == 0) {
//					// //
//					//
//					// We check here if the images have an alpha channel or some
//					// other sort of transparency. In case we have transparency
//					// I also save the index of the transparent channel.
//					//
//					// //
//					model = loadedImage.getColorModel();
//					alphaIn = model.hasAlpha();
//					if (alphaIn)
//						alphaIndex = new int[] { model.getNumComponents() - 1 };
//
//					// //
//					//
//					// ROI has to be computed depending on the value of the
//					// input threshold and on the data type of the images.
//					//
//					// If I request a threshod of 0 on a byte image, I can skip
//					// doing the ROI!
//					//
//					// //
//					doInputImageThreshold = checkIfThresholdIsNeeded(
//							loadedImage, inputImageThresholdValue);
//
//					// //
//					//
//					// Checking if we have to do something against the final
//					// transparent color.
//					//
//					// If we have a valid transparent color we have to remove
//					// the input alpha information.
//					//
//					// However a possible optimization is to check for index
//					// color model images with transparency where the
//					// transparent color is the same requested here and no ROIs
//					// requested.
//					//
//					// //
//					if (transparentColor != null) {
//						// paranoiac check on the provided transparent color
//						transparentColor = new Color(transparentColor.getRed(),
//								transparentColor.getGreen(), transparentColor
//										.getBlue());
//						doTransparentColor = true;
//						//
//						// If the images use an IndexColorModel Bitamsk where
//						// the transparent color is the same that was requested,
//						// the optimization is to avoid removing the alpha
//						// information just to readd it at the end. We can
//						// simply go with what we have from the input.
//						//
//						// However, we have to take into account that no action
//						// has to be take if a ROI is requested on the input
//						// images since that would imply doing an RGB
//						// conversion.
//						//
//						//
//						if (model instanceof IndexColorModel
//								&& alphaIn
//								&& model.getTransparency() == Transparency.BITMASK) {
//							final IndexColorModel icm = (IndexColorModel) model;
//							final int transparentPixel = icm
//									.getTransparentPixel();
//							if (transparentPixel != -1) {
//								final int oldTransparentColor = icm
//										.getRGB(transparentPixel);
//								if (oldTransparentColor == transparentColor
//										.getRGB()) {
//									doTransparentColor = false;
//								}
//
//							}
//
//						}
//
//					}
//
//				}
//
//				// /////////////////////////////////////////////////////////////////////
//				//
//				// add to the mosaic collection
//				//
//				// /////////////////////////////////////////////////////////////////////
//				// LOGGER.debug("Adding to mosaic image number " + i);
//				addToMosaic(pbjMosaic, bound, ULC, res, loadedImage,
//						doInputImageThreshold, rois, i,
//						inputImageThresholdValue, alphaIn, alphaIndex,
//						alphaChannels, finalLayout, doTransparentColor,
//						transparentColor);
//
//				i++;
//			} while (i < numImages);
//
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// Prepare the last parameters for the mosaic.
//			//
//			// First of all we set the input threshold accordingly to the input
//			// image data type. I find the default value (which is 0) very bad
//			// for data type other than byte and ushort. With float and double
//			// it can cut off a large par of fthe dynamic.
//			//
//			// Second step is the the management of the input threshold that is
//			// converted into a roi because the way we want to manage such
//			// threshold is by applying it on the intensitiy of the input image.
//			// Note that this ROI has to be mutually exclusive with the alpha
//			// management due to the rules of the JAI Mosaic Operation which
//			// ignore the ROIs in case an alpha information is provided for the
//			// input images.
//			//
//			// Third step is the management of the alpha information which can
//			// be the result of a masking operation upong the request for a
//			// transparent color or the result of input images with internal
//			// transparency.
//			//
//			// Fourth step is the blending for having nice Fading effect at
//			// overlapping regions.�
//			//
//			// /////////////////////////////////////////////////////////////////////
//			final double th = getThreshold(loadedImage.getSampleModel()
//					.getDataType());
//			pbjMosaic
//					.setParameter("sourceThreshold", new double[][] { { th } });
//			if (doInputImageThreshold) {
//				// //
//				//
//				// Set the ROI parameter in case it was requested by setting a
//				// threshold.
//				// 
//				// //
//				pbjMosaic.setParameter("sourceROI", rois);
//
//			} else if (alphaIn || doTransparentColor) {
//				// //
//				//
//				// In case the input images have transparency information this
//				// way we can handle it.
//				//
//				// //
//				pbjMosaic.setParameter("sourceAlpha", alphaChannels);
//
//			}
//			// //
//			//
//			// It might important to set the mosaic tpe to blend otherwise
//			// sometimes strange results jump in.
//			// 
//			// //
//			if (blend) {
//				pbjMosaic.setParameter("mosaicType",
//						MosaicDescriptor.MOSAIC_TYPE_BLEND);
//
//			}
//
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// Create the mosaic image by doing a crop if necessary and also
//			// managing the transparent color if applicablw. Be aware that
//			// management of the transparent color involves removing
//			// transparency information from the input images.
//			// 
//			// /////////////////////////////////////////////////////////////////////
//			return prepareMosaic(location, requestedOriginalEnvelope,
//					intersectionEnvelope, res, loadedDataSetBound, pbjMosaic,
//					finalLayout, outputTransparentColor);
//		} catch (final IOException e) {
//			throw new DataSourceException("Unable to create this mosaic", e);
//		}
//	}
//
//	/**
//	 * ROI has to be computed depending on the value of the input threshold and
//	 * on the data type of the images.
//	 * 
//	 * If I request a threshod of 0 on a byte image, I can skip doing the ROI!
//	 * 
//	 * @param loadedImage
//	 *            to check before applying a threshold.
//	 * @param thresholdValue
//	 *            is the value that is suggested to be used for the threshold.
//	 * @return true in case the threshold is to be performed, false otherwise.
//	 */
//	private boolean checkIfThresholdIsNeeded(final PlanarImage loadedImage,
//			final double thresholdValue) {
//		if (Double.isNaN(thresholdValue) || Double.isInfinite(thresholdValue))
//			return false;
//		switch (loadedImage.getSampleModel().getDataType()) {
//		case DataBuffer.TYPE_BYTE:
//			final int bTh = (int) thresholdValue;
//			if (bTh <= 0 || bTh >= 255)
//				return false;
//		case DataBuffer.TYPE_USHORT:
//			final int usTh = (int) thresholdValue;
//			if (usTh <= 0 || usTh >= 65535)
//				return false;
//		case DataBuffer.TYPE_SHORT:
//			final int sTh = (int) thresholdValue;
//			if (sTh <= Short.MIN_VALUE || sTh >= Short.MAX_VALUE)
//				return false;
//		case DataBuffer.TYPE_INT:
//			final int iTh = (int) thresholdValue;
//			if (iTh <= Integer.MIN_VALUE || iTh >= Integer.MAX_VALUE)
//				return false;
//		case DataBuffer.TYPE_FLOAT:
//			final float fTh = (float) thresholdValue;
//			if (fTh <= -Float.MAX_VALUE || fTh >= Float.MAX_VALUE
//					|| Float.isInfinite(fTh) || Float.isNaN(fTh))
//				return false;
//		case DataBuffer.TYPE_DOUBLE:
//			final double dTh = thresholdValue;
//			if (dTh <= -Double.MAX_VALUE || dTh >= Double.MAX_VALUE
//					|| Double.isInfinite(dTh) || Double.isNaN(dTh))
//				return false;
//
//		}
//		return true;
//	}
//
//	/**
//	 * Returns a suitable threshold depending on the {@link DataBuffer} type.
//	 * 
//	 * <p>
//	 * Remember that the threshold works with >=.
//	 * 
//	 * @param dataType
//	 *            to create a low threshold for.
//	 * @return a minimum threshold value suitable for this data type.
//	 */
//	private double getThreshold(final int dataType) {
//		switch (dataType) {
//		case DataBuffer.TYPE_BYTE:
//		case DataBuffer.TYPE_USHORT:
//			// XXX change to zero when bug fixed
//			return 1.0;
//		case DataBuffer.TYPE_INT:
//			return Integer.MIN_VALUE;
//		case DataBuffer.TYPE_SHORT:
//			return Short.MIN_VALUE;
//		case DataBuffer.TYPE_DOUBLE:
//			return -Double.MAX_VALUE;
//		case DataBuffer.TYPE_FLOAT:
//			return -Float.MAX_VALUE;
//		}
//		return 0;
//	}
//
//	/**
//	 * Retrieves the ULC of the BBOX composed by all the tiles we need to load.
//	 * 
//	 * @param double
//	 * @return A {@link Point2D} pointing to the ULC of the smalles area made by
//	 *         mosaicking all the tile that actually intersect the passed
//	 *         envelope.
//	 * @throws IOException
//	 */
//	private com.vividsolutions.jts.geom.Envelope getLoadedDataSetBoud(
//			final List features) throws IOException {
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Load feaures and evaluate envelope
//		//
//		// /////////////////////////////////////////////////////////////////////
//		final Envelope loadedULC = new Envelope();
//		final Iterator it = features.iterator();
//		while (it.hasNext()) {
//			loadedULC.expandToInclude(((SimpleFeature) it.next())
//					.getDefaultGeometry().getEnvelopeInternal());
//		}
//		return loadedULC;
//
//	}
//
//	/**
//	 * Retrieves the list of features that intersect the provided evelope
//	 * loadinf them inside an index in memory where beeded.
//	 * 
//	 * @param envelope
//	 *            Envelope for selectig features that intersect.
//	 * @return A list of fetaures.
//	 * @throws IOException
//	 *             In case loading the needed features failes.
//	 */
//	private List getFeaturesFromIndex(final Envelope envelope)
//			throws IOException {
//		List features = null;
//		Object o;
//		synchronized (index) {
//			// LOGGER.debug("Trying to use the index...");
//			o = index.get();
//			if (o != null) {
//				// LOGGER.debug("Index does not need to be created...");
//
//			} else {
//				// LOGGER.debug("Index needa to be recreated...");
//				o = new MemorySpatialIndex(featureSource.getFeatures());
//			}
//			// LOGGER.debug("Index Loaded");
//		}
//		features = ((MemorySpatialIndex) o).findFeatures(envelope);
//		return features;
//	}
//
//	/**
//	 * Once we reach this method it means that we have loaded all the images
//	 * which were intersecting the requested nevelope. Next step is to create
//	 * the final mosaic image and cropping it to the exact requested envelope.
//	 * 
//	 * @param location
//	 * 
//	 * @param envelope
//	 * @param requestedEnvelope
//	 * @param intersectionEnvelope
//	 * @param res
//	 * @param loadedTilesEnvelope
//	 * @param pbjMosaic
//	 * @param transparentColor
//	 * @param doAlpha
//	 * @param doTransparentColor
//	 * @param finalLayout
//	 * @param outputTransparentColor
//	 * @param singleImageROI
//	 * @return A {@link GridCoverage}, wewll actually a {@link GridCoverage2D}.
//	 * @throws IllegalArgumentException
//	 * @throws FactoryRegistryException
//	 * @throws DataSourceException
//	 */
//	private GridCoverage prepareMosaic(final String location,
//			final GeneralEnvelope requestedOriginalEnvelope,
//			final GeneralEnvelope intersectionEnvelope, final double[] res,
//			final Envelope loadedTilesEnvelope,
//			final ParameterBlockJAI pbjMosaic, final Area finalLayout,
//			final Color outputTransparentColor) throws DataSourceException {
//
//		GeneralEnvelope finalenvelope = null;
//		PlanarImage preparationImage;
//		final Rectangle loadedTilePixelsBound = finalLayout.getBounds();
//		// LOGGER.debug(new StringBuffer("Loaded bbox ").append(
//		// loadedTilesEnvelope.toString())
//		// .append(" while requested bbox ").append(
//		// requestedOriginalEnvelope.toString()).toString());
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Check if we need to do a crop on the loaded tiles or not. Keep into
//		// account that most part of the time the loaded tiles will be go
//		// beyoind the requested area, hence there is a need for cropping them
//		// while mosaicking them.
//		//
//		// /////////////////////////////////////////////////////////////////////
//		final GeneralEnvelope loadedTilesBoundEnv = new GeneralEnvelope(
//				new double[] { loadedTilesEnvelope.getMinX(),
//						loadedTilesEnvelope.getMinY() }, new double[] {
//						loadedTilesEnvelope.getMaxX(),
//						loadedTilesEnvelope.getMaxY() });
//		loadedTilesBoundEnv.setCoordinateReferenceSystem(crs);
//		final double loadedTilesEnvelopeDim0 = loadedTilesBoundEnv.getLength(0);
//		final double loadedTilesEnvelopeDim1 = loadedTilesBoundEnv.getLength(1);
//		if (!intersectionEnvelope.equals(loadedTilesBoundEnv, Math
//				.min((loadedTilesEnvelopeDim0 / loadedTilePixelsBound
//						.getWidth()) / 2.0,
//						(loadedTilesEnvelopeDim1 / loadedTilePixelsBound
//								.getHeight()) / 2.0), false)) {
//
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// CROP the mosaic image to the requested BBOX
//			//
//			// /////////////////////////////////////////////////////////////////////
//			// intersect them
//			final GeneralEnvelope intersection = new GeneralEnvelope(
//					intersectionEnvelope);
//			intersection.intersect(loadedTilesBoundEnv);
//
//			// get the transform for going from world to grid
//			try {
//				final GridToEnvelopeMapper gridToEnvelopeMapper = new GridToEnvelopeMapper(
//						new GeneralGridRange(loadedTilePixelsBound),
//						loadedTilesBoundEnv);
//				gridToEnvelopeMapper.setGridType(PixelInCell.CELL_CORNER);
//				final MathTransform transform = gridToEnvelopeMapper
//						.createTransform().inverse();
//				final GeneralGridRange finalRange = new GeneralGridRange(CRS
//						.transform(transform, intersection));
//				// CROP
//				finalLayout.intersect(new Area(finalRange.toRectangle()));
//				final Rectangle tempRect = finalLayout.getBounds();
//
//				/**
//				 * DOKU
//				 * 
//				 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//				 *         Kr&uuml;ger</a> If zoomed in too much, we would end
//				 *         up with height/width = 0x0 ... leading to an
//				 *         exception.
//				 */
//				if (tempRect.height < 1)
//					tempRect.height = 1;
//				if (tempRect.width < 1)
//					tempRect.width = 1;
//
//				preparationImage = JAI.create("Mosaic", pbjMosaic,
//						new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
//								new ImageLayout(tempRect.x, tempRect.y,
//										tempRect.width, tempRect.height)));
//
//				finalenvelope = intersection;
//
//				// gt2.4.2
//				// } catch (final MismatchedDimensionException e) {
//				// throw new DataSourceException(
//				// "Problem when creating this mosaic.", e);
//			} catch (final NoninvertibleTransformException e) {
//				throw new DataSourceException(
//						"Problem when creating this mosaic.", e);
//			} catch (final TransformException e) {
//				throw new DataSourceException(
//						"Problem when creating this mosaic.", e);
//			}
//
//		} else {
//			preparationImage = JAI.create("Mosaic", pbjMosaic);
//			finalenvelope = new GeneralEnvelope(intersectionEnvelope);
//		}
//		// LOGGER.debug(new StringBuffer("Mosaic created ").toString());
//
//		//
//		// ///////////////////////////////////////////////////////////////////
//		//
//		// FINAL ALPHA
//		//
//		//
//		// ///////////////////////////////////////////////////////////////////
//		if (outputTransparentColor != null) {
//			// LOGGER.debug(new StringBuffer("Support for alpha").toString());
//			//
//			// ///////////////////////////////////////////////////////////////////
//			//
//			// If requested I can perform the ROI operation on the prepared ROI
//			// image for building up the alpha band
//			//
//			//
//			// ///////////////////////////////////////////////////////////////////
//			final ImageWorker w = new ImageWorker(preparationImage);
//			if (preparationImage.getColorModel() instanceof IndexColorModel) {
//				preparationImage = w.maskIndexColorModelByte(
//						outputTransparentColor).getPlanarImage();
//			} else
//				preparationImage = w.maskComponentColorModelByte(
//						outputTransparentColor).getPlanarImage();
//
//			// ///////////////////////////////////////////////////////////////////
//			//
//			// create the coverage
//			//
//			//
//			// ///////////////////////////////////////////////////////////////////
//			return coverageFactory.create(coverageName, preparationImage,
//					finalenvelope);
//		}
//		// ///////////////////////////////////////////////////////////////////
//		//		
//		// create the coverage
//		//		
//		// ///////////////////////////////////////////////////////////////////
//		return coverageFactory.create(coverageName, preparationImage,
//				finalenvelope);
//
//	}
//
//	/**
//	 * Adding an image which intersect the requested envelope to the final
//	 * moisaic. This operation means computing the translation factor keeping
//	 * into account the resolution of the actual image, the envelope of the
//	 * loaded dataset and the envelope of this image.
//	 * 
//	 * @param pbjMosaic
//	 * @param bound
//	 *            Lon-Lat bounds of the loaded image
//	 * @param ulc
//	 * @param res
//	 * @param loadedImage
//	 * @param removeAlpha
//	 * @param rois
//	 * @param i
//	 * @param inputImageThresholdValue
//	 * @param alphaChannels
//	 * @param alphaIndex
//	 * @param alphaIn
//	 * @param finalLayout
//	 * @param imageFile
//	 * @param transparentColor
//	 * @param doTransparentColor
//	 * @throws FileNotFoundException
//	 * @throws IOException
//	 */
//	private void addToMosaic(final ParameterBlockJAI pbjMosaic,
//			final Envelope bound, final Point2D ulc, final double[] res,
//			final PlanarImage loadedImage, final boolean doInputImageThreshold,
//			final ROI[] rois, final int i,
//			final double inputImageThresholdValue, final boolean alphaIn,
//			int[] alphaIndex, final PlanarImage[] alphaChannels,
//			final Area finalLayout, final boolean doTransparentColor,
//			final Color transparentColor) {
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// Computing TRANSLATION AND SCALING FACTORS
//		//
//		// Using the spatial resolution we compute the translation factors for
//		// positioning the actual image correctly in final mosaic.
//		//
//		// /////////////////////////////////////////////////////////////////////
//		PlanarImage readyToMosaicImage = scaleAndTranslate(bound, ulc, res,
//				loadedImage);
//
//		// ///////////////////////////////////////////////////////////////////
//		//
//		// INDEX COLOR MODEL EXPANSION
//		//
//		// Take into account the need for an expansions of the original color
//		// model.
//		//
//		// If the original color model is an index color model an expansion
//		// might be requested in case the differemt palettes are not all the
//		// same. In this case the mosaic operator from JAI would provide wrong
//		// results since it would take the first palette and use that one for
//		// all the other images.
//		//
//		// There is a special case to take into account here. In case the input
//		// images use an IndexColorModel t might happen that the transparent
//		// color is present in some of them while it is not present in some
//		// others. This case is the case where for sure a color expansion is
//		// needed. However we have to take into account that during the masking
//		// phase the images where the requested transparent color was present
//		// willl have 4 bands, the other 3. If we want the mosaic to work we
//		// have to add na extra band to the latter type of images for providing
//		// alpha information to them.
//		//
//		//
//		// ///////////////////////////////////////////////////////////////////
//		if (expandMe
//				&& readyToMosaicImage.getColorModel() instanceof IndexColorModel) {
//			readyToMosaicImage = new ImageWorker(readyToMosaicImage)
//					.forceComponentColorModel().getPlanarImage();
//		}
//
//		// ///////////////////////////////////////////////////////////////////
//		//
//		// TRANSPARENT COLOR MANAGEMENT
//		//
//		//
//		// ///////////////////////////////////////////////////////////////////
//		if (doTransparentColor) {
//			// LOGGER.debug(new StringBuffer(
//			// "Support for alpha on input image number " + i).toString());
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// If requested I can perform the ROI operation on the prepared ROI
//			// image for building up the alpha band
//			//
//			// /////////////////////////////////////////////////////////////////////
//			final ImageWorker w = new ImageWorker(readyToMosaicImage);
//			if (readyToMosaicImage.getColorModel() instanceof IndexColorModel) {
//				readyToMosaicImage = w
//						.maskIndexColorModelByte(transparentColor)
//						.getPlanarImage();
//			} else
//				readyToMosaicImage = w.maskComponentColorModelByte(
//						transparentColor).getPlanarImage();
//			alphaIndex = new int[] { readyToMosaicImage.getColorModel()
//					.getNumComponents() - 1 };
//
//		}
//		// ///////////////////////////////////////////////////////////////////
//		//
//		// ROI
//		//
//		// ///////////////////////////////////////////////////////////////////
//		if (doInputImageThreshold) {
//			final ImageWorker w = new ImageWorker(readyToMosaicImage);
//			w.tileCacheEnabled(false).intensity().binarize(
//					inputImageThresholdValue);
//			rois[i] = w.getImageAsROI();
//
//		} else if (alphaIn || doTransparentColor) {
//			final ImageWorker w = new ImageWorker(readyToMosaicImage);
//			// /////////////////////////////////////////////////////////////////////
//			//
//			// ALPHA in INPUT
//			//
//			// I have to select the alpha band and provide it to the final
//			// mosaic operator. I have to force going to ComponentColorModel in
//			// case the image is indexed.
//			//
//			// /////////////////////////////////////////////////////////////////////
//			if (readyToMosaicImage.getColorModel() instanceof IndexColorModel) {
//				alphaChannels[i] = w.forceComponentColorModel()
//						.retainLastBand().getPlanarImage();
//			}
//
//			else
//				alphaChannels[i] = w.retainBands(alphaIndex).getPlanarImage();
//
//		}
//
//		// /////////////////////////////////////////////////////////////////////
//		//
//		// ADD TO MOSAIC
//		//
//		// /////////////////////////////////////////////////////////////////////
//		pbjMosaic.addSource(readyToMosaicImage);
//		finalLayout.add(new Area(readyToMosaicImage.getBounds()));
//
//	}
//
//	/**
//	 * Computing TRANSLATION AND SCALING FACTORS
//	 * 
//	 * Using the spatial resolution we compute the translation factors for
//	 * positioning the actual image correctly in final mosaic.
//	 * 
//	 * @param bound
//	 * @param ulc
//	 * @param res
//	 * @param image
//	 * @return
//	 */
//	private PlanarImage scaleAndTranslate(final Envelope bound,
//			final Point2D ulc, final double[] res, final PlanarImage image) {
//		// evaluate translation and scaling factors.
//		final double resX = (bound.getMaxX() - bound.getMinX())
//				/ image.getWidth();
//		final double resY = (bound.getMaxY() - bound.getMinY())
//				/ image.getHeight();
//		double scaleX = 1.0, scaleY = 1.0;
//		double xTrans = 0.0, yTrans = 0.0;
//		if (Math.abs((resX - res[0]) / resX) > EPS
//				|| Math.abs(resY - res[1]) > EPS) {
//			scaleX = res[0] / resX;
//			scaleY = res[1] / resY;
//
//		}
//		xTrans = (bound.getMinX() - ulc.getX()) / res[0];
//		yTrans = (ulc.getY() - bound.getMaxY()) / res[1];
//		//
//		// Optimising scale and translate.
//		//
//		// In case the scale factors are very close to 1 we have two
//		// optimizarions: if fthe translation factors are close to zero we do
//		// thing, otherwise if thery are integers we do a simple translate.
//		//
//		// In the general case when wew have translation and scaling we do a
//		// warp affine which is the most precise operation we can perform.
//		//
//		// //
//		final ParameterBlock pbjAffine = new ParameterBlock();
//		if (Math.abs(xTrans - (int) xTrans) < Math.pow(10, -3)
//				&& Math.abs(yTrans - (int) yTrans) < Math.pow(10, -3)
//				&& Math.abs(scaleX - 1) < Math.pow(10, -6)
//				&& Math.abs(scaleY - 1) < Math.pow(10, -6)) {
//
//			// return the original image
//			if (Math.abs(xTrans) < Math.pow(10, -3)
//					&& Math.abs(yTrans) < Math.pow(10, -3)) {
//				return image;
//
//			}
//
//			// translation
//			pbjAffine.addSource(image).add(new Float(xTrans)).add(
//					new Float(yTrans)).add(
//					ImageUtilities.NN_INTERPOLATION_HINT
//							.get(JAI.KEY_INTERPOLATION));
//			// avoid doing the color expansion now since it might not be needed
//			return JAI.create("Translate", pbjAffine,
//					ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL);
//
//		}
//		// translation and scaling
//		pbjAffine.addSource(image).add(
//				new AffineTransform(scaleX, 0, 0, scaleY, xTrans, yTrans))
//				.add(
//						ImageUtilities.NN_INTERPOLATION_HINT
//								.get(JAI.KEY_INTERPOLATION));
//		// avoid doing the color expansion now since it might not be needed
//		final RenderingHints hints = (RenderingHints) ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL
//				.clone();
//		// adding the capability to do a border extension which is great when
//		// doing
//		hints.add(ImageUtilities.EXTEND_BORDER_BY_COPYING);
//		return JAI.create("Affine", pbjAffine, hints);
//	}
//
//	/**
//	 * Extends a {@link URL}. The given URL must be a directory, otherwise
//	 * {@link MalformedURLException} will be thrown
//	 * 
//	 * @param base
//	 *            base {@link URL} to extend
//	 * @param sub
//	 *            file or folder name to extend the {@link URL} with
//	 * 
//	 * @return a new {@link URL}
//	 * 
//	 * @throws MalformedURLException
//	 */
//	public static URL extendURL(final URL base, final String sub)
//			throws MalformedURLException {
//		String a = base.toExternalForm();
//		if (!a.endsWith("/"))
//			a += "/";
//		a += sub;
//		return new URL(a);
//	}
//
//	/**
//	 * Changes the ending (e.g. ".sld") of a {@link URL}
//	 * 
//	 * @param url
//	 *            {@link URL} like <code>file:/sds/a.bmp</code>
//	 * @param postfix
//	 *            New file extension for the {@link URL} without <code>.</code>
//	 * 
//	 * @return A new {@link URL} with new extension.
//	 * 
//	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	 *         Kr&uuml;ger</a>
//	 */
//	public static URL changeUrlExt(final URL url, final String postfix)
//			throws IllegalArgumentException {
//		String a = url.toExternalForm();
//		a = a.substring(0, a.lastIndexOf('.')) + "." + postfix;
//		try {
//			return new URL(a);
//		} catch (final MalformedURLException e) {
//			throw new IllegalArgumentException("can't create a new URL for "
//					+ url + " with new extension " + postfix, e);
//		}
//	}
//
//	// /**
//	// * Die Funktion soll der Funktion File.getParent() fuer URLs entsprechen.
//	// * Die URL wird in einen Sting konvertiert und dann (kuerzer) neu
//	// * zusammengesetzt. Falls eine Verkuerzung nicht moeglich ist, wird eine
//	// * {@link MalformedURLException} geworfen
//	// *
//	// * @param sourceURL
//	// *
//	// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	// * Kr&uuml;ger</a>
//	// * @return
//	// */
//	// public static URL getParentURL(URL url) throws MalformedURLException {
//	// String a = url.toExternalForm();
//	// a = a.substring(0, a.lastIndexOf('/'));
//	// return new URL(a);
//	// }
//	//
//	// /**
//	// * Help the GC...
//	// *
//	// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
//	// * Kr&uuml;ger</a>
//	// */
//	// @Override
//	// public void dispose() {
//	// // We shouln't clear the tilesCache here, since its a static cache.
//	// // tilesCache.clear()
//	// super.dispose();
//	// }
//
//	public Color getInputTransparentColor() {
//		return inputTransparentColor;
//	}
//
//	public void setInputTransparentColor(Color inputTransparentColor) {
//		this.inputTransparentColor = inputTransparentColor;
//	}
//
//	public Color getOutputTransparentColor() {
//		return outputTransparentColor;
//	}
//
//	public void setOutputTransparentColor(Color outputTransparentColor) {
//		this.outputTransparentColor = outputTransparentColor;
//	}
//
//	public double getInputImageThreshold() {
//		return inputImageThreshold;
//	}
//
//	public void setInputImageThreshold(double inputImageThreshold) {
//		this.inputImageThreshold = inputImageThreshold;
//	}
//}
