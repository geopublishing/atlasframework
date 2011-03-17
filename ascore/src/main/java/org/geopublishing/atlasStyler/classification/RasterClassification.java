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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.TreeSet;

import javax.media.jai.Histogram;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.OperationJAI;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.opengis.parameter.ParameterValueGroup;

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

	int band = 0;

	private Histogram stats = null;

	private final StyledGridCoverageReaderInterface styledRaster;

	public RasterClassification(StyledGridCoverageReaderInterface styledRaster) {
		this.styledRaster = styledRaster;
	}

	@Override
	public BufferedImage createHistogramImage(boolean showMean, boolean showSd,
			int histogramBins, String label_xachsis)
			throws InterruptedException, IOException {
		HistogramDataset hds = new HistogramDataset();

		// DoubleArrayList valuesAL;
		// valuesAL = getStatistics().elements();
		// // new double[] {0.4,3,4,2,5.,22.,4.,2.,33.,12.}
		// double[] elements = Arrays.copyOf(valuesAL.elements(),
		// getStatistics()
		// .size());
		// hds.addSeries(1, elements, histogramBins);

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
	 * Help the GC to clean up this object.
	 */
	@Override
	public void dispose() {
		super.dispose();
		stats = null;
	}

	/**
	 * @return A {@link ComboBoxModel} that contains a list of class numbers.<br/>
	 *         When we supported SD as a classification METHOD long ago, this
	 *         retured something dependent on the {@link #method}. Not it always
	 *         returns a list of numbers.
	 */
	@Override
	public ComboBoxModel getClassificationParameterComboBoxModel() {

		DefaultComboBoxModel nClassesComboBoxModel = new DefaultComboBoxModel(
				new Integer[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });

		switch (getMethod()) {
		case EI:
		case QUANTILES:
		default:
			nClassesComboBoxModel.setSelectedItem(numClasses);
			return nClassesComboBoxModel;

		}
	}

	@Override
	public Long getCount() {
		try {
			return Long.valueOf(styledRaster.getGeoObject()
					.getOriginalGridRange().getSpan(0)
					* styledRaster.getGeoObject().getOriginalGridRange()
							.getSpan(1));
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	@Override
	public Double getMax() {
		Double last = null;
		try {

			double low = getStatistics().getLowValue(0);
			double high = getStatistics().getHighValue(0);

			int countBins = -1;

			// TODO Das ist bestimmt noch falsch
			for (double d = low; d < high; d += 1.) {
				countBins++;

				if (d == styledRaster.getNodataValue())
					continue;

				if (d == Double.NaN)
					continue;
				if (d == Double.NEGATIVE_INFINITY)
					continue;
				if (d == Double.POSITIVE_INFINITY)
					continue;

				if (getStatistics().getBins()[0][countBins] == 0)
					continue;

				last = d;
			}
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}

		return last;
	}

	@Override
	public Double getMean() {
		try {
			return getStatistics().getMean()[band];
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	@Override
	public Double getMedian() {
		try {
			return null;
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	@Override
	public Double getMin() {
		try {

			double low = getStatistics().getLowValue(0);
			double high = getStatistics().getHighValue(0);

			int countBins = -1;

			for (double d = low; d < high; d += 1.) {
				countBins++;

				if (d == styledRaster.getNodataValue())
					continue;

				if (d == Double.NaN)
					continue;
				if (d == Double.NEGATIVE_INFINITY)
					continue;
				if (d == Double.POSITIVE_INFINITY)
					continue;

				if (getStatistics().getBins()[0][countBins] == 0)
					continue;

				return d;
			}
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}

		return null;
	}

	/**
	 * Quantiles classification method distributes a set of values into groups
	 * that contain an equal number of values. This method places the same
	 * number of data values in each class and will never have empty classes or
	 * classes with too few or too many values. It is attractive in that this
	 * method always produces distinct map patterns.
	 * 
	 * @return nClasses + 1 breaks
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Override
	public TreeSet<Double> getQuantileLimits() {

		try {
			getStatistics();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// LOGGER.debug("getQuantileLimits numClasses ziel variable ist : "
		// + numClasses);

		breaks = new TreeSet<Double>();
		final Double step = 100. / new Double(numClasses);
		for (double i = 0; i < 100;) {
			final double percent = (i) * 0.01;
			// final double quantile = stats.quantile(percent);
			final double quantile = System.nanoTime();
			breaks.add(quantile);
			i = i + step;
		}
		breaks.add(getMax());
		breaks = ASUtil.roundLimits(breaks, getClassValueDigits());

		// // Special case: Create a second classLimit with the same value!
		// if (breaks.size() == 1) {
		// breaks.add(breaks.first());
		// }

		return breaks;
	}

	@Override
	public Double getSD() {
		try {
			return getStatistics().getStandardDeviation()[band];
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

	/**
	 * This is where the magic happens. Here the attributes of the features are
	 * summarized in a {@link DynamicBin1D} class.
	 * 
	 * @throws IOException
	 */
	synchronized public Histogram getStatistics() throws InterruptedException,
			IOException {

		cancelCalculation.set(false);

		if (stats == null) {

			// Forget about the count of NODATA values
			resetNoDataCount();

			GridCoverage2D coverage = styledRaster.getGeoObject().read(null);
			final OperationJAI op = new OperationJAI("Histogram");
			ParameterValueGroup params = op.getParameters();
			params.parameter("Source").setValue(coverage);

			coverage = (GridCoverage2D) op.doOperation(params, null);
			final Histogram hist = (Histogram) coverage
					.getProperty("histogram");
			stats = hist;

			// Search for the bin with the NODATA value (if NODATA != null)
			if (styledRaster.getNodataValue() != null) {

				double low = getStatistics().getLowValue(0);
				double high = getStatistics().getHighValue(0);

				int countBins = -1;

				// Das ist bestimmt noch falsch!
				for (double d = low; d < high; d += 1.) {
					countBins++;
					if (d == styledRaster.getNodataValue()) {
						noDataValuesCount = getStatistics().getBinSize(band,
								(int) d);
					}
					continue;
				}
			}

		}
		return stats;
	}

	@Override
	public Double getSum() {
		try {
			return null;
		} catch (Exception e) {
			LOGGER.error("Error calculating statistics", e);
			return null;
		}
	}

}
