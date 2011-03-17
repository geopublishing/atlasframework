package org.geopublishing.atlasStyler.rulesLists;

import java.awt.Color;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.media.jai.Histogram;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.styling.ColorMap;
import org.opengis.parameter.ParameterValueGroup;

import de.schmitzm.geotools.data.rld.RasterLegendData;
import de.schmitzm.geotools.styling.StyledGridCoverageReaderInterface;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.i18n.Translation;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class RasterRulesList_DistinctValues extends RasterRulesList implements
		UniqueValuesRulesListInterface<Double> {

	private static final Logger LOGGER = Logger
			.getLogger(RasterRulesList_DistinctValues.class);

	public RasterRulesList_DistinctValues(StyledRasterInterface<?> styledRaster) {
		super(RulesListType.RASTER_COLORMAP_DISTINCTVALUES, styledRaster,
				ColorMap.TYPE_VALUES);
	}

	public Integer addAllValues(AtlasSwingWorker<Integer> sw) {

		int countBefore = getNumClasses();
		int countNew = 0;
		pushQuite();

		try {

			for (final Double uniqueValue : getAllUniqueValuesThatAreNotYetIncluded()) {
				if (sw != null && sw.isCancelled())
					return 0;
				addUniqueValue(uniqueValue);
				countNew++;
			}
		} catch (Exception e) {
			LOGGER.error("Error calculating raster statistics", e);
			if (sw != null)
				ExceptionDialog.show(e);
		}

		if (countBefore == 0) {
			applyPalette(null);
		}

		/** Fire an event * */
		if (countNew > 0)
			popQuite(new RuleChangedEvent("Added " + countNew + " values.",
					this));
		else
			popQuite();

		return countNew;

	}

	/**
	 * @param uniqueValue
	 *            Unique value to all to the list.
	 * 
	 * @return <code>false</code> is the value already exists
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	public boolean addUniqueValue(final Double uniqueValue)
			throws IllegalArgumentException {

		if (getValues().contains(uniqueValue)) {
			LOGGER.warn("The unique Value '" + uniqueValue
					+ "' can't be added, it is allready in the list");
			return false;
		}

		getValues().add(uniqueValue);
		getLabels().add(new Translation(String.valueOf(uniqueValue)));
		getColors().add(Color.WHITE);
		getOpacities().add(getOpacity());

		return true;
	}

	/**
	 * Returns a {@link Set} not yet included in any of the rule lists.
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public Set<Double> getAllUniqueValuesThatAreNotYetIncluded()
			throws IllegalArgumentException, IOException {

		SwingUtil.checkNotOnEDT();

		final Set<Double> uniques = new TreeSet<Double>();

		StyledGridCoverageReaderInterface styledReader = (StyledGridCoverageReaderInterface) getStyledRaster();
		AbstractGridCoverage2DReader reader = styledReader.getGeoObject();

		// try {
		GridCoverage2D coverage = reader.read(null);
		final OperationJAI op = new OperationJAI("Histogram");
		ParameterValueGroup params = op.getParameters();
		params.parameter("Source").setValue(coverage);

		coverage = (GridCoverage2D) op.doOperation(params, null);
		final Histogram hist = (Histogram) coverage.getProperty("histogram");

		double low = hist.getLowValue(0);
		double high = hist.getHighValue(0);

		int countBins = -1;
		for (double d = low; d < high; d += 1.) {
			countBins++;

			// Ignoring the NODATA-Value
			if (d == styledReader.getNodataValue())
				continue;

			if (d == Double.NaN)
				continue;
			if (d == Double.NEGATIVE_INFINITY)
				continue;
			if (d == Double.POSITIVE_INFINITY)
				continue;

			if (hist.getBins()[0][countBins] == 0)
				continue;

			if (!getValues().contains(d))
				uniques.add(d);
		}

		return uniques;
	}

	@Override
	public RasterLegendData getRasterLegendData() {
		RasterLegendData rld = new RasterLegendData(true);

		for (int i = 0; i < getNumClasses(); i++) {
			// if (getShowInLegends().get(i))
			rld.put(getValues().get(i), getLabels().get(i));
		}

		return rld;
	}

	/**
	 * @param row
	 * @param delta
	 *            -1 to move the row one up
	 */
	public void move(int row, int delta) {

		getValues().add(row + delta, getValues().remove(row));
		getLabels().add(row + delta, getLabels().remove(row));
		getColors().add(row + delta, getColors().remove(row));
		getOpacities().add(row + delta, getOpacities().remove(row));
		fireEvents(new RuleChangedEvent("Index " + row + " moved up to "
				+ (row - 1), this));
	}

	public void applyOpacity() {
		pushQuite();

		try {

			final Double op = getOpacity();
			if (op == null)
				return;

			for (int i = 0; i < getValues().size(); i++) {

				if (i >= getOpacities().size())
					getOpacities().add(op);

				if (getOpacities().get(i) != 0)
					setOpacity(i, op);
			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied an OPACITY to all ColorMapEntries ", this));
		}
	}

	/**
	 * Throws an exception as soon as the array sizes of values, colors and
	 * opacities are not in sync
	 */
	protected void test(int classesExpected) {
		int valSize = getValues().size();

		if (classesExpected == -1)
			classesExpected = valSize;

		int opSize = getOpacities().size();
		int colSize = getColors().size();
		int labelSize = getLabels().size();
		String error = "expectedClasses=" + classesExpected + "  valSize="
				+ valSize + " opSize=" + opSize + " colSize=" + colSize
				+ " labelSize=" + labelSize;
		if (opSize != classesExpected || (valSize != classesExpected)
				|| colSize != classesExpected || labelSize != classesExpected)
			throw new RuntimeException(error);
	}
}
