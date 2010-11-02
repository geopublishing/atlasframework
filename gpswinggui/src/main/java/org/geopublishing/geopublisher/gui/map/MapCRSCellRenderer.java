/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.map;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.geopublishing.geopublisher.gui.TableModelWithToolTooltip;

public class MapCRSCellRenderer extends DefaultTableCellRenderer {
	private static final Color BRIGHTER_RED = new Color(.9f, .5f, .5f);
	public static final int MAXWIDTH = 300;
	public static final int PREFWIDTH = 80;

	public MapCRSCellRenderer() {
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.TOP);
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table,
			Object value, final boolean isSelected, final boolean hasFocus,
			final int row, final int column) {

		boolean warning = false;

		if (value instanceof List) {
			final List<String> crss = (List<String>) value;

			if (crss.size() > 1)
				warning = true;

			// String s = "";
			// for (String crs : crss){
			// s+= crs;
			// s+=",";
			// }
			// s = s.substring(0, s.length()-1);
			//
			// value = s;

			value = crss.toString();
		}

		final Component fromSuper = super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (warning)
			fromSuper.setBackground(BRIGHTER_RED);
		else if (!isSelected)
			fromSuper.setBackground(null);

		if (warning) {
			// Create a ToolTip that can be depending on the row
			final TableModel tm = table.getModel();
			if (tm instanceof TableModelWithToolTooltip) {
				final String tt = ((TableModelWithToolTooltip) tm)
						.getToolTipFor(row, column);
				if (tt != null)
					((JComponent) fromSuper).setToolTipText(tt);
			}
		} else {
			((JComponent) fromSuper).setToolTipText(null);
		}
		
		return fromSuper;
	}

}
