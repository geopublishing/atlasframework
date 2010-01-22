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
package skrueger.sld;

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
