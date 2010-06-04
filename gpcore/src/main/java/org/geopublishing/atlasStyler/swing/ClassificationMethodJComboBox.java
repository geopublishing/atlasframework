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

import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangedAdapter;
import org.geopublishing.atlasStyler.classification.QuantitiesClassification;
import org.geopublishing.atlasStyler.classification.QuantitiesClassification.METHOD;

import schmitzm.swing.SwingUtil;

/**
 * An extension of a {@link JComboBox} that allows to choose one of the
 * classification methods. It supports tooltips for the entries and localised
 * labels. The values inside the {@link JComboBox} are hold as {@link Enum} type
 * {@link METHOD}.
 * 
 * @author SK
 * 
 */
public class ClassificationMethodJComboBox extends JComboBox {

	public ClassificationMethodJComboBox(
			final QuantitiesClassification classifier) {

		/**
		 * Internally the JComboBox works on METHOS enums, but renders localized
		 * labels and tooltips.
		 */
		setRenderer(new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				METHOD mValue = (METHOD) value;

				if (isSelected) {
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
					if (-1 < index) {
						final String toolTip = mValue.getToolTip();
						if (toolTip != null && (!toolTip.equals("")))
							list.setToolTipText(toolTip);
						else
							list.setToolTipText(null);
					}
				} else {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
				setFont(list.getFont());
				setText((mValue == null) ? "" : mValue.getDesc());
				return this;
			}
		});

		setModel(new DefaultComboBoxModel(QuantitiesClassification.METHOD
				.values()));
		setSelectedItem(classifier.getMethod());

		/**
		 * Changes to the GUI are passed on to the underlying classification
		 * object
		 */
		addItemListener(new java.awt.event.ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					classifier.setMethod((METHOD) e.getItem());
				}
			}
		});

		/**
		 * The METHOD may have been changed via editing the table... so we
		 * listen to the classifier *
		 */
		classifier.addListener(new ClassificationChangedAdapter() {

			@Override
			public void classifierMethodChanged(ClassificationChangeEvent e) {
				ClassificationMethodJComboBox.this.setSelectedItem(classifier
						.getMethod());
			}

		});

		SwingUtil.addMouseWheelForCombobox(this);

	}
}
