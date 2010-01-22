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
package skrueger.creator.gui;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import skrueger.AttributeMetadata;
import skrueger.atlas.AtlasConfig;

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
		
		if (value instanceof AttributeMetadata) {
			AttributeMetadata atm = (AttributeMetadata)value;
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
