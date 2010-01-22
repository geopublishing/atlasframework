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

import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import skrueger.sld.ASUtil;
import skrueger.sld.classification.ClassificationChangeEvent;
import skrueger.sld.classification.ClassificationChangedAdapter;
import skrueger.sld.classification.QuantitiesClassification;
import skrueger.sld.classification.QuantitiesClassification.METHOD;

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

		ASUtil.addMouseWheelForCombobox(this);

	}
}
