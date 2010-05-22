/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

/**
 * TODO Das ist zu speuiell. das muss in unique... umbeannt werden
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class SingleRuleListCellRenderer extends DefaultTableCellRenderer {

	/**
	 * Size of the image rendered in the table cell.
	 */
	private static final Dimension CELL_IMAGE_SIZE = new Dimension(25, 25);

	protected Logger LOGGER = ASUtil.createLogger(this);

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value == null)
			return new JLabel("null");

		Component comp = null;

		SingleRuleList singleRL = (SingleRuleList) value;

		comp = getTCR(table, value, isSelected, hasFocus, singleRL, row, column);

		return comp;
	}

	private Component getTCR(JTable table, Object value, boolean isSelected,
			boolean hasFocus, SingleRuleList singleRL, int row, int column) {
		BufferedImage image = singleRL.getImage(CELL_IMAGE_SIZE);

		JLabel tableCellRendererComponent = (JLabel) super
				.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);

		tableCellRendererComponent.setText("");
		tableCellRendererComponent
				.setHorizontalAlignment(SwingConstants.CENTER);
		tableCellRendererComponent.setIcon(new ImageIcon(image));

		return tableCellRendererComponent;
	}

}
