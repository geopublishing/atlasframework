/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.geopublishing.atlasViewer.AtlasConfig;

import skrueger.AttributeMetadataImpl;

/**
 * A renderer that visually represents the quality percentage value (0-100). It
 * creates a {@link JLabel} with a red icon for quality 0, and a green one for
 * quality 100.
 */
public class QualityPercentageTableCellRenderer extends
		DefaultTableCellRenderer {
	
	private AtlasConfig atlasConfig;

	public QualityPercentageTableCellRenderer(AtlasConfig ac) {
		atlasConfig = ac;
	}

	public static final int MAXWIDTH = 40;
	public static final int MINWIDTH = 32;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		Color c = null;
		
		boolean validQualValue = true;
		
		if (value instanceof AttributeMetadataImpl) {
			AttributeMetadataImpl atm = (AttributeMetadataImpl)value;
			validQualValue = atm.isVisible();
			value = atm.getQuality(atlasConfig.getLanguages());
		}
		
		if (value instanceof Double) {
			Double d = (Double) value;
			

			String stringValue = NumberFormat.getPercentInstance()
					.format(value);
			JLabel label = new JLabel(stringValue);
			if (validQualValue) {
				Float green = d.floatValue();
				Float red = 1f - d.floatValue();
				c = new Color(red, green, 0.1f);
				label.setBackground(c);
				label.setOpaque(true);
				label.setForeground(Color.white);
			} else 
				label.setForeground(Color.black);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setVerticalAlignment(SwingConstants.TOP);

			TableModel tm = table.getModel();
			if (tm instanceof TableModelWithToolTooltip) {
				String tt = ((TableModelWithToolTooltip) tm).getToolTipFor(row,
						column);
				if (tt != null)
					label.setToolTipText(tt);
			}

			return label;

		} else {
			Component fromSuper = super.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);
			return fromSuper;
		}
	}
}
