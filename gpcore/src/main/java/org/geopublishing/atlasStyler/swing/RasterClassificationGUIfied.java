package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.io.IOException;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.classification.RasterClassification;
import org.geopublishing.atlasStyler.swing.classification.FeatureClassificationGUIfied;

import de.schmitzm.geotools.styling.StyledGridCoverageReaderInterface;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.swingworker.AtlasStatusDialog;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class RasterClassificationGUIfied extends RasterClassification {

	public RasterClassificationGUIfied(Component owner,
			StyledGridCoverageReaderInterface styledRaster) {
		super(styledRaster);
		this.owner = owner;
	}

	private final static Logger LOGGER = LangUtil
			.createLogger(FeatureClassificationGUIfied.class);

	volatile private AtlasSwingWorker<TreeSet<Double>> calculateStatisticsWorker;
	private final Component owner;

	@Override
	/**
	 * This overwritten variant of #calculateClassLimits manages a AtlasStatusDialog to be shown while calculating. Calling this method takes it's time, but the GUI is not blocked.
	 */
	public void calculateClassLimits() {
		breaks = new TreeSet<Double>();

		/**
		 * If there is another thread running, cancel it first. But remember,
		 * that swing-workers may not be reused!
		 */
		if (calculateStatisticsWorker != null
				&& !calculateStatisticsWorker.isDone()) {
			LOGGER.debug("Cancelling calculation on another thread");
			cancelCalculation.set(true);
			calculateStatisticsWorker.cancel(true);
			calculateStatisticsWorker = null;
		}

		AtlasStatusDialog statusDialog = new AtlasStatusDialog(owner);

		calculateStatisticsWorker = new AtlasSwingWorker<TreeSet<Double>>(
				statusDialog) {

			@Override
			protected TreeSet<Double> doInBackground() throws IOException,
					InterruptedException {
				return calculateClassLimitsBlocking();
			}

		};

		TreeSet<Double> newLimits;
		pushQuite();
		try {
			newLimits = calculateStatisticsWorker.executeModal();
			setClassLimits(newLimits);
		} catch (Exception e) {
			LOGGER.error("Error calculating classification", e);
		} finally {
			popQuite();
		}

	}

}
