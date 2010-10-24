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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.swing.AtlasSwingWorker;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;

import skrueger.geotools.StyledFeaturesInterface;

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
public class QuantitiesClassification extends org.geopublishing.atlasStyler.classification.QuantitiesClassification {

	protected Logger LOGGER = ASUtil.createLogger(this);
	
	volatile private AtlasSwingWorker<TreeSet<Double>> calculateStatisticsWorker;
	private Component owner;
	
	public QuantitiesClassification(Component owner,
			StyledFeaturesInterface<?> styledFeatures,
			final String value_field_name, final String normalizer_field_name) {
		super(styledFeatures, value_field_name, normalizer_field_name);
		this.owner = owner;
	}


	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 */
	public QuantitiesClassification(Component owner,
			final StyledFeaturesInterface<?> styledFeatures) {
		this(owner, styledFeatures, null, null);
	}

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 * @param value_field_name
	 *            The column that is used for the classification
	 */
	public QuantitiesClassification(Component owner,
			final StyledFeaturesInterface<?> styledFeatures,
			final String value_field_name) {
		this(owner, styledFeatures, value_field_name, null);
	}

	@Override
	public void calculateClassLimitsWithWorker() {
		classLimits = new TreeSet<Double>();

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
			setCancelCalculation(true);
			calculateStatisticsWorker.cancel(true);
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

		pushQuite();
		TreeSet<Double> newLimits;
		try {
			newLimits = calculateStatisticsWorker.executeModal();
			setClassLimits(newLimits);
			popQuite();
		} catch (InterruptedException e) {
			setQuite(stackQuites.pop());
		} catch (CancellationException e) {
			setQuite(stackQuites.pop());
		} catch (ExecutionException exception) {
//			ExceptionMonitor.show(owner, exception);
			setQuite(stackQuites.pop());
		} finally {
		}

	}

}
