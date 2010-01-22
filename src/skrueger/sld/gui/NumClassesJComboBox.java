/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.sld.gui;

import java.awt.event.ItemEvent;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import skrueger.sld.ASUtil;
import skrueger.sld.classification.ClassificationChangeEvent;
import skrueger.sld.classification.ClassificationChangedAdapter;
import skrueger.sld.classification.QuantitiesClassification;
import skrueger.sld.classification.QuantitiesClassification.METHOD;

/**
 * An extension to a {@link JComboBox} that allows to change the number of
 * classes used for the classification. It propagates the changes to the
 * underlying classification object.<br/>
 * If classification method {@link METHOD.MANUAL} is chosen, the
 * {@link JComboBox} is disabled.
 */
public class NumClassesJComboBox extends JComboBox {
	final protected static Logger LOGGER = Logger
			.getLogger(NumClassesJComboBox.class);

	public NumClassesJComboBox(final QuantitiesClassification classifier) {
		setModel(classifier.getClassificationParameterComboBoxModel());

		setSelectedItem(classifier.getNumClasses());

		addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					LOGGER.debug("StateChangeEvent for ParamComboBox");

					Integer newNum = (Integer) e.getItem();
					LOGGER
							.debug("       new number of clases set via the GUI = "
									+ newNum);

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

				if (classifier.getMethod() == METHOD.MANUAL)
					NumClassesJComboBox.this.setEnabled(false);
				else
					NumClassesJComboBox.this.setEnabled(true);
			}
		});

		/**
		 * For manual classification, this JComboBox is disabled.
		 */
		setEnabled(classifier.getMethod() != METHOD.MANUAL);

		ASUtil.addMouseWheelForCombobox(this);

	}

}
