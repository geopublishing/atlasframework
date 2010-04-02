/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;

import schmitzm.swing.SwingUtil;

/**
 * A {@link TableCellRenderer} that renders a sub-table listing the available
 * views.
 * 
 * @author Stefan A. Krüger
 */
public class LayerViewsTableCellRenderer extends DefaultTableCellRenderer {
	Logger LOGGER = Logger.getLogger(LayerViewsTableCellRenderer.class);

	public LayerViewsTableCellRenderer() {
		setHorizontalAlignment(SwingConstants.LEFT);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (value instanceof List && (!((List<LayerStyle>) value).isEmpty())) {

			List<LayerStyle> styles = (List<LayerStyle>) value;

			Vector<String> cols = new Vector<String>();
			cols.add(""); // Added but is never shown, still cols need the right amount of values 

			Box stylesBox = new Box(BoxLayout.Y_AXIS);

			Vector<Vector<String>> titles = new Vector<Vector<String>>();
			for (LayerStyle ls : styles) {
				JLabel styleNameLabel = new JLabel(ls.getTitle().toString());
				stylesBox.add(styleNameLabel);
				Vector<String> row1 = new Vector<String>();
				// row1.add(ls.getFilename());
				row1.add(ls.getTitle().toString());
				titles.add(row1);
			}
			JTable stylesTable = new JTable(titles, cols);
			stylesTable.setTableHeader(null);

			if (isSelected)
				stylesTable.getSelectionModel().addSelectionInterval(0, 1000);
			else
				stylesTable.getSelectionModel().clearSelection();

			stylesTable.setShowVerticalLines(false);
			stylesTable.setGridColor(new Color(210, 210, 210));
			//
			if (stylesTable.getPreferredSize().getHeight() > 7) {
				SwingUtil.setPreferredHeight(stylesTable, (int) stylesTable
						.getPreferredSize().getHeight() - 7);
				SwingUtil.setPreferredWidth(stylesTable, (int) stylesTable
						.getPreferredSize().getWidth() + 2);
			}

			return stylesTable;
		} else
			return super.getTableCellRendererComponent(table, "", isSelected,
					hasFocus, row, column);

	}

}
