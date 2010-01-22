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
package skrueger.creator.gui.map;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import skrueger.creator.gui.TableModelWithToolTooltip;

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
