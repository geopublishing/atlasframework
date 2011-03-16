package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geotools.styling.Symbolizer;

import de.schmitzm.lang.LangUtil;

class SingleRuleListTableCellRenderer extends
		DefaultTableCellRenderer {

	protected Logger LOGGER = LangUtil.createLogger(this);

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		
		JLabel tableCellRendererComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		if (value == null)
			return new JLabel("null");

		SingleRuleList<? extends Symbolizer> singleRL = (SingleRuleList<?>) value;

		BufferedImage image = singleRL
				.getImage(AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE);

		// JLabel tableCellRendererComponent = (JLabel) super
		// .getTableCellRendererComponent(table, value, isSelected,
		// hasFocus, row, column);

		//
		// Color fg = null;
		// Color bg = null;
		//
		// JTable.DropLocation dropLocation = table.getDropLocation();
		// if (dropLocation != null && !dropLocation.isInsertRow()
		// && !dropLocation.isInsertColumn()
		// && dropLocation.getRow() == row
		// && dropLocation.getColumn() == column) {
		//
		// fg = UIManager.getColor("Table.dropCellForeground");
		// bg = UIManager.getColor("Table.dropCellBackground");
		//
		// isSelected = true;
		// }
		//
		// if (isSelected) {
		// tableCellRendererComponent.setForeground(fg == null ? table
		// .getSelectionForeground() : fg);
		// tableCellRendererComponent.setBackground(bg == null ? table
		// .getSelectionBackground() : bg);
		// } else {
		// tableCellRendererComponent
		// .setForeground(unselectedForeground != null ?
		// unselectedForeground
		// : table.getForeground());
		// tableCellRendererComponent
		// .setBackground(unselectedBackground != null ?
		// unselectedBackground
		// : table.getBackground());
		// }

		tableCellRendererComponent.setText("");
		tableCellRendererComponent
				.setHorizontalAlignment(SwingConstants.CENTER);
		tableCellRendererComponent.setIcon(new ImageIcon(image));

		return tableCellRendererComponent;
	}

}