/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.datapool;

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

import schmitzm.swing.SwingUtil;
import skrueger.atlas.dp.layer.LayerStyle;

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
