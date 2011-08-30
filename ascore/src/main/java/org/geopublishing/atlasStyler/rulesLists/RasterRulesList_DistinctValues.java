package org.geopublishing.atlasStyler.rulesLists;

import hep.aida.bin.QuantileBin1D;

import java.awt.Color;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.media.jai.Histogram;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geotools.brewer.color.PaletteType;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.opengis.parameter.ParameterValueGroup;

import de.schmitzm.geotools.styling.StyledGridCoverageReaderInterface;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class RasterRulesList_DistinctValues extends RasterRulesListColormap
		implements UniqueValuesRulesListInterface<Double> {

	private static final Logger LOGGER = Logger
			.getLogger(RasterRulesList_DistinctValues.class);

	public RasterRulesList_DistinctValues(StyledRasterInterface<?> styledRaster) {
		super(RulesListType.RASTER_COLORMAP_DISTINCTVALUES, styledRaster,
				ColorMap.TYPE_VALUES);

		// The default palette is not suitable for distinct values
		setPalette(ASUtil.getPalettes(new PaletteType(false, true), -1)[0]);

	}

	/**
	 * 1 value = 1 class
	 * 
	 * @param parentGui
	 *            is <code>null</code>, no warnings will be shown if the number
	 *            of classes if higher than the number of colors
	 */
	@Override
	public void applyPalette(JComponent parentGui) {
		pushQuite();

		try {
			getColors().clear();

			if (getValues().size() == 0)
				return;

			if (getNumClassesVisible() > getPalette().getMaxColors()
					&& parentGui != null) {

				final String msg = ASUtil
						.R("UniqueValuesGUI.WarningDialog.more_classes_than_colors.msg",
								getPalette().getMaxColors(),
								getNumClassesVisible());
				JOptionPane.showMessageDialog(
						SwingUtil.getParentWindowComponent(parentGui), msg);
			}

			final Color[] colors = getPalette().getColors(
					Math.min(getValues().size(), getPalette().getMaxColors()));

			int idx = 0;
			for (int i = 0; i < getValues().size(); i++) {

				if (getOpacities().get(i) == 0.) {
					getColors().add(Color.white);
					// idx nicht erhöhen!
					continue;
				}

				if (i >= getColors().size())
					getColors().add(colors[idx]);
				else
					getColors().set(i, colors[idx]);

				idx++;
				idx = idx % getPalette().getMaxColors();

			}

		} finally {
			popQuite(new RuleChangedEvent(
					"Applied a COLORPALETTE to all ColorMapEntries", this));
		}

	}

	/**
	 * Imports the ColorMap for a DistinctValues Ruleslist. Here every
	 * ColorMapEntry equals one Class
	 */
	@Override
	public void importColorMap(ColorMap cm) {
		QuantileBin1D ops = new QuantileBin1D(1.);

		for (ColorMapEntry cme : cm.getColorMapEntries()) {

			importValuesLabelsQuantitiesColors(cme);
			// Add the last added opacity to the statistics
			ops.add(getOpacities().get(getOpacities().size() - 1));
		}

		if (ops.median() >= 0 && ops.median() < 1.)
			setOpacity(ops.median());
		else
			setOpacity(1.);
	}

	protected void importValuesLabelsQuantitiesColors(ColorMapEntry cme) {
		final Double valueDouble = Double.valueOf(cme.getQuantity()
				.evaluate(null).toString());

		final String labelFromCM = cme.getLabel();
		Translation translation = new Translation("");
		if (labelFromCM != null && !labelFromCM.isEmpty())
			translation = new Translation(labelFromCM);
		// }

		if (translation.toString().startsWith(
				RulesListInterface.RULENAME_DONTIMPORT))
			return;

		add(valueDouble,
				Double.valueOf(cme.getOpacity().evaluate(null).toString()),
				StylingUtil.getColorFromColorMapEntry(cme), translation);
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
		// coverage.getNumSampleDimensions();
		final OperationJAI op = new OperationJAI("Histogram");
		ParameterValueGroup params = op.getParameters();
		params.parameter("Source").setValue(coverage);

		coverage = (GridCoverage2D) op.doOperation(params, null);
		final Histogram hist = (Histogram) coverage.getProperty("histogram");

		double low = hist.getLowValue(getBand());
		double high = hist.getHighValue(getBand());

		int countBins = -1;
		for (double d = low; d < high; d += 1.) {
			countBins++;

			// Ignoring the NODATA-Value
			if (styledReader.getNodataValue() != null
					&& d == styledReader.getNodataValue())
				continue;

			if (d == Double.NaN)
				continue;
			if (d == Double.NEGATIVE_INFINITY)
				continue;
			if (d == Double.POSITIVE_INFINITY)
				continue;

			if (hist.getBins()[getBand()][countBins] == 0)
				continue;

			if (!getValues().contains(d))
				uniques.add(d);
		}

		return uniques;
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

	@Override
	public void applyOpacity() {
		pushQuite();

		try {

			final Double op = getOpacity();
			if (op == null)
				return;

			for (int i = 0; i < getValues().size(); i++) {

				if (i >= getOpacities().size())
					getOpacities().add(op);

				if (getOpacities().get(i) != 0.
						|| (getValues().get(i) != null && !getValues().get(i)
								.equals(getStyledRaster().getNodataValue()))){
					setOpacity(i, op);
				}
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
	@Override
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
