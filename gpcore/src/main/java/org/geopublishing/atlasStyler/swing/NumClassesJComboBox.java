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
package org.geopublishing.atlasStyler.swing;

import java.awt.event.ItemEvent;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.classification.CLASSIFICATION_METHOD;
import org.geopublishing.atlasStyler.classification.Classification;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangedAdapter;

import de.schmitzm.swing.SwingUtil;

/**
 * An extension to a {@link JComboBox} that allows to change the number of
 * classes used for the classification. It propagates the changes to the
 * underlying classification object.<br/>
 * If classification method {@link CLASSIFICATION_METHOD.MANUAL} is chosen, the
 * {@link JComboBox} is disabled.
 */
public class NumClassesJComboBox extends JComboBox {
	final protected static Logger LOGGER = Logger
			.getLogger(NumClassesJComboBox.class);

	public NumClassesJComboBox(final Classification classifier) {
		setModel(classifier.getClassificationParameterComboBoxModel());

		setSelectedItem(classifier.getNumClasses());

		addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// LOGGER.debug("StateChangeEvent for ParamComboBox");

					Integer newNum = (Integer) e.getItem();
					// LOGGER
					// .debug("       new number of clases set via the GUI = "
					// + newNum);

					classifier.setNumClasses(newNum);
					//
					// // When the classParam has been changed by the
					// // user, throw away the ruleTitles
					// rulesList.getRuleTitles().clear();
				}
			}
		});
		classifier.addListener(new ClassificationChangedAdapter() {

			@Override
			public void classifierNumClassesChanged(ClassificationChangeEvent e) {
				NumClassesJComboBox.this.setSelectedItem(classifier
						.getNumClasses());
			}

			@Override
			public void classifierMethodChanged(ClassificationChangeEvent e) {
				NumClassesJComboBox.this.setModel(classifier
						.getClassificationParameterComboBoxModel());

				if (classifier.getMethod() == CLASSIFICATION_METHOD.MANUAL)
					NumClassesJComboBox.this.setEnabled(false);
				else
					NumClassesJComboBox.this.setEnabled(true);
			}
		});

		/**
		 * For manual classification, this JComboBox is disabled.
		 */
		setEnabled(classifier.getMethod() != CLASSIFICATION_METHOD.MANUAL);

		SwingUtil.addMouseWheelForCombobox(this);

	}

}
