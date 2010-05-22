/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
/** 
 Copyright 2009 Stefan Alfons Tzeggai 

 atlas-framework - This file is part of the Atlas Framework

 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA

 Diese Bibliothek ist freie Software; Sie dürfen sie unter den Bedingungen der GNU Lesser General Public License, wie von der Free Software Foundation veröffentlicht, weiterverteilen und/oder modifizieren; entweder gemäß Version 2.1 der Lizenz oder (nach Ihrer Option) jeder späteren Version.
 Diese Bibliothek wird in der Hoffnung weiterverbreitet, daß sie nützlich sein wird, jedoch OHNE IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Mehr Details finden Sie in der GNU Lesser General Public License.
 Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit dieser Bibliothek erhalten haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
 **/
package org.geopublishing.geopublisher.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geopublishing.geopublisher.gui.datapool.DataPoolJTable;


/**
 * A {@link TableCellRenderer} for {@link DpEntryType} values. Used in
 * {@link DataPoolJTable}
 */
public class DpEntryTypeTableCellRenderer extends DefaultTableCellRenderer {
	public static final int MAXWIDTH = 66;
	public static final int MINWIDTH = 66;
	Logger LOGGER = Logger.getLogger(DpEntryTypeTableCellRenderer.class);

	public DpEntryTypeTableCellRenderer() {
		setHorizontalAlignment(SwingConstants.LEFT);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (value instanceof DpEntry) {

			final DpEntry dpEntry = (DpEntry)value;
			final DpEntryType type = (DpEntryType) dpEntry.getType();

			ImageIcon icon = DpEntryType.getIconBigFor(type);
			String line1 = DpEntryType.getLine1For(type);
			String line2 = DpEntryType.getLine2For(type);

			JPanel prototype = new JPanel(new BorderLayout());

			JLabel comp = new JLabel(icon);
			comp.setPreferredSize(new Dimension(24, 26));
			Box box = new Box(BoxLayout.Y_AXIS);
			box.add(comp);
			prototype.add(box, BorderLayout.WEST);

			JLabel line1Label = new JLabel(line1);
			JLabel line2Label = new JLabel(line2);

			line1Label.setFont(line1Label.getFont().deriveFont(11f));
			line2Label.setFont(line2Label.getFont().deriveFont(9f));

			Box center = new Box(BoxLayout.Y_AXIS);
			center.add(line1Label);
			center.add(line2Label);
			prototype.add(center, BorderLayout.CENTER);
//
//			if (dpEntry.isBroken()){
////				Graphics2D g = (Graphics2D) prototype.getGraphics();
////				g.setColor(Color.RED);
////				g.setFont(new Font("Arial",Font.BOLD, 15));
////				g.drawString("!", 1, 15);
//				comp.setIcon(DpEntryType.getIconBigFor(DpEntryType.UNKNOWN));
//				prototype.setToolTipText("ERROR "+dpEntry.getBrokenException().getLocalizedMessage());
//			}else
//				prototype.setToolTipText(type.getDesc());
//			
//			
			prototype.setPreferredSize(new Dimension(66, 24));
			// prototype.setMaximumSize(new Dimension(66, 26));

			// setColorsForSelectionState(prototype, isSelected);
			

			return prototype;
		} else
			return super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);

	}

}
