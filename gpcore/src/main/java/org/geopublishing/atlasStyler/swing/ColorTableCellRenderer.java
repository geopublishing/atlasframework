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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * A {@link TableCellRenderer} that paints a colored field for a given
 * {@link Color} value object. Used in {@link GraduatedColorQuantitiesGUI} to
 * show the colors used for the classes.
 * 
 * @author Stefan A. Tzeggai
 * 
 */
public class ColorTableCellRenderer extends DefaultTableCellRenderer implements
		TableCellRenderer {

	private static final long serialVersionUID = -2538584781131815363L;

	// The current color to display
	Color curColor;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {

		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				rowIndex, vColIndex);

		// // Set the color to paint
		// if (curColor instanceof Color) {
		curColor = (Color) value;
		// } else {
		// // If color unknown, use table's background
		// curColor = table.getBackground();
		// }
		return this;
	}

	// Paint current color
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(curColor);
		g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
	}
}
