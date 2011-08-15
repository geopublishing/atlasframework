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
package org.geopublishing.atlasStyler.classification;

import hep.aida.bin.DynamicBin1D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geotools.coverage.grid.GridCoverage2D;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import cern.colt.list.DoubleArrayList;
import de.schmitzm.geotools.styling.StyledGridCoverageReaderInterface;
import de.schmitzm.lang.LangUtil;

/**
 * A quantitative classification. The inveralls are defined by upper and lower
 * limits
 * 
 * 
 * @param <T>
 *            The type of the value field
 * 
 * @author stefan
 */
public class RasterClassification extends Classification {

	final private static Logger LOGGER = LangUtil
			.createLogger(RasterClassification.class);

	private final StyledGridCoverageReaderInterface styledRaster;

	/**
	 * Working on the first band is the default.
	 */
	private int band = 0;

	public RasterClassification(StyledGridCoverageReaderInterface styledRaster) {
		this.styledRaster = styledRaster;
	}

	@Override
	public BufferedImage createHistogramImage(boolean showMean, boolean showSd,
			int histogramBins, String label_xachsis)
			throws InterruptedException, IOException {
		HistogramDataset hds = new HistogramDataset();

		DoubleArrayList valuesAL;
		valuesAL = getStatistics().elements();
		// new double[] {0.4,3,4,2,5.,22.,4.,2.,33.,12.}
		double[] elements = Arrays.copyOf(valuesAL.elements(), getStatistics()
				.size());
		hds.addSeries(1, elements, histogramBins);

		/** Statically label the Y Axis **/
		String label_yachsis = ASUtil
				.R("QuantitiesClassificationGUI.Histogram.YAxisLabel");

		JFreeChart chart = org.jfree.chart.ChartFactory.createHistogram(null,
				label_xachsis, label_yachsis, hds, PlotOrientation.VERTICAL,
				false, true, true);

		/***********************************************************************
		 * Paint the classes into the JFreeChart
		 */
		int countLimits = 0;
		for (Double cLimit : getClassLimits()) {
			ValueMarker marker = new ValueMarker(cLimit);
			XYPlot plot = chart.getXYPlot();
			marker.setPaint(Color.orange);
			marker.setLabel(String.valueOf(countLimits));
			marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
			marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
			plot.addDomainMarker(marker);

			countLimits++;
		}

		/***********************************************************************
		 * Optionally painting SD and MEAN into the histogram
		 */
		try {
			if (showSd) {
				ValueMarker marker;
				marker = new ValueMarker(getSD(), Color.green.brighter(),
						new BasicStroke(1.5f));
				XYPlot plot = chart.getXYPlot();
				marker.setLabel(ASUtil
						.R("QuantitiesClassificationGUI.Histogram.SD.ShortLabel"));
				marker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
				plot.addDomainMarker(marker);
			}

			if (showMean) {
				ValueMarker marker;
				marker = new ValueMarker(getMean(), Color.green.darker(),
						new BasicStroke(1.5f));
				XYPlot plot = chart.getXYPlot();
				marker.setLabel(ASUtil
						.R("QuantitiesClassificationGUI.Histogram.Mean.ShortLabel"));
				marker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
				marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
				plot.addDomainMarker(marker);
			}

		} catch (Exception e) {
			LOGGER.error("Painting SD and MEAN into the histogram", e);
		}

		/***********************************************************************
		 * Render the Chart
		 */
		BufferedImage image = chart.createBufferedImage(400, 200);

		return image;
	}
 
	/**
	 * This is where the magic happens. Here the attributes of the features are
	 * summarized in a {@link DynamicBin1D} class.
	 * 
	 * @throws IOException
	 */
	@Override
	synchronized public DynamicBin1D getStatistics()
			throws InterruptedException, IOException {

		cancelCalculation.set(false);

		if (stats == null) {
			GridCoverage2D coverage = getStyledRaster().getGeoObject().read(
					null);

//			stats = new DynamicBin1D();
			stats = new DynamicBin1D();
			
			noDataValuesCount.set(0);

			final RenderedImage rim = coverage.getRenderedImage();
			
			long size = Long.valueOf(rim.getHeight())*Long.valueOf(rim.getWidth());
			long maxPixels = 3000000l;
			if (size > maxPixels){
				setSubsampling((int) (size  / maxPixels));
				LOGGER.info("Subsampling to every "+getSubsampling()+" pixel");
			}

			for (int row = 0; row < rim.getHeight(); row++) {
				
				if (row % getSubsampling() != 0) {
					// Skipping this line for Subsampling
					continue;
				} else {
					// DO
//					System.out.println("");
				}
				
				int x1  = 0;
				int w = rim.getWidth();
				
				int y1 =row;
				int h = 1;
				
				Raster data = rim.getData( new Rectangle(x1, y1, w, h));
				double[] values = data.getSamples(0, y1, w,h,
						getBand(), (double[]) null);

				final DoubleArrayList doubleArrayList = new DoubleArrayList(
						values);

				if (getStyledRaster().getNodataValue() != null) {
					int sizewithNodata = doubleArrayList.size();
					doubleArrayList
							.removeAll(new DoubleArrayList(
									new double[] { getStyledRaster()
											.getNodataValue() }));
					noDataValuesCount.addAndGet(sizewithNodata
							- doubleArrayList.size());
				}  
				
				stats.addAllOf(doubleArrayList);
				
				
				LOGGER.info("Added "+doubleArrayList.size()+" to statistics");
				LOGGER.info(stats.size()+" in stats");
				doubleArrayList.clear();
			}

			// Forget about the count of NODATA values

			// System.out.println(stat.mean());
			// System.out.println(stat.quantile(0.5));
			// System.out.println(stat.min());
			// System.out.println(stat.max());

			// GridCoverage2D coverage = styledRaster.getGeoObject().read(null);
			// final OperationJAI op = new OperationJAI("Histogram");
			// ParameterValueGroup params = op.getParameters();
			// params.parameter("Source").setValue(coverage);
			//
			// coverage = (GridCoverage2D) op.doOperation(params, null);
			// final Histogram hist = (Histogram) coverage
			// .getProperty("histogram");
			// stats = hist;
			//
			// // Search for the bin with the NODATA value (if NODATA != null)
			// if (styledRaster.getNodataValue() != null) {
			//
			// double low = getStatistics().getLowValue(band);
			// double high = getStatistics().getHighValue(band);
			//
			// int countBins = -1;
			//
			// // Das ist bestimmt noch falsch!
			// for (double d = low; d < high; d += 1.) {
			// countBins++;
			// if (d == styledRaster.getNodataValue()) {
			// noDataValuesCount = getStatistics().getBinSize(band,
			// (int) d);
			// }
			// continue;
			// }
			// }

		}
		return stats;
	}

	public StyledGridCoverageReaderInterface getStyledRaster() {
		return styledRaster;
	}

	public void setBand(int band) {
		if (band != this.band) {
			// Statistik wegschmeissen
			stats  = null;
		}
		this.band = band;
	}

	public int getBand() {
		return band;
	}

}
