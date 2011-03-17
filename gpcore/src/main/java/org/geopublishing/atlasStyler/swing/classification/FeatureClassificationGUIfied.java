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
package org.geopublishing.atlasStyler.swing.classification;

import java.awt.Component;
import java.io.IOException;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.classification.FeatureClassification;

import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.swingworker.AtlasStatusDialog;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

/**
 * A quantitative classification. The intervals are defined by upper and lower
 * limits
 * 
 * @param <T>
 *            The type of the value field
 * 
 * @author stefan
 */
public class FeatureClassificationGUIfied extends FeatureClassification {

	private final static Logger LOGGER = LangUtil
			.createLogger(FeatureClassificationGUIfied.class);

	volatile private AtlasSwingWorker<TreeSet<Double>> calculateStatisticsWorker;
	private final Component owner;

	public FeatureClassificationGUIfied(Component owner,
			StyledFeaturesInterface<?> styledFeatures,
			final String value_field_name, final String normalizer_field_name) {
		super(styledFeatures, value_field_name, normalizer_field_name);
		this.owner = owner;
	}

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 */
	public FeatureClassificationGUIfied(Component owner,
			final StyledFeaturesInterface<?> styledFeatures) {
		this(owner, styledFeatures, null, null);
	}

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 * @param value_field_name
	 *            The column that is used for the classification
	 */
	public FeatureClassificationGUIfied(Component owner,
			final StyledFeaturesInterface<?> styledFeatures,
			final String value_field_name) {
		this(owner, styledFeatures, value_field_name, null);
	}

	@Override
	/**
	 * This overwritten variant of #calculateClassLimits manages a AtlasStatusDialog to be shown while calculating. Calling this method takes it's time, but the GUI is not blocked.
	 */
	public void calculateClassLimits() {
		breaks = new TreeSet<Double>();

		/**
		 * Do we have all necessary information to calculate ClassLimits?
		 */
		if (value_field_name == null)
			return;

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
