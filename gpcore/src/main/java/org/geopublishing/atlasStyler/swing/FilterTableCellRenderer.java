package org.geopublishing.atlasStyler.swing;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.geopublishing.atlasViewer.swing.Icons;
import org.opengis.filter.Filter;

public class FilterTableCellRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel proto = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		ImageIcon icon = Icons.ICON_FILTER;
		proto.setIcon(icon);

		Filter filter = (Filter) value;
		proto.setEnabled((filter != null && filter != Filter.INCLUDE));

		return proto;
	}

}
